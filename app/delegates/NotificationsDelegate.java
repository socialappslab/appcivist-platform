package delegates;

import java.util.Date;
import java.util.UUID;

import models.AppCivistBaseModel;
import models.Assembly;
import models.Campaign;
import models.Component;
import models.ComponentMilestone;
import models.Contribution;
import models.NotificationEvent;
import models.ResourceSpace;
import models.WorkingGroup;
import models.transfer.NotificationSignalTransfer;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.i18n.Messages;
import play.libs.Json;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import utils.services.NotificationServiceWrapper;
import enums.AppcivistResourceTypes;
import enums.NotificationEventName;
import enums.ResourceSpaceTypes;

public class NotificationsDelegate {

	/** 
	 * Notify of a new contribution in a Resource Space
	 * 
	 * @param rs the resource space
	 * @param c the contribution
	 * @return the result from sending the signal to the notification service
	 */
	public static Result newContributionInResourceSpace(ResourceSpace rs, Contribution c) {
		Logger.info("NOTIFICATION: New contribution in RESOURCE SPACE of type '"+rs.getType()+"'");
		ResourceSpaceTypes originType = rs.getType();
        NotificationEventName eventName = getContributionEventName(c);
        AppCivistBaseModel origin = null;
        AppCivistBaseModel resource = c;
        switch (rs.getType()) {
		case ASSEMBLY:
			origin = rs.getAssemblyResources() == null ? rs.getAssemblyForum() : rs.getAssemblyResources();
			break;
		case CAMPAIGN:
			origin = rs.getCampaign();
			break;
		case CONTRIBUTION:
			origin = rs.getContribution();
		case WORKING_GROUP:
			origin = rs.getWorkingGroupResources() == null ? rs.getWorkingGroupForum() : rs.getWorkingGroupResources();
		default: 
			break;
        }
        return signalNotification(originType, eventName, origin, resource);
	}
	
