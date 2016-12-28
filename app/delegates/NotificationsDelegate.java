package delegates;

import enums.AppcivistResourceTypes;
import enums.NotificationEventName;
import enums.ResourceSpaceTypes;
import exceptions.ConfigurationException;
import models.*;
import models.transfer.*;
import play.Logger;
import play.i18n.Messages;
import play.libs.Json;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import utils.GlobalData;
import utils.services.NotificationServiceWrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static enums.ResourceSpaceTypes.*;

public class NotificationsDelegate {

    public static HashMap<NotificationEventName, String> eventsTitleByType = new HashMap<>();

    static {
        eventsTitleByType.put(NotificationEventName.NEW_CAMPAIGN, "New Campaign");
        eventsTitleByType.put(NotificationEventName.NEW_WORKING_GROUP, "New Working Group");
        eventsTitleByType.put(NotificationEventName.NEW_VOTING_BALLOT, "New Voting Ballot");
        eventsTitleByType.put(NotificationEventName.NEW_MILESTONE, "New Milestone");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_IDEA, "New Contribution Idea");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_PROPOSAL, "New Contribution Proposal");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_DISCUSSION, "New Contribution Discussion");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_COMMENT, "New Contribution Comment");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_NOTE, "New Contribution note");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_FORUM_POST, "New Contribution Post in Forum");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_FEEDBACK, "New Contribution Feedback");
        eventsTitleByType.put(NotificationEventName.UPDATED_CAMPAIGN, "Updated Campaign");
        eventsTitleByType.put(NotificationEventName.UPDATED_WORKING_GROUP, "Updated Working Group");
        eventsTitleByType.put(NotificationEventName.UPDATED_VOTING_BALLOT, "Updated Voting ballot");
        eventsTitleByType.put(NotificationEventName.UPDATED_MILESTONE, "Updated Milestone");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_IDEA, "Updated Contribution Idea");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_PROPOSAL, "Updated Contribution Proposal");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_DISCUSSION, "Updated Contribution Discussion");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_COMMENT, "Updated Contribution Comment");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_NOTE, "Updated Contribution Note");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_FORUM_POST, "Updated Contribution Post");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_FEEDBACK, "Updated Contribution Feedback");
        eventsTitleByType.put(NotificationEventName.UPDATED_CONTRIBUTION_HISTORY, "Updated Contribution History");
        eventsTitleByType.put(NotificationEventName.MILESTONE_PASSED, "Milestone Passed!");
        eventsTitleByType.put(NotificationEventName.MILESTONE_UPCOMING, "Upcoming Milestone");
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
    public static NotificationEventName campaignEvents[] = {NotificationEventName.NEW_WORKING_GROUP,
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

    public static AppCivistBaseModel getOriginByContribution(ResourceSpace rs, Contribution c){
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
                associatedUser = ((WorkingGroup) resource).getCreator().getName();
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
                break;
            case MILESTONE_UPCOMING_IN_A_DAY:
                break;
            case MEMBER_JOINED:
                break;
            case UPDATED_CONTRIBUTION_FEEDBACK:
                break;
            case UPDATED_CONTRIBUTION_HISTORY:
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
                                            String title, String text
            , UUID resourceUuid, String resourceTitle, String resourceText,
                                            Date notificationDate, String resourceType, String associatedUser) {
        // 1. Prepare the notification event data
        NotificationEventSignal notificationEvent = new NotificationEventSignal();
        notificationEvent.setOrigin(origin);
        notificationEvent.setOriginType(originType);
        notificationEvent.setOriginName(originName);
        notificationEvent.setEventName(eventName);
        notificationEvent.setTitle(title);
        notificationEvent.setText(text);
        notificationEvent.setResourceUUID(resourceUuid);
        notificationEvent.setResourceTitle(resourceTitle);
        notificationEvent.setResourceText(resourceText);
        notificationEvent.setNotificationDate(notificationDate);
        notificationEvent.setResourceType(resourceType);
        notificationEvent.setAssociatedUser(associatedUser);
        Logger.info("NOTIFICATION: Notification event ready");

        // 2. Prepare the Notification signal and send to the Notification Service for dispatch
        Logger.info("NOTIFICATION: Signaling notification from '" + notificationEvent.getOriginType() + "' " + notificationEvent.getOrigin() + " about '" + notificationEvent.getEventName() + "'");

        // TODO: change the notification signal to include and eventId = origin+"_"+eventName and use title for another purpose
        NotificationSignalTransfer newNotificationSignal = prepareNotificationSignal(notificationEvent);

        // Send notification Signal to Notification Service
        try {
            NotificationServiceWrapper ns = new NotificationServiceWrapper();
            WSResponse response = ns.sendNotificationSignal(newNotificationSignal);

            // Relay response to requestor
            if (response.getStatus() == 200) {
                Logger.info("NOTIFICATION: Signaled and with OK status => " + response.getBody().toString());
                notificationEvent.setSignaled(true);
                NotificationEventSignal.create(notificationEvent);
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
            Logger.error("Configuration error: ", e);
            return Controller.internalServerError(Json.toJson(responseBody));
        }
    }

    /**
     * Method to prepare a quick notificaiton event object and send it to the local notification endpoint that will prepare the full notificaiton
     * and send the signal to the notification service
     */
    private static NotificationSignalTransfer prepareNotificationSignal(NotificationEventSignal notificationEvent) {
        NotificationSignalTransfer newNotificationSignal = new NotificationSignalTransfer();

        newNotificationSignal.setEventId(notificationEvent.getOrigin().toString() + "_" + notificationEvent.getEventName());
        // TODO: after updating notification service, use title for something different
        newNotificationSignal.setTitle(notificationEvent.getTitle());

        // parts of the notification text
        //  There are news related to a {0} in {1} '{2}' / {3} / {4}
        String text = "";
        String resourceType = notificationEvent.getResourceType().toString();
        String originType = notificationEvent.getOriginType().toString();
        String originName = notificationEvent.getOriginName();
        String associatedUser = notificationEvent.getAssociatedUser();
        String associatedDate = notificationEvent.getNotificationDate().toString();

        String messageCode = "notification.description.general";
        // Get proper i8tnl messages format
        if (notificationEvent.getEventName().toString().contains("NEW")) {
            messageCode = "notification.description.general.resource_new";
        } else if (notificationEvent.getEventName().toString().contains("UPDATED")) {
            messageCode = "notification.description.general.resource_updated";
        } else if (notificationEvent.getEventName().toString().contains("MILESTONE")) {
            if (notificationEvent.getEventName().toString().contains("UPCOMING")) {
                messageCode = "notification.description.campaign.update.milestone";
            } else {
                messageCode = "notification.description.campaign.update.milestone_passed";
            }
        }
        // notification.description.general.resource_new=A new {0} was created in {1} '{2}' by {3}
        text = Messages.get(messageCode, resourceType, originType, originName, associatedUser, associatedDate);
        // setting the text for the signal
        newNotificationSignal.setText(text);
        newNotificationSignal.setData(Json.toJson(notificationEvent).toString());
        return newNotificationSignal;
    }

    private static NotificationEventName getNewContributionEventName(Contribution c) {
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

    private static NotificationEventName getUpdatedContributionEventName(Contribution c) {
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
        net.setTitle(title);
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

    public static void createNotificationEventsByType(String type, Object transfer) throws ConfigurationException {
        if (type.equals(ASSEMBLY.name())) {
            NotificationEventName[] events = getEventsByResourceType(CAMPAIGN.name());
            createAssemblyNotificationEvents(events, (AssemblyTransfer) transfer);
        }
        if (type.equals(WORKING_GROUP.name())) {
            NotificationEventName[] events = getEventsByResourceType(WORKING_GROUP.name());
            createWorkingGroupNotificationEvents(events, (WorkingGroup) transfer);
        }
        if (type.equals(CAMPAIGN.name())) {
            NotificationEventName[] events = getEventsByResourceType(CAMPAIGN.name());
            createCampaignNotificationEvents(events, (CampaignTransfer) transfer);
        }
        if (type.equals(CONTRIBUTION.name())) {
            NotificationEventName[] events = getEventsByResourceType(CONTRIBUTION.name());
            createContributionNotificationEvents(events, (Contribution) transfer);
        }
    }

    public static void createAssemblyNotificationEvents(NotificationEventName[] events, AssemblyTransfer newResource) throws ConfigurationException {
        for (NotificationEventName e : events) {
            createNotificationEvent(newResource.getUuid(), e, eventsTitleByType.get(e));
        }
    }

    public static void createCampaignNotificationEvents(NotificationEventName[] events, CampaignTransfer newResource) throws ConfigurationException {
        for (NotificationEventName e : events) {
            createNotificationEvent(newResource.getUuid(), e, eventsTitleByType.get(e));
        }
    }

    public static void createWorkingGroupNotificationEvents(NotificationEventName[] events, WorkingGroup newResource) throws ConfigurationException {
        for (NotificationEventName e : events) {
            createNotificationEvent(newResource.getUuid(), e, eventsTitleByType.get(e));
        }
    }

    public static void createContributionNotificationEvents(NotificationEventName[] events, Contribution newResource) throws ConfigurationException {
        for (NotificationEventName e : events) {
            createNotificationEvent(newResource.getUuid(), e, eventsTitleByType.get(e));
        }
    }
}
