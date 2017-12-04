package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;

import delegates.NotificationsDelegate;
import enums.AppcivistNotificationTypes;
import enums.AppcivistResourceTypes;
import enums.NotificationEventName;
import enums.SubscriptionTypes;
import exceptions.ConfigurationException;
import http.Headers;
import io.swagger.annotations.*;
import models.*;
import models.transfer.*;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import scala.tools.nsc.backend.icode.Primitives;
import security.SecurityModelConstants;
import utils.GlobalData;

import java.text.SimpleDateFormat;
import java.util.*;

import static play.data.Form.form;

/**
 * Temporal controller to manage a mockup version of the notification server bus
 * TODO: replace this controller (or adapt) to use a notification queue like RabbitMQ
 *
 * @author cdparra
 */
@SuppressWarnings("unused")
@Api(value = "06 notification: Notifications management")
@With(Headers.class)
public class Notifications extends Controller {

    final private static String NOTIFICATION_TITLE_ASSEMBLY_UPDATE = "notification.title.assembly.update";
    final private static String NOTIFICATION_TITLE_GROUP_UPDATE = "notification.title.group.update";
    final private static String NOTIFICATION_TITLE_CONTRIBUTION_UPDATE = "notification.title.contribution.update";
    final private static String NOTIFICATION_TITLE_CAMPAIGN_UPDATE = "notification.title.campaign.update";
    final private static String NOTIFICATION_TITLE_MILESTONE_UPDATE = "notification.title.campaign.update.milestone";
    final private static String NOTIFICATION_TITLE_MESSAGE_NEW = "notification.title.message.new";
    final private static String NOTIFICATION_TITLE_MESSAGE_REPLY = "notification.title.message.reply";
    final private static String NOTIFICATION_TITLE_MESSAGE_GROUP_NEW = "notification.title.message.new.group";
    final private static String NOTIFICATION_TITLE_MESSAGE_GROUP_REPLY = "notification.title.message.reply.group";
    final private static String NOTIFICATION_TITLE_MESSAGE_ASSEMBLY_NEW = "notification.title.message.new.assembly";
    final private static String NOTIFICATION_TITLE_MESSAGE_ASSEMBLY_REPLY = "notification.title.message.reply.assembly";

    final private static String NOTIFICATION_DESCRIPTION_ASSEMBLY_FORUM_CONTRIBUTION = "notification.description.assembly.forum.contribution";
    final private static String NOTIFICATION_DESCRIPTION_GROUP_FORUM_CONTRIBUTION = "notification.description.group.forum.contribution";
    final private static String NOTIFICATION_DESCRIPTION_CONTRIBUTION_COMMENT = "notification.description.contribution.comment";
    final private static String NOTIFICATION_DESCRIPTION_CAMPAIGN_CONTRIBUTION = "notification.description.campaign.contribution";
    final private static String NOTIFICATION_DESCRIPTION_UPCOMING_MILESTONE = "notification.description.campaign.upcoming.milestone";

    public static final Form<NotificationSubscriptionTransfer> SUBSCRIPTION_FORM = form(NotificationSubscriptionTransfer.class);
    public static final Form<Subscription>  SUBSCRIPTION_FORM_NEW = form(Subscription.class);