	/** 
	 * Notify of a new contribution in an Assembly
	 * 
	 * @param origin the assembly
	 * @param resource the contribution
	 * @return the result from sending the signal to the notification service
	 */
	public static Result newContributionInAssembly(Assembly origin, Contribution resource) {
		Logger.info("NOTIFICATION: New contribution in ASSEMBLY of '"+origin.getName()+"'");
		ResourceSpaceTypes originType = ResourceSpaceTypes.ASSEMBLY;
        NotificationEventName eventName = getContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource);		
	}
	
	/** 
	 * Notify of a new contribution in another contribution
	 * 
	 * @param origin the contribution where the new contribution is being added to
	 * @param resource the new contribution
	 * @return the result from sending the signal to the notification service
	 */
	public static Object newContributionInContribution(Contribution origin, Contribution resource) {
		Logger.info("NOTIFICATION: New contribution in CONTRIBUTION of '"+resource.getTitle()+"'");
		ResourceSpaceTypes originType = ResourceSpaceTypes.CONTRIBUTION;
        NotificationEventName eventName = getContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource);	
	}

	/** 
	 * Notify of a new contribution in another the component of a campaign
	 * 
	 * @param origin the component
	 * @param resource the new contribution
	 * @return the result from sending the signal to the notification service
	 */
	public static Object newContributionInCampaignComponent(Component origin, Contribution resource) {
		Logger.info("NOTIFICATION: New contribution in CONTRIBUTION of '"+resource.getTitle()+"'");
		ResourceSpaceTypes originType = ResourceSpaceTypes.COMPONENT;
        NotificationEventName eventName = getContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource);	
	}

	/** 
	 * Notify of a new contribution in a working group
	 * 
	 * @param origin the component
	 * @param resource the new contribution
	 * @return the result from sending the signal to the notification service
	 */
	public static Object newContributionInAssemblyGroup(WorkingGroup origin, Contribution resource) {
		Logger.info("NOTIFICATION: New contribution in CONTRIBUTION of '"+resource.getTitle()+"'");
		ResourceSpaceTypes originType = ResourceSpaceTypes.COMPONENT;
        NotificationEventName eventName = getContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource);	
	}
	
	/**
	 * Send a notification signal to the notification service
	 * 
	 * @param originType type of resource space in which the notification originates
	 * @param eventName type of event being notified
	 * @param origin the actual resource space from which the notification originates (e.g., an assembly, a campaign, etc.)
	 * @param resource the resource we are referring to in the notification
	 * @return the result from sending the signal to the notification service
	 */
	public static Result signalNotification(ResourceSpaceTypes originType, NotificationEventName eventName, AppCivistBaseModel origin, AppCivistBaseModel resource) {
		Logger.info("NOTIFICATION: Prepare the notification event details");
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
			default :
				break;
		}
		
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
				title = "[AppCivist] New "+resourceType+" in "+originName;
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
				title = "[AppCivist] Updated "+resourceType+" in "+originName;
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
				title = "[AppCivist] New "+resourceType+" in "+originName;
				// TODO: add creator to campaign associatedUser = ((Campaign) resource).getCreator().getName();
				break;
			case UPDATED_CAMPAIGN:
				resourceUuid = ((Campaign) resource).getUuid();
				resourceTitle = ((Campaign) resource).getTitle();
				resourceText = ((Campaign) resource).getGoal();
				resourceDate = resource.getLastUpdate();
				resourceType = AppcivistResourceTypes.CAMPAIGN.toString();
				title = "[AppCivist] Updated "+resourceType+" in "+originName;
				// TODO: add creator to campaign associatedUser = ((Campaign) resource).getCreator().getName();
				break;
			case NEW_WORKING_GROUP:
				resourceUuid = ((WorkingGroup) resource).getUuid();
				resourceTitle = ((WorkingGroup) resource).getName();
				resourceText = ((WorkingGroup) resource).getText();
				resourceDate = resource.getCreation();
				resourceType = AppcivistResourceTypes.WORKING_GROUP.toString();
				title = "[AppCivist] New "+resourceType+" in "+originName;
				associatedUser = ((WorkingGroup) resource).getCreator().getName();
				break;
			case UPDATED_WORKING_GROUP:
				resourceUuid = ((WorkingGroup) resource).getUuid();
				resourceTitle = ((WorkingGroup) resource).getName();
				resourceText = ((WorkingGroup) resource).getText();
				resourceDate = resource.getLastUpdate();
				resourceType = AppcivistResourceTypes.WORKING_GROUP.toString();
				title = "[AppCivist] Updated "+resourceType+" in "+originName;
				associatedUser = ((WorkingGroup) resource).getCreator().getName();
				break;
