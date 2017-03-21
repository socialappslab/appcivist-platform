package controllers;

import static play.data.Form.form;
import http.Headers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.*;
import javax.ws.rs.PathParam;

import models.*;
import models.misc.Views;
import models.transfer.ApiResponseTransfer;
import models.transfer.InvitationTransfer;
import models.transfer.PadTransfer;
import models.transfer.ThemeListTransfer;
import models.transfer.TransferResponseStatus;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;

import play.Logger;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.twirl.api.Content;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.GlobalDataConfigKeys;
import utils.LogActions;
import utils.services.EtherpadWrapper;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.PlayAuthenticate;

import delegates.ContributionsDelegate;
import delegates.NotificationsDelegate;
import delegates.ResourcesDelegate;
import enums.ContributionFeedbackTypes;
import enums.ContributionStatus;
import enums.ContributionTypes;
import enums.ManagementTypes;
import enums.MyRoles;
import enums.NotificationEventName;
import enums.ResourceSpaceTypes;
import enums.ResourceTypes;
import enums.ResponseStatus;
import enums.SupportedMembershipRegistration;
import exceptions.ConfigurationException;
import exceptions.MembershipCreationException;

@Api(value = "05 contribution: Contribution Making", description = "Contribution Making Service: contributions by citizens to different spaces of civic engagement")
@With(Headers.class)
public class Contributions extends Controller {

