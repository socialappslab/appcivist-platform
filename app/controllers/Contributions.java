package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.Ebean;
import com.feth.play.module.pa.PlayAuthenticate;
import delegates.ContributionsDelegate;
import delegates.ResourcesDelegate;
import enums.*;
import exceptions.MembershipCreationException;
import http.Headers;
import io.swagger.annotations.*;
import models.*;
import models.transfer.*;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.services.EtherpadWrapper;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static play.data.Form.form;

@Api(value = "05 contribution: Contribution Making", description = "Contribution Making Service: contributions by citizens to different spaces of civic engagement")
@With(Headers.class)
public class Contributions extends Controller {

    public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);
    public static final Form<ContributionFeedback> CONTRIBUTION_FEEDBACK_FORM = form(ContributionFeedback.class);
    public static final Form<Resource> ATTACHMENT_FORM = form(Resource.class);
    public static final Form<ThemeListTransfer> THEMES_FORM = form(ThemeListTransfer.class);

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
     * GET 	     /api/assembly/:aid/campaign/:cid/contribution
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
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contribution by ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
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
    		@ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = "") String type) {
        ResourceSpace rs = ResourceSpace.read(sid);
        List<Contribution> contributions = ContributionsDelegate
                .findContributionsInResourceSpace(rs, type, null);
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No contributions for {resource space}: " + sid + ", type=" + type)));
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution/:coid/stats
     *
     * @param aid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionStatistics.class, responseContainer = "List", produces = "application/json", 
    		value = "Get contributions statistics")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
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
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution stats: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/contribution/:cid/padid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = String.class, produces = "application/json", value = "Get the padId of a Contribution")
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
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No Pad id for this Contribution")));
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
    @ApiOperation(httpMethod = "GET", response = String.class, produces = "application/json", value = "Read comments on a Contribution")
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
     * GET       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, produces = "application/json", value = "Get contribution by its Universal ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    // TODO: add API token support, some API enpoints must be available only for registered clients
    public static Result findContributionByUUID(
    		@ApiParam(name="uuid", value="Contribution Universal ID") UUID uuid) {
        Contribution contribution;
        try {
            contribution = Contribution.readByUUID(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution withis uuid")));
        }
        return ok(Json.toJson(contribution));
    }
	/**
	 * GET       /api/assembly/:aid/contribution/:cid
	 * @param aid
	 * @param contributionId
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = ContributionHistory.class, responseContainer = "List", produces = "application/json", value = "Get contributions change history")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result getContributionsChangeHistory(Long aid, Long contributionId) throws Exception{
		List<ContributionHistory> contributionHistories = ContributionHistory.getContributionsHistory(contributionId);
		return ok(Json.toJson(contributionHistories));
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
    @ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Create a contribution in a specific Resource Space")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result createContributionInResourceSpaceWithId(@ApiParam(name="sid", value="Resource Space ID") Long sid) {
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
            return ok(Json.toJson(c));
        }
    }

    /**
     * POST      /api/assembly/:aid/contribution
     *
     * @param aid
     * @param space
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "space", value = "Resource space name within assembly", dataType = "String", paramType = "query", allowableValues = "resources,forum", defaultValue = ""),
            @ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createAssemblyContribution(Long aid, String space) {
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
            ResourceSpace rs = space != null && space.equals("forum") ? a
                    .getForum() : a.getResources();

            ContributionTemplate template = null;
            if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                List<ContributionTemplate> templates = rs.getTemplates();
                if (templates != null && !templates.isEmpty())
                    template = rs.getTemplates().get(0);
            }

            newContribution.setContextUserId(author.getUserId());
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
            return ok(Json.toJson(c));
        }
    }

    /**
     * POST      /api/assembly/:aid/campaign/:cid/component/:ciid/contribution
     *
     * @param aid
     * @param cid
     * @param ciid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "cid", value = "Campaign id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "ciid", value = "Component id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createCampaignComponentContribution(Long aid, Long cid, Long ciid) {
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

            Component ci = Component.read(cid, ciid);
            ResourceSpace rs = ci.getResourceSpace();
            ContributionTemplate template = null;
            if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                List<ContributionTemplate> templates = rs.getTemplates();
                if (templates != null && !templates.isEmpty())
                    template = rs.getTemplates().get(0);
            }
            newContribution.setContextUserId(author.getUserId());
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
    @ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "gid", value = "Working group id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "space", value = "Resource space name within assembly", dataType = "String", paramType = "query", allowableValues = "resources,forum", defaultValue = ""),
            @ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createAssemblyGroupContribution(Long aid, Long gid, String space) {
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
    @ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createContributionComment(Long aid, Long cid) {
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
            return ok(Json.toJson(cNew));
        }
    }

    /**
     * POST      /api/assembly/:aid/forumpost
     *
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createAssemblyForumPost(Long aid) {
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
    @ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "gid", value = "working group id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createWorkingGroupForumPost(Long aid, Long gid) {
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
    @ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "attachment_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result addAttachmentContribution(Long aid, Long contributionId) {
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
     * PUT       /api/assembly/:aid/contribution/:cid/feedback
     *
     * @param aid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result readContributionFeedback(Long aid, Long cid) {
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
     * PUT       /api/assembly/:aid/contribution/:cid/feedback
     *
     * @param aid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = ContributionStatistics.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "contribution_feedback_form", value = "Body of Contribution Statistics in JSON", required = true, dataType = "models.ContributionStatistics", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result updateContributionFeedback(Long aid, Long cid) {
        User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        final Form<ContributionFeedback> updatedFeedbackForm = CONTRIBUTION_FEEDBACK_FORM.bindFromRequest();

        if (updatedFeedbackForm.hasErrors()) {
            return contributionFeedbackError(updatedFeedbackForm);
        } else {
            ContributionFeedback existingFeedback = ContributionFeedback.findByContributionAndUserId(cid, author.getUserId());
            ContributionFeedback feedback = updatedFeedbackForm.get();

            try {
                Ebean.beginTransaction();
                feedback.setContributionId(cid);
                feedback.setUserId(author.getUserId());

                if (existingFeedback != null) {
                    feedback.setId(existingFeedback.getId());
                    feedback.update();
                } else {
                    ContributionFeedback.create(feedback);
                }
                Ebean.commitTransaction();
            } catch (Exception e) {
                Ebean.rollbackTransaction();
                return contributionFeedbackError(feedback, e.getLocalizedMessage());
            }

            ContributionStatistics updatedStats = new ContributionStatistics(cid);
            return ok(Json.toJson(updatedStats));
        }
    }

    /**
     * PUT       /api/assembly/:aid/contribution/:cid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result updateContribution(Long aid, Long contributionId) {
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
            List<User> authors = new ArrayList<User>();
            for (User a : newContribution.getAuthors()) {
                User refreshedAuthor = User.read(a.getUserId());
                authors.add(refreshedAuthor);
            }
            newContribution.setAuthors(authors);
            Contribution.update(newContribution);
            return ok(Json.toJson(newContribution));
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
    @ApiOperation(httpMethod = "PUT", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Logical removal of contribution in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    //@Dynamic(value = "ModeratorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result softDeleteContribution(Long aid, Long contributionId) {
        Contribution.softDelete(contributionId);
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
    @ApiOperation(httpMethod = "PUT", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Logical recovery of contribution Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "ModeratorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result recoverContribution(Long aid, Long contributionId) {
        Contribution.softRecovery(contributionId);
        return ok();
    }

    /**
     * DELETE    /api/assembly/:aid/contribution/:cid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result forceDeleteContribution(Long aid, Long contributionId) {
        Contribution.delete(contributionId);
        return ok();
    }


    /**
     * POST  /api/contribution/:uuid/themes
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Theme.class, produces = "application/json", value = "Add a theme to a contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "theme", value = "Themes to add to the contribution", dataType = "models.transfer.ThemeListTransfer", paramType = "body")})
    public static Result addThemeToContribution(@ApiParam(name = "uuid", value = "Contribution's Universal Id (UUID)") UUID uuid) {
        Contribution contribution;

        try {
            // We have to save themes without ID first
            List<Theme> themes = THEMES_FORM.bindFromRequest().get().getThemes();
            List<Theme> newThemes = themes.stream().filter(t -> t.getThemeId() == null).collect(Collectors.toList());
            newThemes.forEach(t -> {
                t.save();
            });
            contribution = Contribution.readByUUID(uuid);
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
     * GET  /api/contributions
     *
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Theme.class, produces = "application/json", value = "List contributions")
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
        if (type.equals(ContributionTypes.PROPOSAL)) {
            newContrib.setStatus(ContributionStatus.NEW);
        }
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

        if (type != null && (type.equals(ContributionTypes.PROPOSAL) || type.equals(ContributionTypes.NOTE))) {
            ContributionsDelegate.createAssociatedPad(etherpadServerUrl, etherpadApiKey, newContrib, t, containerResourceSpace.getResourceSpaceUuid());
        }

        Logger.info("Creating new contribution");
        Logger.debug("=> " + newContrib.toString());

        // Get list of BRAINSTORMING contributions that inspire the new one
        List<Contribution> inspirations = newContrib.getTransientInspirations();

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

        Contribution.create(newContrib);
        newContrib.refresh();

        if (newContrib.getType().equals(ContributionTypes.PROPOSAL) && inspirations != null) {
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
        if (containerResourceSpace !=null && containerResourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
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
            BallotCandidate contributionAssociatedCandidate = new BallotCandidate();
            contributionAssociatedCandidate.setBallotId(b.getId());
            contributionAssociatedCandidate.setCandidateType(new Integer(1));
            contributionAssociatedCandidate.setContributionUuid(newContrib.getUuid());
            contributionAssociatedCandidate.save();
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
	// TODO authorization
	/**
	 * POST /api/assembly/:aid/contribution/ideas/import
	 * Import ideas file
	 * @param aid Assembly Id
	 * @param cid Campaing Id
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", consumes = "application/csv", value = "Import CSV file with campaign ideas",
			notes = "CSV format: idea title, idea summary, idea author, idea theme <br/>" +
					"The values must be separated by coma (,). If the theme column has more than one theme, then it must be separated by dash (-).")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			//@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			//@ApiImplicitParam(name = "cid", value = "Campaign id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "file", value = "CSV file", dataType = "file", paramType = "form"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	public static Result importContributions(@ApiParam(value = "Assembly id") @PathParam("nro_nombre_llamado") Long aid,
											 @ApiParam(value = "Campaign id") @PathParam("nro_nombre_llamado") Long cid,
											 @ApiParam(value = "Type of contribution", required = true, defaultValue = "IDEA", example = "IDEA") @QueryParam("nro_nombre_llamado") String type) {
		Http.MultipartFormData body = request().body().asMultipartFormData();
		Http.MultipartFormData.FilePart uploadFilePart = body.getFile("file");
		Campaign campaign = Campaign.read(cid);
		ResourceSpace rs = null;
		if (campaign != null) {
			rs = campaign.getResources();
		}
		if (uploadFilePart != null) {
			try {
				//Ebean.beginTransaction();
				// read csv file
				BufferedReader br = null;
				br = new BufferedReader(new FileReader(uploadFilePart.getFile()));
				String cvsSplitBy = ",";
				String line = br.readLine();
				while ((line = br.readLine()) != null) {
					String[] cell = line.split(cvsSplitBy);
					switch (type) {
						case "IDEA":
							Contribution c = new Contribution();
							c.setType(ContributionTypes.IDEA);
							c.setTitle(cell[0]);
							c.setText(cell[1]);
							// TODO existing author
							c.setFirstAuthorName(cell[2]);
							// TODO existing theme
							List<Theme> themesList = new ArrayList<Theme>();
							String themeSplitBy = "-";
							String[] themes = cell[3].split(themeSplitBy);
							for(String theme: themes) {
								Theme t = new Theme();
								t.setTitle(theme);
								themesList.add(t);
							}
							c.setThemes(themesList);
							Contribution.create(c);
							rs.addContribution(c);
							ResourceSpace.update(rs);
							break;
						default:
							break;
					}

				}
				//Ebean.commitTransaction();
			} catch (Exception e) {
				//Ebean.rollbackTransaction();
				return contributionFeedbackError(null, e.getLocalizedMessage());
			}
		}
		return ok();
	}

	/**
	 * GET /api/assembly/:aid/contribution/ideas/export
	 * Export ideas file
	 * @param aid Assembly Id
	 * @param cid Campaing Id
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", produces = "application/csv", value = "Export campaign ideas to a CSV file")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
	@ApiImplicitParams({
			//@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			//@ApiImplicitParam(name = "cid", value = "Campaign id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	public static Result exportContributions(@ApiParam(value = "Assembly id") @PathParam("nro_nombre_llamado") Long aid,
											 @ApiParam(value = "Campaign id") @PathParam("nro_nombre_llamado") Long cid,
											 @ApiParam(value = "Type of contribution", required = true, example = "IDEA") @QueryParam("nro_nombre_llamado") String type) {
		String csv = "idea title,idea summary,idea author,idea theme\n";
		Campaign campaign = Campaign.read(cid);
		ResourceSpace rs = null;
		if (campaign != null) {
			rs = campaign.getResources();
		}
		Integer t = null;
		switch (type) {
			case "IDEA":
				t = ContributionTypes.IDEA.ordinal();
				break;
			default:
				break;
		}
		if (t != null && rs != null) {
			List<Contribution> contributions = ContributionsDelegate
					.findContributionsInResourceSpace(rs, t);
			for (Contribution c: contributions) {
				csv = csv + c.getTitle()  + ",";
				csv = csv + c.getAssessmentSummary() + ",";
				// TODO existing author
				csv = csv + c.getFirstAuthorName();
				csv = csv + ",";
				int themeSize = c.getThemes().size();
				for(int i=0; i < themeSize; i++) {
					if (i > 0 && i < themeSize + 1) {
						csv = csv + "-";
					}
					csv = csv + c.getThemes().get(i).getTitle();
				}
				csv = csv + "\n";
			}
		}
		response().setContentType("application/csv");
		response().setHeader("Content-disposition","attachment; filename=contributions.csv");
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
	 * POST /api/assembly/:aid/contribution/pad
	 * Create a new Resource PROPOSAL from CONTRIBUTION_TEMPLATE
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Campaign.class, produces = "application/json", value = "Create a new Campaign")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	public static Result createContributionPad(String aid, String cid) {
		User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));
		String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
		String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);
		// 1: find into campaign templates, 2: find into assembly templates, 3: find generic templates
		List<Resource> templates = new ArrayList<Resource>();
		if (aid != null && aid.compareTo("") != 0) {
			Assembly a = Assembly.read(Long.parseLong(aid));
			List<Resource> resources = a.getResources().getResources();
			for (Resource r: resources) {
				if (r.getResourceType().equals(ResourceTypes.CONTRIBUTION_TEMPLATE)) {
					templates.add(r);
				}
			}
		} else if (cid != null && cid.compareTo("") != 0) {
			Campaign c = Campaign.read(Long.parseLong(cid));
			List<Resource> resources = c.getResources().getResources();
			for (Resource r: resources) {
				if (r.getResourceType().equals(ResourceTypes.CONTRIBUTION_TEMPLATE)) {
					templates.add(r);
				}
			}
		} else {
			templates = Resource.findByResourceType(ResourceTypes.CONTRIBUTION_TEMPLATE);
		}
		if (templates != null) {
			// if there are more than one, then use the last
			String padId = templates.get(templates.size() - 1).getPadId();
			EtherpadWrapper wrapper = new EtherpadWrapper(etherpadServerUrl, etherpadApiKey);
			String templateHtml = wrapper.getHTML(padId);
			Resource res = ResourcesDelegate.createResource(campaignCreator, templateHtml, ResourceTypes.PROPOSAL);
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
	 * PUT /api/assembly/:aid/contribution/pad
	 * Confirm a Resource PROPOSAL
	 * @param rid
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Create a new Campaign")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No resource found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	public static Result confirmContributionPad(Long rid) {
		Resource res = ResourcesDelegate.confirmResource(rid);
		return ok(Json.toJson(res));
	}

    /**
     * PUT /api/assembly/:aid/contribution/:cid/:status
     * Confirm a Resource PROPOSAL
     * @param aid
     * @param cid
     * @param status
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Update status of a Contribution")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result updateContributionStatus(Long aid, Long cid, String status) {
        Contribution c = Contribution.read(cid);
        String upStatus = status.toUpperCase();
        if(ContributionStatus.valueOf(upStatus)!= null) {
            c.setStatus(ContributionStatus.valueOf(upStatus));
            c.update();
            return ok(Json.toJson(c));
        } else{
            return internalServerError("The status is not valid");
        }
    }

    /**
     * POST       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContribution(String uuid) {
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
            Contribution c = new Contribution();
            c.setUuidAsString(uuid);
            c.setUuid(UUID.fromString(uuid));
            try {
                c = createContribution(newContribution, null, type, template, null);
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
     * POST       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContributionOnCampaign(String uuid) {
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
     * POST       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContributionOnAssembly(String uuid) {
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
            } catch (Exception e) {
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            return ok(Json.toJson(c));
        }
    }
}