//			case NEW_VOTING_BALLOT:
//			case UPDATED_VOTING_BALLOT:
//				// TODO: how to describe updated ballots that are not descendants of AppCivistBaseModel
//				break;
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
			default :
				break;
		}
					
		return signalNotification(originUUID, originType, originName, eventName, title, text, resourceUuid, resourceTitle, resourceText, resourceDate, resourceType, associatedUser);
    }
	
	// TODO: signalNotification for non AppCivistBaseModel Resources: VotingBallot 
	
	public static Result signalNotification(UUID origin,
			ResourceSpaceTypes originType, String originName, NotificationEventName eventName,
			String title, String text, UUID resourceUuid, String resourceTitle, String resourceText, 
			Date notificationDate, String resourceType, String associatedUser) {
		// 1. Prepare the notification event data
		NotificationEvent notificationEvent = new NotificationEvent();
		notificationEvent.setOrigin(origin);
		notificationEvent.setOriginType(originType);
		notificationEvent.setOriginName(originName);
		notificationEvent.setEventName(eventName);
		notificationEvent.setTitle(title);
		notificationEvent.setText(text);
		notificationEvent.setResourceUUID(resourceUuid);
		notificationEvent.setResourceTitle(resourceTitle);
		notificationEvent.setResourceText(resourceText);
		notificationEvent.setDate(notificationDate);
		notificationEvent.setResourceType(resourceType);
		notificationEvent.setAssociatedUser(associatedUser);
		Logger.info("NOTIFICATION: Notification event ready");

		// 2. Prepare the Notification signal and send to the Notification Service for dispatch
		Logger.info("NOTIFICATION: Signaling notification from '"+notificationEvent.getOriginType()+"' "+notificationEvent.getOrigin()+" about '"+notificationEvent.getEventName()+"'");
		
		// TODO: change the notification signal to include and eventId = origin+"_"+eventName and use title for another purpose
		NotificationSignalTransfer newNotificationSignal = prepareNotificationSignal(notificationEvent);

		// Send notification Signal to Notification Service
		NotificationServiceWrapper ns = new NotificationServiceWrapper();
		WSResponse response = ns.sendNotificationSignal(newNotificationSignal);
		
		// Relay response to requestor
		if (response.getStatus() == 200) {
			Logger.info("NOTIFICATION: Signaled and with OK status => "+response.getBody().toString());
			// TODO: persist notifications for history NotificationEvent.create(notificationEvent);
			return Controller.ok(Json.toJson(TransferResponseStatus.okMessage("Notification signaled",response.getBody())));
		} else {
			Logger.info("NOTIFICATION: Error while signaling => "+response.getBody().toString());
			return Controller.internalServerError(response.asJson());
		}
    }
	
	/**
	 * Method to prepare a quick notificaiton event object and send it to the local notification endpoint that will prepare the full notificaiton 
	 * and send the signal to the notification service 
	 * @param originType
	 * @param eventName
	 * @param originUUID
	 * @param resourceUUID
	 */
	private static NotificationSignalTransfer prepareNotificationSignal(NotificationEvent notificationEvent) {
		NotificationSignalTransfer newNotificationSignal = new NotificationSignalTransfer();
		
		newNotificationSignal.setEventId(notificationEvent.getOrigin().toString()+"_"+notificationEvent.getEventName());
		// TODO: after updating notification service, use title for something different
		newNotificationSignal.setEventTitle(notificationEvent.getTitle());
		
		// parts of the notification text
		//  There are news related to a {0} in {1} '{2}' / {3} / {4}
		String text = "";
		String resourceType = notificationEvent.getResourceType().toString();
		String originType = notificationEvent.getOriginType().toString();
		String originName = notificationEvent.getOriginName();
		String associatedUser = notificationEvent.getAssociatedUser();
		String associatedDate = notificationEvent.getDate().toString();
		
		String messageCode = "notification.description.general";
		// Get proper i8tnl messages format
		if (notificationEvent.getEventName().toString().contains("NEW")) {
			messageCode = "notification.description.general.resource_new";
		} else if (notificationEvent.getEventName().toString().contains("UPDATED")) {
			messageCode = "notification.description.general.resource_updated";
		} else if (notificationEvent.getEventName().toString().contains("MILESTONE")) {
			if (notificationEvent.getEventName().toString().contains("UPCOMING")) {
				messageCode="notification.description.campaign.update.milestone";
			} else {
				messageCode="notification.description.campaign.update.milestone_passed";
			}
		} 
		// notification.description.general.resource_new=A new {0} was created in {1} '{2}' by {3}
		text = Messages.get(messageCode, resourceType, originType, originName, associatedUser, associatedDate);
		// setting the text for the signal
		newNotificationSignal.setText(text);		
		newNotificationSignal.setData(Json.toJson(notificationEvent).toString());	
		return newNotificationSignal;
	}
	
	private static NotificationEventName getContributionEventName(Contribution c) {
        NotificationEventName eventName = NotificationEventName.NEW_CONTRIBUTION_COMMENT;
		switch (c.getType()) {
		case IDEA:
		case BRAINSTORMING:
			eventName = NotificationEventName.NEW_CONTRIBUTION_IDEA;
			break;
		case DELIBERATIVE_DISCUSSION:
		case DISCUSSION:
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
}
