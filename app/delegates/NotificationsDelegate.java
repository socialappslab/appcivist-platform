package delegates;

import static enums.ResourceSpaceTypes.ASSEMBLY;
import static enums.ResourceSpaceTypes.CAMPAIGN;
import static enums.ResourceSpaceTypes.CONTRIBUTION;
import static enums.ResourceSpaceTypes.WORKING_GROUP;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import enums.*;
import models.*;
import models.transfer.NotificationEventTransfer;
import models.transfer.NotificationSignalTransfer;
import models.transfer.NotificationSubscriptionTransfer;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.i18n.Messages;
import play.libs.Json;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import scala.tools.nsc.backend.icode.Primitives;
import utils.GlobalData;
import utils.LogActions;
import utils.services.NotificationServiceWrapper;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model;

import exceptions.ConfigurationException;

public class NotificationsDelegate {

    public static HashMap<NotificationEventName, String> eventsTitleByType = new HashMap<>();

    static {
        eventsTitleByType.put(NotificationEventName.NEW_CAMPAIGN, "notifications.{{resourceType}}.new.campaign");
        eventsTitleByType.put(NotificationEventName.NEW_WORKING_GROUP, "notifications.{{resourceType}}.new.working.group");
        eventsTitleByType.put(NotificationEventName.NEW_VOTING_BALLOT, "notifications.{{resourceType}}.new.voting.ballot");
        eventsTitleByType.put(NotificationEventName.NEW_MILESTONE, "notifications.{{resourceType}}.new.milestone");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_IDEA, "notifications.{{resourceType}}.new.contribution.idea");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_PROPOSAL, "notifications.{{resourceType}}.new.contribution.proposal");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_DISCUSSION, "notifications.{{resourceType}}.new.contribution.discussion");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_COMMENT, "notifications.{{resourceType}}.new.contribution.comment");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_NOTE, "notifications.{{resourceType}}.new.contribution.note");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_FORUM_POST, "notifications.{{resourceType}}.new.contribution.post.in.forum");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_FEEDBACK, "notifications.{{resourceType}}.new.contribution.feedback");
        eventsTitleByType.put(NotificationEventName.UPDATED_CAMPAIGN, "notifications.{{resourceType}}.updated.campaign");
        eventsTitleByType.put(NotificationEventName.UPDATED_WORKING_GROUP, "notifications.{{resourceType}}.updated.working.group");
        eventsTitleByType.put(NotificationEventName.UPDATED_VOTING_BALLOT, "notifications.{{resourceType}}.updated.voting.ballot");
        eventsTitleByType.put(NotificationEventName.UPDATED_MILESTONE, "notifications.{{resourceType}}.updated.milestone");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_IDEA, "notifications.{{resourceType}}.updated.contribution.idea");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_PROPOSAL, "notifications.{{resourceType}}.updated.contribution.proposal");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_DISCUSSION, "notifications.{{resourceType}}.updated.contribution.discussion");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_COMMENT, "notifications.{{resourceType}}.updated.contribution.comment");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_NOTE, "notifications.{{resourceType}}.updated.contribution.note");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_FORUM_POST, "notifications.{{resourceType}}.updated.contribution.post");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_FEEDBACK, "notifications.{{resourceType}}.updated.contribution.feedback");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_HISTORY, "notifications.{{resourceType}}.updated.contribution.history");
        eventsTitleByType.put(NotificationEventName.MILESTONE_PASSED, "notifications.{{resourceType}}.milestone.passed");
        eventsTitleByType.put(NotificationEventName.MILESTONE_UPCOMING, "notifications.{{resourceType}}.upcoming.milestone");
    }

    public static NotificationEventName assemblyEvents[] = {
            NotificationEventName.NEW_CAMPAIGN,
            NotificationEventName.NEW_WORKING_GROUP,
            NotificationEventName.NEW_VOTING_BALLOT,
            NotificationEventName.NEW_CONTRIBUTION_IDEA,
            NotificationEventName.NEW_CONTRIBUTION_PROPOSAL,
            NotificationEventName.NEW_CONTRIBUTION_DISCUSSION,
            NotificationEventName.NEW_CONTRIBUTION_COMMENT,
            NotificationEventName.NEW_CONTRIBUTION_NOTE,
            NotificationEventName.UPDATED_CAMPAIGN,
            NotificationEventName.UPDATED_VOTING_BALLOT,
            NotificationEventName.UPDATED_CONTRIBUTION_IDEA,
            NotificationEventName.UPDATED_CONTRIBUTION_PROPOSAL,
            NotificationEventName.UPDATED_CONTRIBUTION_DISCUSSION,
            NotificationEventName.UPDATED_CONTRIBUTION_COMMENT,
            NotificationEventName.UPDATED_CONTRIBUTION_NOTE,
            NotificationEventName.UPDATED_CONTRIBUTION_FEEDBACK,
            NotificationEventName.UPDATED_CONTRIBUTION_HISTORY,
            NotificationEventName.MEMBER_JOINED
    };
    public static NotificationEventName campaignEvents[] = {
            NotificationEventName.NEW_WORKING_GROUP,
            NotificationEventName.NEW_VOTING_BALLOT,
            NotificationEventName.NEW_MILESTONE,
            NotificationEventName.NEW_CONTRIBUTION_IDEA,
            NotificationEventName.NEW_CONTRIBUTION_PROPOSAL,
            NotificationEventName.NEW_CONTRIBUTION_DISCUSSION,
            NotificationEventName.NEW_CONTRIBUTION_COMMENT,
            NotificationEventName.NEW_CONTRIBUTION_NOTE,
            NotificationEventName.NEW_CONTRIBUTION_FEEDBACK,
            NotificationEventName.UPDATED_CAMPAIGN,
            NotificationEventName.UPDATED_VOTING_BALLOT,
            NotificationEventName.UPDATED_MILESTONE,
            NotificationEventName.UPDATED_CONTRIBUTION_IDEA,
            NotificationEventName.UPDATED_CONTRIBUTION_PROPOSAL,
            NotificationEventName.UPDATED_CONTRIBUTION_DISCUSSION,
            NotificationEventName.UPDATED_CONTRIBUTION_COMMENT,
            NotificationEventName.UPDATED_CONTRIBUTION_NOTE,
            NotificationEventName.UPDATED_CONTRIBUTION_FEEDBACK,
            NotificationEventName.UPDATED_CONTRIBUTION_HISTORY,
            NotificationEventName.MILESTONE_PASSED,
            NotificationEventName.MILESTONE_UPCOMING_IN_A_WEEK,
            NotificationEventName.MILESTONE_UPCOMING_IN_A_DAY
    };
    public static NotificationEventName workingGroupEvents[] = {
            NotificationEventName.NEW_VOTING_BALLOT,
            NotificationEventName.NEW_CONTRIBUTION_IDEA,
            NotificationEventName.NEW_CONTRIBUTION_PROPOSAL,
            NotificationEventName.NEW_CONTRIBUTION_DISCUSSION,
            NotificationEventName.NEW_CONTRIBUTION_COMMENT,
            NotificationEventName.NEW_CONTRIBUTION_NOTE,
            NotificationEventName.NEW_CONTRIBUTION_FEEDBACK,
            NotificationEventName.UPDATED_WORKING_GROUP,
            NotificationEventName.UPDATED_VOTING_BALLOT,
            NotificationEventName.UPDATED_MILESTONE,
            NotificationEventName.UPDATED_CONTRIBUTION_IDEA,
            NotificationEventName.UPDATED_CONTRIBUTION_PROPOSAL,
            NotificationEventName.UPDATED_CONTRIBUTION_DISCUSSION,
            NotificationEventName.UPDATED_CONTRIBUTION_COMMENT,
            NotificationEventName.UPDATED_CONTRIBUTION_NOTE,
            NotificationEventName.UPDATED_CONTRIBUTION_FEEDBACK,
            NotificationEventName.UPDATED_CONTRIBUTION_HISTORY
    };
    public static NotificationEventName proposalEvents[] = {
            NotificationEventName.NEW_CONTRIBUTION_IDEA,
            NotificationEventName.NEW_CONTRIBUTION_PROPOSAL,
            NotificationEventName.NEW_CONTRIBUTION_DISCUSSION,
            NotificationEventName.NEW_CONTRIBUTION_COMMENT,
            NotificationEventName.NEW_CONTRIBUTION_NOTE,
            NotificationEventName.NEW_CONTRIBUTION_FEEDBACK,
            NotificationEventName.UPDATED_CONTRIBUTION_IDEA,
            NotificationEventName.UPDATED_CONTRIBUTION_PROPOSAL,
            NotificationEventName.UPDATED_CONTRIBUTION_DISCUSSION,
            NotificationEventName.UPDATED_CONTRIBUTION_COMMENT,
            NotificationEventName.UPDATED_CONTRIBUTION_NOTE,
            NotificationEventName.UPDATED_CONTRIBUTION_FEEDBACK,
            NotificationEventName.UPDATED_CONTRIBUTION_HISTORY
    };

    /**
     * Notify of a new contribution in a Resource Space
     *
     * @param rs the resource space
     * @param c  the contribution
     * @return the result from sending the signal to the notification service
     */
    public static Result newContributionInResourceSpace(ResourceSpace rs, Contribution c) throws ConfigurationException {
        Logger.info("NOTIFICATION: New contribution in RESOURCE SPACE of type '" + rs.getType() + "'");
        ResourceSpaceTypes originType = rs.getType();
        NotificationEventName eventName = getNewContributionEventName(c);
        AppCivistBaseModel origin = getOriginByContribution(rs, c);
        AppCivistBaseModel resource = c;
        return signalNotification(originType, eventName, origin, resource);
    }

    public static AppCivistBaseModel getOriginByContribution(ResourceSpace rs, Contribution c) {
        AppCivistBaseModel origin = null;
        switch (rs.getType()) {
            case ASSEMBLY:
                origin = rs.getAssemblyResources() == null ? rs.getAssemblyForum() : rs.getAssemblyResources();
                break;
            case CAMPAIGN:
                origin = rs.getCampaign();
                break;
            case CONTRIBUTION:
                origin = rs.getContribution();
                break;
            case WORKING_GROUP:
                origin = rs.getWorkingGroupResources() == null ? rs.getWorkingGroupForum() : rs.getWorkingGroupResources();
                break;
            default:
                break;
        }
        return origin;
    }


    public static Result updatedContributionInResourceSpace(ResourceSpace rs, Contribution c) throws ConfigurationException {
        Logger.info("NOTIFICATION: New contribution in RESOURCE SPACE of type '" + rs.getType() + "'");
        ResourceSpaceTypes originType = rs.getType();
        NotificationEventName eventName = getUpdatedContributionEventName(c);
        AppCivistBaseModel origin = getOriginByContribution(rs, c);
        AppCivistBaseModel resource = c;
        return signalNotification(originType, eventName, origin, resource);
    }


    /**
     * Notify of a new contribution in an Assembly
     *
     * @param origin   the assembly
     * @param resource the contribution
     * @return the result from sending the signal to the notification service
     */
    public static Result newContributionInAssembly(Assembly origin, Contribution resource) throws ConfigurationException {
        Logger.info("NOTIFICATION: New contribution in ASSEMBLY of '" + origin.getName() + "'");
        ResourceSpaceTypes originType = ResourceSpaceTypes.ASSEMBLY;
        NotificationEventName eventName = getNewContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource);
    }

    public static Result updateContributionInAssembly(Assembly origin, Contribution resource) throws ConfigurationException {
        Logger.info("NOTIFICATION: New contribution in ASSEMBLY of '" + origin.getName() + "'");
        ResourceSpaceTypes originType = ResourceSpaceTypes.ASSEMBLY;
        NotificationEventName eventName = getUpdatedContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource);
    }

    /**
     * Notify of a new contribution in another contribution
     *
     * @param origin   the contribution where the new contribution is being added to
     * @param resource the new contribution
     * @return the result from sending the signal to the notification service
     */
    public static Object newContributionInContribution(Contribution origin, Contribution resource) throws ConfigurationException {
        Logger.info("NOTIFICATION: New contribution in CONTRIBUTION of '" + resource.getTitle() + "'");
        ResourceSpaceTypes originType = ResourceSpaceTypes.CONTRIBUTION;
        NotificationEventName eventName = getNewContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource);
    }

    /**
     * Notify of a new contribution in another the component of a campaign
     *
     * @param origin   the component
     * @param resource the new contribution
     * @return the result from sending the signal to the notification service
     */
    public static Object newContributionInCampaignComponent(Component origin, Contribution resource) throws ConfigurationException {
        Logger.info("NOTIFICATION: New contribution in CONTRIBUTION of '" + resource.getTitle() + "'");
        ResourceSpaceTypes originType = ResourceSpaceTypes.COMPONENT;
        NotificationEventName eventName = getNewContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource);
    }

    public static Object newContributionInCampaign(Campaign origin, Contribution resource) throws ConfigurationException {
        Logger.info("NOTIFICATION: New contribution in CONTRIBUTION of '" + resource.getTitle() + "'");
        ResourceSpaceTypes originType = ResourceSpaceTypes.COMPONENT;
        NotificationEventName eventName = getNewContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource);
    }

    /**
     * Notify of a new contribution in a working group
     *
     * @param origin   the component
     * @param resource the new contribution
     * @return the result from sending the signal to the notification service
     */
    public static Object newContributionInAssemblyGroup(WorkingGroup origin, Contribution resource) throws ConfigurationException {
        Logger.info("NOTIFICATION: New contribution in CONTRIBUTION of '" + resource.getTitle() + "'");
        ResourceSpaceTypes originType = ResourceSpaceTypes.COMPONENT;
        NotificationEventName eventName = getNewContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource);
    }

    /**
     * Send a notification signal to the notification service
     *
     * @param originType type of resource space in which the notification originates
     * @param eventName  type of event being notified
     * @param origin     the actual resource space from which the notification originates (e.g., an assembly, a campaign, etc.)
     * @param resource   the resource we are referring to in the notification
     * @return the result from sending the signal to the notification service
     */
    public static Result signalNotification(ResourceSpaceTypes originType, NotificationEventName eventName,
                                            AppCivistBaseModel origin, AppCivistBaseModel resource) {
        Logger.info("NOTIFICATION: Prepare the notification event details");
        Logger.info("OriginType: " + originType + " EventName: " + eventName);
        UUID originUUID = null;
        String originName = "";
        String title = "";
        String text = "";
        UUID resourceUuid = null;
        String resourceType = "";
        String resourceTitle = "";
        String resourceText = "";
        Date resourceDate = new Date();
        String associatedUser = "";

        switch (originType) {
            case ASSEMBLY:
                originUUID = ((Assembly) origin).getUuid();
                originName = ((Assembly) origin).getName();
                break;
            case CAMPAIGN:
                originUUID = ((Campaign) origin).getUuid();
                originName = ((Campaign) origin).getTitle();
                break;
            case CONTRIBUTION:
                originUUID = ((Contribution) origin).getUuid();
                originName = ((Contribution) origin).getTitle();
                break;
            case WORKING_GROUP:
                originUUID = ((WorkingGroup) origin).getUuid();
                originName = ((WorkingGroup) origin).getName();
                break;
            case COMPONENT:
                originUUID = ((Component) origin).getUuid();
                originName = ((Component) origin).getTitle();
                break;
            default:
                break;
        }
        Logger.info("OriginName: " + originName + " OriginUUID: " + originUUID);
        switch (eventName) {
            case NEW_CONTRIBUTION_IDEA:
            case NEW_CONTRIBUTION_PROPOSAL:
            case NEW_CONTRIBUTION_DISCUSSION:
            case NEW_CONTRIBUTION_COMMENT:
            case NEW_CONTRIBUTION_NOTE:
            case NEW_CONTRIBUTION_FORUM_POST:
                resourceUuid = ((Contribution) resource).getUuid();
                resourceTitle = ((Contribution) resource).getTitle();
                resourceText = ((Contribution) resource).getText();
                resourceDate = resource.getCreation();
                resourceType = ((Contribution) resource).getType().toString();
                if (resourceType.equals("BRAINSTORMING")) resourceType = "IDEA";
                title = "[AppCivist] New " + resourceType + " in " + originName;
                int numAuthors = ((Contribution) resource).getAuthors().size();
                associatedUser = ((Contribution) resource).getAuthors().get(0).getName() + (numAuthors > 1 ? " et. al." : "");
                break;
            case UPDATED_CONTRIBUTION_IDEA:
            case UPDATED_CONTRIBUTION_PROPOSAL:
            case UPDATED_CONTRIBUTION_DISCUSSION:
            case UPDATED_CONTRIBUTION_COMMENT:
            case UPDATED_CONTRIBUTION_NOTE:
            case UPDATED_CONTRIBUTION_FORUM_POST:
                resourceUuid = ((Contribution) resource).getUuid();
                resourceTitle = ((Contribution) resource).getTitle();
                resourceText = ((Contribution) resource).getText();
                resourceDate = resource.getLastUpdate();
                resourceType = ((Contribution) resource).getType().toString();
                if (resourceType.equals("BRAINSTORMING")) resourceType = "IDEA";
                title = "[AppCivist] Updated " + resourceType + " in " + originName;
                numAuthors = ((Contribution) resource).getAuthors().size();
                associatedUser = ((Contribution) resource).getAuthors().get(0).getName() + (numAuthors > 1 ? " et. al." : "");
                break;
//			case NEW_CONTRIBUTION_FEEDBACK:
//			case UPDATED_CONTRIBUTION_FEEDBACK:
//				// TODO: how do we describe contribution feedack? 
//				resourceUuid = ((ContributionFeedback) resource).getUuid();
//				resourceTitle = ((ContributionFeedback) resource).getTitle();
//				resourceText = ((ContributionFeedback) resource).getText();
//				break;
//			case UPDATED_CONTRIBUTION_HISTORY:
//				// TODO: how do we describe a new history?
//				resourceUuid = ((ContributionHistory) resource).getUuid();
//				resourceTitle = ((ContributionHistory) resource).getTitle();
//				resourceText = ((ContributionHistory) resource).getText();
//				break;
            case NEW_CAMPAIGN:
                resourceUuid = ((Campaign) resource).getUuid();
                resourceTitle = ((Campaign) resource).getTitle();
                resourceText = ((Campaign) resource).getGoal();
                resourceDate = resource.getCreation();
                resourceType = AppcivistResourceTypes.CAMPAIGN.toString();
                title = "[AppCivist] New " + resourceType + " in " + originName;
                Logger.info("Title: " + title);
                // TODO: add creator to campaign associatedUser = ((Campaign) resource).getCreator().getName();
                break;
            case NEW_CONTRIBUTION_FEEDBACK:
                break;
            case UPDATED_CAMPAIGN:
                resourceUuid = ((Campaign) resource).getUuid();
                resourceTitle = ((Campaign) resource).getTitle();
                resourceText = ((Campaign) resource).getGoal();
                resourceDate = resource.getLastUpdate();
                resourceType = AppcivistResourceTypes.CAMPAIGN.toString();
                title = "[AppCivist] Updated " + resourceType + " in " + originName;
                // TODO: add creator to campaign associatedUser = ((Campaign) resource).getCreator().getName();
                break;
            case NEW_WORKING_GROUP:
                resourceUuid = ((WorkingGroup) resource).getUuid();
                resourceTitle = ((WorkingGroup) resource).getName();
                resourceText = ((WorkingGroup) resource).getText();
                resourceDate = resource.getCreation();
                resourceType = AppcivistResourceTypes.WORKING_GROUP.toString();
                title = "[AppCivist] New " + resourceType + " in " + originName;
                associatedUser = ((WorkingGroup) resource).getCreator().getName();
                break;
            case UPDATED_WORKING_GROUP:
                resourceUuid = ((WorkingGroup) resource).getUuid();
                resourceTitle = ((WorkingGroup) resource).getName();
                resourceText = ((WorkingGroup) resource).getText();
                resourceDate = resource.getLastUpdate();
                resourceType = AppcivistResourceTypes.WORKING_GROUP.toString();
                title = "[AppCivist] Updated " + resourceType + " in " + originName;
                if(((WorkingGroup) resource).getCreator() !=null) {
                    associatedUser = ((WorkingGroup) resource).getCreator().getName();
                }else{
                    associatedUser ="";
                }
                break;
            case NEW_VOTING_BALLOT:
                Logger.info("NEW_VOTING_BALLOT");
                WorkingGroup wg = (WorkingGroup) resource;
                //TODO: HOW TO KNOW WHAT BALLOT IS THE LAST
                Ballot lastBallot = wg.getBallots().get(wg.getBallots().size() - 1);
                resourceUuid = lastBallot.getUuid();
                resourceTitle = lastBallot.getDecisionType();
                resourceText = lastBallot.getNotes();
                resourceDate = lastBallot.getCreatedAt();
                resourceType = AppcivistResourceTypes.BALLOT.toString();
                title = "[AppCivist] Updated " + resourceType + " in " + originName;
                //TODO: how to get the associatedUser
                associatedUser = "";
                Logger.info("DATOS NOTIFICACION( " + originUUID + ", " + originType + ", " + originName + ", " + eventName + ", " + title + ", " + text + ", " + resourceUuid + ", " + resourceTitle + ", " + resourceText + ", " + resourceDate + ", " + resourceType + ", " + associatedUser + ")");
                break;
            case UPDATED_VOTING_BALLOT:
//				// TODO: how to describe updated ballots that are not descendants of AppCivistBaseModel
                WorkingGroup uwg = (WorkingGroup) resource;
                //TODO: HOW TO KNOW WHAT BALLOT IS THE LAST ARCHIVED
                Ballot ulastBallot = uwg.getBallots().get(uwg.getBallots().size() - 1);
                resourceUuid = ulastBallot.getUuid();
                resourceTitle = ulastBallot.getDecisionType();
                resourceText = ulastBallot.getNotes();
                resourceDate = ulastBallot.getCreatedAt();
                resourceType = AppcivistResourceTypes.BALLOT.toString();
                title = "[AppCivist] Updated " + resourceType + " in " + originName;
                //TODO: how to get the associatedUser
                associatedUser = "";
                Logger.info("DATOS NOTIFICACION( " + originUUID + ", " + originType + ", " + originName + ", " + eventName + ", " + title + ", " + text + ", " + resourceUuid + ", " + resourceTitle + ", " + resourceText + ", " + resourceDate + ", " + resourceType + ", " + associatedUser + ")");
                break;
            case NEW_MILESTONE:
            case UPDATED_MILESTONE:
            case MILESTONE_PASSED:
            case MILESTONE_UPCOMING:
                resourceUuid = ((ComponentMilestone) resource).getUuid();
                resourceTitle = ((ComponentMilestone) resource).getTitle();
                resourceText = ((ComponentMilestone) resource).getDescription();
                resourceDate = ((ComponentMilestone) resource).getDate();
                resourceType = "MILESTONE";
                // TODO: add creator to milestones associatedUser = ((ComponentMilestone) resource).getCreator().getName();
                break;
            case MILESTONE_UPCOMING_IN_A_WEEK:
                resourceUuid = ((ComponentMilestone) resource).getUuid();
                resourceTitle = ((ComponentMilestone) resource).getTitle();
                resourceText = ((ComponentMilestone) resource).getDescription();
                resourceDate = ((ComponentMilestone) resource).getDate();
                resourceType = "MILESTONE";
                break;
            case MILESTONE_UPCOMING_IN_A_DAY:
                resourceUuid = ((ComponentMilestone) resource).getUuid();
                resourceTitle = ((ComponentMilestone) resource).getTitle();
                resourceText = ((ComponentMilestone) resource).getDescription();
                resourceDate = ((ComponentMilestone) resource).getDate();
                resourceType = "MILESTONE";
                break;
            case MEMBER_JOINED:
                break;
            case UPDATED_CONTRIBUTION_FEEDBACK:
                break;
            case UPDATED_CONTRIBUTION_HISTORY:
                break;
            case BALLOT_UPCOMING_IN_A_DAY:
                resourceUuid = ((Campaign) resource).getUuid();
                resourceTitle = ((Campaign) resource).getTitle();
                resourceText = ((Campaign) resource).getGoal();
                resourceDate = resource.getLastUpdate();
                resourceType = AppcivistResourceTypes.BALLOT.toString();
                break;
            case BALLOT_UPCOMING_IN_A_WEEK:
                resourceUuid = ((Campaign) resource).getUuid();
                resourceTitle = ((Campaign) resource).getTitle();
                resourceText = ((Campaign) resource).getGoal();
                resourceDate = resource.getLastUpdate();
                resourceType = AppcivistResourceTypes.BALLOT.toString();
                break;
            case BALLOT_UPCOMING_IN_A_MONTH:
                resourceUuid = ((Campaign) resource).getUuid();
                resourceTitle = ((Campaign) resource).getTitle();
                resourceText = ((Campaign) resource).getGoal();
                resourceDate = resource.getLastUpdate();
                resourceType = AppcivistResourceTypes.BALLOT.toString();
                break;
            case BALLOT_ENDING_IN_A_DAY:
                resourceUuid = ((Campaign) resource).getUuid();
                resourceTitle = ((Campaign) resource).getTitle();
                resourceText = ((Campaign) resource).getGoal();
                resourceDate = resource.getLastUpdate();
                resourceType = AppcivistResourceTypes.BALLOT.toString();
                break;
            case BALLOT_ENDING_IN_A_WEEK:
                resourceUuid = ((Campaign) resource).getUuid();
                resourceTitle = ((Campaign) resource).getTitle();
                resourceText = ((Campaign) resource).getGoal();
                resourceDate = resource.getLastUpdate();
                resourceType = AppcivistResourceTypes.BALLOT.toString();
                break;
            case BALLOT_ENDING_IN_A_MONTH:
                resourceUuid = ((Campaign) resource).getUuid();
                resourceTitle = ((Campaign) resource).getTitle();
                resourceText = ((Campaign) resource).getGoal();
                resourceDate = resource.getLastUpdate();
                resourceType = AppcivistResourceTypes.BALLOT.toString();
                break;
            default:
                break;
        }
        Logger.info("Sending signalNotification( " + originUUID + ", " + originType + ", " + originName + ", " + eventName + ", " + title + ", " + text + ", " + resourceUuid + ", " + resourceTitle + ", " + resourceText + ", " + resourceDate + ", " + resourceType + ", " + associatedUser + ")");
        return signalNotification(originUUID, originType, originName, eventName, title, text, resourceUuid, resourceTitle, resourceText, resourceDate, resourceType, associatedUser);
    }

    // TODO: signalNotification for non AppCivistBaseModel Resources: VotingBallot

    public static Result signalNotification(UUID origin,
                                            ResourceSpaceTypes originType, String originName, NotificationEventName eventName,
                                            String title, String text,
                                            UUID resourceUuid,
                                            String resourceTitle,
                                            String resourceText,
                                            Date notificationDate,
                                            String resourceType,
                                            String associatedUser) {
        // 1. Prepare the notification event data
        NotificationEventSignal notificationEvent = new NotificationEventSignal();
        notificationEvent.setSpaceType(originType);
        //Default value
        notificationEvent.setSignalType(SubscriptionTypes.REGULAR);
        notificationEvent.setEventId(eventName);
        notificationEvent.setTitle(title);
        notificationEvent.setText(text);

        //Construct hash map
        HashMap<String, Object> data = new HashMap<>();
        data.put("origin",origin.toString());
        data.put("originType", originType);
        data.put("eventName", eventName);
        data.put("originName", originName);
        data.put("resourceType",resourceType);
        data.put( "resourceUUID", resourceUuid ==null ? "" : resourceUuid.toString());
        data.put("resourceTitle", resourceTitle);
        data.put("resourceText",resourceText );
        data.put("notificationDate",notificationDate );
        data.put("associatedUser", associatedUser);
        data.put("signaled", false);

        notificationEvent.setData(data);


        Logger.info("NOTIFICATION: Notification event ready");

        NotificationSignalTransfer newNotificationSignal = null;
        try {
            newNotificationSignal = prepareNotificationSignal(notificationEvent);
        } catch (Exception e) {
            e.printStackTrace();
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(e.getMessage());
            responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
            Logger.error("Error signaling notificaiton: " + LogActions.exceptionStackTraceToString(e));
            return Controller.internalServerError(Json.toJson(responseBody));
        }

        List<Long> notificatedUsers = new ArrayList<>();
        //Get all subscriptions and create NotificationEventSignalUser
        List<Subscription> subscriptions = Subscription.findBySignal(newNotificationSignal);
        for(Subscription sub : subscriptions){
            //subscription.ignoredEventsList[signal.eventName] === null OR false
            if(sub.getIgnoredEvents().get(newNotificationSignal.getData().get("eventName"))== null
                    ||sub.getIgnoredEvents().get(newNotificationSignal.getData().get("eventName")) == false ) {
                // If subscription does not have a defaultService override,
                // then iterate the list of enabled identities of the user (where enabled === true),
                // and create the message to send as follow (see signals.js => processMatch):
                if(sub.getDefaultService() == null){
                    User user = User.findByUUID(UUID.fromString(sub.getUserId()));
                    NotificationEventSignalUser userSignal = new NotificationEventSignalUser(user, notificationEvent);
                    notificationEvent.addNotificationEventSignalUser(userSignal);
                    notificatedUsers.add(user.getUserId());
                }

            }

        }
        //if the spaceType is CAMPAIGN
        if(originType.equals(ResourceSpaceTypes.CAMPAIGN)){

            List<Assembly> assemblies = Assembly.findAssemblyFromCampaign(origin);
            if(!assemblies.isEmpty()){
                for(Assembly assembly : assemblies){
                    System.out.println("Members: " + assembly.getMemberships().size());

                    for( MembershipAssembly member : assembly.getMemberships()){
                        //Get configuration CAMPAIGN_NEWSLETTER_AUTO_SUBSCRIPTION
                        User user = member.getUser();

                        if(!notificatedUsers.contains(user.getUserId())) {// if not already notified
                            Config config = Config.findByUser(user.getUuid(), UserProfileConfigsTypes.CAMPAIGN_NEWSLETTER_AUTO_SUBSCRIPTION);

                            //If auto subscription is active
                            if (config != null) {
                                if (new Boolean(config.getValue())) {
                                    //create new signal
                                    NotificationEventSignalUser userSignal = new NotificationEventSignalUser(user, notificationEvent);
                                    notificationEvent.addNotificationEventSignalUser(userSignal);
                                    notificatedUsers.add(user.getUserId());
                                }
                            }
                        }

                    }
                }
            }
        }


        // Send notification Signal to Notification Service
        try {


            // 2. Prepare the Notification signal and send to the Notification Service for dispatch
            Logger.info("NOTIFICATION: Signaling notification from '" + originType + "' " + originName + " about '" + eventName + "'");

            NotificationServiceWrapper ns = new NotificationServiceWrapper();
            WSResponse response = ns.sendNotificationSignal(newNotificationSignal);

            Logger.info("NOTIFICATION: SENDING SIGNAL");

            // Relay response to requestor
            if (response.getStatus() == 200) {
                Logger.info("NOTIFICATION: Signaled and with OK status => " + response.getBody().toString());
                notificationEvent.getData().put("signaled",true);
                NotificationEventSignal.create(notificationEvent);
                // Register signals by user
                return Controller.ok(Json.toJson(TransferResponseStatus.okMessage("Notification signaled", response.getBody())));
            } else {
                Logger.info("NOTIFICATION: Error while signaling => " + response.getBody().toString());
                NotificationEventSignal.create(notificationEvent);
                return Controller.internalServerError(Json.toJson(TransferResponseStatus.errorMessage("Error while signaling", response.asJson().toString())));
            }
        } catch (ConfigurationException e) {
            Logger.info("NOTIFICATION: Error while signaling => " + e.getMessage());
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.MISSING_CONFIGURATION, e.getMessage()));
            responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
        	Logger.error("Configuration error: ", LogActions.exceptionStackTraceToString(e));
            return Controller.internalServerError(Json.toJson(responseBody));
        } catch (Exception e) {
            Logger.info("NOTIFICATION: Error while signaling => " + e.getMessage());
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(e.getMessage());
            responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
        	Logger.error("Error signaling notificaiton: " + LogActions.exceptionStackTraceToString(e));
        	return Controller.internalServerError(Json.toJson(responseBody));
        }
    }

    /**
     * Method to prepare a quick notificaiton event object and send it to the local notification endpoint that will prepare the full notificaiton
     * and send the signal to the notification service
     */
    private static NotificationSignalTransfer prepareNotificationSignal(NotificationEventSignal notificationEvent) throws Exception {
        NotificationSignalTransfer newNotificationSignal = new NotificationSignalTransfer();

        newNotificationSignal.setEventId(notificationEvent.getData().get("origin") + "_" + notificationEvent.getEventId());
        // TODO: after updating notification service, use title for something different
        newNotificationSignal.setTitle(notificationEvent.getTitle());

        newNotificationSignal.setSpaceId((String) notificationEvent.getData().get("origin"));
        newNotificationSignal.setSignalType(notificationEvent.getSignalType().toString());
        newNotificationSignal.setSpaceType((String) notificationEvent.getData().get("resourceType"));

        // parts of the notification text
        //  There are news related to a {0} in {1} '{2}' / {3} / {4}
        String text = "";
        String resourceType = (String) notificationEvent.getData().get("resourceType");
        String originType = (String) notificationEvent.getData().get("originType").toString();
        String originName = (String) notificationEvent.getData().get("originName");
        String associatedUser = (String) notificationEvent.getData().get("associatedUser");
        Date associatedDate = (Date) notificationEvent.getData().get("notificationDate");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        String associatedDateString = format.format(associatedDate);
        String eventName = (String) notificationEvent.getData().get("eventName").toString();

        String messageCode = "notification.description.general";
        // Get proper i8tnl messages format
        if (eventName.contains("NEW")) {
            messageCode = "notification.description.general.resource_new";
        } else if (eventName.contains("UPDATED")) {
            messageCode = "notification.description.general.resource_updated";
        } else if (eventName.contains("MILESTONE")) {
            if (eventName.contains("UPCOMING")) {
                messageCode = "notification.description.campaign.update.milestone";
            } else {
                messageCode = "notification.description.campaign.update.milestone_passed";
            }
        }
        // notification.description.general.resource_new=A new {0} was created in {1} '{2}' by {3}
        text = Messages.get(messageCode, resourceType, originType, originName, associatedUser, associatedDateString);
        // setting the text for the signal
        newNotificationSignal.setText(text);
        newNotificationSignal.setData(notificationEvent.getData());
        return newNotificationSignal;
    }

    public static NotificationEventName getNewContributionEventName(Contribution c) {
        NotificationEventName eventName = NotificationEventName.NEW_CONTRIBUTION_COMMENT;
        switch (c.getType()) {
            case IDEA:
                eventName = NotificationEventName.NEW_CONTRIBUTION_IDEA;
                break;
            case BRAINSTORMING:
                eventName = NotificationEventName.NEW_CONTRIBUTION_IDEA;
                break;
            case DELIBERATIVE_DISCUSSION:
            case DISCUSSION:
                eventName = NotificationEventName.NEW_CONTRIBUTION_DISCUSSION;
                break;
            case FORUM_POST:
                eventName = NotificationEventName.NEW_CONTRIBUTION_DISCUSSION;
                break;
            case PROPOSAL:
                eventName = NotificationEventName.NEW_CONTRIBUTION_PROPOSAL;
                break;
            case NOTE:
                eventName = NotificationEventName.NEW_CONTRIBUTION_NOTE;
                break;
            default:
                break;
        }
        return eventName;
    }

    public static NotificationEventName getUpdatedContributionEventName(Contribution c) {
        NotificationEventName eventName = NotificationEventName.UPDATED_CONTRIBUTION_COMMENT;
        switch (c.getType()) {
            case IDEA:
                eventName = NotificationEventName.UPDATED_CONTRIBUTION_IDEA;
                break;
            case BRAINSTORMING:
                eventName = NotificationEventName.UPDATED_CONTRIBUTION_IDEA;
                break;
            case DELIBERATIVE_DISCUSSION:
            case DISCUSSION:
                eventName = NotificationEventName.UPDATED_CONTRIBUTION_DISCUSSION;
                break;
            case FORUM_POST:
                eventName = NotificationEventName.UPDATED_CONTRIBUTION_DISCUSSION;
                break;
            case PROPOSAL:
                eventName = NotificationEventName.UPDATED_CONTRIBUTION_PROPOSAL;
                break;
            case NOTE:
                eventName = NotificationEventName.UPDATED_CONTRIBUTION_NOTE;
                break;
            default:
                break;
        }
        return eventName;
    }

    /* Subscriptions */
    @Deprecated
    public static Result subscribeToEvent(NotificationSubscriptionTransfer subscription) throws ConfigurationException {
        NotificationServiceWrapper ns = new NotificationServiceWrapper();
        WSResponse response = ns.createNotificationSubscription(subscription);
        // Relay response to requestor
        if (response.getStatus() == 200) {
            Logger.info("NOTIFICATION: Subscription created => " + response.getBody().toString());
            return Controller.ok(Json.toJson(TransferResponseStatus.okMessage("Subscription created", response.getBody())));
        } else {
            Logger.info("NOTIFICATION: Error while subscribing => " + response.getBody().toString());
            return Controller.internalServerError(Json.toJson(TransferResponseStatus.errorMessage("Error while subscribing", response.getBody().toString())));
        }
    }

    /* Subscriptions */
    public static Result subscribeToEvent(Subscription subscription) throws ConfigurationException {
        NotificationServiceWrapper ns = new NotificationServiceWrapper();
        WSResponse response = ns.createNotificationSubscription(subscription);
        // Relay response to requestor
        if (response.getStatus() == 200) {
            Logger.info("NOTIFICATION: Subscription created => " + response.getBody().toString());
            return Controller.ok(Json.toJson(TransferResponseStatus.okMessage("Subscription created", response.getBody())));
        } else {
            Logger.info("NOTIFICATION: Error while subscribing => " + response.getBody().toString());
            return Controller.internalServerError(Json.toJson(TransferResponseStatus.errorMessage("Error while subscribing", response.getBody().toString())));
        }
    }

    public static Result unSubscribeToEvent(NotificationSubscriptionTransfer subscription) throws ConfigurationException {
        NotificationServiceWrapper ns = new NotificationServiceWrapper();
        WSResponse response = ns.deleteSubscription(subscription);
        // Relay response to requestor
        if (response.getStatus() == 200) {
            Logger.info("NOTIFICATION: Subscription deleted => " + response.getBody().toString());
            return Controller.ok(Json.toJson(TransferResponseStatus.okMessage("Subscription deleted", response.getBody())));
        } else {
            Logger.info("NOTIFICATION: Error while un-subscribing => " + response.getBody().toString());
            return Controller.internalServerError(Json.toJson(TransferResponseStatus.errorMessage("Error while subscribing", response.getBody().toString())));
        }
    }

    public static Result listSubscriptions(String alertEndpoint) throws ConfigurationException {
        NotificationServiceWrapper ns = new NotificationServiceWrapper();
        WSResponse response = ns.listSubscriptionPerAlertEndpoint(alertEndpoint);
        // Relay response to requestor
        if (response.getStatus() == 200) {
            Logger.info("NOTIFICATION: Subscription deleted => " + response.getBody().toString());
            return Controller.ok(response.getBody());
        } else {
            Logger.info("NOTIFICATION: Error while un-subscribing => " + response.getBody().toString());
            return Controller.internalServerError(Json.toJson(TransferResponseStatus.errorMessage("Error while subscribing", response.asJson().toString())));
        }
    }


    public static void createNotificationEvent(UUID uuid, NotificationEventName eventName, String title) throws ConfigurationException {
        NotificationServiceWrapper wrapper = new NotificationServiceWrapper();
        NotificationEventTransfer net = new NotificationEventTransfer();
        net.setEventId(uuid + "_" + eventName);
        net.setTitle(title.replace("{{resourceType}}",eventName.toString().toLowerCase()));
        System.out.println("== title == " + net.getTitle());
        wrapper.createNotificationEvent(net);
    }

    public static NotificationEventName[] getEventsByResourceType(String type) throws ConfigurationException {

        if (type.equals(ASSEMBLY.name())) {
            return assemblyEvents;
        }
        if (type.equals(WORKING_GROUP.name())) {
            return workingGroupEvents;
        }
        if (type.equals(CAMPAIGN.name())) {
            return campaignEvents;
        }
        if (type.equals(CONTRIBUTION.name())) {
            return proposalEvents;
        }
        return null;
    }

    public static void createNotificationEventsByType(String type, UUID uuid) throws ConfigurationException {
        if (type.equals(ASSEMBLY.name())) {
            NotificationEventName[] events = getEventsByResourceType(ASSEMBLY.name());
            createNotificationEvents(events, uuid);
        }
        if (type.equals(WORKING_GROUP.name())) {
            NotificationEventName[] events = getEventsByResourceType(WORKING_GROUP.name());
            createNotificationEvents(events, uuid);
        }
        if (type.equals(CAMPAIGN.name())) {
            NotificationEventName[] events = getEventsByResourceType(CAMPAIGN.name());
            createNotificationEvents(events, uuid);
        }
        if (type.equals(CONTRIBUTION.name())) {
            NotificationEventName[] events = getEventsByResourceType(CONTRIBUTION.name());
            createNotificationEvents(events, uuid);
        }
    }

    public static void createNotificationEvents(NotificationEventName[] events, UUID uuid) throws ConfigurationException {
        for (NotificationEventName e : events) {

            createNotificationEvent(uuid, e, eventsTitleByType.get(e));
        }
    }

    public static Result manageSubscriptionToResourceSpace(String action, ResourceSpace rs, String endpointType, User subscriber) {

        try {
            NotificationEventName events[] = getEventsByResourceType(rs.getType().name());
            HashMap<String, ArrayList<String>> results = new HashMap<>();
            results.put("OK", new ArrayList<>());
            results.put("ERROR", new ArrayList<>());

            for (NotificationEventName e : events) {
                Logger.info("NOTIFICATION: Subscription to => " + e);
                NotificationSubscriptionTransfer n = new NotificationSubscriptionTransfer();
                n.setEventName(e);
                n.setOrigin(getOriginUuId(rs));
                n.setEventIdFromOriginAndEventName();
                n.setEndpointType(endpointType);
                n.setAlertEndpoint(subscriber.getEmail());
                NotificationServiceWrapper ns = new NotificationServiceWrapper();

                WSResponse response;
                if (action.equals("SUBSCRIBE")) {
                    response = ns.createNotificationSubscription(n);
                } else {
                    response = ns.deleteSubscription(n);
                }
                if (response.getStatus() == 200) {
                    results.get("OK").add(n.getEventId());
                    Logger.info("NOTIFICATION: Subscription created => " + response.getBody().toString());
                    //return Controller.ok(Json.toJson(TransferResponseStatus.okMessage("Subscription created", response.getBody())));
                } else {
                    results.get("ERROR").add(n.getEventId() + ": " + response.getBody().toString());
                    Logger.info("NOTIFICATION: Error while subscribing => " + response.getBody().toString());
                    //return Controller.internalServerError(Json.toJson(TransferResponseStatus.errorMessage("Error while subscribing", response.getBody().toString())));
                }
            }

            return Controller.ok(Json.toJson(results));
        } catch (Exception ce) {
            Logger.error("NOTIFICATION: Error while subscribing => ", ce);
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.MISSING_CONFIGURATION, ce.getMessage()));
            Logger.error("Configuration error: ", ce);
            return Controller.internalServerError(Json.toJson(responseBody));
        }
    }

    private static UUID getOriginUuId(ResourceSpace origin) throws Exception {
        switch (origin.getType()) {
            case ASSEMBLY:
                return origin.getAssemblyResources().getUuid();
            case CAMPAIGN:
                return origin.getCampaign().getUuid();
            case CONTRIBUTION:
                return origin.getContribution().getUuid();
            case WORKING_GROUP:
                return origin.getWorkingGroupResources().getUuid();
            default:
                throw new Exception("Not matching resource space found: " + origin.getType());
        }
    }

    public static void createAllEventsforResourceSpace(String type) throws Exception {
        NotificationEventName events[] = getEventsByResourceType(type);

        switch (ResourceSpaceTypes.valueOf(type)) {
            case ASSEMBLY:
                Model.Finder<Long, Assembly> assemblyFinder = new Model.Finder<>(
                        Assembly.class);
                for (Assembly c : assemblyFinder.all()) {
                    for (NotificationEventName n : events) {
                        createNotificationEvent(c.getUuid(), n, eventsTitleByType.get(n));
                    }
                }
                break;
            case CAMPAIGN:
                Model.Finder<Long, Campaign> campaignFinder = new Model.Finder<>(
                        Campaign.class);
                for (Campaign c : campaignFinder.all()) {
                    for (NotificationEventName n : events) {
                        createNotificationEvent(c.getUuid(), n, eventsTitleByType.get(n));
                    }
                }
                break;
            case CONTRIBUTION:
                Model.Finder<Long, Contribution> contributionFinder = new Model.Finder<>(
                        Contribution.class);
                for (Contribution c : contributionFinder.all()) {
                    for (NotificationEventName n : events) {
                        createNotificationEvent(c.getUuid(), n, eventsTitleByType.get(n));
                    }
                }
                break;
            case WORKING_GROUP:
                Model.Finder<Long, WorkingGroup> workingFinder = new Model.Finder<>(
                        WorkingGroup.class);
                for (WorkingGroup c : workingFinder.all()) {
                    for (NotificationEventName n : events) {
                        createNotificationEvent(c.getUuid(), n, eventsTitleByType.get(n));
                    }
                }
                break;
            default:
                throw new Exception("Not matching resource space found: " + type);
        }

    }

    static Model.Finder<Long, NotificationEventSignal> finder = new Model.Finder<>(
            NotificationEventSignal.class);

    public static List<NotificationEventSignal> findNotifications(Map<String, Object> conditions, Integer page, Integer pageSize){
        ExpressionList<NotificationEventSignal> q = finder.where();

        if(conditions != null){
            for(String key : conditions.keySet()){
                switch (key){
                    case "resourceSpaceUuid":
                        q.eq("resourceUUID", conditions.get(key));
                        break;
                    case "userUuid": // just an example
                        q.eq("userUuid", conditions.get(key));
                        break;
                }
            }
        }

        if(page != null && pageSize != null){
            return q.findPagedList(page, pageSize).getList();
        }else{
            return q.findList();
        }
    }

    static Model.Finder<Long, NotificationEventSignalUser> finderNotificationUser = new Model.Finder<>(
            NotificationEventSignalUser.class);

    public static List<NotificationEventSignalUser> findNotificationsUser(Map<String, Object> conditions, Integer page, Integer pageSize){
        ExpressionList<NotificationEventSignalUser> q = finderNotificationUser.where();

        if(conditions != null){
            for(String key : conditions.keySet()){
                switch (key){
                    case "user":
                        q.eq("user.userId", conditions.get(key));
                        break;
                }
            }
        }

        if(page != null && pageSize != null){
            return q.findPagedList(page, pageSize).getList();
        }else{
            return q.findList();
        }
    }
}