    /**
     * userInbox is the method called by the route GET /user/{uuid}/inbox
     * it returns a list of TransferUpdate containing the latest news from User's assemblies, groups, and contributions
     *
     * @param userUUID
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = UpdateTransfer.class, responseContainer = "List", produces = "application/json", value = "Update user information", notes = "Updates user information")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            //@ApiImplicitParam(name="user", value="user", dataType="String", defaultValue="user", paramType = "path"),
            @ApiImplicitParam(name = "uuid", value = "User's UUID", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
    })
    @Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
    public static Result userInbox(UUID userUUID) {
        // 0. Obtain User
        User u = User.findByUUID(userUUID);

        List<Subscription> subscriptions = Subscription.findByUserId(u);

        if (!subscriptions.isEmpty()) return ok(Json.toJson(subscriptions));
        else
            return notFound(Json.toJson(new TransferResponseStatus("No updates")));
    }


   /* @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Subscribe to receive notifications for eventName on origin", httpMethod = "POST")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Subscription Object", value = "Body of Subscription in JSON. Only origin and eventName needed", required = true, dataType = "models.transfer.NotificationSubscriptionTransfer", paramType = "body", example = "{'origin':'6b0d5134-f330-41ce-b924-2663015de5b5','eventName':'NEW_CONTRIBUTION_IDEA'}"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.USER_ROLE)})
    @Deprecated
    public static Result subscribe() {
        // Get the user record of the creator
        User subscriber = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        final Form<NotificationSubscriptionTransfer> newSubscriptionForm = SUBSCRIPTION_FORM.bindFromRequest();
        if (newSubscriptionForm.hasErrors()) {
            return badRequest(Json.toJson(TransferResponseStatus.badMessage("Subscription error", newSubscriptionForm.errorsAsJson().toString())));
        } else {
            NotificationSubscriptionTransfer newSubscription = newSubscriptionForm.get();
            newSubscription.setEventIdFromOriginAndEventName();
            // TODO: add support for other endpoint types beyond email
            newSubscription.setAlertEndpoint(subscriber.getEmail());
            newSubscription.setEndpointType("email");
            try {
                return NotificationsDelegate.subscribeToEvent(newSubscription);
            } catch (ConfigurationException e) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.MISSING_CONFIGURATION, e.getMessage()));
                Logger.error("Configuration error: ", e);
                return internalServerError(Json.toJson(responseBody));
            }
        }
    }*/

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Subscribe to receive notifications for eventName on origin", httpMethod = "POST")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Subscription Object", value = "Body of Subscription in JSON. Only origin and eventName needed", required = true, dataType = "models.Subscription", paramType = "body", example = "{'origin':'6b0d5134-f330-41ce-b924-2663015de5b5'}"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result subscribe() {
        // Get the user record of the creator
        User subscriber = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));

        JsonNode json = request().body().asJson();
        Subscription sub = Json.fromJson(json, Subscription.class);

        sub.setUserId(subscriber.getUuid().toString());
        Logger.info("Ignored Events " + sub.getIgnoredEvents());
        if(sub.getIgnoredEvents()==null || sub.getIgnoredEvents().isEmpty()){
            Logger.info("Ignored Events null or empty. Setting default value");
            HashMap<String, Boolean> ignoredEvents = new HashMap<String, Boolean>();
            ignoredEvents.put(EventKeys.UPDATED_CAMPAIGN_CONFIGS, true);
            sub.setIgnoredEvents(ignoredEvents);
        }
        Logger.info("USER ID: " + sub.getUserId());
        try {
            NotificationsDelegate.subscribeToEvent(sub);

            sub.insert();
		} catch (Exception e) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			String error = e.getMessage();
			Boolean uniqueConstraintError = error.contains("unique constraint");
			if (uniqueConstraintError) {
				error = "User is already subscribed to this space";
	            responseBody.setStatusMessage("Error creating subscription: "+error);
	            Logger.info("Subscription already exists");
	            return ok(Json.toJson(responseBody));
			} else {
	            responseBody.setStatusMessage("Error creating subscription: "+error);
	            Logger.error("Configuration error: ", e);
	            return internalServerError(Json.toJson(responseBody));
			}
		}

        return ok();
    }

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Unsubscribe to stop receiving notifications for eventName on origin", httpMethod = "DELETE")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result unsubscribe(UUID origin, String eventName) {
        // Get the user record of the creator
        User subscriber = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        NotificationSubscriptionTransfer subscription = new NotificationSubscriptionTransfer();
        subscription.setOrigin(origin);
        subscription.setEventName(NotificationEventName.valueOf(eventName));
        subscription.setEventIdFromOriginAndEventName();
        // TODO: add support for other endpoint types beyond email
        subscription.setAlertEndpoint(subscriber.getEmail());
        subscription.setEndpointType("email");
        try {
            return NotificationsDelegate.unSubscribeToEvent(subscription);
        } catch (ConfigurationException e) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.MISSING_CONFIGURATION, e.getMessage()));
            Logger.error("Configuration error: ", e);
            return internalServerError(Json.toJson(responseBody));
        }
    }

    @ApiOperation(response = NotificationSubscriptionTransfer.class, responseContainer = "List", produces = "application/json", value = "List notification events to which the user is subscribed", httpMethod = "GET")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Errors in the server", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result subscriptions() {
        // Get the user record of the creator
        User subscriber = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        try {
            return NotificationsDelegate.listSubscriptions(subscriber.getEmail());
        } catch (ConfigurationException e) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.MISSING_CONFIGURATION, e.getMessage()));
            Logger.error("Configuration error: ", e);
            return internalServerError(Json.toJson(responseBody));
        }
    }

    /* PRIVATE METHODS */
    private static List<UpdateTransfer> processMyAssemblies(User u,
                                                            List<UpdateTransfer> updates, List<Membership> myAssemblyMemberships) {

        for (Membership membership : myAssemblyMemberships) {
            Assembly a = ((MembershipAssembly) membership).getAssembly();

            // 4.1. New Assembly Forum Posts
            ResourceSpace aForum = a.getForum();
            List<Contribution> posts = null;
            Contribution latestForumPost = null;
            if (aForum != null) posts = aForum.getContributions();
            if (posts != null && !posts.isEmpty()) latestForumPost = posts.get(posts.size() - 1);
            if (latestForumPost != null) {
                UpdateTransfer up = UpdateTransfer.getInstance(
                        AppcivistNotificationTypes.ASSEMBLY_UPDATE,
                        AppcivistResourceTypes.CONTRIBUTION_COMMENT,
                        AppcivistResourceTypes.ASSEMBLY,
                        NOTIFICATION_TITLE_ASSEMBLY_UPDATE,
                        NOTIFICATION_DESCRIPTION_ASSEMBLY_FORUM_CONTRIBUTION,
                        u.getName(),
                        u.getLanguage(),
                        a.getAssemblyId(),
                        a.getUuid(),
                        a.getName(),
                        latestForumPost.getContributionId(),
                        latestForumPost.getUuid(),
                        latestForumPost.getTitle(),
                        latestForumPost.getText(),
                        latestForumPost.getAuthors().get(0).getName(),
                        latestForumPost.getCreation());
                up.setRelativeUrl("/assembly/" + a.getAssemblyId() + "/forum");
                up.setResourceProperty("author", latestForumPost.getFirstAuthorName());
                up.setResourceProperty("title", latestForumPost.getTitle());
                up.setResourceProperty("text", latestForumPost.getText());
                up.setResourceProperty("themes", latestForumPost.getThemes());
                up.setContainerProperty("title", a.getName());
                updates.add(up);
            }


            // 4.2. Current Ongoing Campaigns Upcoming Milestones
            ResourceSpace resources = a.getResources();
            List<Campaign> campaigns = null;
            if (resources != null) campaigns = resources.getCampaigns();
            if (campaigns != null && !campaigns.isEmpty()) {
                for (Campaign c : campaigns) {
                    List<Component> components = c.getResources().getComponents();
                    if (components != null && !components.isEmpty()) {
                        for (Component p : components) {
                            Calendar today = Calendar.getInstance();
                            if (p.getEndDate() != null && p.getEndDate().after(today.getTime())) {
                                // 4.2. Current Ongoing Campaigns Upcoming Milestones
                                List<ComponentMilestone> milestones = p.getMilestones();
                                if (milestones != null && !milestones.isEmpty()) {
                                    for (ComponentMilestone m : milestones) {
                                        Date mStart = m.getDate();
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(mStart); // Now use today date.
                                        cal.add(Calendar.DATE, m.getDays());
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        if (cal.getTime().after(today.getTime())) {
                                            UpdateTransfer up = UpdateTransfer.getInstance(
                                                    AppcivistNotificationTypes.UPCOMING_MILESTONE,
                                                    AppcivistResourceTypes.CAMPAIGN_COMPONENT,
                                                    AppcivistResourceTypes.CAMPAIGN,
                                                    NOTIFICATION_TITLE_MILESTONE_UPDATE,
                                                    NOTIFICATION_DESCRIPTION_UPCOMING_MILESTONE,
                                                    u.getName(),
                                                    u.getLanguage(),
                                                    c.getCampaignId(),
                                                    c.getUuid(),
                                                    c.getTitle(),
                                                    m.getComponentMilestoneId(),
                                                    m.getUuid(), m.getTitle(),
                                                    sdf.format(cal.getTime()),
                                                    a.getName(),
                                                    m.getCreation());
                                            up.setRelativeUrl(
                                                    "/assembly/" + a.getAssemblyId()
                                                            + "/campaign/" + c.getCampaignId()
                                                            + "/" + p.getComponentId()
                                                            + "/" + m.getComponentMilestoneId());
                                            up.setContainerProperty("title", c.getTitle());
                                            updates.add(up);
                                        }
                                    }
                                }
                            }

                            // TODO: 4.3. Current Ongoing Campaigns Latest Contribution
                        }
                    }

                }
            }
        }
        return updates;
    }

    private static List<UpdateTransfer> processMyGroups(User u,
                                                        List<UpdateTransfer> updates, List<Membership> myGroupMemberships) {
        for (Membership membership : myGroupMemberships) {
            WorkingGroup g = membership.getTargetGroup();

            // 4.1. New Group Forum Posts
            // TODO
//			ResourceSpace gForum = g.getResources().get;
//			List<Contribution> posts = null;
//			Contribution latestForumPost = null;
//			if (gForum !=null) posts = gForum.getContributions();
//			if (posts != null && !posts.isEmpty()) latestForumPost = posts.get(0);
//			if (latestForumPost != null)
//				updates.add(TransferUpdate.getInstance(
//						AppcivistNotificationTypes.ASSEMBLY_UPDATE,
//						AppcivistResourceTypes.CONTRIBUTION_COMMENT,
//						AppcivistResourceTypes.ASSEMBLY,
//						NOTIFICATION_TITLE_ASSEMBLY_UPDATE,
//						NOTIFICATION_DESCRIPTION_ASSEMBLY_FORUM_CONTRIBUTION, u
//								.getName(), u.getLanguage(), g.getAssemblyId(),
//						g.getUuid(), g.getName(), latestForumPost
//								.getContributionId(),
//						latestForumPost.getUuid(), latestForumPost.getTitle(),
//						latestForumPost.getText(), latestForumPost.getAuthor()
//								.getName(), latestForumPost.getCreation()));
        }

        return updates;
    }

    private static List<UpdateTransfer> processMyContributions(User u,
                                                               List<UpdateTransfer> updates, List<Contribution> myContribs) {
        // TODO

        return null;
    }

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Subscribe to receive notifications for events in resource space", httpMethod = "POST")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result subscribeToResourceSpace(long sid) {
        User subscriber = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        ResourceSpace rs = ResourceSpace.read(sid);
        return NotificationsDelegate.manageSubscriptionToResourceSpace("SUBSCRIBE", rs, "email", subscriber);
    }

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Unsubscribe to receive notifications for events in resource space", httpMethod = "POST")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result unsubscribeToResourceSpace(long sid) {
        User subscriber = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        ResourceSpace rs = ResourceSpace.read(sid);
        return NotificationsDelegate.manageSubscriptionToResourceSpace("UNSUBSCRIBE", rs, "email", subscriber);    }

    public static Result createResourceSpaceEvents(String type) {
        try{
            NotificationsDelegate.createAllEventsforResourceSpace(type);
            return ok();
        }catch(ConfigurationException e){
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.MISSING_CONFIGURATION, e.getMessage()));
            Logger.error("Configuration error: ", e);
            return internalServerError(Json.toJson(responseBody));
        }catch(Exception e){
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.MISSING_RESOURCE_SPACE_TYPE, type));
            Logger.error("Configuration error: ", e);
            return internalServerError(Json.toJson(responseBody));
        }
    }


    @ApiOperation(httpMethod = "GET", response = UpdateTransfer.class, responseContainer = "List", produces = "application/json", value = "Get space notifications inbox", notes = "Get space notifications inbox")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
    })
    public static Result spaceInbox(@ApiParam(name = "sid", value = "Resource Space ID") Long sid,
                                    @ApiParam(name = "page", value = "Page", defaultValue = "0") Integer page,
                                    @ApiParam(name = "pageSize", value = "Number of elements per page") Integer pageSize) {
        ResourceSpace rs = ResourceSpace.read(sid);

        List<NotificationEventSignal> notifications = new ArrayList<NotificationEventSignal>();
        notifications = processSpaceNotifications(rs, page, pageSize);
        if (notifications.isEmpty()) {
            return notFound(Json.toJson(new TransferResponseStatus("No updates")));
        } else {
            if (page != null && pageSize != null) {
                PaginatedListTransfer<NotificationEventSignal> response = new PaginatedListTransfer<>();
                List<NotificationEventSignal> notificationsTotal = processSpaceNotifications(rs, null, null);
                response.setPage(page);
                response.setPageSize(pageSize);
                response.setTotal(notificationsTotal.size());
                response.setList(notifications);
                return ok(Json.toJson(response));
            } else {
                return ok(Json.toJson(notifications));
            }
        }

    }

    private static List<NotificationEventSignal> processSpaceNotifications(ResourceSpace rs, Integer page, Integer pageSize) {
        // rs.getResourceSpaceUuid() must get in with conditions
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("resourceSpaceUuid", rs.getResourceSpaceUuid());
        return NotificationsDelegate.findNotifications(conditions, page, pageSize);
    }


    @ApiOperation(httpMethod = "GET", response = UpdateTransfer.class, responseContainer = "List", produces = "application/json", value = "Get notifications by user", notes = "Get notifications by user")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
    })
    public static Result getNotificationSignals(@ApiParam(name = "id", value = "User ID") Long id,
                                    @ApiParam(name = "page", value = "Page", defaultValue = "0") Integer page,
                                                @ApiParam(name = "type", value = "Type") String type) {

        List<NotificationEventSignalUser> notifications = new ArrayList<>();
        if((type != null) && (!type.equals(SubscriptionTypes.NEWSLETTER.name()) && !type.equals(SubscriptionTypes.REGULAR.name()))) {
            return badRequest();
        }

        NotificationSignalStatsTransfer tmp = processUserNotificationSignalStats(id, type);
        Integer pageSize = 5;
        if (tmp.getRead().intValue() == tmp.getTotal().intValue()) {
            pageSize = 10;
        }
        notifications = processUserNotificationsSignal(id, page - 1, pageSize, type);
        if (notifications.isEmpty()) {
            return notFound(Json.toJson(new TransferResponseStatus("No data")));
        } else {
            if (page != null) {
                PaginatedListTransfer<NotificationEventSignalUser> response = new PaginatedListTransfer<>();
                response.setPage(page);
                response.setPageSize(pageSize);
                response.setTotal(tmp.getTotal());
                response.setList(notifications);
                return ok(Json.toJson(response));
            } else {
                return ok(Json.toJson(notifications));
            }
          }
    }

    @ApiOperation(httpMethod = "PUT", response = NotificationEventSignalUser.class, responseContainer = "Object", produces = "application/json", value = "Update user notification", notes = "Update user notification")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Notification not found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
    })
    public static Result updateNotification(@ApiParam(name = "id", value = "User ID") Long id, @ApiParam(name = "nid", value = " ID") Long nid) {

        NotificationEventSignalUser notif = NotificationEventSignalUser.getNotificationByUser(id, nid);

        if (notif != null) {
            notif.setRead(true);
            notif.update();
            return ok(Json.toJson(notif));
        } else {
            return notFound(Json.toJson(new TransferResponseStatus("Notification does not exist")));
        }
    }

    private static List<NotificationEventSignalUser> processUserNotificationsSignal(Long userId, Integer page, Integer pageSize, String subsType) {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("user", userId);
        if (subsType != null) {
            conditions.put("signal.signalType", SubscriptionTypes.valueOf(subsType));
        }
        return NotificationsDelegate.findNotificationsUser(conditions, page, pageSize);
    }


    @ApiOperation(httpMethod = "GET", response = UpdateTransfer.class, responseContainer = "Object", produces = "application/json", value = "Get notifications stats by user", notes = "Get notifications stats by user")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
    })
    public static Result getNotificationSignalsStats(@ApiParam(name = "id", value = "User ID") Long id,
                                                     @ApiParam(name = "type", value = "Type") String type) {
        if((type != null) && (!type.equals(SubscriptionTypes.NEWSLETTER.name()) && !type.equals(SubscriptionTypes.REGULAR.name()))) {
            return badRequest();
        }
        NotificationSignalStatsTransfer stats = processUserNotificationSignalStats(id, type);
        return ok(Json.toJson(stats));
    }

    private static NotificationSignalStatsTransfer processUserNotificationSignalStats(Long userId, String subsType) {

        Map<String, Object> conditions = new HashMap<>();
        if(subsType != null) {
            conditions.put("signal.signalType", SubscriptionTypes.valueOf(subsType));
        }
        conditions.put("user", userId);
        List<NotificationEventSignalUser> allNotif = NotificationsDelegate.findNotificationsUser(conditions, null, null);

        NotificationSignalStatsTransfer stats = new NotificationSignalStatsTransfer();

        int total = allNotif.size();
        int read = 0;
        int unread = 0;
        for (NotificationEventSignalUser notif : allNotif) {
            if (notif.getRead() == true) {
                read++;
            } else {
                unread++;
            }
        }
        stats.setTotal(total);
        stats.setPages(total/5); //total/5
        stats.setRead(read);
        stats.setUnread(unread);

        return stats;
    }

    @ApiOperation(httpMethod = "PUT", response = NotificationEventSignalUser.class, responseContainer = "List", produces = "application/json", value = "Update user notifications", notes = "Update user notifications")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Notification not found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
    })
    public static Result updateNotifications(@ApiParam(name = "id", value = "User ID") Long id) {
        List<NotificationEventSignalUser> notifs = NotificationEventSignalUser.getNotificationsByUser(id);

        if (notifs != null) {
            for (NotificationEventSignalUser notif: notifs) {
                notif.setRead(true);
                notif.update();
            }
            return ok(Json.toJson(notifs));
        } else {
            return notFound(Json.toJson(new TransferResponseStatus("User has no notifications")));
        }
    }
}