    public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);
    public static final Form<ContributionFeedback> CONTRIBUTION_FEEDBACK_FORM = form(ContributionFeedback.class);
    public static final Form<Resource> ATTACHMENT_FORM = form(Resource.class);
    public static final Form<ThemeListTransfer> THEMES_FORM = form(ThemeListTransfer.class);
    public static final Form<User> AUTHORS_FORM = form(User.class);

    private static BufferedReader br;

    /**
     * GET       /api/assembly/:aid/contribution
     *
     * @param aid
     * @param space
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findAssemblyContributions(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "space", value = "Resource space name within assembly from which we want to query contributions", allowableValues = "forum,resources", defaultValue = "forum") String space,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note, discussion", defaultValue = "idea") String type) {
        Assembly a = Assembly.read(aid);
        ResourceSpace rs = null;
        if (a != null) {
            if (space != null && space.equals("forum"))
                rs = a.getForum();
            else
                rs = a.getResources();
        }
        List<Contribution> contributions = ContributionsDelegate
                .findContributionsInResourceSpace(rs, type, null);

        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No resource space for assembly: " + aid)));
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/component/:ciid/contribution
     *
     * @param aid
     * @param cid
     * @param ciid
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List",
            produces = "application/json", value = "Get contributions in a component of a campaign within an assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findCampaignComponentContributions(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "ciid", value = "Component ID") Long ciid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note, discussion", defaultValue = "idea") String type) {
        Component c = Component.read(cid, ciid);
        ResourceSpace rs = null;
        if (c != null) {

            rs = c.getResourceSpace();
        }
        List<Contribution> contributions = ContributionsDelegate
                .findContributionsInResourceSpace(rs, type, null);
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No contributions for {assembly, campaign, component}: " + aid + ", " + cid + ", " + ciid)));
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution
     *
     * @param aid
     * @param cid
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in a Campaign")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findCampaignContributions(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note, discussion", defaultValue = "idea") String type) {

        Campaign c = Campaign.read(cid);
        ResourceSpace rs = null;
        if (c != null) {
            rs = c.getResources();
        }
        List<Contribution> contributions = ContributionsDelegate
                .findContributionsInResourceSpace(rs, type, null);
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No contributions for {assembly, campaign}: " + aid + ", " + cid)));
    }

    /**
     * GET       /api/assembly/:aid/group/:gid/contribution
     *
     * @param aid
     * @param gid
     * @param space
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in a Working Group")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findAssemblyGroupContributions(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Working Group ID") Long gid,
            @ApiParam(name = "space", value = "Resource space name within the working group from which we want to query contributions", allowableValues = "forum,resources", defaultValue = "forum") String space,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note, discussion", defaultValue = "idea") String type) {
        WorkingGroup wg = WorkingGroup.read(gid);
        ResourceSpace rs = null;
        if (wg != null) {
            if (space != null && space.equals("forum"))
                rs = wg.getForum();
            else
                rs = wg.getResources();
        }
        List<Contribution> contributions = ContributionsDelegate
                .findContributionsInResourceSpace(rs, type, null);
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No resource space for assembly: " + aid)));
    }

    /**
     * GET       /api/assembly/:aid/contribution/:cid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, produces = "application/json", value = "Get contribution by ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        Contribution contribution = Contribution.read(contributionId);
        return ok(Json.toJson(contribution));
    }

    /**
     * GET       /api/space/:sid/contribution
     *
     * @param sid
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json",
            value = "Get contributions in a specific Resource Space",
            notes = "Every entity in AppCivist has a Resource Space to associate itself to other entities")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result findResourceSpaceContributions(
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = "") String type,
            @ApiParam(name = "by_text", value = "String") String byText,
            @ApiParam(name = "groups", value = "List") List<Integer> byGroup,
            @ApiParam(name = "themes", value = "List") List<Integer> byTheme,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Page", defaultValue = "0") Integer page,
            @ApiParam(name = "pageSize", value = "Number of elements per page") Integer pageSize,
            @ApiParam(name = "sorting", value = "Ordering of proposals") String sorting,
            @ApiParam(name = "random", value = "Boolean") String random) {
        if (pageSize == null) {
            pageSize = GlobalData.DEFAULT_PAGE_SIZE;
        }
        ResourceSpace rs = ResourceSpace.read(sid);
        List<Contribution> contributions;


        Map<String, Object> conditions = new HashMap<>();
        conditions.put("containingSpaces", rs.getResourceSpaceId());
        if (type != null && !type.isEmpty()) {
            ContributionTypes mappedType = ContributionTypes.valueOf(type.toUpperCase());
            conditions.put("type", mappedType);
        }
        if (byText != null && !byText.isEmpty()) {
            conditions.put("by_text", byText);
        }
        if (byGroup != null && !byGroup.isEmpty()) {
            conditions.put("group", byGroup);
        }
        if (byTheme != null && !byTheme.isEmpty()) {
            conditions.put("theme", byTheme);
        }
        if (sorting != null && !sorting.isEmpty()) {
            conditions.put("sorting", sorting);
        }
        if (!rs.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
            conditions.put("status",ContributionStatus.PUBLISHED);
        }

        PaginatedContribution pag = new PaginatedContribution();
        if(all != null){
            contributions = ContributionsDelegate.findContributions(conditions, null, null);
            return contributions != null ? ok(Json.toJson(contributions))
                    : notFound(Json.toJson(new TransferResponseStatus(
                    "No contributions for {resource space}: " + sid + ", type=" + type)));
        }else{
            List<Contribution> contribs = ContributionsDelegate.findContributions(conditions, null, null);
            if(random != null && random.equals("true")){
                int totalRows = contribs.size();
                int totalPages = (totalRows+pageSize-1) / pageSize;
                page = RandomUtils.nextInt(0,totalPages);
            }
            contributions = ContributionsDelegate.findContributions(conditions, page, pageSize);
            pag.setPageSize(pageSize);
            pag.setTotal(contribs.size());
            pag.setPage(page);
            pag.setList(contributions);
            return contributions != null ? ok(Json.toJson(pag))
                    : notFound(Json.toJson(new TransferResponseStatus(
                    "No contributions for {resource space}: " + sid + ", type=" + type)));
        }

    }


    /**
     * GET       /api/space/:sid/contribution
     *
     * @param sid
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json",
            value = "Get contributions in a specific Resource Space",
            notes = "Every entity in AppCivist has a Resource Space to associate itself to other entities")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result findResourceSpacePinnedContributions(
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = "") String type) {

        ContributionTypes mappedType = null;
        if (type != null)
            ContributionTypes.valueOf(type.toUpperCase());
        List<Contribution> contributions = ContributionsDelegate.findPinnedContributionsInSpace(sid, mappedType);
        if (contributions == null) {
            contributions = new ArrayList<Contribution>();
        }
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No pinned contributions for {resource space}: " + sid + ", type=" + type)));
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution/:coid/stats
     *
     * @param aid
     * @param cid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionStatistics.class, produces = "application/json",
            value = "Get contributions statistics")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution stats found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result readContributionStats(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid) {
        try {
            ContributionStatistics stats = new ContributionStatistics(coid);
            return ok(Json.toJson(stats));
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution stats: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/group/:gid/contribution/:coid/stats
     *
     * @param aid
     * @param cid
     * @param gid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionStatistics.class, produces = "application/json",
            value = "Get workgroup contributions statistics")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution stats found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result readWGContributionStats(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "gud", value = "Group ID") Long gid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid) {
        try {
            ContributionStatistics stats = new ContributionStatistics(gid, coid);
            return ok(Json.toJson(stats));
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading workgroup contribution stats: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution/:coid/feedback
     *
     * @param aid
     * @param cid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, responseContainer = "List", produces = "application/json",
            value = "Get contributions feedbacks")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution feedbacks found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result readContributionFeedbacks(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid) {
        try {
            List<ContributionFeedback> feedbacks = ContributionFeedback.getFeedbacksByContribution(coid);
            return ok(Json.toJson(feedbacks));
        } catch (Exception e) {
            Logger.error("Error retrieving feedbacks", e);
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution feedbacks: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution/:coid/feedback/:fid
     *
     * @param aid
     * @param cid
     * @param coid
     * @param fid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, produces = "application/json",
            value = "Get individual ContributionFeedback")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution feedback found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result readContributionFeedback(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid,
            @ApiParam(name = "fid", value = "Feedback ID") Long fid) {
        try {
            ContributionFeedback feedback = ContributionFeedback.read(fid);
            return ok(Json.toJson(feedback));
        } catch (Exception e) {
            Logger.error("Error retrieving feedbacks", e);
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution feedbacks: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/group/:gid/contribution/:coid/feedback?type=x
     *
     * @param aid
     * @param gid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, responseContainer = "List", produces = "application/json",
            value = "Get Contribution Feedbacks")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution feedbacks found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.MEMBERSHIP_RESOURCE_PATH)
    public static Result readContributionFeedbackPrivate(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Group ID") Long gid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid,
            @ApiParam(name = "type", value = "Type") String type) {
        try {
            List<ContributionFeedback> feedbacks = ContributionFeedback.getPrivateFeedbacksByContributionTypeAndWGroup(coid, gid, type);
            return ok(Json.toJson(feedbacks));
        } catch (Exception e) {
            Logger.error("Error retrieving feedbacks", e);
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution feedbacks: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/contribution/:coid/feedback?type=x
     *
     * @param aid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, responseContainer = "List", produces = "application/json",
            value = "Get Contribution Feedbacks")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution feedbacks found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    //@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.AUTHOR_OF_CONTRIBUTION_FEEDBACK)
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result readContributionFeedbackNoGroupId(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid,
            @ApiParam(name = "type", value = "Type") String type) {
        try {
            // 1. obtaining the user of the requestor
            User author = User.findByAuthUserIdentity(PlayAuthenticate
                    .getUser(session()));
            Membership m = MembershipAssembly.findByUserAndAssemblyIds(author.getUserId(), aid);
            if (m!=null){
                List<ContributionFeedback> feedbacks = ContributionFeedback.getPrivateFeedbacksByContributionType(coid, null, type);
                return ok(Json.toJson(feedbacks));
            }else{
                List<ContributionFeedback> feedbacks = ContributionFeedback.getPrivateFeedbacksByContributionType(coid, author.getUserId(), type);
                return ok(Json.toJson(feedbacks));
            }

        } catch (Exception e) {
            Logger.error("Error retrieving feedbacks", e);
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution feedbacks: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/contribution/:couuid/feedback?type=x
     *
     * @param couuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, responseContainer = "List", produces = "application/json",
            value = "Get Contribution Feedbacks")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution feedbacks found", response = TransferResponseStatus.class)})
    public static Result readContributionFeedbackPublic(
            @ApiParam(name = "couuid", value = "Contribution UUID") String couuid,
            @ApiParam(name = "type", value = "Type") String type) {
        try {
            Contribution co = Contribution.readByUUID(UUID.fromString(couuid));
            List<ContributionFeedback> feedbacks = ContributionFeedback.getPublicFeedbacksByContributionType(co.getContributionId(), type);
            return ok(Json.toJson(feedbacks));
        } catch (Exception e) {
            Logger.error("Error retrieving feedbacks", e);
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution feedbacks: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/contribution/:cid/padid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = PadTransfer.class, produces = "application/json", value = "Get the pad of a Contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findContributionPadId(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        Contribution c = Contribution.read(contributionId);
        if (c != null) {
            Resource pad = c.getExtendedTextPad();
            String padId = pad.getPadId();
            PadTransfer p = new PadTransfer();
            p.setPadId(padId);
            if (padId != null) {
                return ok(Json.toJson(p));
            } else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No Pad for this Contribution")));
            }
        }
        return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Contribution with ID " + contributionId + " not found")));
    }

    /**
     * GET       /api/assembly/:aid/contribution/:cid/comments
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Read comments on a Contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findContributionComments(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        Contribution c = Contribution.read(contributionId);
        if (c != null) {
            List<Contribution> comments = Contribution.readCommentsOfSpace(c.getResourceSpaceId());
            if (comments != null) {
                return ok(Json.toJson(comments));
            } else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No comments on this contribution")));
            }
        }
        return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Contribution with ID " + contributionId + " not found")));
    }

    /**
     * GET       /api/assembly/:aid/contribution/:cid/contributions
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Read associated contributions of a Contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findAssociatedContributions(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize) {
        Contribution c = Contribution.read(contributionId);
        if (c == null) {
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Contribution with ID " + contributionId + " not found")));
        }
        if (pageSize == null) {
            pageSize = GlobalData.DEFAULT_PAGE_SIZE;
        }
        List<Contribution> associatedContributions;
        if (all != null) {
            associatedContributions = c.getAssociatedContributions();
        } else {
            associatedContributions = c.getPagedAssociatedContributions(page, pageSize);
        }
        return ok(Json.toJson(associatedContributions));
    }

    /**
     * GET       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, produces = "application/json", value = "Get contribution by its Universal ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    // TODO: add API token support, some API enpoints must be available only for registered clients
    public static Result findContributionByUUID(
            @ApiParam(name = "uuid", value = "Contribution Universal ID") UUID uuid) {
        Contribution contribution;
        try {
            contribution = Contribution.readByUUID(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with this uuid")));
        }
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        String result;
        try {
            result = mapper.writerWithView(Views.Public.class).writeValueAsString(contribution);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return internalServerError(Json.toJson(new TransferResponseStatus(ResponseStatus.SERVERERROR, "Error while mapping the public view of the contribution")));
        }

        Content ret = new Content() {
            @Override
            public String body() {
                return result;
            }

            @Override
            public String contentType() {
                return "application/json";
            }
        };

        return ok(ret);
        
        //return ok(Json.toJson(contribution));
    }

    /**
     * GET       /api/contribution/:uuid/history
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionHistory.class, responseContainer = "List", produces = "application/json", value = "Get contribution histories by its Universal ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution histories found", response = TransferResponseStatus.class)})
    public static Result findContributionHistoryByUUID(
            @ApiParam(name = "uuid", value = "Contribution Universal ID") UUID uuid) {
        List<ContributionHistory> contributionHistories;
        String result;
        try {
            Contribution contribution = Contribution.readByUUID(uuid);
            contributionHistories = ContributionHistory.getContributionsHistory(contribution.getContributionId());
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            result  = mapper.writerWithView(Views.Public.class)
                    .writeValueAsString(contributionHistories);
        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution histories with this uuid")));
        }
        Content ret = new Content() {
            @Override
            public String body() {
                return result;
            }

            @Override
            public String contentType() {
                return "application/json";
            }
        };
        return ok(ret);
    }

    /**
     * GET       /api/space/:uuid/contribution
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contribution by its Universal Resource Space ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    // TODO: add API token support, some API enpoints must be available only for registered clients
    public static Result findResourceSpaceContributionsByUUID(
            @ApiParam(name = "uuid", value = "Resource Space Universal ID") UUID uuid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = "") String type,
            @ApiParam(name = "by_text", value = "String") String byText,
            @ApiParam(name = "groups", value = "List") List<Integer> byGroup,
            @ApiParam(name = "themes", value = "List") List<Integer> byTheme,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Page", defaultValue = "0") Integer page,
            @ApiParam(name = "pageSize", value = "Number of elements per page") Integer pageSize,
            @ApiParam(name = "sorting", value = "Ordering of proposals") String sorting,
            @ApiParam(name = "random", value = "Boolean") String random) {
        if (pageSize == null) {
            pageSize = GlobalData.DEFAULT_PAGE_SIZE;
        }
        try {
            ResourceSpace rs = ResourceSpace.readByUUID(uuid);
            List<Contribution> contributions;
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("containingSpaces", rs.getResourceSpaceId());
            if (type != null && !type.isEmpty()) {
                ContributionTypes mappedType = ContributionTypes.valueOf(type.toUpperCase());
                conditions.put("type", mappedType);
            }
            if (byText != null && !byText.isEmpty()) {
                conditions.put("by_text", byText);
            }
            if (byGroup != null && !byGroup.isEmpty()) {
                conditions.put("group", byGroup);
            }
            if (byTheme != null && !byTheme.isEmpty()) {
                conditions.put("theme", byTheme);
            }
            if (sorting != null && !sorting.isEmpty()) {
                conditions.put("sorting", sorting);
            }
            if (!rs.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
                conditions.put("status",ContributionStatus.PUBLISHED);
            }

            PaginatedContribution pag = new PaginatedContribution();
            if (all != null) {
                contributions = ContributionsDelegate.findContributions(conditions, null, null);
            } else {
                List<Contribution> contribs = ContributionsDelegate.findContributions(conditions, null, null);
                if(random != null && random.equals("true")){
                    int totalRows = contribs.size();
                    int totalPages = (totalRows+pageSize-1) / pageSize;
                    page = RandomUtils.nextInt(0,totalPages);
                }
                contributions = ContributionsDelegate.findContributions(conditions, page, pageSize);
                pag.setPageSize(pageSize);
                pag.setTotal(contribs.size());
                pag.setPage(page);
                pag.setList(contributions);
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            String result;
            if(all != null){
                result = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(contributions);
            } else {
                result = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(pag);
            }

            Content ret = new Content() {
                @Override
                public String body() {
                    return result;
                }

                @Override
                public String contentType() {
                    return "application/json";
                }
            };

            return Results.ok(ret);

        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }

    }
    /**
     * GET       /api/space/:uuid/contribution/public/pinned
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contribution by its Universal Resource Space ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    // TODO: add API token support, some API enpoints must be available only for registered clients
    public static Result findResourceSpacePinnedContributionsByUUID(
            @ApiParam(name = "uuid", value = "Resource Space Universal ID") UUID uuid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = "") String type) {
        ResourceSpace rs = ResourceSpace.readByUUID(uuid);
        ContributionTypes mappedType = null;
        if (type != null)
            mappedType = ContributionTypes.valueOf(type.toUpperCase());
        List<Contribution> contributions = ContributionsDelegate
                .findPinnedContributionsInResourceSpace(rs, mappedType, ContributionStatus.PUBLISHED);
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No contributions for {resource space}: " + uuid + ", type=" + type)));
    }

    /**
     * GET       /api/assembly/:aid/contribution/:cid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionHistory.class, responseContainer = "List", produces = "application/json", value = "Get contributions change history")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    public static Result getContributionsChangeHistory(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) throws Exception {
        List<ContributionHistory> contributionHistories = ContributionHistory.getContributionsHistory(contributionId);
        return ok(Json.toJson(contributionHistories));
    }

    /**
     * POST       /api/contribution/history
     *
     * @return
     */
    @ApiOperation(httpMethod = "POST", produces = "application/json", value = "Generates histories for all contributions")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result createHistories() {
        List<Contribution> contributions = Contribution.findAll();
        for(Contribution c : contributions){
            ContributionHistory.createHistoricFromContribution(c);
        }
        return ok("ok");
    }

    /**
     * POST       /api/contribution/clean/history
     *
     * @return
     */
    @ApiOperation(httpMethod = "POST", produces = "application/json", value = "Delete duplicate contributions change history")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result deleteUnchangedContributionHistories() throws Exception {
        List<Contribution> contributions = Contribution.findAll();
        for(Contribution c : contributions){
            List<ContributionHistory> contributionHistories = ContributionHistory.getContributionsHistory(c.getContributionId());
            boolean createdHistory = false;
            for (ContributionHistory contributionHistory :contributionHistories
                    ) {
                if(createdHistory){
                    if (contributionHistory.getChanges().getAssociationChanges().isEmpty() && contributionHistory.getChanges().getExternalChanges().isEmpty()
                            && contributionHistory.getChanges().getInternalChanges().isEmpty()){
                        contributionHistory.softRemove();
                    }
                }
                if (contributionHistory.getChanges().getAssociationChanges().isEmpty() && contributionHistory.getChanges().getExternalChanges().isEmpty()
                        && contributionHistory.getChanges().getInternalChanges().isEmpty()){
                    createdHistory = true;
                }
            }
        }
        return ok("ok");
    }

    /**
     * POST       /api/contribution/clean/feedback
     *
     * @return
     */
    @ApiOperation(httpMethod = "POST", produces = "application/json", value = "Archives duplicated feedbacks")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result cleanUnarchivedContributionFeedbacks() throws Exception {

        List<ContributionFeedback> feedbacks = ContributionFeedback.findAll();

        for(ContributionFeedback feedback : feedbacks){
            List<ContributionFeedback> relatedFeedbacks = ContributionFeedback.findPreviousContributionFeedback(
                    feedback.getContributionId(), feedback.getUserId(), feedback.getWorkingGroupId(),
                    feedback.getType(), feedback.getStatus(), feedback.getNonMemberAuthor());
            if(relatedFeedbacks != null && relatedFeedbacks.size() > 1){
                relatedFeedbacks.stream().sorted((feedback1, feedback2) -> feedback1.getCreation().
                        compareTo(feedback2.getCreation()));
                //We have to mark every feedback (but the last, wich is the newer) as archived
                relatedFeedbacks.remove(relatedFeedbacks.size() - 1);
                for(ContributionFeedback relatedFeedback : relatedFeedbacks){
                    relatedFeedback.setArchived(true);
                    relatedFeedback.update();
                }
            }
        }
        return ok("ok");
    }


    /* CREATE ENDPOINTS
     * TODO: reduce complexity by removing uncessary create methods
     */

    /**
     * POST       /api/space/:sid/contribution
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create a contribution in a specific Resource Space")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result createContributionInResourceSpaceWithId(@ApiParam(name = "sid", value = "Resource Space ID") Long sid) {
        // 1. obtaining the user of the requestor
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {

            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            ResourceSpace rs = ResourceSpace.read(sid);
            ContributionTemplate template = null;

            if (rs != null && newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                // TODO: make the template stored in the campaign rather than the proposal making component
                List<Component> components = rs.getComponents();
                if (components != null && !components.isEmpty()) {
                    for (Component component : components) {
                        String componentKey = component.getKey();
                        if (componentKey != null && componentKey.toLowerCase().equals("proposalmaking")) {
                            List<ContributionTemplate> templates = component.getTemplates();
                            if (templates != null && !templates.isEmpty()) {
                                template = templates.get(0);
                                break;
                            }
                        }
                    }
                }
            }

            newContribution.setContextUserId(author.getUserId());
            Contribution c;

            Ebean.beginTransaction();
            try {
                c = createContribution(newContribution, author, type, template, rs);
            } catch (Exception e) {
                Ebean.rollbackTransaction();
                e.printStackTrace();
                Logger.error(e.getStackTrace().toString());
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            if (c != null) {
                rs.addContribution(c);
                rs.update();

   
            }

            Ebean.commitTransaction();
          
            Logger.info("Notification will be sent if it is IDEA or PROPOSAL: " + c.getType());
            if (c.getType().equals(ContributionTypes.IDEA) ||
                    c.getType().equals(ContributionTypes.PROPOSAL)) {
                try {
                    NotificationsDelegate.createNotificationEventsByType(
                            ResourceSpaceTypes.CONTRIBUTION.toString(), c.getUuid());
                } catch (ConfigurationException e) {
                    Logger.error("Configuration error when creating events for contribution: " + LogActions.exceptionStackTraceToString(e));
                } catch (Exception e) {
                    Logger.error("Error when creating events for contribution: " + LogActions.exceptionStackTraceToString(e));
                }
            }

            Promise.promise( () -> {
                ContributionsDelegate.updateCommentCounters(c, "+");
                return NotificationsDelegate.newContributionInResourceSpace(rs, c);
            });

            return ok(Json.toJson(c));
        }
    }

    /**
     * POST      /api/assembly/:aid/group/:gid/contribution
     *
     * @param aid
     * @param gid
     * @param space
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create contributions in the Working Group of an Assembly")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createAssemblyGroupContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Working Group ID") Long gid,
            @ApiParam(name = "space", value = "Resource Space within Working Group", allowableValues = "resources, forum", defaultValue = "resources") String space) {
        // 1. obtaining the user of the requestor
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {
            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            WorkingGroup wg = WorkingGroup.read(gid);
            ResourceSpace rs = space != null && space.equals("forum") ? wg
                    .getForum() : wg.getResources();
            ContributionTemplate template = null;
            if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                List<ContributionTemplate> templates = rs.getTemplates();
                if (templates != null && !templates.isEmpty())
                    template = rs.getTemplates().get(0);
            }
            Contribution c;
            try {
                c = createContribution(newContribution, author, type, template, rs);
            } catch (Exception e) {
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            if (c != null) {
                rs.addContribution(c);
                rs.update();
            }

            // Signal a notification asynchronously
            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInAssemblyGroup(wg, c);
            });
            return ok(Json.toJson(c));
        }
    }

    /**
     * POST      /api/assembly/:aid/contribution/:cid/comment
     *
     * @param aid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create comment on contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createContributionComment(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid) {
        // 1. obtaining the user of the requestor
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {
            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            Contribution c = Contribution.read(cid);
            ResourceSpace rs = c.getResourceSpace();
            ContributionTemplate template = null;
            if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                List<ContributionTemplate> templates = rs.getTemplates();
                if (templates != null && !templates.isEmpty())
                    template = rs.getTemplates().get(0);
            }
            Contribution cNew;
            try {
                cNew = createContribution(newContribution, author, type, template, rs);
            } catch (Exception e) {
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            if (cNew != null) {
                rs.addContribution(cNew);
                rs.update();
            }

            // Signal a notification asynchronously
            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInContribution(c, cNew);
            });
            return ok(Json.toJson(cNew));
        }
    }

    /**
     * POST      /api/assembly/:aid/forumpost
     *
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create Assembly forum post",
            notes = "An Assembly Forum POST is a contribution of type FORUM_POST in the 'forum' resource space of an Assembly")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createAssemblyForumPost(@ApiParam(name = "aid", value = "Assembly ID") Long aid) {
        // 1. obtaining the user of the requestor
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {
            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            Assembly a = Assembly.read(aid);
            ResourceSpace rs = a.getForum();
            ContributionTemplate template = null;
            if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                List<ContributionTemplate> templates = rs.getTemplates();
                if (templates != null && !templates.isEmpty())
                    template = rs.getTemplates().get(0);
            }
            Contribution cNew;
            try {
                cNew = createContribution(newContribution, author, type, template, rs);
            } catch (Exception e) {
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            if (cNew != null) {
                rs.addContribution(cNew);
                rs.update();
            }

            // Signal a notification asynchronously
            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInAssembly(a, cNew);
            });
            return ok(Json.toJson(cNew));
        }
    }

    /**
     * POST      /api/assembly/:aid/group/:gid/forumpost
     *
     * @param aid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Crete forum post in Working Group",
            notes = "A forum post is a contribution of type FORUM_POST in the the 'forum' resource space of the Working Group")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createWorkingGroupForumPost(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                                     @ApiParam(name = "gid", value = "Working Group ID") Long gid) {
        // 1. obtaining the user of the requestor
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {
            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            WorkingGroup wg = WorkingGroup.read(gid);
            ResourceSpace rs = wg.getForum();
            ContributionTemplate template = null;
            if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                List<ContributionTemplate> templates = rs.getTemplates();
                if (templates != null && !templates.isEmpty())
                    template = rs.getTemplates().get(0);
            }
            Contribution c;
            try {
                c = createContribution(newContribution, author, type, template, rs);
            } catch (Exception e) {
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            if (c != null) {
                rs.addContribution(c);
                rs.update();
            }

            // Signal a notification asynchronously
            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInAssemblyGroup(wg, c);
            });
            return ok(Json.toJson(c));
        }
    }

    /**
     * POST      /api/assembly/:aid/contribution/:cid/attachment
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Resource.class, produces = "application/json", value = "Add an attachment to a contribution",
            notes = "An attachment is a RESOURCE (with an URL) added to the 'resources' resource space of a Contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Resource form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Attachment object", value = "Body of Contribution in JSON", required = true, dataType = "models.Resource", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result addAttachmentContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        // 1. read the new contribution data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Resource> newAttachmentForm = ATTACHMENT_FORM
                .bindFromRequest();
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        if (newAttachmentForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,
                    newAttachmentForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
            Contribution c = Contribution.read(contributionId);
            ResourceSpace contributionRs = c.getResourceSpace();
            Resource newAttachment = newAttachmentForm.get();
            newAttachment.setCreator(author);
            contributionRs.addResource(newAttachment);
            contributionRs.update();
            return ok(Json.toJson(newAttachment));
        }
    }

    /* Update Endpoints */

    /**
     * GET       /api/assembly/:aid/contribution/:cid/feedback
     *
     * @param aid
     * @param cid
     * @return
     */
    // TODO: REVIEW to evaluate if removing
    // TODO: erased from routes
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, produces = "application/json", value = "Read contribution Feedback",
            notes = "Feedback on a contribution is a summary of its ups/downs/favs (TBD if this endpoint will remain)")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "ContributionFeedback form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result readContributionFeedbackByUser(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid) {
        User user = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        ContributionFeedback feedback = ContributionFeedback.findByContributionAndUserId(cid, user.getUserId());
        if (feedback != null) {
            return ok(Json.toJson(feedback));
        } else {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("User Feedback Does Not Exist Yet");
            return notFound(Json.toJson(responseBody));
        }
    }

    /**
     * PUT       /api/assembly/:aid/campaign/:caid/contribution/:cid/feedback
     *
     * @param aid
     * @param caid
     * @param cid
     * @return
     */
    // TODO: REVIEW to evaluate if removing
    @ApiOperation(httpMethod = "PUT", response = ContributionStatistics.class, responseContainer = "List", produces = "application/json", value = "Update Feedback on a Contribution",
            notes = "Feedback on a contribution is a summary of its ups/downs/favs (TBD if this endpoint will remain)")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Statistics object", value = "Body of Contribution Statistics in JSON", required = true, dataType = "models.ContributionStatistics", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result updateContributionFeedback(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "caid", value = "Campaign ID") Long caid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid) {
        User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        final Form<ContributionFeedback> updatedFeedbackForm = CONTRIBUTION_FEEDBACK_FORM.bindFromRequest();
        ContributionStatistics updatedStats = new ContributionStatistics(cid);
        Contribution contribution = Contribution.read(cid);
        if (updatedFeedbackForm.hasErrors()) {
            return contributionFeedbackError(updatedFeedbackForm);
        } else {
            ContributionFeedback feedback = updatedFeedbackForm.get();
            Campaign campaignPath = Campaign.read(caid);
            if (campaignPath==null){
                return notFound(Json.toJson(new TransferResponseStatus(
                        "No campaign with id: " + caid )));
            }
            
            // Feedback of tpye TECHNICAL ASSESSMENT, check the password for technical assessment
            if (feedback.getType() != null && feedback.getType().equals(
                    ContributionFeedbackTypes.TECHNICAL_ASSESSMENT)) {
                List<Config> configs = Config
                        .findByCampaignAndKey(
                                campaignPath.getUuid(),
                                GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD);
                // TODO: Leaving the following as example for other cases where
                // we have to read all the configs at once
                // Map<String, Config> configMap =
                // Config.convertConfigsToMap(configs);
                // Config c =
                // configMap.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD);

                boolean authorized = false || configs == null
                        || configs.isEmpty();
                for (Config config : configs) {
                    if (feedback.getPassword() != null && feedback // there is a
                                                                    // password
                                                                    // so verify
                            .getPassword().equals(config.getValue())) {
                        authorized = true;
                    }
                }

                if (!authorized) {
                    return unauthorized(Json.toJson(new TransferResponseStatus(
                            ResponseStatus.UNAUTHORIZED,
                            "Password in feedback form is incorrect")));
                }
            }
            feedback.setContribution(contribution);
            feedback.setUserId(author.getUserId());
            List<ContributionFeedback> existingFeedbacks = ContributionFeedback.findPreviousContributionFeedback(feedback.getContributionId(),
                    feedback.getUserId(), feedback.getWorkingGroupId(), feedback.getType(), feedback.getStatus(), feedback.getNonMemberAuthor());

            Ebean.beginTransaction();
            try {
                feedback.setContribution(contribution);
                feedback.setUserId(author.getUserId());

                //If we found a previous feedback, we set that feedback as archived
                if (existingFeedbacks != null) {
                    for (ContributionFeedback existingFeedback : existingFeedbacks) {
                        existingFeedback.setArchived(true);
                        existingFeedback.update();
                    }


                }

                // We have to do some authorization control
                if (feedback.getWorkingGroupId() != null) {
                    //The user has to be member of working group
                    Membership m = MembershipGroup.findByUserAndGroupId(feedback.getUserId(), feedback.getWorkingGroupId());
                    List<SecurityRole> membershipRoles = m!=null ? m.filterByRoleName(MyRoles.MEMBER.getName()) : null;

                    if (feedback.getType().equals(ContributionFeedbackTypes.WORKING_GROUP) && feedback.getOfficialGroupFeedback() != null && feedback.getOfficialGroupFeedback()) {
                        //The user has to be coordinator of working group
                        membershipRoles =  m!=null ? m.filterByRoleName(MyRoles.COORDINATOR.getName()) : null;
                        if (membershipRoles == null || membershipRoles.isEmpty()) {
                            Logger.error("User has to be coordinator of working group");
                            return unauthorized(Json
                                    .toJson(new TransferResponseStatus(
                                            ResponseStatus.UNAUTHORIZED,
                                            "User has to be coordinator of working group")));
                        }
                    }

                }

                // Make sure ContributionFeedback Type and Status are correct
                ContributionFeedback.create(feedback);

                //NEW_CONTRIBUTION_FEEDBACK NOTIFICATION
                NotificationEventName eventName = existingFeedbacks != null ? NotificationEventName.NEW_CONTRIBUTION_FEEDBACK : NotificationEventName.UPDATED_CONTRIBUTION_FEEDBACK;
                Promise.promise(() -> {
                    Contribution c = Contribution.read(feedback.getContributionId());
                    for (Long campId : c.getCampaignIds()) {
                        Campaign campaign = Campaign.read(campId);
                            NotificationsDelegate.signalNotification(ResourceSpaceTypes.CAMPAIGN, eventName, campaign, feedback);
                    }
                    return true;
                });

                feedback.getWorkingGroupId();
                Promise.promise(() -> {
                    return NotificationsDelegate.signalNotification(
                            ResourceSpaceTypes.WORKING_GROUP,
                            eventName,
                            WorkingGroup.read(feedback.getWorkingGroupId()).getResources(),
                            feedback);
                });

                contribution.setPopularity(new Long(updatedStats.getUps() - updatedStats.getDowns()).intValue());
                contribution.update();
                ContributionHistory.createHistoricFromContribution(contribution);

            } catch (Exception e) {
                Promise.promise(() -> {
                    Logger.error(LogActions.exceptionStackTraceToString(e));
                    return true;
                });
                Ebean.rollbackTransaction();
                return contributionFeedbackError(feedback, e.getLocalizedMessage());
            }

            Ebean.commitTransaction();
            return ok(Json.toJson(updatedStats));
        }
    }

    /**
     * PUT       /api/campaign/:cuuid/contribution/:uuid/feedback
     *
     * @param cuuid
     * @param uuid
     * @return
     */
    // TODO: REVIEW to evaluate if removing
    @ApiOperation(httpMethod = "PUT", response = ContributionFeedback.class, responseContainer = "List", produces = "application/json", value = "Update Feedback on a Contribution",
            notes = "Feedback on a contribution is a summary of its ups/downs/favs (TBD if this endpoint will remain)")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution feedback form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Feedback object", value = "Body of Contribution Feedback in JSON", required = true, dataType = "models.ContributionFeedback", paramType = "body")})
    public static Result updateContributionFeedbackNonMemberAuthor(
            @ApiParam(name = "cuuid", value = "Campaign UUID") UUID cuuid,
            @ApiParam(name = "uuid", value = "Contribution UUID") UUID uuid) {
        final Form<ContributionFeedback> updatedFeedbackForm = CONTRIBUTION_FEEDBACK_FORM.bindFromRequest();
        Contribution contribution = Contribution.readByUUID(uuid);
        if (contribution==null){
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No contributions with uuid: " + uuid )));
        }
        ContributionStatistics updatedStats = new ContributionStatistics(contribution.getContributionId());
        if (updatedFeedbackForm.hasErrors()) {
            return contributionFeedbackError(updatedFeedbackForm);
        } else {
            ContributionFeedback feedback = updatedFeedbackForm.get();
            Campaign campaignPath = Campaign.readByUUID(cuuid);
            if (campaignPath==null){
                return notFound(Json.toJson(new TransferResponseStatus(
                        "No campaign with uuid: " + cuuid )));
            }
            // Feedback of tpye TECHNICAL ASSESSMENT, check the password for technical assessment
            if (feedback.getType().equals(
                    ContributionFeedbackTypes.TECHNICAL_ASSESSMENT)) {
                List<Config> configs = Config
                        .findByCampaignAndKey(
                                campaignPath.getUuid(),
                                GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD);
                // TODO: Leaving the following as example for other cases where we have to read all the configs at once
                // Map<String, Config> configMap = Config.convertConfigsToMap(configs);
                // Config c = configMap.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD);

                boolean authorized = false || configs == null
                        || configs.isEmpty();
                for (Config config : configs) {
                    if (feedback.getPassword() != null && feedback // there is a password so verify
                            .getPassword().equals(config.getValue())) {
                        authorized = true;
                    }
                }

                if (!authorized) {
                    return unauthorized(Json.toJson(new TransferResponseStatus(
                            ResponseStatus.UNAUTHORIZED,
                            "Password in feedback form is incorrect")));
                }
            }
            feedback.setContribution(contribution);
            List<ContributionFeedback> existingFeedbacks = ContributionFeedback.findPreviousContributionFeedback(feedback.getContributionId(),
                    feedback.getUserId(), feedback.getWorkingGroupId(), feedback.getType(), feedback.getStatus(), feedback.getNonMemberAuthor());

            Ebean.beginTransaction();
            try {
                feedback.setContribution(contribution);

                //If we found a previous feedback, we set that feedback as archived
                if (existingFeedbacks != null) {
                    for (ContributionFeedback existingFeedback : existingFeedbacks) {
                        existingFeedback.setArchived(true);
                        existingFeedback.update();
                    }


                }

                // We have to do some authorization control
                if (feedback.getWorkingGroupId() != null) {
                    //The user has to be member of working group
                    Membership m = MembershipGroup.findByUserAndGroupId(feedback.getUserId(), feedback.getWorkingGroupId());
                    List<SecurityRole> membershipRoles = m!=null ? m.filterByRoleName(MyRoles.MEMBER.getName()) : null;

                    if (feedback.getType().equals(ContributionFeedbackTypes.WORKING_GROUP) && feedback.getOfficialGroupFeedback() != null && feedback.getOfficialGroupFeedback()) {
                        //The user has to be coordinator of working group
                        membershipRoles =  m!=null ? m.filterByRoleName(MyRoles.COORDINATOR.getName()) : null;
                        if (membershipRoles == null || membershipRoles.isEmpty()) {
                            Logger.error("User has to be coordinator of working group");
                            return unauthorized(Json
                                    .toJson(new TransferResponseStatus(
                                            ResponseStatus.UNAUTHORIZED,
                                            "User has to be coordinator of working group")));
                        }
                    }

                }

                // Make sure ContributionFeedback Type and Status are correct
                ContributionFeedback.create(feedback);

                //NEW_CONTRIBUTION_FEEDBACK NOTIFICATION
                NotificationEventName eventName = existingFeedbacks != null ? NotificationEventName.NEW_CONTRIBUTION_FEEDBACK : NotificationEventName.UPDATED_CONTRIBUTION_FEEDBACK;
                Promise.promise(() -> {
                    Contribution c = Contribution.read(feedback.getContributionId());
                    for (Long campId : c.getCampaignIds()) {
                        Campaign campaign = Campaign.read(campId);
                        NotificationsDelegate.signalNotification(ResourceSpaceTypes.CAMPAIGN, eventName, campaign, feedback);
                    }
                    return true;
                });

                feedback.getWorkingGroupId();
                Promise.promise(() -> {
                    return NotificationsDelegate.signalNotification(
                            ResourceSpaceTypes.WORKING_GROUP,
                            eventName,
                            WorkingGroup.read(feedback.getWorkingGroupId()).getResources(),
                            feedback);
                });

                contribution.setPopularity(new Long(updatedStats.getUps() - updatedStats.getDowns()).intValue());
                contribution.update();
                ContributionHistory.createHistoricFromContribution(contribution);

            } catch (Exception e) {
                Promise.promise(() -> {
                    Logger.error(LogActions.exceptionStackTraceToString(e));
                    return true;
                });
                Ebean.rollbackTransaction();
                return contributionFeedbackError(feedback, e.getLocalizedMessage());
            }

            Ebean.commitTransaction();
            return ok(Json.toJson(updatedStats));
        }
    }

    /**
     * PUT       /api/assembly/:aid/contributions/popularity
     *
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = String.class, produces = "application/json", value = "Response status")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result updateContributionsPopularity(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid) {
        List<Contribution> contributions = Contribution.findAll();
        for (Contribution c : contributions) {
            ContributionStatistics updatedStats = new ContributionStatistics(c.getContributionId());
            c.setPopularity(new Long(updatedStats.getUps() - updatedStats.getDowns()).intValue());
            c.update();
        }
        return ok(Json.toJson("Ok"));

    }

    /**
     * PUT       /api/assembly/:aid/contribution/:cid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Contribution.class, produces = "application/json", value = "Update contribution in Assembly")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result updateContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        // 1. read the new contribution data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM.bindFromRequest();
        User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));

        if (newContributionForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,
                    newContributionForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
            Contribution newContribution = newContributionForm.get();
            newContribution.setContributionId(contributionId);
            newContribution.setContextUserId(author.getUserId());
          
            Contribution existingContribution = Contribution.read(contributionId);
            for (Field field : existingContribution.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    if (field.getName().toLowerCase().contains("ebean") || field.isAnnotationPresent(ManyToMany.class)
                            || field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToMany.class)
                            || field.isAnnotationPresent(OneToOne.class)) {
                        continue;
                    }
                    field.set(existingContribution, field.get(newContribution));
                } catch (Exception e) {
                }
            }
            existingContribution.setContextUserId(author.getUserId());
            Ebean.beginTransaction();
          
            try {
                        Contribution.update(existingContribution);
                        Ebean.commitTransaction();
                  } catch (Exception e) {
                        Ebean.rollbackTransaction();
                        e.printStackTrace();
                        Logger.error("Error while updating contribution => ", LogActions.exceptionStackTraceToString(e));
                  TransferResponseStatus responseBody = new TransferResponseStatus();
                  responseBody.setStatusMessage(e.getMessage());
                  return Controller.internalServerError(Json.toJson(responseBody));
                  }

            ResourceSpace rs = Assembly.read(aid).getResources();
            Promise.promise(() -> {
                return NotificationsDelegate.updatedContributionInResourceSpace(rs, existingContribution);
            });

            return ok(Json.toJson(existingContribution));
        }
    }

    /**
     * POST      /api/space/:sid/space/:new_sid
     *
     * @param sid
     * @param new_sid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = String.class, produces = "application/json", value = "Assign a resouce space to other resource space")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result assignResourceSpaceToResourceSpace(
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid,
            @ApiParam(name = "new_sid", value = "New Resource Space ID") Long new_sid) {
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        ResourceSpace rs = ResourceSpace.read(sid);
        ResourceSpace rsNew = ResourceSpace.read(new_sid);

        ResourceSpace rCombined =  ResourceSpace.setResourceSpaceItems(rs,rsNew);
        try {
            rCombined.update();
        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error when assigning Resource Space to Resource Space: " + e.toString())));
        }
        return ok();
    }

    /**
     * POST      /api/assembly/:aid/contribution/:cid/space/:sid
     *
     * @param aid
     * @param cid
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Assign a contribution to a resource space")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result assignContributionResourceSpace(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid,
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid) {
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        Contribution contribution = Contribution.read(cid);
        if (contribution == null) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("No contribution found");
            return notFound(Json.toJson(responseBody));
        }
        ResourceSpace rsNew = ResourceSpace.read(contribution.getResourceSpaceId());
        ResourceSpace rs = ResourceSpace.read(sid);
        ResourceSpace rCombined = ResourceSpace.setResourceSpaceItems(rs,rsNew);

        try {
            rCombined.update();
            ContributionHistory.createHistoricFromContribution(contribution);
        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error when assigning Contribution to Resource Space: " + e.toString())));
        }

        // Signal a notification asynchronously
        Promise.promise(() -> {
            return NotificationsDelegate.newContributionInResourceSpace(rs,contribution);
        });
        return ok(Json.toJson(contribution));
    }

    /**
     * DELETE      /api/assembly/:aid/contribution/:cid/space/:sid
     *
     * @param aid
     * @param cid
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = String.class, produces = "application/json", value = "Delete a contribution to a resource space")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result deleteContributionResourceSpace(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid,
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid) {
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        Contribution contribution = Contribution.read(cid);

        if (contribution == null) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("No contribution found");
            return notFound(Json.toJson(responseBody));
        }
        ResourceSpace rsNew = ResourceSpace.read(contribution.getResourceSpaceId());
        ResourceSpace rs = ResourceSpace.read(sid);
        try {


            rs.getContributions().remove(contribution);
            rs.update();
            ContributionHistory.createHistoricFromContribution(contribution);

            

        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error when removing Contribution from Resource Space: " + e.toString())));
        }
        return ok();
    }

    /**
     * PUT       /api/assembly/:aid/contribution/:cid/moderate
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = String.class, produces = "application/json", value = "Contribution moderation. Soft deletes contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result moderateContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution id") Long contributionId) {
        // 1. read the new contribution data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM.bindFromRequest();
        if (newContributionForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,
                    newContributionForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
            Contribution contributionFromDatabase = Contribution.read(contributionId);
            Contribution moderated = newContributionForm.get();
            contributionFromDatabase.setModerationComment(moderated.getModerationComment());

            // ContributionsDelegate.updateCommentCounters(contributionFromDatabase, "-");

            Contribution.update(contributionFromDatabase);
            Contribution.softDelete(contributionFromDatabase);

            ResourceSpace rs = Assembly.read(aid).getResources();
            Promise.promise(() -> {
                ContributionsDelegate.updateCommentCounters(contributionFromDatabase, "-");
                return NotificationsDelegate.updatedContributionInResourceSpace(rs, contributionFromDatabase);
            });
            return ok();
        }
    }

    /**
     * PUT       /api/assembly/:aid/contribution/:cid/softremoval
     * TODO: create a dynamic handler to check if the contribution belongs to the user
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = String.class, produces = "application/json", value = "Logical removal of contribution in Assembly")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    //@Dynamic(value = "ModeratorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result softDeleteContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        Contribution c = Contribution.read(contributionId);

        // ContributionsDelegate.updateCommentCounters(c, "-");

        Contribution.softDelete(contributionId);
        ResourceSpace rs = Assembly.read(aid).getResources();
        Promise.promise(() -> {
            ContributionsDelegate.updateCommentCounters(c, "-");
            return NotificationsDelegate.updatedContributionInResourceSpace(rs, c);
        });
        return ok();
    }

    /**
     * PUT       /api/assembly/:aid/contribution/:cid/recover
     * TODO: create a dynamic handler to check if the contribution belongs to the user
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = String.class, produces = "application/json", value = "Logical recovery of contribution Assembly")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "ModeratorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result recoverContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        Contribution.softRecovery(contributionId);
        Contribution c = Contribution.read(contributionId);
        ResourceSpace rs = Assembly.read(aid).getResources();
        Promise.promise(() -> {
            return NotificationsDelegate.updatedContributionInResourceSpace(rs, c);
        });
        return ok();
    }

    /**
     * DELETE    /api/assembly/:aid/contribution/:cid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = String.class, produces = "application/json", value = "Delete a contribution (will remove it from the database)",
            notes = "Only for ADMINS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result forceDeleteContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {

        // NEW PROMISE
        Contribution contribution = Contribution.read(contributionId);
        Promise.promise(() -> {
             ContributionsDelegate.updateCommentCounters(contribution, "-");
             return ok();
        });
        Contribution.delete(contributionId);
        return ok();
    }


    /**
     * POST  /api/contribution/:uuid/themes
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Theme.class, responseContainer = "List", produces = "application/json", value = "Add themes to a contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Theme form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Theme objects", value = "Themes to add to the contribution", dataType = "models.transfer.ThemeListTransfer", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result addThemeToContribution(@ApiParam(name = "uuid", value = "Contribution's Universal Id (UUID)") UUID uuid) {
        Contribution contribution;
        User authorActive = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        contribution = Contribution.readByUUID(uuid);

        try {
            // We have to save themes without ID first
            List<Theme> themes = THEMES_FORM.bindFromRequest().get().getThemes();
            List<Theme> newThemes = themes.stream().filter(t -> t.getThemeId() == null).collect(Collectors.toList());
            newThemes.forEach(t -> {
                t.save();
            });
            contribution.setThemes(themes);
            contribution.update();

            // add newThemes to compaings
            contribution.getCampaignIds().forEach(id -> {
                Campaign campaign = Campaign.find.byId(id);
                List<Theme> campaignThemes = campaign.getThemes();
                campaignThemes.addAll(newThemes);
                campaign.setThemes(campaignThemes);
                campaign.update();
            });
            return ok(Json.toJson(themes));
        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with the given uuid")));
        }
    }

    /**
     * POST  /api/contribution/:uuid/authors
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Add a author to a contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authors objects", value = "Authors to add to the contribution", dataType = "models.User", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result addAuthorToContribution(@ApiParam(name = "uuid", value = "Contribution's Universal Id (UUID)") UUID uuid) {
        Contribution contribution;
        User authorActive = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        contribution = Contribution.readByUUID(uuid);

        try {
            User user = AUTHORS_FORM.bindFromRequest().get();
            User author = User.read(user.getUserId());
            boolean authorExist = contribution.getAuthors().contains(author);
            if(!authorExist) {
                contribution.getAuthors().add(author);
                contribution.update();
                return ok(Json.toJson(contribution));
            }else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Author already in contribution")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with the given uuid")));
        }
    }

    /**
     * DELETE  /api/contribution/:uuid/themes/:tid
     *
     * @param uuid
     * @param tid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Contribution.class, produces = "application/json", value = "Add a theme to a contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Theme objects", value = "Themes to add to the contribution", dataType = "models.transfer.ThemeListTransfer", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result deleteThemeFromContribution(@ApiParam(name = "uuid", value = "Contribution's Universal Id (UUID)") UUID uuid,
                                                     @ApiParam(name = "tid", value = "Theme's Id") Long tid) {
        Contribution contribution;
        User authorActive = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        contribution = Contribution.readByUUID(uuid);

        try {
            Theme theme = Theme.read(tid);
            contribution.getThemes().remove(theme);
            contribution.update();
            return ok(Json.toJson(contribution));
        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with the given uuid")));
        }
    }

    /**
     * DELETE  /api/contribution/:uuid/authors/:auuid
     *
     * @param uuid
     * @param auuid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Contribution.class, produces = "application/json", value = "Add a author to a contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authors objects", value = "Authors to add to the contribution", dataType = "models.User", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result deleteAuthorFromContribution(@ApiParam(name = "uuid", value = "Contribution's Universal Id (UUID)") UUID uuid,
                                                      @ApiParam(name = "auuid", value = "Author's Universal Id (UUID)") UUID auuid) {
        Contribution contribution;
        User authorActive = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        contribution = Contribution.readByUUID(uuid);

        try {
            User author = User.findByUUID(auuid);
            boolean authorExist = contribution.getAuthors().contains(author);
            if(authorExist) {
                contribution.getAuthors().remove(author);
                contribution.update();
                return ok(Json.toJson(contribution));
            }else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Uuid given is not a contribution author")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with the given uuid")));
        }
    }

    /**
     * GET  /api/contributions
     *
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "List contributions")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class),
            @ApiResponse(code = 200, message = "List of contributions", response = ApiResponseTransfer.class)})
    public static Result all(@ApiParam(name = "page", defaultValue = "1", value = "Page to retrieve") String pageStr,
                             @ApiParam(name = "by_uuid", value = "Filter contributions by UUID") String byUuid,
                             @ApiParam(name = "by_theme_id", value = "Filter contributions by theme") String byThemeIdStr) {
        try {
            Integer page = Integer.parseInt(pageStr);
            Long byThemeId = null;

            if (!byThemeIdStr.equals("")) {
                byThemeId = Long.parseLong(byThemeIdStr);
            }
            List<Contribution> contributions = ContributionsDelegate.findBy(page, byUuid, byThemeId);
            ApiResponseTransfer<Contribution> rsp = new ApiResponseTransfer<Contribution>();
            rsp.setResults(contributions);
            rsp.setPageSize(ContributionsDelegate.PAGE_SIZE);
            rsp.setTotalCount(ContributionsDelegate.countBy(byUuid, byThemeId));
            return ok(Json.toJson(rsp));
        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Problems fetching list of contributions")));
        }
    }

    /*
     * Non-exposed methods: creation methods
     */

    /**
     * This method is reused by all other contribution creation methods to centralize its logic
     *
     * @param newContrib
     * @param author
     * @param type
     * @param etherpadServerUrl
     * @param etherpadApiKey
     * @param t
     * @param containerResourceSpace
     * @return
     * @throws MalformedURLException
     * @throws MembershipCreationException
     */
    public static Contribution createContribution(Contribution newContrib,
                                                  User author, ContributionTypes type, String etherpadServerUrl, String etherpadApiKey,
                                                  ContributionTemplate t, ResourceSpace containerResourceSpace) throws MalformedURLException, MembershipCreationException {

        newContrib.setType(type);
        // if type is PROPOSAL, then change the default status value
//        if (type.equals(ContributionTypes.PROPOSAL)) {
//            newContrib.setStatus(ContributionStatus.NEW);
//        }
        if (author != null) {
            newContrib.addAuthor(author);
            if (newContrib.getLang() == null)
                newContrib.setLang(author.getLanguage());
            newContrib.setContextUserId(author.getUserId());
        }


        if (etherpadServerUrl == null || etherpadServerUrl.isEmpty()) {
            // read etherpad server url from config file
            Logger.info("Etherpad URL was not configured");
            etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
        }

        if (etherpadApiKey == null || etherpadApiKey.isEmpty()) {
            // read etherpad server url from config file
            Logger.info("Etherpad API Key was not configured");
            etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);
        }

        Logger.info("Using Etherpad server at: " + etherpadServerUrl);
        Logger.debug("Using Etherpad API Key: " + etherpadApiKey);
        
        if (containerResourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN) && type != null && (type.equals(ContributionTypes.PROPOSAL) || type.equals(ContributionTypes.NOTE))) {
            Campaign c = containerResourceSpace.getCampaign(); 
            
        	List<Config> campaignConfigs = c.getConfigs();
        	Integer hasStatusConfig = 0;     
        	Integer hasEtherpadConfig = 0;
        	
        	for(Config cc: campaignConfigs){
        		if (type.equals(ContributionTypes.PROPOSAL)) {
        			if (cc.getKey().equals(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_PROPOSAL_DEFAULT_STATUS)) {
        				hasStatusConfig = 1;
	        			if (cc.getValue().equalsIgnoreCase("NEW")) {
	        				newContrib.setStatus(ContributionStatus.NEW);
	        			} else if (cc.getValue().equalsIgnoreCase("PUBLISHED")) {
	        				newContrib.setStatus(ContributionStatus.PUBLISHED);
	        			}
        			}
        		}
    		
    			if (cc.getKey().equals(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_ETHERPAD)){
    				hasEtherpadConfig = 1;
    				if (cc.getValue().equalsIgnoreCase("FALSE")) {
    					ContributionsDelegate.createAssociatedPad(etherpadServerUrl, etherpadApiKey, newContrib, t, containerResourceSpace.getResourceSpaceUuid());
    				}    	            
    	        }        			 
        	}
        	// If the configuration is not defined, get the defaults values
        	if (hasStatusConfig == 0 && type.equals(ContributionTypes.PROPOSAL)) {
        		String status = GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_PROPOSAL_DEFAULT_STATUS);
        		newContrib.setStatus(ContributionStatus.valueOf(status));
        	} 
        	
        	if (hasEtherpadConfig == 0) {
        		String etherpad = GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_ETHERPAD);
        		if (etherpad.equalsIgnoreCase("FALSE"))
        			ContributionsDelegate.createAssociatedPad(etherpadServerUrl, etherpadApiKey, newContrib, t, containerResourceSpace.getResourceSpaceUuid());
        	}
        }
        
        Logger.info("Creating new contribution");
        Logger.debug("=> " + newContrib.toString());

        // Get list of BRAINSTORMING contributions that inspire the new one
        List<Contribution> inspirations = newContrib.getAssociatedContributions();

        // If contribution is a proposal and there is no working group associated as author,
        // create one automatically with the creator as coordinator
        List<WorkingGroup> workingGroupAuthors = newContrib
                .getWorkingGroupAuthors();
        String newWorkingGroupName = "WG for '" + newContrib.getTitle() + "'";
        if (workingGroupAuthors != null && !workingGroupAuthors.isEmpty()) {
            WorkingGroup wg = workingGroupAuthors.get(0);
            if (wg.getGroupId() == null) {
                newWorkingGroupName = wg.getName();
                workingGroupAuthors = null;
                newContrib.setWorkingGroupAuthors(null);
            }
            List<WorkingGroup> workingGroupAuthorsLoaded = new ArrayList<WorkingGroup>();
            for (WorkingGroup wgroup: newContrib.getWorkingGroupAuthors()) {
                WorkingGroup contact = WorkingGroup.read(wgroup.getGroupId());
                workingGroupAuthorsLoaded.add(contact);
            }
            newContrib.setWorkingGroupAuthors(workingGroupAuthorsLoaded);
        }

        WorkingGroup newWorkingGroup = new WorkingGroup();
        Boolean workingGroupIsNew = false;
        if (newContrib.getType().equals(ContributionTypes.PROPOSAL)
                && (workingGroupAuthors == null || workingGroupAuthors
                .isEmpty())) {
            workingGroupIsNew = true;
            newWorkingGroup.setCreator(author);
            newWorkingGroup.setName(newWorkingGroupName);
            newWorkingGroup.setLang(author.getLanguage());
            newWorkingGroup.setExistingThemes(newContrib.getExistingThemes());
            newWorkingGroup.setListed(false);
            newWorkingGroup
                    .setInvitationEmail("Hello! You have been invited to be a member of the "
                            + "Working Group \"" + newWorkingGroup.getName() + "\" in AppCivist. "
                            + "If you are interested, follow the link attached to this "
                            + "invitation to accept it. If you are not interested, you "
                            + "can just ignore this message");
            newWorkingGroup.setMajorityThreshold("simple");
            newWorkingGroup.setBlockMajority(false);

            WorkingGroupProfile newWGProfile = new WorkingGroupProfile();
            newWGProfile.setIcon("https://s3-us-west-1.amazonaws.com/appcivist-files/icons/justicia-140.png");
            newWGProfile.setManagementType(ManagementTypes.COORDINATED_AND_MODERATED);
            newWGProfile.setSupportedMembership(SupportedMembershipRegistration.INVITATION_AND_REQUEST);

            newWorkingGroup.setProfile(newWGProfile);
            newWorkingGroup = WorkingGroup.create(newWorkingGroup);

            containerResourceSpace.addWorkingGroup(newWorkingGroup);

            // Find resource space of the assembly and add it also in there
            if (containerResourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
                Campaign c = containerResourceSpace.getCampaign();
                Assembly a = Assembly.read(c.getAssemblies().get(0));
                ResourceSpace aRs = a.getResources();
                aRs.addWorkingGroup(newWorkingGroup);
                aRs.update();
            }

            newContrib.getWorkingGroupAuthors().add(newWorkingGroup);
        }

        List<User> authorsLoaded = new ArrayList<User>();
        Map<Long,Boolean> authorAlreadyAdded = new HashMap<>();
        for (User user: newContrib.getAuthors()) {
            Long userId = user.getUserId();
            User auth = User.read(userId);
            Boolean alreadyAdded = authorAlreadyAdded.get(userId);
            if (alreadyAdded == null || !alreadyAdded) {
                authorsLoaded.add(auth);
                authorAlreadyAdded.put(auth .getUserId(), true);
            }
        }
        newContrib.setAuthors(authorsLoaded);
        Contribution.create(newContrib);
        newContrib.refresh();

        //Previously we also asked the associated contribution to be PROPOSAL,
        //but now any type of contribution can be associated to another
        if (inspirations != null && !inspirations.isEmpty()) {
            ResourceSpace cSpace = ResourceSpace.read(newContrib.getResourceSpaceId());
            for (Contribution inspiration : inspirations) {
                Contribution c = Contribution.read(inspiration.getContributionId());
                cSpace.addContribution(c);
            }
            cSpace.update();
            cSpace.refresh();

            // If the proposal is coming from brainstorming, invite the
            // commenters of the
            // each brainstorming contributions
            if (!inspirations.isEmpty() && workingGroupIsNew) {
                inviteCommentersInInspirationList(inspirations, newContrib,
                        newWorkingGroup);
            }
        }

        // If contribution is a proposal and the resource space where it is added is a Campaign
        // create automatically a related candidate for the contribution in the bindingBallot
        // and consultiveBallot associated to the campaign.
        if (containerResourceSpace != null && containerResourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
            UUID binding = Campaign.queryBindingBallotByCampaignResourceSpaceId(containerResourceSpace
                    .getResourceSpaceId());
            UUID consultive = Campaign.queryConsultiveBallotByCampaignResourceSpaceId(containerResourceSpace
                    .getResourceSpaceId());

            // Add the candidates automatically only to the binding ballot marked in the campaign as such
            // and to the "consultive" ballot marked in the campaign as such
            for (Ballot ballot : containerResourceSpace.getBallots()) {
                if ((ballot.getDecisionType().equals("BINDING") && (ballot.getUuid().equals(binding)))
                        || (ballot.getDecisionType().equals("CONSULTIVE") && ballot.getUuid().equals(consultive))) {
                    BallotCandidate contributionAssociatedCandidate = new BallotCandidate();
                    contributionAssociatedCandidate.setBallotId(ballot.getId());
                    contributionAssociatedCandidate.setCandidateType(new Integer(1));
                    contributionAssociatedCandidate.setContributionUuid(newContrib.getUuid());
                    contributionAssociatedCandidate.save();
                }
            }
        }

        // If the contribution is a proposal, create an associated candidate in the ballot
        // of the working group authors
        for (WorkingGroup wg : newContrib.getWorkingGroupAuthors()) {
            UUID consensus = WorkingGroup.queryConsensusBallotByGroupResourceSpaceId(wg.getResourcesResourceSpaceId());
            Ballot b = Ballot.findByUUID(consensus);

            if (b!=null) {
                BallotCandidate contributionAssociatedCandidate = new BallotCandidate();
                contributionAssociatedCandidate.setBallotId(b.getId());
                contributionAssociatedCandidate.setCandidateType(new Integer(1));
                contributionAssociatedCandidate.setContributionUuid(newContrib.getUuid());
                contributionAssociatedCandidate.save();
            }
        }

        return newContrib;
    }

    /**
     * It looks into the list of comments of the brainstorming contributions that serve as inspiration
     * for a new proposal
     *
     * @param inspirations
     * @param newContrib
     * @param newWorkingGroup
     * @throws MembershipCreationException
     */
    private static void inviteCommentersInInspirationList(
            List<Contribution> inspirations, Contribution newContrib,
            WorkingGroup newWorkingGroup) throws MembershipCreationException {
        HashMap<String, Boolean> invitedEmails = new HashMap<String, Boolean>();
        for (Contribution inspirationObject : inspirations) {
            Contribution inspiration = Contribution.read(inspirationObject.getContributionId());
            List<Contribution> comments = inspiration.getComments();
            for (Contribution comment : comments) {
                User commentAuthor = comment.getAuthors().get(0);
                String authorEmail = commentAuthor.getEmail();
                String creatorEmail = newContrib.getAuthors().get(0).getEmail();
                Boolean invited = invitedEmails.get(authorEmail);
                if ((invited == null || !invited) && !authorEmail.equals(creatorEmail)) {
                    InvitationTransfer invitation = new InvitationTransfer();
                    invitation.setEmail(authorEmail);
                    invitation.setInvitationEmail(newWorkingGroup.getInvitationEmail());
                    invitation.setTargetId(newWorkingGroup.getGroupId());
                    invitation.setTargetType("GROUP");
                    invitation.setCoordinator(false);
                    invitation.setModerator(false);
                    MembershipInvitation.create(invitation, newWorkingGroup.getCreator(), newWorkingGroup);
                    invitedEmails.put(authorEmail, true);
                }
            }
        }

    }

    public static Result createContributionResult(Contribution newContrib,
                                                  User author, ContributionTypes type, ContributionTemplate t, ResourceSpace containerResourceSpace) {
        try {
            return ok(Json.toJson(createContribution(newContrib, author, type, t, containerResourceSpace)));
        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error when creating Contribution: " + e.toString())));
        }
    }

    public static Contribution createContribution(Contribution newContrib,
                                                  User author, ContributionTypes type, ContributionTemplate t,
                                                  ResourceSpace containerResourceSpace) throws MalformedURLException, MembershipCreationException {
        // TODO: dynamically obtain etherpad server URL and Key from component configuration
        return createContribution(newContrib, author, type, null, null, t, containerResourceSpace);
    }

    public static Result createContributionInAssembly(
            Contribution newContrib, User author, Assembly a,
            ContributionTypes type) {
        ResourceSpace rs = a.getResources();

        ContributionTemplate template = null;
        if (newContrib.getType() != null && newContrib.getType().equals(ContributionTypes.PROPOSAL)) {
            List<ContributionTemplate> templates = rs.getTemplates();
            if (templates != null && !templates.isEmpty())
                template = rs.getTemplates().get(0);
        }

        Contribution c;
        try {
            c = createContribution(newContrib, author, type, template, rs);
        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error when creating contribution: " + e.toString())));
        }
        rs.addContribution(c);
        rs.update();
        return ok(Json.toJson(c));
    }

    public static Result updateContributionResult(Long sid, Long id,
                                                  Contribution updatedContribution, ContributionTypes type) {
        updatedContribution.setContributionId(id);
        if (updatedContribution.getType().equals(type)) {
            Contribution.update(updatedContribution);
            Logger.info("Updating contribution");
            Logger.debug("=> " + updatedContribution.toString());
            return ok(Json.toJson(updatedContribution));
        } else {
            return contributionUpdateError(updatedContribution, "Update on "
                    + type + " for Contribution that is not of this type");
        }
    }

    /*
     * Non-exposed methods: error Message Results
     */
    private static Result contributionCreateError(
            Form<Contribution> newContributionForm) {
        return contributionOperationError(
                GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, newContributionForm
                        .errorsAsJson().toString());
    }

    private static Result contributionFeedbackError(Form<ContributionFeedback> newStatsForm) {
        // TODO Auto-generated method stub
        return contributionOperationError(
                GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, newStatsForm
                        .errorsAsJson().toString());
    }

    private static Result contributionFeedbackError(ContributionFeedback feedback, String errorMsg) {
        // TODO Auto-generated method stub
        return contributionOperationError(
                GlobalData.CONTRIBUTION_UPDATE_MSG_ERROR, "{ error : " + errorMsg + ", object : "
                        + Json.toJson(feedback));
    }

    public static Result contributionUpdateError(
            Form<Contribution> updatedContributionForm) {
        return contributionOperationError(
                GlobalData.CONTRIBUTION_UPDATE_MSG_ERROR,
                updatedContributionForm.errorsAsJson().toString());
    }

    public static Result contributionUpdateError(
            Contribution updatedContribution, String errorMsg) {
        return contributionOperationError(
                GlobalData.CONTRIBUTION_UPDATE_MSG_ERROR,
                "{ error : " + errorMsg + ", object : "
                        + Json.toJson(updatedContribution));
    }

    public static Result contributionCreateError(Contribution newContribution,
                                                 String errorMsg) {
        return contributionOperationError(GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, "{ error : " + errorMsg + ", object : "
                + Json.toJson(newContribution));
    }

    public static Result contributionOperationError(String msgi18nCode,
                                                    String msg) {
        TransferResponseStatus responseBody = new TransferResponseStatus();
        responseBody.setStatusMessage(Messages.get(msgi18nCode) + ": " + msg);
        return badRequest(Json.toJson(responseBody));
    }

    /** IDEAS **/
    /**
     * POST      /api/assembly/:aid/campaign/:cid/contribution/import
     * Import ideas file
     *
     * @param aid Assembly Id
     * @param cid Campaing Id
     * @return
     */
    @ApiOperation(httpMethod = "POST", consumes = "application/csv", value = "Import CSV file with campaign ideas",
            notes = "CSV format: idea title, idea summary, idea author, idea theme <br/>" +
                    "The values must be separated by coma (,). If the theme column has more than one theme, then it must be separated by dash (-).")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "CSV file", dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result importContributionsOld(
            @ApiParam(name = "aid", value = "Assembly id") Long aid,
            @ApiParam(name = "cid", value = "Campaign id") Long cid) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("file");
        Campaign campaign = null;

        if (uploadFilePart != null) {
            try {
                campaign = Campaign.read(cid);
                ResourceSpace rs = null;
                if (campaign != null) {
                    rs = campaign.getResources();
                }
                br = new BufferedReader(new FileReader(uploadFilePart.getFile()));
                String cvsSplitBy = ",";
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    String[] cell = line.split(cvsSplitBy);
                    Contribution c = new Contribution();
                    c.setTitle(cell[0]);
                    c.setText(cell[1]);
                    // TODO existing author
                    c.setFirstAuthorName(cell[2]);
                    // TODO existing theme
                    List<Theme> themesList = new ArrayList<Theme>();
                    String themeSplitBy = "-";
                    String[] themes = cell[3].split(themeSplitBy);
                    for (String theme : themes) {
                        Theme t = new Theme();
                        t.setTitle(theme);
                        themesList.add(t);
                    }
                    c.setThemes(themesList);
                    c.setSourceCode(cell[4]);

                    Resource res = null;

                    switch (cell[5]) {
                        case "IDEA":
                            c.setType(ContributionTypes.IDEA);
                            break;
                        case "PROPOSAL":
                            c.setType(ContributionTypes.PROPOSAL);
                            // Etherpad support
                            String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
                            String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);

                            if (cell[6] != null) {
                                res = ResourcesDelegate.createResource(null, cell[6], ResourceTypes.PROPOSAL, true, false);
                            } else {
                                List<Resource> templates = ContributionsDelegate.getTemplates(aid.toString(), cid.toString());

                                if (templates != null) {
                                    // if there are more than one, then use the last
                                    String padId = templates.get(templates.size() - 1).getPadId();
                                    EtherpadWrapper wrapper = new EtherpadWrapper(etherpadServerUrl, etherpadApiKey);
                                    String templateHtml = wrapper.getHTML(padId);
                                    // save the etherpad
                                    res = ResourcesDelegate.createResource(null, templateHtml, ResourceTypes.PROPOSAL, true, false);
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    rs.addContribution(c);
                    ResourceSpace.update(rs);

                    if (res != null) {
                        List<Resource> resources = new ArrayList<>();
                        resources.add(res);
                        c.setExistingResources(resources);
                    }

                    Contribution.create(c);

                    // Feedback support
                    if (cell.length == 9) {
                        String feedback = cell[7];
                        String feedbackUser = cell[8];
                        ContributionFeedback cFeed = new ContributionFeedback();
                        switch (feedback) {
                            case "up":
                                cFeed.setUp(true);
                                break;
                            case "down":
                                cFeed.setDown(true);
                                break;
                            default:
                                break;
                        }
                        User user = User.findByUserName(feedbackUser);
                        cFeed.setUserId(user.getUserId());
                        cFeed.setContribution(c);
                        cFeed.setType(ContributionFeedbackTypes.TECHNICAL_ASSESSMENT);
                        ContributionFeedback.create(cFeed);
                    }

                }
                //Ebean.commitTransaction();
            } catch (EntityNotFoundException ex) {
                ex.printStackTrace();
                return internalServerError("The campaign doesn't exist");
            } catch (Exception e) {
                //Ebean.rollbackTransaction();
                e.printStackTrace();
                return contributionFeedbackError(null, e.getLocalizedMessage());
            }
        }
        return ok();
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution/export
     * Export ideas file
     *
     * @param aid Assembly Id
     * @param cid Campaing Id
     * @return
     */
    @ApiOperation(httpMethod = "GET", produces = "application/csv", value = "Export campaign ideas to a CSV file")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result exportContributions(
            @ApiParam(name = "aid", value = "Assembly id") Long aid,
            @ApiParam(name = "cid", value = "Campaign id") Long cid) {
        String csv = "idea title,idea summary,idea author,idea theme, source code, type\n";
        Campaign campaign = null;
        try {
            campaign = Campaign.read(cid);
            ResourceSpace rs = null;
            if (campaign != null) {
                rs = campaign.getResources();
                List<Contribution> contributions = ContributionsDelegate
                        .findContributionsInResourceSpace(rs, null);
                for (Contribution c : contributions) {
                    csv = csv + (c.getTitle() != null ? c.getTitle() : "") + ",";
                    csv = csv + (c.getAssessmentSummary() != null ? c.getAssessmentSummary() : "") + ",";
                    // TODO existing author
                    csv = csv + (c.getFirstAuthorName() != null ? c.getFirstAuthorName() : "");
                    csv = csv + ",";
                    int themeSize = c.getThemes().size();
                    for (int i = 0; i < themeSize; i++) {
                        if (i > 0 && i < themeSize + 1) {
                            csv = csv + "-";
                        }
                        csv = csv + c.getThemes().get(i).getTitle();
                    }
                    csv = csv + "," + (c.getSourceCode() != null ? c.getSourceCode() : "");
                    csv = csv + "," + c.getType().toString() + "\n";
                }

            }
        } catch (EntityNotFoundException ex) {
            return internalServerError("The campaign doesn't exist");
        }

        response().setContentType("application/csv");
        response().setHeader("Content-disposition", "attachment; filename=contributions.csv");
        File tempFile;
        try {
            tempFile = File.createTempFile("contributions.csv", ".tmp");
            FileUtils.writeStringToFile(tempFile, csv);
            return ok(tempFile);
        } catch (IOException e) {
            return internalServerError();
        }

    }


    /**
     * POST      /api/assembly/:aid/campaign/:cid/contribution/import
     * Import ideas file
     *
     * @param aid Assembly Id
     * @param cid Campaing Id
     * @return
     */
    @ApiOperation(httpMethod = "POST", consumes = "application/csv", value = "Import CSV file with campaign ideas or proposals",
            notes = "CSV format: the values must be separated by coma (;). If the theme column has more than one theme, then it must be separated by dash (-).")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "CSV file", dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result importContributions(
            @ApiParam(name = "aid", value = "Assembly id") Long aid,
            @ApiParam(name = "cid", value = "Campaign id") Long cid,
            @ApiParam(name = "type", value = "Contribution Type", allowableValues = "IDEA, PROPOSAL", defaultValue = "IDEA") String type) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("file");
        Campaign campaign = Campaign.read(cid);

        if (uploadFilePart != null && campaign != null) {
            try {
                br = new BufferedReader(new FileReader(uploadFilePart.getFile()));
                String cvsSplitBy = "\\t";
                String line = br.readLine();
                Logger.debug("Importing record => " + line);
                switch (type) {
                    case "IDEA":
                        try {
                            Ebean.beginTransaction();
                            while ((line = br.readLine()) != null) {
                                String[] cell = line.split(cvsSplitBy);
                                Contribution c = new Contribution();
                                c.setType(ContributionTypes.IDEA);

                                // Supported Format:
                                // source_code  title   text    working group   categories  author  age gender  creation_date

                                // SET source_code  title   text
                                c.setSourceCode(cell[0]);
                                c.setTitle(cell[1]);
                                c.setText(cell[2].replaceAll("\n", ""));
                                Logger.info("Importing => Contribution => " + cell[0]);
                                if (cell.length > 8) {
                                    String creationDate = cell[8];
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                    Logger.info("Importing => date => " + creationDate);

                                    LocalDate ldt = LocalDate.parse(creationDate, formatter);
                                    c.setCreation(Date.from(ldt.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                                    Logger.info("Importing => creating...");
                                }


                                // TODO: CHECK THAT HISTORY ADDS AN ITEM FOR THE AUTHORS
                                if (cell.length > 5) {
                                    // SET author   age gender  creation_date
                                    // get author name from cell 2
                                    Logger.info("Importing => author => " + cell.length);
                                    String author = cell[5];
                                    Logger.info("Importing => author => " + author);
                                    boolean createNonMember = false;
                                    if (author != null && !author.equals("")) {
                                        c.setFirstAuthorName(author);
                                        List<User> authors = User.findByName(c.getFirstAuthorName());
                                        // If more than one user matches the name
                                        // criteria, we'll skip the author set up
                                        if (authors != null && authors.size() == 1) {
                                            c.getAuthors().add(authors.get(0));
                                            Logger.info("The author was FOUND!");
                                        } else {
                                            createNonMember = true;
                                        }
                                    }

                                    if (createNonMember) {
                                        Logger.info("Importing => non member author => " + author);
                                        // Create a non member author
                                        NonMemberAuthor nma = new NonMemberAuthor();
                                        nma.setName(c.getFirstAuthorName());
                                        if (cell.length > 6) nma.setAge(new Integer(cell[6]));
                                        if (cell.length > 7) nma.setGender(cell[7]);
                                        nma.save();
                                        c.setNonMemberAuthor(nma);
                                    }
                                }

                                // SET categories
                                // TODO: use some kind of string buffer to make this more efficient as strings are immutable
                                if (cell.length > 4) {
                                    String categoriesLine = cell[4];
                                    String[] categories = categoriesLine.split(",");
                                    List<Theme> existing = new ArrayList<>();
                                    for (String category : categories) {
                                        Logger.info("Importing => Category => " + category);
                                        List<Theme> themes = campaign.filterThemesByTitle(category.trim());
                                        Logger.info(themes.size() + " themes found thath match category " + category);
                                        existing.addAll(themes);
                                    }
                                    c.setExistingThemes(existing);
                                }
                                // Create the contribution
                                Contribution.create(c);
                                if (cell.length > 3) {
                                    // Add IDEA to Working Group
                                    String wgName = cell[3];
                                    Logger.info("Importing => wg => " + wgName);
                                    WorkingGroup wg = WorkingGroup.readByName(wgName);
                                    if (wg != null) {
                                        Logger.info("Addng contribution to WG => " + wgName);
                                        wg.addContribution(c);
                                        wg.update();
                                    }
                                }
                                Logger.info("Adding contribution to campaign...");
                                campaign.getResources().addContribution(c);
                                campaign.update();
                                ContributionHistory.createHistoricFromContribution(c);

                            }
                        } catch (Exception e) {
                            Ebean.rollbackTransaction();
                            Logger.info(e.getLocalizedMessage());
                            e.printStackTrace();
                            return internalServerError(Json.toJson(new TransferResponseStatus(ResponseStatus.SERVERERROR, e.getLocalizedMessage())));
                        }
                        Ebean.commitTransaction();
                        break;
                    case "PROPOSAL":
                        Ebean.beginTransaction();
                        Logger.info("Beginning import transaction...");
                        try {
                            while ((line = br.readLine()) != null) {
                                String[] cell = line.split(cvsSplitBy);
                                Contribution c = new Contribution();
                                c.setType(ContributionTypes.PROPOSAL);
                                // get source code from cell 1
                                c.setSourceCode(cell[0]);
                                // get title from cell 2
                                c.setTitle(cell[1]);

                                Logger.info("Importing " + cell[1] + "...");
                                // get summary from cell 3 for ehterpad support we need aid & cid
                                String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
                                String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);

                                Resource res = new Resource();
                                if (cell[2] != null) {
                                    Logger.info("Creating etherpad...");
                                    // set text, only first paragraph
                                    String fullText = cell[2];
                                    String[] paragraphs = fullText.split("\\.");
                                    if (paragraphs != null) {
                                        Logger.info("Copying first paragraph..." + paragraphs.length);
                                        Logger.info("Text: " + paragraphs[0]);
                                        String text = paragraphs[0];
                                        c.setText(text);
                                        res = ResourcesDelegate.createResource(null, cell[2], ResourceTypes.PROPOSAL, true, false);
                                    }
                                } else {
                                    Logger.info("Creating etherpad from template...");
                                    // use generic template
                                    List<Resource> templates = ContributionsDelegate.getTemplates(null, null);

                                    if (templates != null) {
                                        // if there are more than one, then use the last
                                        String padId = templates.get(templates.size() - 1).getPadId();
                                        EtherpadWrapper wrapper = new EtherpadWrapper(etherpadServerUrl, etherpadApiKey);
                                        String templateHtml = wrapper.getHTML(padId);
                                        // save the etherpad
                                        res = ResourcesDelegate.createResource(null, templateHtml, ResourceTypes.PROPOSAL, true, false);
                                    }
                                }

                                if (res != null) {
                                    Logger.info("Adding etherpad to contribution...");
                                    c.setExtendedTextPad(res);
                                }

                                // get wgroup from cell 4 & get resource space from wgroup
                                WorkingGroup wg = WorkingGroup.readByName(cell[3]);
                                if (wg != null) {
                                    Logger.info("Adding contribution to working group...");
                                    wg.getResources().getContributions().add(c);
                                } else {
                                    Logger.info("working group with name '" + cell[3] + "' not found");
                                }

                                // email authors
                                if (cell.length > 4 && cell[4] != null) {
                                    Logger.info("Adding authors...");
                                    String[] emails = cell[4].split(",");
                                    for (String email : emails) {
                                        User u = User.findByEmail(email);
                                        if (u != null) {
                                            c.getAuthors().add(u);
                                        }
                                    }

                                }

                                Logger.info("Creating contribution...");
                                Contribution.create(c);
                                c.refresh();

                                // get & create atachments from cells 5,6
                                List<Resource> resources = new ArrayList<>();
                                if (cell.length > 5 && cell[5] != null) {
                                    Logger.info("Adding attachments to contribution...");
                                    String[] attachmentNames = cell[5].split(",");
                                    String[] attachmentUrls = cell[6].split(",");
                                    for (int i = 0; i < attachmentUrls.length; i++) {
                                        String name = attachmentNames[i];
                                        String url = attachmentUrls[i];

                                        if (name != null && !name.equals("")) {
                                            Logger.info("Adding attachment" + name + "...");
                                            Resource resource = new Resource();
                                            resource.setName(name);
                                            Logger.info(name + " => " + url);
                                            resource.setUrl(new URL(url));
                                            resource.setResourceType(ResourceTypes.FILE);
                                            resources.add(resource);
                                        }
                                    }
                                }

                                Logger.info("URL Resources size: " + resources.size());
                                c.getAttachments().addAll(resources);
                                c.update();

                                // get related contributions by source_code
                                List<Contribution> inspirations = new ArrayList<>();
                                if (cell.length > 7 && cell[7] != null) {
                                    Logger.info("Adding inspirations...");
                                    for (String sourceCode : cell[7].split(",")) {
                                        Logger.info("Adding inspiration " + sourceCode + "...");
                                        // get source code from cell i
                                        Contribution contrib = Contribution.readBySourceCode(sourceCode);
                                        if (contrib != null) {
                                            inspirations.add(contrib);
                                        }
                                    }
                                    c.getAssociatedContributions().addAll(inspirations);
                                }
                                c.update();

                                if (wg != null) {
                                    wg.update();
                                }

                                Logger.info("Adding contribution to campaign...");
                                campaign.getResources().addContribution(c);
                                campaign.update();

                                // TODO: is this correct?
                                Logger.info("Updating history of contribution...");
                                ContributionHistory.createHistoricFromContribution(c);
                                for (Contribution inspiration : inspirations) {
                                    ContributionHistory.createHistoricFromContribution(inspiration);
                                }
                            }
                        } catch (Exception e) {
                            Ebean.rollbackTransaction();
                            Logger.info(e.getLocalizedMessage());
                            e.printStackTrace();
                            return internalServerError(Json.toJson(new TransferResponseStatus(ResponseStatus.SERVERERROR, e.getLocalizedMessage())));
                        }
                        Ebean.commitTransaction();
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                Logger.info(e.getLocalizedMessage());
                return contributionFeedbackError(null, e.getLocalizedMessage());
            }
        }
        return ok();
    }


    /**
     * POST      /api/contribution/pad
     * Create a new Resource PROPOSAL from CONTRIBUTION_TEMPLATE
     *
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Campaign.class, produces = "application/json", value = "Create a new propolsal from contribution template")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result createContributionPad(
            @ApiParam(name = "aid", value = "Assembly ID") String aid,
            @ApiParam(name = "cid", value = "Contribution ID") String cid) {
        User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
        String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);
        // 1: find into campaign templates, 2: find into assembly templates, 3: find generic templates
        List<Resource> templates = ContributionsDelegate.getTemplates(aid, cid);

        if (templates != null) {
            // if there are more than one, then use the last
            String padId = templates.get(templates.size() - 1).getPadId();
            EtherpadWrapper wrapper = new EtherpadWrapper(etherpadServerUrl, etherpadApiKey);
            String templateHtml = wrapper.getHTML(padId);
            Resource res = ResourcesDelegate.createResource(campaignCreator, templateHtml, ResourceTypes.PROPOSAL, false, true);
            //Create this relationship when the contribution is saved
            //Assembly ass = Assembly.read(aid);
            //ass.getResources().addResource(res);
            //ass.update();
            return ok(Json.toJson(res));
        } else {
            return internalServerError("There are no templates available");
        }

    }

    /**
     * PUT       /api/contribution/pad
     * Confirm a Resource PROPOSAL
     *
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Resource.class, produces = "application/json", value = "Create a new Campaign")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result confirmContributionPad(
            @ApiParam(name = "rid", value = "Resource (that represents that PAD) ID") Long rid) {
        Resource res = ResourcesDelegate.confirmResource(rid);
        return ok(Json.toJson(res));
    }

    /**
     * PUT /api/assembly/:aid/contribution/:cid/:status
     * Confirm a Resource PROPOSAL
     *
     * @param aid
     * @param cid
     * @param status
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Update status of a Contribution")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result updateContributionStatus(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid,
            @ApiParam(name = "status", value = "New Status for the Contribution", allowableValues = "NEW,PUBLISHED,EXCLUDED,ARCHIVED") String status) {
        Contribution c = Contribution.read(cid);
        String upStatus = status.toUpperCase();
        if (ContributionStatus.valueOf(upStatus) != null) {
            c.setStatus(ContributionStatus.valueOf(upStatus));
            c.update();
            return ok(Json.toJson(c));
        } else {
            return internalServerError("The status is not valid");
        }
    }

    /**
     * POST       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create an anonymous contribution within another contribution")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Error creating contribution", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContribution(@ApiParam(name = "uuid", value = "Universal ID of the target contribution") String uuid) {
        //TODO uuid from who? the contribution must be associated with the resource space at least

        // 1. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {

            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            ContributionTemplate template = null;
            Contribution c;
            Contribution inContribution = Contribution.readByUUID(UUID.fromString(uuid));
            try {
                c = createContribution(newContribution, null, type, template, inContribution.getResourceSpace());
                if (type.equals(ContributionTypes.COMMENT) || type.equals(ContributionTypes.DISCUSSION)) {
                    if (inContribution.getForum().getContributions() == null) {
                        inContribution.getForum().setContributions(new ArrayList<Contribution>());
                    }
                    
                    // Integer forumCommentCount = inContribution.getForumCommentCount();
                    // inContribution.setForumCommentCount(forumCommentCount+1);

                    
                    inContribution.getForum().getContributions().add(c);
                    inContribution.getForum().update();
                    // NEW PROMISE
                    Promise.promise(() -> {
                        ContributionsDelegate.updateCommentCounters(c, "+");
                        return ok();
                    });
                    
                } else {
                    inContribution.getResourceSpace().getContributions().add(c);
                    inContribution.getResourceSpace().update();
                }
            } catch (Exception e) {
                Logger.error("Error creating ", e);
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            return ok(Json.toJson(c));
        }
    }


    /**
     * POST       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create an anonymous contribution in a campaign")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContributionOnCampaign(@ApiParam(name = "uuid", value = "Universal ID of the target campaign") String uuid) {
        // 1. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {

            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            Campaign campaign = Campaign.readByUUID(UUID.fromString(uuid));

            ContributionTemplate template = null;
            Contribution c;
            try {
                c = createContribution(newContribution, null, type, template, campaign.getResources());

                if (type.equals(ContributionTypes.COMMENT) || type.equals(ContributionTypes.DISCUSSION)) {
                    if (campaign.getForum().getContributions() == null) {
                        campaign.getForum().setContributions(new ArrayList<Contribution>());
                    }
                    campaign.getForum().getContributions().add(c);
                    campaign.getForum().update();
                } else {
                    campaign.getResources().getContributions().add(c);
                    campaign.getResources().update();
                }

                Promise.promise(() -> {
                    return NotificationsDelegate.newContributionInResourceSpace(campaign.getResources(),
                            c);
                });

            } catch (Exception e) {
                e.printStackTrace();
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }

            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInCampaign(campaign, c);
            });

            return ok(Json.toJson(c));
        }
    }

    /**
     * POST       /api/group/:uuid/contribution
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create an anonymous contribution in a working group")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContributionOnWorkingGroup(@ApiParam(name = "uuid", value = "Universal ID of the target working group") String uuid) {
        // 1. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {

            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            WorkingGroup wgroup = WorkingGroup.readByUUID(UUID.fromString(uuid));

            ContributionTemplate template = null;
            Contribution c;
            try {
                c = createContribution(newContribution, null, type, template, wgroup.getResources());

                if (type.equals(ContributionTypes.COMMENT) || type.equals(ContributionTypes.DISCUSSION)) {
                    if (wgroup.getForum().getContributions() == null) {
                        wgroup.getForum().setContributions(new ArrayList<Contribution>());
                    }
                    wgroup.getForum().getContributions().add(c);
                    wgroup.getForum().update();
                } else {
                    wgroup.getResources().getContributions().add(c);
                    wgroup.getResources().update();
                }

                Promise.promise(() -> {
                    return NotificationsDelegate.newContributionInResourceSpace(wgroup.getResources(),
                            c);
                });

            } catch (Exception e) {
                e.printStackTrace();
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }

            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInAssemblyGroup(wgroup, c);
            });

            return ok(Json.toJson(c));
        }
    }

    /**
     * POST       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create anonymous contribution in Assembly")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContributionOnAssembly(@ApiParam(name = "uuid", value = "Universal ID of the target contribution") String uuid) {
        // 1. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {

            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            ContributionTemplate template = null;

            Assembly assembly = Assembly.readByUUID(UUID.fromString(uuid));

            Contribution c;
            try {
                c = createContribution(newContribution, null, type, template, assembly.getResources());
                Promise.promise(() -> {
                    return NotificationsDelegate.newContributionInResourceSpace(assembly.getResources(), c);
                });

            } catch (Exception e) {
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            return ok(Json.toJson(c));
        }
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution/:coid/body
     *
     * @param aid
     * @param cid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = String.class, produces = "application/json", value = "Get the pad body url of a Contribution in a Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result findAssemblyContributionPadBody(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid,
            @ApiParam(name = "rev", value = "Revision", defaultValue = "0") Long rev,
            @ApiParam(name = "format", value = "String", allowableValues = "text, html", defaultValue = "html") String format) {
        Contribution c = Contribution.read(coid);
        String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
        String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);
        if (c != null) {
            Long revision = rev !=null && rev != 0 ? rev : c.getPublicRevision();
            Resource pad = c.getExtendedTextPad();
            String padId = pad.getPadId();
            String finalFormat = format != null && format == "text" ? "TEXT":"HTML";
            EtherpadWrapper wrapper = new EtherpadWrapper(etherpadServerUrl, etherpadApiKey);
            if (padId != null) {
                if(finalFormat.equals("TEXT")){
                    String body = wrapper.getTextRevision(padId,revision);
                    return ok(Json.toJson(body));
                }else if(finalFormat.equals("HTML")){
                    String body = wrapper.getHTMLRevision(padId,revision);
                    return ok(Json.toJson(body));
                }
            } else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No Pad for this Contribution")));
            }
        }
        return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Contribution with ID " + coid + " not found")));
    }

    /**
     * GET       /api/contribution/:couuid/body
     *
     * @param couuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = String.class, produces = "application/json", value = "Get the pad body url of a Contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    public static Result findContributionPadBody(
            @ApiParam(name = "couuid", value = "Contribution UUID") UUID couuid,
            @ApiParam(name = "rev", value = "Revision", defaultValue = "0") Long rev,
            @ApiParam(name = "format", value = "String", allowableValues = "text, html", defaultValue = "html") String format) {
        Contribution c = Contribution.readByUUID(couuid);        
        String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
        String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);
        if (c != null) {
            Long revision = rev !=null && rev != 0 ? rev : c.getPublicRevision();
            Resource pad = c.getExtendedTextPad();
            String padId = pad.getPadId();
            String finalFormat = format != null && format == "text" ? "TEXT":"HTML";
            EtherpadWrapper wrapper = new EtherpadWrapper(etherpadServerUrl, etherpadApiKey);
            if (padId != null) {
                if(finalFormat.equals("TEXT")){
                    String body = wrapper.getTextRevision(padId,revision);
                    return ok(Json.toJson(body));
                }else if(finalFormat.equals("HTML")){
                    String body = wrapper.getHTMLRevision(padId,revision);
                    return ok(Json.toJson(body));
                }
            } else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No Pad for this Contribution")));
            }
        }
        return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Contribution with UUID " + couuid.toString() + " not found")));
    }
  
    /** 
     * PUT       /api/space/:sid/contribution/comment/reset
     *
     * @param sid
     * @return
     */    
    @ApiOperation(httpMethod = "PUT", response = Contribution.class, produces = "application/json", value = "Update comment counts on contributions", notes="Only for ADMINS")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result updateContributionCounters (@ApiParam(name = "sid", value = "Resource Space ID") Long sid){
    	List<Contribution> contributions = Contribution.findAllByContainingSpace(sid);
        for (Contribution c: contributions){
	        Promise.promise( () -> { 
	        	return ContributionsDelegate.resetParentCommentCountersToZero(c); 
	        }).fallbackTo(
	        		Promise.promise( () ->{ 
	        			return ContributionsDelegate.resetChildrenCommentCountersToZero(c); 
	        		
	        }));
    	}
        
        return ok();
    }
}

class PaginatedContribution {

    @JsonView(Views.Public.class)
    private int pageSize;

    @JsonView(Views.Public.class)
    private int page;

    @JsonView(Views.Public.class)
    private int total;

    @JsonView(Views.Public.class)
    private List<Contribution> list;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Contribution> getList() {
        return list;
    }

    public void setList(List<Contribution> list) {
        this.list = list;
    }
}