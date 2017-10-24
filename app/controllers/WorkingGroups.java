package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.PlayAuthenticate;
import delegates.ContributionsDelegate;
import delegates.NotificationsDelegate;
import delegates.WorkingGroupsDelegate;
import enums.*;
import exceptions.ConfigurationException;
import http.Headers;
import io.swagger.annotations.*;
import models.*;
import models.misc.Views;
import models.transfer.InvitationTransfer;
import models.transfer.MembershipTransfer;
import models.transfer.TransferResponseStatus;
import models.transfer.WorkingGroupSummaryTransfer;
import org.json.simple.JSONArray;
import play.Logger;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.twirl.api.Content;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.services.EtherpadWrapper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static play.data.Form.form;

@Api(value = "02 group: Working Group Management", description = "Group Management endpoints in the Assembly Making service")
@With(Headers.class)
public class WorkingGroups extends Controller {

    public static final Form<WorkingGroup> WORKING_GROUP_FORM = form(WorkingGroup.class);
    public static final Form<MembershipTransfer> MEMBERSHIP_FORM = form(MembershipTransfer.class);

    /**
     * Return the full list of working groups in an assembly
     * GET       /api/assembly/:aid/group
     *
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = WorkingGroup.class, responseContainer = "List", produces = "application/json", value = "List groups of an assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
    public static Result findWorkingGroups(@ApiParam(name = "aid", value = "Assembly ID") Long aid) {
        List<WorkingGroup> workingGroups = WorkingGroup.findByAssembly(aid);
        return ok(Json.toJson(workingGroups));
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/group
     *
     * @param aid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = WorkingGroup.class, responseContainer = "List", produces = "application/json", value = "List of groups created in a campaign")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findWorkingGroupsInCampaign(@ApiParam(name = "aid", value = "Assembly ID") Long aid, @ApiParam(name = "cid", value = "Campaign ID") Long cid) {
        Campaign c = Campaign.read(cid);
        if (c != null) {
            List<WorkingGroup> workingGroups = c.getWorkingGroups();
            return ok(Json.toJson(workingGroups));
        } else {
            TransferResponseStatus response = new TransferResponseStatus();
            response.setResponseStatus(ResponseStatus.NODATA);
            response.setStatusMessage("Campaign " + cid + " does not exist");
            return notFound(Json.toJson(response));
        }
    }

    /**
     * GET       /api/assembly/:aid/group/:gid
     *
     * @param aid
     * @param wGroupId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = WorkingGroup.class, produces = "application/json", value = "Get working group by ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
    public static Result findWorkingGroup(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                          @ApiParam(name = "gid", value = "Working Group ID") Long wGroupId) {
        WorkingGroup workingGroup = WorkingGroup.read(wGroupId);
        return workingGroup != null ? ok(Json.toJson(workingGroup))
                : notFound(Json
                .toJson(new TransferResponseStatus(
                        ResponseStatus.NODATA, "No group with ID = "
                        + wGroupId)));
    }

    /**
     * DELETE       /api/assembly/:aid/group/:gid
     *
     * @param aid
     * @param wGroupId
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = String.class, produces = "application/json", value = "Delete group by ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
    public static Result deleteWorkingGroup(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                            @ApiParam(name = "gid", value = "Working Group ID") Long wGroupId) {
        WorkingGroup.delete(wGroupId);
        return ok();
    }

    /**
     * POST      /api/assembly/:aid/group
     *
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = WorkingGroup.class, produces = "application/json",
            value = "Create a new working group",
            notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "group create error", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Working Group Object", value = "New Working Group in json", dataType = "models.WorkingGroup", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    @SubjectPresent
    public static Result createWorkingGroup(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                            @ApiParam(name = "invitations", value = "Send invitations if true") String invitations) {
        // 1. obtaining the user of the requestor
        User groupCreator = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new group data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<WorkingGroup> newWorkingGroupForm = WORKING_GROUP_FORM
                .bindFromRequest();

        if (newWorkingGroupForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.GROUP_CREATE_MSG_ERROR,
                    newWorkingGroupForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
            WorkingGroup newWorkingGroup = newWorkingGroupForm.get();

            if (newWorkingGroup.getLang() == null)
                newWorkingGroup.setLang(groupCreator.getLanguage());

            TransferResponseStatus responseBody = new TransferResponseStatus();

            if (WorkingGroup.numberByNameInAssembly(newWorkingGroup.getName(), aid) > 0) {
                Logger.info("Working Group already exists");
                responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
                responseBody.setStatusMessage("Working Group already exists");
                return internalServerError(Json.toJson(responseBody));
            } else {
                Ebean.beginTransaction();
                try {
                    if (newWorkingGroup.getCreator() == null) {
                        newWorkingGroup.setCreator(groupCreator);
                    }
                    List<InvitationTransfer> invitationsList = newWorkingGroup.getInvitations();
                    newWorkingGroup = WorkingGroup.create(newWorkingGroup);

                    // Add the working group to the assembly
                    ResourceSpace rs = Assembly.read(aid).getResources();
                    rs.addWorkingGroup(newWorkingGroup);
                    rs.update();

                    // Create and send invitations
                    if (invitations != null && invitations.equals("true")) {
                        for (InvitationTransfer invitation : invitationsList) {
                            MembershipInvitation.create(invitation, groupCreator, newWorkingGroup);
                        }
                    }
                    try {
                        NotificationsDelegate.createNotificationEventsByType(
                                ResourceSpaceTypes.WORKING_GROUP.toString(), newWorkingGroup.getUuid());
                    } catch (ConfigurationException e) {
                        Logger.error("Configuration error when creating events for contribution: " + e.getMessage());
                    }

                    Assembly assembly = Assembly.read(aid);
                    WorkingGroup finalNewWorkingGroup = newWorkingGroup;
                    Promise.promise(() -> {
                        return NotificationsDelegate.signalNotification(ResourceSpaceTypes.ASSEMBLY, NotificationEventName.NEW_WORKING_GROUP, assembly, finalNewWorkingGroup);
                    });

                } catch (Exception e) {
                    Ebean.rollbackTransaction();
                    e.printStackTrace();
                    Logger.info("Error creating Working Group: " + e.getMessage());
                    responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
                    responseBody.setStatusMessage("Error creating Working Group: " + e.getMessage());
                    return internalServerError(Json.toJson(responseBody));
                }
                Ebean.commitTransaction();
            }
            return ok(Json.toJson(newWorkingGroup));
        }
    }

    /**
     * POST      /api/assembly/:aid/campaign/:cid/group
     *
     * @param aid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = WorkingGroup.class, produces = "application/json",
            value = "Create a Working Group in the Campaign identified by ID",
            notes = "This will also add the Working Gorup to the Assembly organizing this campaign. Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Group create error", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Working Group Object", value = "New Working Group in json", dataType = "models.WorkingGroup", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createWorkingGroupInCampaign(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                                      @ApiParam(name = "cid", value = "Campaign ID") Long cid,
                                                      @ApiParam(name = "invitations", value = "Send invitations if true") String invitations) {
        // 1. obtaining the user of the requestor
        User groupCreator = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new group data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<WorkingGroup> newWorkingGroupForm = WORKING_GROUP_FORM
                .bindFromRequest();

        if (newWorkingGroupForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.GROUP_CREATE_MSG_ERROR,
                    newWorkingGroupForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
            WorkingGroup newWorkingGroup = newWorkingGroupForm.get();

            if (newWorkingGroup.getLang() == null)
                newWorkingGroup.setLang(groupCreator.getLanguage());

            TransferResponseStatus responseBody = new TransferResponseStatus();
            Assembly a = Assembly.read(aid);
            Long containingSpace = a != null ? a.getResourcesResourceSpaceId() : null;

            if (WorkingGroup.numberByNameInAssembly(newWorkingGroup.getName(), containingSpace) > 0) {
                Logger.info("Working Group already exists");
                responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
                responseBody.setStatusMessage(Messages.get("appcivist.api.error.groups.group_with_same_name_already_exists"));
                return internalServerError(Json.toJson(responseBody));
            } else {
                Ebean.beginTransaction();
                try {
                    if (newWorkingGroup.getCreator() == null) {
                        newWorkingGroup.setCreator(groupCreator);
                    }
                    List<InvitationTransfer> invitationsList = newWorkingGroup.getInvitations();
                    newWorkingGroup = WorkingGroup.create(newWorkingGroup);
                    newWorkingGroup.refresh();
                    // Add the working group to the assembly
                    ResourceSpace rs = ResourceSpace.read(a.getResourcesResourceSpaceId());
                    rs.addWorkingGroup(newWorkingGroup);
                    newWorkingGroup.getContainingSpaces().add(rs);
                    rs.update();

                    // Add the working group to the campaign
                    Campaign c = Campaign.read(cid);
                    ResourceSpace rsC = ResourceSpace.read(c.getResourceSpaceId());
                    rsC.addWorkingGroup(newWorkingGroup);
                    newWorkingGroup.getContainingSpaces().add(rsC);
                    rsC.update();

                    newWorkingGroup.refresh();

                    // Create and send invitations
                    if (invitations != null && invitations.equals("true")) {
                        for (InvitationTransfer invitation : invitationsList) {
                            MembershipInvitation.create(invitation, groupCreator, newWorkingGroup);
                        }
                    }


                    WorkingGroup finalNewWorkingGroup = newWorkingGroup;
                    Promise.promise(() -> {
                        return NotificationsDelegate.signalNotification(ResourceSpaceTypes.CAMPAIGN,
                                NotificationEventName.NEW_WORKING_GROUP,
                                c,
                                finalNewWorkingGroup);
                    });

                } catch (Exception e) {
                    Ebean.rollbackTransaction();
                    e.printStackTrace();
                    Logger.info("Error creating Working Group: " + e.getMessage());
                    responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
                    responseBody.setStatusMessage("Error creating Working Group: " + e.getMessage());
                    return internalServerError(Json.toJson(responseBody));
                }
                Ebean.commitTransaction();
            }
            return ok(Json.toJson(newWorkingGroup));
        }
    }

    /**
     * PUT       /api/assembly/:aid/group/:gid
     *
     * @param aid
     * @param groupId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = TransferResponseStatus.class, produces = "application/json", value = "Update Working Group by ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Group create error", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Working Group Object", value = "New Working Group in json", dataType = "models.WorkingGroup", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result updateWorkingGroup(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                            @ApiParam(name = "aid", value = "Working Group ID") Long groupId) {
        // 1. read the new group data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<WorkingGroup> newWorkingGroupForm = WORKING_GROUP_FORM
                .bindFromRequest();

        if (newWorkingGroupForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.GROUP_CREATE_MSG_ERROR,
                    newWorkingGroupForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
            WorkingGroup newWorkingGroup = newWorkingGroupForm.get();

            TransferResponseStatus responseBody = new TransferResponseStatus();

            if (WorkingGroup.numberByNameInAssembly(newWorkingGroup.getName(), aid) > 0) {
                String status_message = "Working group already exists with the same name already exists";
                Logger.info(status_message);
                responseBody.setResponseStatus(ResponseStatus.UNAUTHORIZED);
                responseBody.setStatusMessage(status_message);
            } else {
                newWorkingGroup.setGroupId(groupId);
                List<Theme> themes = newWorkingGroup.getThemes();
                List<Theme> themesLoaded = new ArrayList<Theme>();
                for (Theme theme : themes) {
                    Theme themeRead = Theme.read(theme.getThemeId());
                    themesLoaded.add(themeRead);
                }
                newWorkingGroup.setThemes(themesLoaded);
                newWorkingGroup.update();

                // TODO: return URL of the new group
                Logger.info("Creating working group");
                Logger.debug("=> " + newWorkingGroupForm.toString());

                responseBody.setNewResourceId(newWorkingGroup.getGroupId());
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.GROUP_CREATE_MSG_SUCCESS,
                        newWorkingGroup.getName()/*
                                                 * ,
												 * groupCreator.getIdentifier()
												 */));
                responseBody.setNewResourceURL(GlobalData.GROUP_BASE_PATH + "/"
                        + newWorkingGroup.getGroupId());
            }
            Promise.promise(() -> {
                return NotificationsDelegate.signalNotification(
                        ResourceSpaceTypes.WORKING_GROUP,
                        NotificationEventName.UPDATED_WORKING_GROUP,
                        Assembly.read(aid).getResources(), newWorkingGroup);
            });

            return ok(Json.toJson(responseBody));
        }
    }

    /**
     * POST      /api/assembly/:aid/group/:id/membership/:type
     *
     * @param aid
     * @param id
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Membership.class, produces = "application/json", value = "Add membership to the working group",
            notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "mebership create error", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Membership simplified object", value = "membership's form in body", dataType = "models.transfer.MembershipTransfer", paramType = "body", required = true),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
    public static Result createGroupMembership(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Working Group ID") Long id,
            @ApiParam(name = "Type", value = "Type of Membership") String type) {
        // 1. obtaining the user of the requestor
        User requestor = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new group data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<MembershipTransfer> newMembershipForm = MEMBERSHIP_FORM
                .bindFromRequest();

        if (newMembershipForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
                    newMembershipForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
            MembershipTransfer newMembership = newMembershipForm.get();
            return Memberships.createMemberShip(requestor, "group", newMembership, id);
        }
    }

    /**
     * GET       /api/assembly/:aid/group/:id/membership/:status
     *
     * @param aid
     * @param gid
     * @param status
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Membership.class, responseContainer = "List", produces = "application/json", value = "Get Working Group Memberships by ID and status")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No membership in this group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result listMembershipsWithStatus(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Working Group ID") Long gid,
            @ApiParam(name = "status", value = "Status of membership invitation or request", allowableValues = "REQUESTED, INVITED, FOLLOWING, ALL", required = true) String status) {
        List<Membership> m = MembershipGroup.findByAssemblyIdGroupIdAndStatus(aid, gid, status);
        if (m != null && !m.isEmpty())
            return ok(Json.toJson(m));
        return notFound(Json.toJson(new TransferResponseStatus(
                "No memberships with status '" + status + "' in Working Group '"
                        + gid + "'")));
    }

    /**
     * GET       /api/assembly/:aid/group/:gid/user/:uid
     *
     * @param aid
     * @param gid
     * @param userId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = TransferResponseStatus.class, produces = "application/json", value = "Get Working Group Memberships by user ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "User is not Member of Group", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    //@SubjectPresent
    public static Result isUserMemberOfGroup(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Working Group ID") Long gid,
            @ApiParam(name = "uid", value = "User ID") Long userId) {
        Boolean result = MembershipGroup.isUserMemberOfGroup(userId, gid);
        if (result) return ok(Json.toJson(new TransferResponseStatus(ResponseStatus.OK,
                "User '" + userId + "' is a member of Working Group '" + gid + "'")));
        else return ok(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA,
                "User '" + userId + "' is not a member of Working Group '" + gid + "'")));
    }

    /**
     * GET       /api/assembly/:aid/group/:gid/public
     *
     * @param aid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = WorkingGroupSummaryTransfer.class, produces = "application/json", value = "Get working group profile if it is listed")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
    })
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result getListedWorkingGroupProfile(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                                      @ApiParam(name = "gid", value = "Working Group ID") Long gid) {
        User requestor = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        WorkingGroupSummaryTransfer group = WorkingGroupsDelegate.readListedWorkingGroup(gid, requestor);
        if (group != null) {
            return ok(Json.toJson(group));
        } else {
            return notFound(Json.toJson(TransferResponseStatus.noDataMessage("Group with id = '" + gid + "' is not available for this user", "")));
        }
    }

    /**
     * GET       /api/assembly/:aid/group/:gid/proposals
     *
     * @param aid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get working group PROPOSALS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
    })
    @Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
    public static Result listWorkingGroupProposals(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                                   @ApiParam(name = "gid", value = "Working Group ID") Long gid) {
        List<Contribution> proposals = WorkingGroup.listWorkingGroupProposals(gid);
        return ok(Json.toJson(proposals));
    }

    /**
     * GET       /api/assembly/:aid/group/:gid/contributions
     *
     * @param aid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get working group CONTRIBUTIONS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
    })
    @Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
    public static Result listWorkingGroupContributions(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                                       @ApiParam(name = "gid", value = "Working Group ID") Long gid) {
        List<Contribution> contributions = WorkingGroup.listWorkingGroupContributions(gid);
        return ok(Json.toJson(contributions));
    }

    /**
     * GET       /api/assembly/:aid/group/:id/membership
     *
     * @param aid
     * @param id
     * @return
     */
    //TODO
    @ApiOperation(httpMethod = "GET", value = "List memberships in group", hidden = true)
    @SubjectPresent
    public static Result listMemberships(Long aid, Long id) {
        // check the user who is accepting the invitation is
        // TODO
        TransferResponseStatus responseBody = new TransferResponseStatus();
        responseBody.setStatusMessage("Not implemented yet");
        return notFound(Json.toJson(responseBody));
    }

    /**
     * POST      /api/assembly/:aid/group/:gid/ballot
     *
     * @param aid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Ballot.class, produces = "application/json", value = "Creates new Consensus Ballot from selected/remaining proposals in working group")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result nextBallotForWorkingGroup(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Working Group ID") Long gid) {

        List<String> selectedProposalsPayload = null;
        List<UUID> selectedProposals = null;
        if (request().body().asJson().isArray()) {
            selectedProposalsPayload = new ObjectMapper().convertValue(request().body().asJson(), ArrayList.class);
            selectedProposals = selectedProposalsPayload.stream().map(p -> UUID.fromString(p)).collect(Collectors.toList());
        }
        //First, we have to find the working group and the current Ballot for the working group
        WorkingGroup workingGroup = WorkingGroup.read(gid);
        UUID consensus = WorkingGroup.queryConsensusBallotByGroupResourceSpaceId(workingGroup.getResourcesResourceSpaceId());
        Ballot currentBallot = Ballot.findByUUID(consensus);
        List<BallotCandidate> newCandidates = new ArrayList<>();

        //Creates new candidates based on selected proposals (if any), or based on current non excluded proposals
        List<BallotCandidate> currentCandidates = currentBallot.getBallotCandidates();
        if (selectedProposals == null || selectedProposals.isEmpty()) {
            //Select only candidates with non-excluded proposals
            if (currentCandidates != null) {
                newCandidates = currentCandidates.stream().
                        filter(candidate -> !ContributionStatus.EXCLUDED.equals(candidate.getContribution().getStatus())).collect(Collectors.toList());
            }
        } else {
            newCandidates = selectedProposals.stream().map(c -> {
                BallotCandidate filtered = null;
                for (BallotCandidate bc : currentCandidates) {
                    if (bc.getCandidateUuid().equals(c)) {
                        filtered = bc;
                        break;
                    }
                }
                return filtered;
            }).collect(Collectors.toList());
        }
        Ballot newBallot = Ballot.createConsensusBallotForWorkingGroup(workingGroup);

        for (BallotCandidate candidate : newCandidates) {
            BallotCandidate contributionAssociatedCandidate = new BallotCandidate();
            contributionAssociatedCandidate.setBallotId(newBallot.getId());
            contributionAssociatedCandidate.setCandidateType(BallotCandidateTypes.ASSEMBLY);
            contributionAssociatedCandidate.setCandidateUuid(candidate.getContribution().getUuid());
            contributionAssociatedCandidate.save();
        }
        //Send the current ballot to a historic
        workingGroup.getBallotHistories().add(currentBallot);
        workingGroup.save();

        //Archive previous ballot
        currentBallot.setStatus(BallotStatus.ARCHIVED);

        Promise.promise(() -> {
            return NotificationsDelegate.signalNotification(ResourceSpaceTypes.WORKING_GROUP, NotificationEventName.NEW_VOTING_BALLOT, workingGroup, workingGroup);
        });

        return ok(Json.toJson(newBallot));
    }

    /**
     * PUT       /api/assembly/:aid/group/:gid/ballot
     *
     * @param aid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Ballot.class, produces = "application/json", value = "Creates new Consensus Ballot from selected/remaining proposals in working group")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result archiveWorkingGroupsBallot(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Working Group ID") Long gid) {

        WorkingGroup workingGroup = WorkingGroup.read(gid);
        UUID consensus = WorkingGroup.queryConsensusBallotByGroupResourceSpaceId(workingGroup.getResourcesResourceSpaceId());
        Ballot ballot = Ballot.findByUUID(consensus);
        ballot.setStatus(BallotStatus.ARCHIVED);
        ballot.update();
        Promise.promise(() -> {
            return NotificationsDelegate.signalNotification(
                    ResourceSpaceTypes.WORKING_GROUP,
                    NotificationEventName.UPDATED_VOTING_BALLOT,
                    workingGroup,
                    workingGroup);
        });
        Promise.promise(() -> {
            return NotificationsDelegate.signalNotification(
                    ResourceSpaceTypes.ASSEMBLY,
                    NotificationEventName.UPDATED_VOTING_BALLOT,
                    Assembly.read(aid).getResources(),
                    workingGroup);
        });
        return ok(Json.toJson(ballot));
    }

    /**
     * GET       /api/group/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = WorkingGroup.class, produces = "application/json", value = "Read working group by Universal ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No working group found", response = TransferResponseStatus.class)})
    public static Result findWorkingGroupByUUID(@ApiParam(name = "uuid", value = "Working Group Universal ID (UUID)") UUID uuid) {
        try {

            WorkingGroup wgroup = WorkingGroup.readByUUID(uuid);
            if (wgroup == null) {
                return ok(Json
                        .toJson(new TransferResponseStatus("No working group found")));
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            mapper.addMixIn(WorkingGroup.class, WorkingGroup.MembeshipsVisibleMixin.class);
            mapper.addMixIn(Membership.class, Membership.AuthorsVisibleMixin.class);
            String result = mapper.writerWithView(Views.Public.class)
                    .writeValueAsString(wgroup);

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
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }

    }

    /**
     * POST      /api/assembly/:aid/campaign/:cid/group/:gid/assignments
     *
     * @param aid
     * @param cid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = WorkingGroup.class, produces = "application/json", value = "Assigns contributions to the working group")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result assignContributionsToGroup(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "gid", value = "Working Group ID") Long gid) {

        try {
            List<BigInteger> selectedContributionsPayload = null;
            List<Long> selectedContributions = null;
            if (request().body().asJson().isArray()) {
                selectedContributionsPayload = new ObjectMapper().convertValue(request().body().asJson(), ArrayList.class);
                selectedContributions = selectedContributionsPayload.stream().map(p -> p.longValue()).collect(Collectors.toList());
            }

            List<Contribution> selectedContributionsList = selectedContributions.stream().map(p -> Contribution.read(p)).collect(Collectors.toList());
            WorkingGroup workingGroup = WorkingGroup.read(gid);
            workingGroup.getAssignedContributions().addAll(selectedContributionsList);
            workingGroup.update();
            for (Contribution contribution : selectedContributionsList) {
                ContributionHistory.createHistoricFromContribution(contribution);
                Promise.promise(() -> {
                    return NotificationsDelegate.signalNotification(
                            ResourceSpaceTypes.WORKING_GROUP,
                            NotificationEventName.UPDATED_CONTRIBUTION_HISTORY,
                            workingGroup.getResources(),
                            contribution);
                });
            }


            return ok(Json.toJson(workingGroup));
        } catch (Exception e) {
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }


    }

    /**
     * PUT       /api/assembly/:aid/group/:gid/proposals/:pid/publish
     *
     * @param aid
     * @param gid
     * @param pid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Integer.class, produces = "application/json", value = "Publishes a Contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result publishProposal(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Working Group ID") Long gid,
            @ApiParam(name = "pid", value = "Contribution ID") Long pid) {

        try {
            Contribution proposal = Contribution.read(pid);

            String etherpadServerUrl = Play
                    .application()
                    .configuration()
                    .getString(
                            "appcivist.services.etherpad.default.serverBaseUrl");
            String etherpadApiKey = Play.application().configuration()
                    .getString("appcivist.services.etherpad.default.apiKey");
            EtherpadWrapper wrapper = new EtherpadWrapper(etherpadServerUrl, etherpadApiKey);

            Integer newRevision = null;
            try {
                //Let's save the revision with no number, so etherpad can generate one by itself
                wrapper.getEtherpadClient().saveRevision(proposal.getExtendedTextPad().getPadId());
                Map revisions = wrapper.getEtherpadClient().listSavedRevisions(proposal.getExtendedTextPad().getPadId());
                JSONArray savedRevisions = (JSONArray) revisions.get("savedRevisions");
                //Integer newRevision = null;
                if (savedRevisions != null && !savedRevisions.isEmpty()) {
                    newRevision = ((Long) savedRevisions.get(savedRevisions.size() - 1)).intValue();
                    proposal.addRevisionToContributionPublishHistory(newRevision);
                }
            } catch (Exception e) {
                newRevision = 0;
            }

            proposal.setStatus(ContributionStatus.PUBLISHED);
            proposal.update();

            Promise.promise(() -> {
                ResourceSpace wg = WorkingGroup.read(gid).getResources();
                if (wg != null) {
                    return NotificationsDelegate.updatedContributionInResourceSpace(wg, proposal);
                } else {
                    return true;
                }
            });

            return ok(Json.toJson(newRevision));
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }


    }

    /**
     * POST               /api/assembly/:aid/campaign/:cid/contribution/:coid/document
     *
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Integer.class, produces = "application/json", value = "Publishes a Contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createPad(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid,
            @ApiParam(name = "typeDocument", value = "Type of document") String typeDocument) {

        try {
            JsonNode body = request().body().asJson();
            Contribution contribution = Contribution.read(coid);
            Campaign campaign = Campaign.read(cid);

            User groupCreator = User.findByAuthUserIdentity(PlayAuthenticate
                    .getUser(session()));
            ResourceTypes resourceTypes;

            Assembly a = Assembly.read(aid);
            ResourceSpace rs = a.getResources();

            ContributionTemplate template = null;
            List<ContributionTemplate> templates = rs.getTemplates();
            if (templates != null && !templates.isEmpty()) {
                template = rs.getTemplates().get(0);
            }

            String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
            String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);


            if (typeDocument.equals("etherpad")) {
                if (body.get("etherpadServerUrl") != null) {
                    etherpadServerUrl = body.get("etherpadServerUrl").asText();
                }
                resourceTypes = ResourceTypes.PROPOSAL;
            }

            if (typeDocument.equals("gdoc")) {
                if (body.get("etherpadServerUrl") != null) {
                    etherpadServerUrl = body.get("gdocLink").asText();
                }
                resourceTypes = ResourceTypes.GDOC;
            }

            if (body.get("etherpadServerApiKey") != null) {
                etherpadApiKey = body.get("etherpadServerApiKey").asText();
            }

            Resource res = null;


            // save the etherpad
            ContributionsDelegate.createAssociatedPad(etherpadServerUrl,
                    etherpadApiKey,
                    contribution,
                    template,
                    campaign.getResources().getUuid());


        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }
        return ok(" ok");

    }

    /**
     * Return the full list of working groups in an assembly
     * GET       /api/group/:location_name
     *
     * @param locationName
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = WorkingGroup.class, responseContainer = "List", produces = "application/json", value = "List groups with specific location name")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
    public static Result findWorkingGroupsByLocationName(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "location_name", value = "Location Name") String locationName) {
        System.out.println("findWorkingGroupsByLocationName");
        List<WorkingGroup> workingGroups = WorkingGroup.findByLocationName(locationName);
        return ok(Json.toJson(workingGroups));
    }

}
