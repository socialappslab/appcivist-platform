package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.PlayAuthenticate;
import delegates.NotificationsDelegate;
import delegates.WorkingGroupsDelegate;
import enums.*;
import exceptions.ConfigurationException;
import exceptions.MembershipCreationException;
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
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.twirl.api.Content;
import providers.LdapAuthProvider;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.services.EtherpadWrapper;
import utils.services.PeerDocWrapper;

import java.math.BigInteger;
import java.util.*;
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
     *
     * @param aid
     * @param wGroupId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = WorkingGroup.class, produces = "application/json", value = "Publish group by ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No working group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
    public static Result publish(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                            @ApiParam(name = "gid", value = "Working Group ID") Long wGroupId) {
        WorkingGroup wg = WorkingGroup.read(wGroupId);
        if (wg == null) {
            TransferResponseStatus response = new TransferResponseStatus();
            response.setResponseStatus(ResponseStatus.NODATA);
            response.setStatusMessage("Working group " + wGroupId + " does not exist");
            return notFound(Json.toJson(response));
        }
        return ok(Json.toJson(WorkingGroupsDelegate.publish(wGroupId)));

    }

    @ApiOperation(httpMethod = "PUT", response = WorkingGroup.class, produces = "application/json", value = "Publish group by ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No working group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
    public static Result createMembership(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                 @ApiParam(name = "gid", value = "Working Group ID") Long wGroupId) {
        WorkingGroup wg = WorkingGroup.read(wGroupId);
        if (wg == null) {
            TransferResponseStatus response = new TransferResponseStatus();
            response.setResponseStatus(ResponseStatus.NODATA);
            response.setStatusMessage("Working group " + wGroupId + " does not exist");
            return notFound(Json.toJson(response));
        }
        try {
            return ok(Json.toJson(WorkingGroup.createMembership(wGroupId)));
        } catch (MembershipCreationException e) {
            e.printStackTrace();
            Logger.info("Error creating Working Group: " + e.getMessage());
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
            responseBody.setStatusMessage("Error creating Working Group: " + e.getMessage());
            return internalServerError(Json.toJson(responseBody));
        }

    }

    @ApiOperation(httpMethod = "PUT", response = WorkingGroup.class, produces = "application/json", value = "Create working group resources")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Working Group Object", value = "Body of WG in JSON", required = true, dataType = "models.WorkingGroup", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    @Restrict({ @Group(GlobalData.USER_ROLE) })
    public static Result createWorkingGroupResource(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "id", value = "WG ID") Long id) {
        final Form<WorkingGroup> newWGForm = WORKING_GROUP_FORM.bindFromRequest();
        // Check for errors in received data
        if (newWGForm.hasErrors()) {
            return badRequest(Json.toJson(TransferResponseStatus.badMessage(
                    Messages.get(GlobalData.GROUP_CREATE_MSG_ERROR,
                            newWGForm.errorsAsJson()), newWGForm
                            .errorsAsJson().toString())));
        } else {
            WorkingGroup workingGroup = newWGForm.get();
            workingGroup.setGroupId(id);
            return ok(Json.toJson(WorkingGroup.createResources(workingGroup)));

        }
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

                    // Add the working group to the assembly
                    ResourceSpace rs = Assembly.read(aid).getResources();
                    rs.addWorkingGroup(newWorkingGroup);
                    newWorkingGroup.getContainingSpaces().add(rs);
                    rs.update();

                    List<InvitationTransfer> invitationsList = newWorkingGroup.getInvitations();
                    newWorkingGroup = WorkingGroup.create(newWorkingGroup);



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

                    /*Assembly assembly = Assembly.read(aid);
                    WorkingGroup finalNewWorkingGroup = newWorkingGroup;
                    Promise.promise(() -> {
                        return NotificationsDelegate.signalNotification(ResourceSpaceTypes.ASSEMBLY, NotificationEventName.NEW_WORKING_GROUP, assembly, finalNewWorkingGroup);
                    });*/
                    Ebean.commitTransaction();

                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.info("Error creating Working Group: " + e.getMessage());
                    responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
                    responseBody.setStatusMessage("Error creating Working Group: " + e.getMessage());
                    return internalServerError(Json.toJson(responseBody));
                } finally {
                    Ebean.endTransaction();
                }
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

                    // Add the working group to the campaign
                    Campaign c = Campaign.read(cid);
                    ResourceSpace rsC = ResourceSpace.read(c.getResourceSpaceId());
                    rsC.addWorkingGroup(newWorkingGroup);
                    newWorkingGroup.getContainingSpaces().add(rsC);
                    rsC.update();


                    // Create and send invitations
                    if (invitations != null && invitations.equals("true")) {
                        for (InvitationTransfer invitation : invitationsList) {
                            MembershipInvitation.create(invitation, groupCreator, newWorkingGroup);
                        }
                    }
                    Ebean.commitTransaction();
                } catch (Exception e) {
                    Logger.info("Error creating Working Group: " + e.getLocalizedMessage());
                    responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
                    responseBody.setStatusMessage("Error creating Working Group: " + e.getMessage());
                    return internalServerError(Json.toJson(responseBody));
                }
                finally {
                    Ebean.endTransaction();
                }
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
                WorkingGroup oldWorkingGroup = WorkingGroup.read(groupId);
                oldWorkingGroup.setName(newWorkingGroup.getName());
                oldWorkingGroup.setInvitationEmail(newWorkingGroup.getInvitationEmail());
                oldWorkingGroup.setText(newWorkingGroup.getText());
                oldWorkingGroup.setConsensusBallot(newWorkingGroup.getConsensusBallot());
                oldWorkingGroup.setMajorityThreshold(newWorkingGroup.getMajorityThreshold());
                oldWorkingGroup.setIsTopic(newWorkingGroup.getIsTopic());
                oldWorkingGroup.setListed(newWorkingGroup.getListed());
                if (newWorkingGroup.getProfile() != null) {
                    oldWorkingGroup.setProfile(newWorkingGroup.getProfile());
                }
                oldWorkingGroup.setProfile(newWorkingGroup.getProfile());
                newWorkingGroup.setGroupId(groupId);
                List<Theme> themes = newWorkingGroup.getThemes();
                List<Theme> themesLoaded = new ArrayList<Theme>();
                for (Theme theme : themes) {
                    Theme themeRead = Theme.read(theme.getThemeId());
                    themesLoaded.add(themeRead);
                }
                oldWorkingGroup.setThemes(themesLoaded);
                oldWorkingGroup.update();

                // TODO: return URL of the new group
                Logger.info("Updating working group");
                Logger.debug("=> " + newWorkingGroupForm.toString());

                responseBody.setNewResourceId(oldWorkingGroup.getGroupId());
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.GROUP_CREATE_MSG_SUCCESS,
                        oldWorkingGroup.getName()/*
                                                 * ,
												 * groupCreator.getIdentifier()
												 */));
                responseBody.setNewResourceURL(GlobalData.GROUP_BASE_PATH + "/"
                        + oldWorkingGroup.getGroupId());
            }
           /* Promise.promise(() -> {
                return NotificationsDelegate.signalNotification(
                        ResourceSpaceTypes.WORKING_GROUP,
                        NotificationEventName.UPDATED_WORKING_GROUP,
                        Assembly.read(aid).getResources(), newWorkingGroup);
            });*/

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
            Result aRet =  Memberships.createMemberShip(requestor, "group", newMembership, id);
            F.Promise.promise(() -> {
                Boolean coordinator = false;
                for (SecurityRole role : requestor.getRoles()) {
                    if (role.getName().equals("COORDINATOR")) {
                        coordinator = true;
                    }
                }
                WorkingGroup wg = WorkingGroup.read(id);
                if (!coordinator) {
                    if (Subscription.findByUserIdAndSpaceId(requestor, wg.getResourcesResourceSpaceUUID()).isEmpty()) {
                        HashMap<String, Boolean> ignoredEvents = new HashMap<String, Boolean>();
                        ignoredEvents.put(EventKeys.NEW_CONTRIBUTION_FEEDBACK, true);
                        ignoredEvents.put(EventKeys.NEW_CONTRIBUTION_FEEDBACK_FLAG, true);
                        ignoredEvents.put(EventKeys.UPDATED_ASSEMBLY_CONFIGS, true);
                        ignoredEvents.put(EventKeys.UPDATED_WORKING_GROUP_CONFIGS, true);
                        ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_IDEA, true);
                        ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_PROPOSAL, true);
                        ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_DISCUSSION, true);
                        ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_COMMENT, true);
                        ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_NOTE, true);
                        ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_FORUM_POST, true);
                        ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_FEEDBACK, true);
                        ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_HISTORY, true);
                        ignoredEvents.put(EventKeys.MEMBER_JOINED, true);
                        ignoredEvents.put(EventKeys.DELETED_CONTRIBUTION, true);
                        ignoredEvents.put(EventKeys.MODERATED_CONTRIBUTION, true);
                        Subscription subscription = new Subscription();
                        subscription.setIgnoredEvents(ignoredEvents);
                        subscription.setSpaceId(wg.getResourcesResourceSpaceUUID());
                        subscription.setSubscriptionType(SubscriptionTypes.REGULAR);
                        subscription.setUserId(requestor.getUuid().toString());
                        subscription.setSpaceType(ResourceSpaceTypes.WORKING_GROUP);
                        subscription.insert();
                        try {
                            NotificationsDelegate.subscribeToEvent(subscription);
                        } catch (ConfigurationException e) {
                            Logger.error("Error notification the subscription creation", e);
                            e.printStackTrace();
                        }
                        Config config = Config.findByUser(requestor.getUuid(), "notifications.preference." +
                                "contributed-contributions.auto-subscription");
                        if (config != null
                                && config.getValue().equals("true")) {
                            Subscription sub = new Subscription();
                            sub.setUserId(requestor.getUuid().toString());
                            sub.setSpaceType(ResourceSpaceTypes.CAMPAIGN);
                            sub.setSpaceId(wg.getResourcesResourceSpaceUUID());
                            sub.setSubscriptionType(SubscriptionTypes.NEWSLETTER);
                            HashMap<String, Boolean> services = new HashMap<>();
                            services.put("facebook-messenger", true);
                            sub.setDisabledServices(services);
                            sub.insert();
                            try {
                                NotificationsDelegate.subscribeToEvent(sub);
                            } catch (ConfigurationException e) {
                                Logger.error("Error notification the subscription creation", e);
                                e.printStackTrace();
                            }
                        }

                    }


                }
                return Optional.ofNullable(null);
            });

            return aRet;
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
            @ApiParam(name = "status", value = "Status of membership invitation or request", allowableValues = "REQUESTED, INVITED, FOLLOWING, ALL", required = true) String status,
            @ApiParam(name = "ldap", value = "Include LDAP users") String ldap,
            @ApiParam(name = "ldapsearch", value = "Status of membership invitation or request") String ldapsearch) {

        Map<String, List> aRet = new HashMap<>();
        List<Membership> m = MembershipGroup.findByAssemblyIdGroupIdAndStatus(aid, gid, status);
        aRet.put("members", m);

        if(ldap.equals("true")) {
            Assembly assembly = Assembly.read(aid);
            try {
                aRet.put("ldap", LdapAuthProvider.getMemberLdapUsers(assembly, ldapsearch));
            } catch (Exception e) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage("Error: "+e.getMessage());
                return internalServerError(Json.toJson(responseBody));
            }
        }

        if (!aRet.get("members").isEmpty() || (ldap.equals("true") && !aRet.get("ldap").isEmpty()))
            return ok(Json.toJson(aRet));
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

       /* Promise.promise(() -> {
            return NotificationsDelegate.signalNotification(ResourceSpaceTypes.WORKING_GROUP, NotificationEventName.NEW_VOTING_BALLOT, workingGroup, workingGroup);
        });*/

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
        /*Promise.promise(() -> {
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
        });*/
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
                /*Promise.promise(() -> {
                    return NotificationsDelegate.signalNotification(
                            ResourceSpaceTypes.WORKING_GROUP,
                            NotificationEventName.UPDATED_CONTRIBUTION_HISTORY,
                            workingGroup.getResources(),
                            contribution);
                });*/
            }


            return ok(Json.toJson(workingGroup));
        } catch (Exception e) {
            e.printStackTrace();
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

        Integer newRevision = null;
        try {
            Contribution proposal = Contribution.read(pid);
            if(proposal.getExtendedTextPad() != null && proposal.getExtendedTextPad().getResourceType().equals(ResourceTypes.PEERDOC)) {
                User user = User.findByAuthUserIdentity(PlayAuthenticate
                        .getUser(session()));
                PeerDocWrapper peerDocWrapper = new PeerDocWrapper(user);
                peerDocWrapper.publish(proposal.getExtendedTextPad());
                newRevision = 1;
            } else {
                try {
                    String etherpadServerUrl = Play
                            .application()
                            .configuration()
                            .getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
                    String etherpadApiKey = Play.application().configuration()
                            .getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);
                    Logger.info("Etherpad Server URL: "+etherpadServerUrl);
                    Logger.info("Etherpad Server URL: "+etherpadApiKey);
                    EtherpadWrapper wrapper = new EtherpadWrapper(etherpadServerUrl, etherpadApiKey);

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
