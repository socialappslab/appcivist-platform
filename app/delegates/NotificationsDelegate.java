package delegates;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model;
import enums.*;
import exceptions.ConfigurationException;
import models.*;
import models.transfer.NotificationEventTransfer;
import models.transfer.NotificationSignalTransfer;
import models.transfer.NotificationSubscriptionTransfer;
import models.transfer.TransferResponseStatus;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import play.Logger;
import play.Play;
import play.i18n.Lang;
import play.i18n.Messages;
import play.libs.Json;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import service.BusComponent;
import utils.GlobalData;
import utils.GlobalDataConfigKeys;
import utils.LogActions;
import utils.services.NotificationServiceWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static enums.ResourceSpaceTypes.*;

public class NotificationsDelegate {

    public static HashMap<NotificationEventName, String> eventsTitleByType = new HashMap<>();
    private static String MAIL_TEMPLATES_DIRECTORY = "conf/signals-notification-templates/";
    private static String NEWSLETTER_MAIL_TEMPLATES_DIRECTORY = MAIL_TEMPLATES_DIRECTORY + "newsletters-templates/";
    private static String NEWSLETTER_NO_ACTIVITY_TEMPLATE_NAME = NEWSLETTER_MAIL_TEMPLATES_DIRECTORY  +
            "newsletter-backend-template-no-activity.html";
    private static String NEWSLETTER_WITH_ACTIVITY_TEMPLATE_NAME = NEWSLETTER_MAIL_TEMPLATES_DIRECTORY + "newsletter-backend" +
            "-template-with-activity.html";
    private static String NEWSLETTER_PROPOSAL_TEMPLATE_NAME = NEWSLETTER_MAIL_TEMPLATES_DIRECTORY + "newsletter-backend" +
            "-template-proposal-stage.html";

    private static String NEWSLETTER_NO_ACTIVITY_TEMPLATE_NAME_MAIL =  NEWSLETTER_MAIL_TEMPLATES_DIRECTORY+ "mail/newsletter-backend" +
            "-template-no-activity.html";
    private static String NEWSLETTER_WITH_ACTIVITY_TEMPLATE_NAME_MAIL = NEWSLETTER_MAIL_TEMPLATES_DIRECTORY + "mail/newsletter-backend" +
            "-template-with-activity.html";
    private static String NEWSLETTER_PROPOSAL_TEMPLATE_NAME_MAIL = NEWSLETTER_MAIL_TEMPLATES_DIRECTORY + "mail/newsletter-backend" +
            "-template-proposal-stage.html";


    private  static  String REGULAR_MAIL_TEMPLATE = MAIL_TEMPLATES_DIRECTORY + "regular-templates/regular.html";

    private static String LI = "<li style ='background-color: #efefef;\n" +
            "        padding: 1rem 2rem;\n" +
            "        margin-bottom: 1rem;'>";

    private static int INITIAL_LIST_OF_SIGNALS_FOR_NEW_SUBSCRIBER = 10;

    private static List<String> filteredEvents = new ArrayList<>();

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
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_FORK, "notifications.{{resourceType}}.new.fork.contribution");
        eventsTitleByType.put(NotificationEventName.NEW_CONTRIBUTION_MERGE, "notifications.{{resourceType}}.new.merge.contribution");
        eventsTitleByType.put(NotificationEventName.MILESTONE_PASSED, "notifications.{{resourceType}}.milestone.passed");
        eventsTitleByType.put(NotificationEventName.MILESTONE_UPCOMING, "notifications.{{resourceType}}.upcoming.milestone");

        // TODO: review list of events not to include in a initialization list, this one is arbitrary
        filteredEvents.add("UPDATED%");
        filteredEvents.add("%FEEDBACK%");
        filteredEvents.add("%MODERATED%");
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
            NotificationEventName.UPDATED_CONTRIBUTION_HISTORY,
            NotificationEventName.NEW_CONTRIBUTION_FORK,
            NotificationEventName.NEW_CONTRIBUTION_MERGE
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
        return signalNotification(originType, eventName, origin, resource, SubscriptionTypes.REGULAR, null);
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
        return signalNotification(originType, eventName, origin, resource, SubscriptionTypes.REGULAR, null);
    }

    public static Result forkMergeContributionInResourceSpace(ResourceSpace rs, Contribution c,
                                                              NotificationEventName forkOrMerge) {
        Logger.info("NOTIFICATION: FORK/MERGE contribution in RESOURCE SPACE of type '" + rs.getType() + "'");
        ResourceSpaceTypes originType = rs.getType();
        AppCivistBaseModel origin = getOriginByContribution(rs, c);
        return signalNotification(originType, forkOrMerge, origin, c, SubscriptionTypes.REGULAR, null);
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
        return signalNotification(originType, eventName, origin, resource, SubscriptionTypes.REGULAR, null);
    }

    public static Result updateContributionInAssembly(Assembly origin, Contribution resource) throws ConfigurationException {
        Logger.info("NOTIFICATION: New contribution in ASSEMBLY of '" + origin.getName() + "'");
        ResourceSpaceTypes originType = ResourceSpaceTypes.ASSEMBLY;
        NotificationEventName eventName = getUpdatedContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource, SubscriptionTypes.REGULAR, null);
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
        return signalNotification(originType, eventName, origin, resource, SubscriptionTypes.REGULAR, null);
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
        return signalNotification(originType, eventName, origin, resource, SubscriptionTypes.REGULAR, null);
    }

    public static Object newContributionInCampaign(Campaign origin, Contribution resource) throws ConfigurationException {
        Logger.info("NOTIFICATION: New contribution in CONTRIBUTION of '" + resource.getTitle() + "'");
        ResourceSpaceTypes originType = ResourceSpaceTypes.COMPONENT;
        NotificationEventName eventName = getNewContributionEventName(resource);
        return signalNotification(originType, eventName, origin, resource, SubscriptionTypes.REGULAR, null);
    }

    public static Object newNewsletterInCampaign(Campaign origin, UUID userId) throws ConfigurationException {
        NotificationEventName eventName = NotificationEventName.NEWSLETTER;
        User user = User.findByUUID(userId);
        return signalNotification(ResourceSpaceTypes.CAMPAIGN, eventName, origin, origin, SubscriptionTypes.NEWSLETTER, user);
    }
    public static Object newNewsletterInWorkingGroup(WorkingGroup origin, UUID userId) throws ConfigurationException {
        NotificationEventName eventName = NotificationEventName.NEWSLETTER;
        User user = User.findByUUID(userId);
        return signalNotification(ResourceSpaceTypes.WORKING_GROUP, eventName, origin, origin, SubscriptionTypes.NEWSLETTER, user);
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
        return signalNotification(originType, eventName, origin, resource, SubscriptionTypes.REGULAR, null);
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
                                            AppCivistBaseModel origin, AppCivistBaseModel resource, SubscriptionTypes subscriptionType,
                                            User userParam) {
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
        Long resourceId = null;
        Map<String, Long> urls = new HashMap<>();
        Boolean originIsResourceSpace = false;
        if (origin instanceof ResourceSpace) {
            originIsResourceSpace = true;
        }

        AppCivistBaseModel originParent = origin;
        switch (originType) {
            case ASSEMBLY:
                if (originIsResourceSpace) {
                    originParent = ((ResourceSpace) origin).getAssemblyResources();
                }
                originUUID = ((Assembly) originParent).getUuid();
                originName = ((Assembly) originParent).getName();
                break;
            case CAMPAIGN:
                if (originIsResourceSpace) {
                    originParent = ((ResourceSpace) origin).getCampaign();
                }
                originUUID = ((Campaign) originParent).getUuid();
                originName = ((Campaign) originParent).getTitle();
                break;
            case CONTRIBUTION:
                if (originIsResourceSpace) {
                    originParent = ((ResourceSpace) origin).getContribution();
                }
                originUUID = ((Contribution) originParent).getUuid();
                originName = ((Contribution) originParent).getTitle();
                break;
            case WORKING_GROUP:
                if (originIsResourceSpace) {
                    originParent = ((ResourceSpace) origin).getWorkingGroupResources();
                }
                originUUID = ((WorkingGroup) originParent).getUuid();
                originName = ((WorkingGroup) originParent).getName();
                break;
            case COMPONENT:
                if (originIsResourceSpace) {
                    originParent = ((ResourceSpace) origin).getComponent();
                }
                originUUID = ((Component) originParent).getUuid();
                originName = ((Component) originParent).getTitle();
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
                resourceId = ((Contribution) resource).getContributionId();
                if (resourceType.equals("BRAINSTORMING")) resourceType = "IDEA";
                title = "[AppCivist] New " + resourceType + " in " + originName;
                if (eventName.equals(NotificationEventName.NEW_CONTRIBUTION_COMMENT)) {
                    title = "[AppCivist] New Comment in " + originName;
                }
                int numAuthors = ((Contribution) resource).getAuthors().size();
                associatedUser = ((Contribution) resource).getCreator() + (numAuthors > 1 ? " et. al." : "");
                Contribution contribution = (Contribution) resource;
                setContributionUrl(contribution, urls);

                break;
            case UPDATED_CONTRIBUTION_IDEA:
            case UPDATED_CONTRIBUTION_PROPOSAL:
            case UPDATED_CONTRIBUTION_DISCUSSION:
            case UPDATED_CONTRIBUTION_COMMENT:
            case UPDATED_CONTRIBUTION_NOTE:
            case UPDATED_CONTRIBUTION_FORUM_POST:
            case NEW_CONTRIBUTION_FORK:
            case NEW_CONTRIBUTION_MERGE:
                resourceUuid = ((Contribution) resource).getUuid();
                resourceTitle = ((Contribution) resource).getTitle();
                resourceText = ((Contribution) resource).getText();
                resourceDate = resource.getLastUpdate();
                resourceType = ((Contribution) resource).getType().toString();
                resourceId = ((Contribution) resource).getContributionId();
                numAuthors = ((Contribution) resource).getAuthors().size();
                associatedUser = ((Contribution) resource).getCreator().getName() + (numAuthors > 1 ? " et. al." : "");
                setContributionUrl((Contribution) resource, urls);
                if (resourceType.equals("BRAINSTORMING")) resourceType = "IDEA";
                title = "[AppCivist] Updated " + resourceType + " in " + originName;
                if (eventName.equals(NotificationEventName.UPDATED_CONTRIBUTION_COMMENT)) {
                    title = "[AppCivist] Updated Comment in " + originName;
                }
                if (eventName.equals(NotificationEventName.NEW_CONTRIBUTION_FORK)) {
                    title = "[AppCivist] The contribution " + resourceTitle + " was forked in " + originName;
                }
                if (eventName.equals(NotificationEventName.NEW_CONTRIBUTION_MERGE)) {
                    title = "[AppCivist] The contribution " + resourceTitle + " was merged in " + originName;
                }

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
                resourceId = ((Campaign) resource).getCampaignId();
                resourceDate = resource.getCreation();
                resourceType = AppcivistResourceTypes.CAMPAIGN.toString();
                title = "[AppCivist] New " + resourceType + " in " + originName;
                Logger.info("Title: " + title);
                setCampaignUrl(((Campaign) resource).getCampaignId(), urls);
                // TODO: add creator to campaign associatedUser = ((Campaign) resource).getCreator().getName();
                break;
            case NEW_CONTRIBUTION_FEEDBACK:
                break;
            case UPDATED_CAMPAIGN:
                resourceUuid = ((Campaign) resource).getUuid();
                resourceTitle = ((Campaign) resource).getTitle();
                resourceText = ((Campaign) resource).getGoal();
                resourceId = ((Campaign) resource).getCampaignId();
                resourceDate = resource.getLastUpdate();
                resourceType = AppcivistResourceTypes.CAMPAIGN.toString();
                title = "[AppCivist] Updated " + resourceType + " in " + originName;
                setCampaignUrl(((Campaign) resource).getCampaignId(), urls);
                // TODO: add creator to campaign associatedUser = ((Campaign) resource).getCreator().getName();
                break;
            case NEW_WORKING_GROUP:
                resourceUuid = ((WorkingGroup) resource).getUuid();
                resourceTitle = ((WorkingGroup) resource).getName();
                resourceText = ((WorkingGroup) resource).getText();
                resourceId = ((WorkingGroup) resource).getGroupId();
                resourceDate = resource.getCreation();
                resourceType = AppcivistResourceTypes.WORKING_GROUP.toString();
                title = "[AppCivist] New " + resourceType + " in " + originName;
                associatedUser = ((WorkingGroup) resource).getCreator().getName();
                setWorkingGroupUrl(resourceId, urls);
                break;
            case UPDATED_WORKING_GROUP:
                resourceUuid = ((WorkingGroup) resource).getUuid();
                resourceTitle = ((WorkingGroup) resource).getName();
                resourceText = ((WorkingGroup) resource).getText();
                resourceId = ((WorkingGroup) resource).getGroupId();
                resourceDate = resource.getLastUpdate();
                resourceType = AppcivistResourceTypes.WORKING_GROUP.toString();
                title = "[AppCivist] Updated " + resourceType + " in " + originName;
                if (((WorkingGroup) resource).getCreator() != null) {
                    associatedUser = ((WorkingGroup) resource).getCreator().getName();
                } else {
                    associatedUser = "";
                }
                setWorkingGroupUrl(resourceId, urls);
                break;
            case NEW_VOTING_BALLOT:
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
                resourceId = ulastBallot.getId();
                title = "[AppCivist] Updated " + resourceType + " in " + originName;
                //TODO: how to get the associatedUser
                associatedUser = "";
                Logger.info("DATOS NOTIFICACION( " + originUUID + ", " + originType + ", " + originName + ", " + eventName + ", " + title + ", " + text + ", " + resourceUuid + ", " + resourceTitle + ", " + resourceText + ", " + resourceDate + ", " + resourceType + ", " + associatedUser + ")");
                break;
            case NEW_MILESTONE:
            case UPDATED_MILESTONE:
            case MILESTONE_PASSED:
            case MILESTONE_UPCOMING:
            case MILESTONE_UPCOMING_IN_A_WEEK:
            case MILESTONE_UPCOMING_IN_A_DAY:
                // TODO: add creator to milestones associatedUser = ((ComponentMilestone) resource).getCreator().getName();
                resourceUuid = ((ComponentMilestone) resource).getUuid();
                resourceTitle = ((ComponentMilestone) resource).getTitle();
                resourceText = ((ComponentMilestone) resource).getDescription();
                resourceDate = ((ComponentMilestone) resource).getDate();
                resourceId = ((ComponentMilestone) resource).getComponentMilestoneId();
                resourceType = "MILESTONE";
                try {
                    ComponentMilestone cm = (ComponentMilestone) resource;
                    List<ResourceSpace> rscmList = cm.getContainingSpaces();
                    if (rscmList != null && rscmList.size() > 0) {
                        ResourceSpace rscm = rscmList.get(0);
                        Campaign ca = rscm.getCampaign();
                        Component co = null;
                        Long caId = null;
                        if (ca == null) {
                            co = rscm.getComponent();
                            List<ResourceSpace> rscoList = co.getContainingSpaces();
                            if (rscoList != null && rscoList.size() > 0) {
                                ResourceSpace rsco = rscoList.get(0);
                                ca = rsco.getCampaign();
                                caId = ca.getCampaignId();
                            }
                        } else {
                            caId = ca.getCampaignId();
                        }

                        if (caId !=null) {
                            setCampaignUrl(caId, urls);
                        } else {
                            String error = "Component Milestone ["
                                    + cm.getComponentMilestoneId()
                                    + "] has no campaign associated to itself or to its parent Component ["
                                    + co.getComponentId()!=null ? co.getComponentId()+"" : "null"
                                    +"]";
                            throw new Exception(error);
                        }
                    }
                } catch (Exception e) {
                    Logger.error("Error setting the milestone for the campaign ID: none campaign found. "+e.getMessage());
                }
                break;
            case MEMBER_JOINED:
                break;
            case UPDATED_CONTRIBUTION_FEEDBACK:
                break;
            case UPDATED_CONTRIBUTION_HISTORY:
                break;
            case BALLOT_UPCOMING_IN_A_DAY:
            case BALLOT_UPCOMING_IN_A_WEEK:
            case BALLOT_UPCOMING_IN_A_MONTH:
            case BALLOT_ENDING_IN_A_DAY:
            case BALLOT_ENDING_IN_A_WEEK:
            case BALLOT_ENDING_IN_A_MONTH:
                resourceUuid = ((Campaign) resource).getUuid();
                resourceTitle = ((Campaign) resource).getTitle();
                resourceText = ((Campaign) resource).getGoal();
                resourceId = ((Campaign) resource).getCampaignId();
                resourceDate = resource.getLastUpdate();
                resourceType = AppcivistResourceTypes.BALLOT.toString();
                setCampaignUrl(resourceId, urls);
                break;
            case NEWSLETTER:
                switch (originType) {
                    case CAMPAIGN:
                        resourceUuid = ((Campaign) resource).getUuid();
                        resourceTitle = ((Campaign) resource).getTitle();
                        resourceText = ((Campaign) resource).getGoal();
                        resourceId = ((Campaign) resource).getCampaignId();
                        resourceDate = resource.getLastUpdate();
                        resourceType = AppcivistResourceTypes.CAMPAIGN.toString();
                        title = "[AppCivist] New Newsletter for " + resourceType;
                        setCampaignUrl(resourceId, urls);
                        break;
                    case WORKING_GROUP:
                        resourceUuid = ((WorkingGroup) resource).getUuid();
                        resourceTitle = ((WorkingGroup) resource).getName();
                        resourceText = ((WorkingGroup) resource).getText();
                        resourceId = ((WorkingGroup) resource).getGroupId();
                        resourceDate = resource.getCreation();
                        resourceType = AppcivistResourceTypes.WORKING_GROUP.toString();
                        title = "[AppCivist] New Newsletter for " + resourceType;
                        setWorkingGroupUrl(resourceId, urls);
                        break;
                }
            default:
                break;
        }
        Logger.info("Sending signalNotification( " + originUUID + ", " + originType + ", " + originName + ", " + eventName + ", " + title + ", " + text + ", " + resourceUuid + ", " + resourceTitle + ", " + resourceText + ", " + resourceDate + ", " + resourceType + ", " + associatedUser + ","+resourceId+")");
        return signalNotification(originUUID, originType, originName, eventName, title, text, resourceUuid,
                resourceTitle, resourceText, resourceDate, resourceType, associatedUser, subscriptionType,
                userParam, resourceId, urls, false);

    }

    private static void setContributionUrl(Contribution contribution, Map<String, Long> urls) {
        urls.put("contributionId", contribution.getContributionId());
        if (contribution.getWorkingGroups() != null && !contribution.getWorkingGroups().isEmpty()) {
            setWorkingGroupUrl(contribution.getWorkingGroups().get(0).getGroupId(), urls);
        }
        if(!contribution.getCampaignIds().isEmpty()) {
            setCampaignUrl(contribution.getCampaignIds().get(0), urls);
        }
    }

    private static void setWorkingGroupUrl(Long workingGroupId, Map<String, Long> urls) {
        WorkingGroup workingGroup = WorkingGroup.read(workingGroupId);
        urls.put("workingGroupId", workingGroupId);
        if(!workingGroup.getCampaigns().isEmpty()) {
            setCampaignUrl(workingGroup.getCampaigns().get(0), urls);
        }
    }

    private static void setCampaignUrl(Long campaignId, Map<String, Long> urls) {
        urls.put("campaignId", campaignId);
        Campaign campaign = Campaign.find.byId(campaignId);
        if(!campaign.getAssemblies().isEmpty()){
            urls.put("assemblyId", campaign.getAssemblies().get(0));
        }
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
                                            String associatedUser,
                                            SubscriptionTypes subscriptionType,
                                            User userParam,
                                            Long resourceID, Map<String, Long> urls,
                                            boolean ownTextAndTitle) {
        // 1. Prepare the notification event data
        NotificationEventSignal notificationEvent = new NotificationEventSignal();
        notificationEvent.setSpaceType(originType);
        //Default value
        notificationEvent.setSignalType(subscriptionType);
        notificationEvent.setEventId(eventName);
        notificationEvent.setTitle(title);
        notificationEvent.setText(text);

        //Construct hash map
        HashMap<String, Object> data = new HashMap<>();
        data.put("origin", origin.toString());
        data.put("originType", originType);
        data.put("eventName", eventName);
        data.put("originName", originName);
        data.put("resourceType", resourceType);
        data.put("resourceUUID", resourceUuid == null ? "" : resourceUuid.toString());
        data.put("resourceTitle", resourceTitle);
        data.put("resourceText", resourceText);
        data.put("notificationDate", notificationDate);
        data.put("associatedUser", associatedUser);
        data.put("signaled", false);
        data.put("originIds", urls);
        try {
            if (subscriptionType.equals(SubscriptionTypes.NEWSLETTER)) {

                    Map<String, Object> template = getNewsletterTemplate(originType, UUID.fromString(origin.toString()), userParam);
                    if(template.get("richText") != null) {
                        notificationEvent.setRichText(String.valueOf(template.get("richText")));
                        template.remove("richText");
                    }
                    if(template.get("richTextMail") != null) {
                        notificationEvent.setRichTextMail(String.valueOf(template.get("richTextMail")));
                        template.remove("richTextMail");
                    }
                    data.put("template", template);


            }
        } catch (IOException e) {
            Logger.error("Error creating the rich text for the notification");
            e.printStackTrace();
        }


        notificationEvent.setData(data);


        Logger.info("NOTIFICATION: Notification event ready");

        NotificationSignalTransfer newNotificationSignal = null;
        try {
            newNotificationSignal = prepareNotificationSignal(notificationEvent);
            if(!ownTextAndTitle) {
                if (subscriptionType.equals(SubscriptionTypes.REGULAR)) {
                    String lang = userParam == null ? Lang.defaultLang().code() : userParam.getLang();
                    lang = lang == null ? Lang.defaultLang().code() : lang;
                    String richTextMail = getRegularMailToSend(newNotificationSignal.getTitle(),
                            newNotificationSignal.getText(), lang, urls);
                    notificationEvent.setRichTextMail(richTextMail);
                }
            } else {
                notificationEvent.setRichTextMail(text);
                notificationEvent.setTitle(title);
            }
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
        Logger.info(subscriptions.size() + " subscriptions found");
        for (Subscription sub : subscriptions) {
            //subscription.ignoredEventsList[signal.eventName] === null OR false
            if (sub.getIgnoredEvents().get(newNotificationSignal.getData().get("eventName")) == null
                    || sub.getIgnoredEvents().get(newNotificationSignal.getData().get("eventName")) == false) {
                // If subscription does not have a defaultService override,
                // then iterate the list of enabled identities of the user (where enabled === true),
                // and create the message to send as follow (see signals.js => processMatch):
                User user = User.findByUUID(UUID.fromString(sub.getUserId()));
                if (sub.getDefaultService() == null && user != null) {
                    Logger.info("Notificated user: " + user.getName());
                    NotificationEventSignalUser userSignal = new NotificationEventSignalUser(user, notificationEvent);
                    notificationEvent.addNotificationEventSignalUser(userSignal);
                    notificatedUsers.add(user.getUserId());
                }

            }

        }


        if(originType.equals(ResourceSpaceTypes.CONTRIBUTION) &&
                (eventName.equals(NotificationEventName.NEW_CONTRIBUTION_MERGE) ||
                        eventName.equals(NotificationEventName.NEW_CONTRIBUTION_FORK))) {
            Contribution contribution = Contribution.getByUUID(resourceUuid);
            notificatedUsers.add(contribution.getCreator().getUserId());
            for(User user: contribution.getAuthors()) {
                if(!notificatedUsers.contains(user.getUserId())) {
                    notificatedUsers.add(user.getUserId());
                }
            }
        }

        //if the spaceType is CAMPAIGN
        if (originType.equals(ResourceSpaceTypes.CAMPAIGN)) {

            List<Assembly> assemblies = Assembly.findAssemblyFromCampaign(origin);
            if (!assemblies.isEmpty()) {
                for (Assembly assembly : assemblies) {
                    System.out.println("Members: " + assembly.getMemberships().size());

                    for (MembershipAssembly member : assembly.getMemberships()) {
                        //Get configuration CAMPAIGN_NEWSLETTER_AUTO_SUBSCRIPTION
                        User user = member.getUser();

                        if (notificatedUsers!=null && user !=null && !notificatedUsers.contains(user.getUserId())) {// if not already notified
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

        //remove possible duplicated
        notificatedUsers = notificatedUsers.stream()
                .distinct()
                .collect(Collectors.toList());

        Logger.info("FINAL NOTIFICATED USERS: " + notificatedUsers);

        // Send notification Signal to Notification Service
        try {
            // 2. Prepare the Notification signal and send to the Notification Service for dispatch
            Logger.info("NOTIFICATION: Signaling notification from '" + originType + "' "
                    + originName + " about '" + eventName + "'");
            Boolean rabbitIsActive = Play.application().configuration().getBoolean("appcivist.services.rabbitmq.active");
            Boolean socialBusIsActive = Play.application().configuration().getBoolean("appcivist.services.notification.default.useSocialBus");
            if(rabbitIsActive !=null && rabbitIsActive) {
                Logger.info("NOTIFICATION: Signaling notification to rabbitmq is enabled");
                notificationEvent = NotificationEventSignal.create(notificationEvent);
                if(eventName.equals(NotificationEventName.NEW_CONTRIBUTION_FORK) ||
                        eventName.equals(NotificationEventName.NEW_CONTRIBUTION_MERGE)) {
                    for(Long userId: notificatedUsers) {
                        User user = User.findByUserId(userId);
                        NotificationEventSignalUser notificationEventSignalUser = new NotificationEventSignalUser(user, notificationEvent);
                        notificationEventSignalUser.save();
                    }
                    BusComponent.sendToRabbit(newNotificationSignal, notificatedUsers,
                            notificationEvent.getRichTextMail(), ownTextAndTitle, true);
                } else {
                    BusComponent.sendToRabbit(newNotificationSignal, notificatedUsers,
                            notificationEvent.getRichTextMail(), ownTextAndTitle, false);
                }
                notificationEvent.getData().put("signaled", true);
                notificationEvent.update();
                return Controller.ok(Json.toJson(TransferResponseStatus.okMessage("Notification signaled","")));
            } else if (socialBusIsActive !=null && socialBusIsActive)  {
                NotificationServiceWrapper ns = new NotificationServiceWrapper();
                WSResponse response = ns.sendNotificationSignal(newNotificationSignal);
                Logger.info("NOTIFICATION: SENDING SIGNAL");
                // Relay response to requestor
                if (response.getStatus() == 200) {
                    Logger.info("NOTIFICATION: Signaled and with OK status => " + response.getBody().toString());
                    notificationEvent.getData().put("signaled", true);
                    NotificationEventSignal.create(notificationEvent);
                    // Register signals by user
                    return Controller.ok(Json.toJson(TransferResponseStatus.okMessage("Notification signaled", response.getBody())));
                } else {
                    Logger.info("NOTIFICATION: Error while signaling => " + response.getBody().toString());
                    NotificationEventSignal.create(notificationEvent);
                    return Controller.internalServerError(Json.toJson(TransferResponseStatus.errorMessage("Error while signaling", response.asJson().toString())));
                }
            } else {
                Logger.info("NOTIFICATION: Created but not signaled to either Social Bus or RabbitMQ");
                notificationEvent.getData().put("signaled", false);
                NotificationEventSignal.create(notificationEvent);
                return Controller.ok(Json.toJson(TransferResponseStatus.okMessage("Notification created but not signaled to external push service","")));
            }
        } catch (IOException | TimeoutException e) {
            Logger.info("NOTIFICATION: Error while signaling => " + e.getLocalizedMessage());
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(e.getLocalizedMessage());
            responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
            Logger.error("Notification Service Error ("+e.getClass().toString()+"): ", LogActions.exceptionStackTraceToString(e));
            NotificationEventSignal.create(notificationEvent);
            return Controller.internalServerError(Json.toJson(responseBody));
        } catch (Exception e) {
            Logger.info("NOTIFICATION: Error while signaling => " + e.getLocalizedMessage());
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(e.getLocalizedMessage());
            responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
            Logger.error("Error signaling notification: ("+e.getClass().toString()+") " + LogActions.exceptionStackTraceToString(e));
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
        if(eventName.equals(NotificationEventName.NEW_CONTRIBUTION_COMMENT.name()) ||
                eventName.equals(NotificationEventName.UPDATED_CONTRIBUTION_COMMENT.name())) {
            resourceType = "COMMENT";
        }
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
                eventName = NotificationEventName.NEW_CONTRIBUTION_FORUM_POST;
                break;
            case PROPOSAL:
                eventName = NotificationEventName.NEW_CONTRIBUTION_PROPOSAL;
                break;
            case NOTE:
                eventName = NotificationEventName.NEW_CONTRIBUTION_NOTE;
                break;
            case ISSUE:
                eventName = NotificationEventName.NEW_CONTRIBUTION_ISSUE;
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
            case COMMENT:
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
        return processSubscribeToEventResponse(response);
    }

    /* Subscriptions */
    public static Result subscribeToEvent(Subscription subscription) throws ConfigurationException {
        NotificationServiceWrapper ns = new NotificationServiceWrapper();
        WSResponse response = ns.createNotificationSubscription(subscription);
        return processSubscribeToEventResponse(response);
    }

    private static Result processSubscribeToEventResponse(WSResponse response) {
        if (response != null && response.getStatus() == 200) {
            Logger.info("NOTIFICATION: Subscription created => " + response.getBody().toString());
            return Controller.ok(Json.toJson(TransferResponseStatus.okMessage("Subscription created", response.getBody())));
        } else if (response == null) {
            Logger.info("NOTIFICATION: Error while subscribing =>  No response from notifications server");
            return Controller.internalServerError(Json.toJson(TransferResponseStatus.errorMessage("Error while subscribing", "NOTIFICATION: Error while subscribing =>  No response from notifications server")));
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
        if(title != null) {
            net.setTitle(title.replace("{{resourceType}}", eventName.toString().toLowerCase()));
        }
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

    /**
     * Returns the frequency, in number of days, to send the newsletter for the given uuid
     * If none frequency is configured then returns the default value: 7 days
     * @param uuid: space id
     * @return newsletterFrequency
     */
    private static Integer getNewsletterFrequency(UUID uuid) {
        Integer newsletterFrequency = Integer.valueOf(GlobalDataConfigKeys.CONFIG_DEFAULTS
                .get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_NEWSLETTER_FRECUENCY));
        List<Config> config = Config.findByCampaignAndKey(uuid,
                GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_NEWSLETTER_FRECUENCY);
        if (config != null && !config.isEmpty()) {
            newsletterFrequency = Integer.valueOf(config.get(0).getValue());
        }
        return newsletterFrequency;
    }

    /**
     * Returns True if the last {@link NotificationEventSignal} from the given space UUID
     * was before getNewsletterFrequency amount of days, else False.
     * If there is no {@link NotificationEventSignal} returns True, too
     * @param spaceId: space id
     * @return
     */
    public static Boolean checkIfNewNewsletterIsRequired(String spaceId) {
        Integer newsletterFrequency = getNewsletterFrequency(UUID.fromString(spaceId));
        List<NotificationEventSignal> list = NotificationEventSignal.findByOriginUuid(spaceId);
        if (list == null || list.isEmpty()) {
            return true;
        } else {
            NotificationEventSignal event = list.get(0);
            long diff = new Date().getTime() - event.getCreation().getTime();
            long days =  TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            return days > newsletterFrequency;
        }
    }

    public static List<NotificationEventSignal> findNotifications(Map<String, Object> conditions, Integer page, Integer pageSize) {
        ExpressionList<NotificationEventSignal> q = finder.where();

        if (conditions != null) {
            for (String key : conditions.keySet()) {
                switch (key) {
                    case "resourceSpaceUuid":
                        q.eq("resourceUUID", conditions.get(key));
                        break;
                    case "userUuid": // just an example
                        q.eq("userUuid", conditions.get(key));
                        break;
                }
            }
        }

        if (page != null && pageSize != null) {
            return q.findPagedList(page, pageSize).getList();
        } else {
            return q.findList();
        }
    }

    static Model.Finder<Long, NotificationEventSignalUser> finderNotificationUser = new Model.Finder<>(
            NotificationEventSignalUser.class);

    public static List<NotificationEventSignalUser> findNotificationsUser(Map<String, Object> conditions, Integer page, Integer pageSize) {
        ExpressionList<NotificationEventSignalUser> q = finderNotificationUser.where();

        if (conditions != null) {
            for (String key : conditions.keySet()) {
                switch (key) {
                    case "user":
                        q.eq("user.userId", conditions.get(key));
                        break;
                    case "signal.signalType":
                        q.eq("signal.signalType", conditions.get(key));
                        break;
                }
            }
        }
        if (page != null && pageSize != null) {
            return q.orderBy("creation desc").findPagedList(page, pageSize).getList();
        } else {
            return q.orderBy("creation desc").findList();
        }
    }

    public static NotificationEventName getUpdateConfigEventName(Config c) {
        switch (c.getConfigTarget()) {
            case ASSEMBLY:
                return NotificationEventName.UPDATED_ASSEMBLY_CONFIGS;

            case CAMPAIGN:
                return NotificationEventName.UPDATED_CAMPAIGN_CONFIGS;

            case WORKING_GROUP:
                return NotificationEventName.UPDATED_WORKING_GROUP_CONFIGS;
            default:
                break;
        }
        return null;
    }

    private static String getUrl(Map<String, Long> url) {
        String aRet = Play.application().configuration().getString("application.uiUrl") + "/";
        ///assembly/113/campaign/215
        if (url.get("assemblyId") != null) {
            aRet = aRet.concat("assembly/" + url.get("assemblyId"));
        }
        if (url.get("campaignId") != null) {
            aRet = aRet.concat("/campaign/" + url.get("campaignId"));
        }
        if (url.get("contributionId") != null) {
            aRet = aRet.concat("/contribution/" + url.get("contributionId"));
        }

        return aRet;
    }


    private static String getRegularMailToSend(String title, String description, String lang, Map<String, Long> url) throws IOException {
        File file = Play.application().getFile(REGULAR_MAIL_TEMPLATE);
        LocalDate now = new LocalDate();
        String content = new String(Files.readAllBytes(Paths.get(file.toString())));
        content = content.replace("{{REGULAR_TITLE}}", title);
        content = content.replace("{{NEW_ACTIVITY}}", title);
        content = content.replace("{{VISIT_BUTTON_URL}}", getUrl(url));
        content = content.replace("{{REGULAR_DESCRIPTION}}", description);
        content = content.replace("{{DATE}}", now.getDayOfMonth() +" " + now.toString("MMM"));
        content = content.replace("{{YEAR}}", String.valueOf(now.getYear()));
        content = content.replace("{{VISIT_BUTTON_TEXT}}", Messages.get(Lang.forCode(lang),
                "playauthenticate.index.details"));
        return content;
    }

    /**
     * Returns a Map object with the template of the newsletter acording to the type of
     * {@link Campaign}
     * @param spaceType
     * @param spaceID
     * @param user
     * @return
     */
    private static Map<String, Object> getNewsletterTemplate(ResourceSpaceTypes spaceType,
                                                             UUID spaceID, User user) throws IOException {
        String CAMPAIGN_NAME = "\\{\\{CAMPAIGN_NAME}}";
        String CAMPAIGN_DESCRIPTION = "\\{\\{CAMPAIGN_DESCRIPTION}}";
        String PROPOSAL_NEW = "\\{\\{PROPOSAL_NEW}}";
        String PROPOSAL_DEVELOPING = "\\{\\{PROPOSAL_DEVELOPING}}";
        String WORKING_GROUPS = "\\{\\{WORKING_GROUP}}";
        String UPDATES = "\\{\\{UPDATES}}";
        String DATE = "\\{\\{DATE}}";
        String NEW_IDEAS_NUMBER = "\\{\\{NEW_IDEAS_NUMBER}}";
        String NEW_IDEAS_TEXT = "\\{\\{NEW_IDEAS_TEXT}}";
        String THEMES = "\\{\\{THEMES}}";
        String RESOURCES = "\\{\\{RESOURCES}}";
        String UNSUSCRIBE_URL = "\\{\\{UNSUSCRIBE_URL}}";
        Map<String, Object> toRet = new HashMap<>();
        Integer newsletterFrequency = getNewsletterFrequency(spaceID);
        LocalDate now = new LocalDate();
        LocalDate monday = now.withDayOfWeek(DateTimeConstants.MONDAY);
        LocalDate friday = now.withDayOfWeek(DateTimeConstants.FRIDAY);
        String week = monday.getDayOfMonth() + " " + monday.toString("MMM")
                + " - " + friday.getDayOfMonth() + " " + friday.toString("MMM");
        String year = String.valueOf(now.getYear());
        String unsuscribeUrl = Play.application().configuration().getString("appcivist.newsletter.unsuscribeUrl");
        if(unsuscribeUrl == null) {
            unsuscribeUrl = "";
        }
        String lang = user == null ? Lang.defaultLang().code() : user.getLang();
        lang = lang == null ? Lang.defaultLang().code() : lang;
        switch (spaceType) {
            case CAMPAIGN:
                Campaign campaign = Campaign.readByUUID(spaceID);
                ComponentTypes stage = Component.getCurrentComponentType(campaign.getCampaignId());
                List<NotificationEventSignal> notificationEventSignals = NotificationEventSignal
                        .findLatestByOriginUuid(spaceID.toString(), newsletterFrequency);
                toRet.put("campaignName", campaign.getTitle());

                //Campaign without Activity
                if (notificationEventSignals.isEmpty()) {
                    File file = Play.application().getFile(NEWSLETTER_NO_ACTIVITY_TEMPLATE_NAME);
                    File fileMail = Play.application().getFile(NEWSLETTER_NO_ACTIVITY_TEMPLATE_NAME_MAIL);
                    String content = new String(Files.readAllBytes(Paths.get(file.toString())));
                    String contentMail = new String(Files.readAllBytes(Paths.get(fileMail.toString())));
                    content = content.replaceAll(CAMPAIGN_NAME,campaign.getTitle()).replaceAll(DATE, week)
                            .replaceAll(UNSUSCRIBE_URL,unsuscribeUrl);
                    contentMail = contentMail.replaceAll(CAMPAIGN_NAME,campaign.getTitle()).replaceAll(DATE, week)
                            .replaceAll(UNSUSCRIBE_URL,unsuscribeUrl).replace("{{DATE_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.date_text"))
                            .replace("{{CAMPAIGN_DESCRIPTION_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.campaign_description_text"))
                            .replace("{{STAGE_NAME_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.stage_name_text"))
                            .replace("{{CAMPAIGN_NAME_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.campaign_name_text"))
                            .replace("{{MORE_INFORMATION_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.more_information_text"))
                            .replace("{{VISIT_BUTTON_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.unsubscribe"))
                            .replace("{{YEAR}}", year);
                    toRet.put("campaignNewsletterDescription",campaign.getGoal());
                    if(campaign.getGoal() != null) {
                        content = content.replaceAll(CAMPAIGN_DESCRIPTION, campaign.getGoal());
                        contentMail = contentMail.replaceAll(CAMPAIGN_DESCRIPTION, campaign.getGoal());
                    } else {
                        content = content.replaceAll(CAMPAIGN_DESCRIPTION, campaign.getTitle());
                        contentMail = contentMail.replaceAll(CAMPAIGN_DESCRIPTION, campaign.getTitle());
                    }
                    if (stage!=null) {
                        toRet.put("stageName", stage.name());
                        content = content.replaceAll("STAGE_NAME",stage.name());
                        contentMail = contentMail.replaceAll("STAGE_NAME",stage.name());
                    } else {
                        content = content.replaceAll("STAGE_NAME","");
                        contentMail = contentMail.replaceAll("STAGE_NAME","");
                    }
                    List<String> themes = campaign.getThemes().stream()
                            .filter(theme -> theme.getType().equals(ThemeTypes.OFFICIAL_PRE_DEFINED))
                            .map(Theme::getTitle).collect(Collectors.toList());
                    toRet.put("themes", themes);
                    StringBuilder themesString = new StringBuilder();
                    for (String theme: themes) {
                        themesString.append(LI).append(theme).append("</li>");
                    }
                    content = content.replaceAll(THEMES, themesString.toString());
                    contentMail = contentMail.replaceAll(THEMES, themesString.toString());
                    List<String> workingGroups = campaign.getWorkingGroups().stream().map(WorkingGroup::getName)
                            .collect(Collectors.toList());
                    toRet.put("workingGroups", workingGroups);
                    StringBuilder wgString = new StringBuilder();
                    for (String wg: workingGroups) {
                        wgString.append(LI).append(wg).append("</li>");
                    }
                    content = content.replaceAll(WORKING_GROUPS, wgString.toString());
                    contentMail = contentMail.replaceAll(WORKING_GROUPS, wgString.toString());
                    List<Map<String, Object>> resourcesFormated = new ArrayList<>();
                    StringBuilder resources = new StringBuilder();
                    for(Resource con: campaign.getResourceList()) {
                        Map<String, Object> cont = new HashMap<>();
                        cont.put("title", con.getTitle());
                        cont.put("link", con.getUrlLargeString());
                        resources.append(LI).append("<a href =").append(con.getUrlLargeString()).
                                append(">").append(con.getTitle()).append("</a></li>");
                        resourcesFormated.add(cont);
                    }
                    toRet.put("resources", resourcesFormated);
                    content = content.replaceAll(RESOURCES, resources.toString());
                    contentMail = contentMail.replaceAll(RESOURCES, resources.toString());
                    toRet.put("richText",content);
                    toRet.put("richTextMail",contentMail);
                //Campaign in Idea Collection Stage
                } else if (stage == null) {
                    return toRet;
                } else if (stage.equals(ComponentTypes.IDEAS)) {
                    File file = Play.application().getFile(NEWSLETTER_WITH_ACTIVITY_TEMPLATE_NAME);
                    File fileMail = Play.application().getFile(NEWSLETTER_WITH_ACTIVITY_TEMPLATE_NAME_MAIL);
                    String content = new String(Files.readAllBytes(Paths.get(file.toString())));
                    String contentMail = new String(Files.readAllBytes(Paths.get(fileMail.toString())));
                    content = content.replaceAll(CAMPAIGN_NAME, campaign.getTitle()).replaceAll(CAMPAIGN_DESCRIPTION,
                            campaign.getGoal()).replaceAll(DATE, week).replaceAll(UNSUSCRIBE_URL, unsuscribeUrl);
                    contentMail = contentMail.replaceAll(CAMPAIGN_NAME, campaign.getTitle()).replaceAll(CAMPAIGN_DESCRIPTION,
                            campaign.getGoal()).replaceAll(DATE, week).replaceAll(UNSUSCRIBE_URL, unsuscribeUrl)
                            .replace("{{DATE_TEXT}}",
                            Messages.get(Lang.forCode(lang), "mail.notification.date_text"))
                            .replace("{{VISIT_BUTTON_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.unsubscribe"))
                            .replace("{{YEAR}}", year)
                            .replace("{{NEW_IDEAS_NUMBER_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.new_ideas_number_text"))
                            .replace("{{CAMPAIGN_DESCRIPTION_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.campaign_description_text.no_activity"))
                            .replace("{{UPDATES_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.updates_text"))
                            .replace("{{UPDATES_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.updates_text"));
                    List<Contribution> contributions = Contribution.findLatestContributionIdeas(campaign.getResources(),
                            newsletterFrequency);
                    List<Map<String, Object>> contributionsFormated = new ArrayList<>();
                    StringBuilder contributionsString = new StringBuilder();
                    for(Contribution con: contributions) {
                        Map<String, Object> cont = new HashMap<>();
                        cont.put("ideaTitle", con.getTitle());
                        cont.put("user", con.getFirstAuthorName());
                        contributionsFormated.add(cont);
                        contributionsString.append(LI).append(con.getTitle())
                                .append(" submitted by ").append(con.getFirstAuthorName())
                                .append("</li>");

                    }
                    toRet.put("newIdeas", contributionsFormated);
                    content = content.replaceAll(NEW_IDEAS_NUMBER, String.valueOf(contributionsFormated.size()))
                            .replaceAll(NEW_IDEAS_TEXT, contributionsString.toString());
                    contentMail = contentMail.replaceAll(NEW_IDEAS_NUMBER, String.valueOf(contributionsFormated.size()))
                            .replaceAll(NEW_IDEAS_TEXT, contributionsString.toString());
                  List<NotificationEventSignal> updatedIdeas = NotificationEventSignal
                            .findLatestIdeasByOriginUuid(spaceID.toString(), newsletterFrequency);
                    List<String> updatedIdeasFormat = updatedIdeas.stream().map(NotificationEventSignal
                            ::getTitle)
                            .collect(Collectors.toList());
                    toRet.put("updatedIdeas", updatedIdeasFormat);
                    StringBuilder updatesString = new StringBuilder();
                    for(String update: updatedIdeasFormat) {
                        updatesString.append(LI).append(update).append("</li>");
                    }
                    content = content.replaceAll(UPDATES, updatesString.toString());
                    contentMail = contentMail.replaceAll(UPDATES, updatesString.toString());
                    toRet.put("richText", content);
                    toRet.put("richTextMail", contentMail);
                    //Campaign in Proposal Stage
                } else if(stage.equals(ComponentTypes.PROPOSALS)) {
                    File file = Play.application().getFile(NEWSLETTER_PROPOSAL_TEMPLATE_NAME);
                    File fileMail = Play.application().getFile(NEWSLETTER_PROPOSAL_TEMPLATE_NAME_MAIL);
                    String content = new String(Files.readAllBytes(Paths.get(file.toString())));
                    String contentMail = new String(Files.readAllBytes(Paths.get(fileMail.toString())));
                    content = content.replaceAll(CAMPAIGN_NAME,campaign.getTitle()).replaceAll(DATE, week)
                            .replaceAll(UNSUSCRIBE_URL, unsuscribeUrl);
                    contentMail = contentMail.replaceAll(CAMPAIGN_NAME,campaign.getTitle()).replaceAll(DATE, week)
                            .replaceAll(UNSUSCRIBE_URL, unsuscribeUrl)
                            .replace("{{DATE_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.date_text"))
                            .replace("{{VISIT_BUTTON_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.unsubscribe"))
                            .replace("{{YEAR}}", year)
                            .replace("{{CAMPAIGN_DESCRIPTION_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.date_text"));
                    List<Map<String, Object>> contributionsFormated = new ArrayList<>();
                    List<Map<String, Object>> developingProposals = new ArrayList<>();
                    List<String> updates = new ArrayList<>();
                    StringBuilder contributionsString = new StringBuilder();
                    StringBuilder developingString = new StringBuilder();
                    for (WorkingGroup wg: campaign.getWorkingGroups()) {
                        if (wg.getMembers().stream()
                                .anyMatch(t -> t.getUser().getUserId().equals(user.getUserId()))) {
                            toRet.put("workingGroupName", wg.getName());
                            content = content.replaceAll(WORKING_GROUPS, wg.getName());

                            for(Contribution proposal: wg.getProposals()) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.DAY_OF_MONTH, - newsletterFrequency);

                                if (proposal.getCreation().after(calendar.getTime())) {
                                    Map<String, Object> cont = new HashMap<>();
                                    cont.put("proposalTitle", proposal.getTitle());
                                    cont.put("user", proposal.getFirstAuthorName());
                                    contributionsFormated.add(cont);
                                    contributionsString.append(LI).append(proposal.getTitle())
                                            .append(" submitted by ").append(proposal.getFirstAuthorName())
                                            .append("</li>");
                                }
                                if (proposal.getStatus().equals(ContributionStatus.DRAFT)) {
                                    Map<String, Object> prop = new HashMap<>();
                                    prop.put("proposalTitle", proposal.getTitle());
                                    prop.put("user", proposal.getFirstAuthorName());
                                    developingProposals.add(prop);
                                    developingString
                                            .append(LI)
                                            .append(proposal.getTitle())
                                            .append(" submitted by ").append(proposal.getFirstAuthorName())
                                            .append("</li>");
                                }
                            }
                            toRet.put("newProposals", contributionsFormated);
                            toRet.put("developingProposals", developingProposals);
                            content = content.replaceAll("PROPOSAL_NUMBER",
                                        String.valueOf(contributionsFormated.size()))
                                    .replaceAll(PROPOSAL_NEW, contributionsString.toString())
                                    .replaceAll(PROPOSAL_DEVELOPING, developingString.toString());
                            contentMail = contentMail.replaceAll("PROPOSAL_NUMBER",
                                    String.valueOf(contributionsFormated.size()))
                                    .replaceAll(PROPOSAL_NEW, contributionsString.toString())
                                    .replaceAll(PROPOSAL_DEVELOPING, developingString.toString());
                            contentMail = contentMail.replace("{{CAMPAIGN_DESCRIPTION_TEXT}}",
                                    Messages.get(Lang.forCode(lang), "mail.notification.new_proposal_text",
                                    wg.getName(), String.valueOf(contributionsFormated.size())))
                                    .replace("{{UPDATES_TEXT}}", Messages.get(Lang.forCode(lang),
                                    "mail.notification.proposal_developing_text", wg.getName()));

                            break;
                        } else {
                            updates.addAll(NotificationEventSignal
                                    .findLatesWGtBySpaceUuid(wg.getUuid().toString(), newsletterFrequency)
                                    .stream().map(NotificationEventSignal::getTitle).collect(Collectors.toList()));
                        }

                    }
                    StringBuilder updatesString = new StringBuilder();
                    for(String update: updates) {
                        updatesString
                                .append(LI)
                                .append(update).append("</li>");
                    }
                    content = content.replaceAll(UPDATES, updatesString.toString());
                    contentMail = contentMail.replaceAll(UPDATES, updatesString.toString());
                    toRet.put("richText", content);
                    toRet.put("richTextMail", contentMail);
                    toRet.put("updatedWG", updates);
                }
                break;
            default:
                break;
        }
        return toRet;
    }

    /**
     * Create a first set of signals for the user who is subscribing to a space
     * @param sub subscription
     */
    public static void initializeUserSignals(Subscription sub) {

        String spaceUUID = sub.getSpaceId();
        String origin = "";

        ResourceSpace rs = ResourceSpace.readByUUID(UUID.fromString(spaceUUID));

        switch (sub.getSpaceType()) {
            case ASSEMBLY:
                origin = rs.getAssemblyResources().getUuidAsString();
                break;
            case CAMPAIGN:
                origin = rs.getCampaign().getUuidAsString();
                break;
            case WORKING_GROUP:
                origin = rs.getWorkingGroupResources().getUuid().toString();
                break;
            case COMPONENT:
                origin = rs.getComponent().getUuid().toString();
                break;
            case CONTRIBUTION:
                origin = rs.getContribution().getUuidAsString();
                break;
//          TODO
//          case VOTING_BALLOT:
//                origin = ...
//                break;
        }

        try {
            // Read a list of N notification signals to initialize the notifications of the user
            Logger.info("Reading signals for origin ("+origin+") to initialize notifications for user...");
            List<NotificationEventSignal> initializationSignals = NotificationEventSignal
                    .findLastNtByOriginUuidWithFilteredEvents
                            (origin, sub.getSubscriptionType().name(),
                                    INITIAL_LIST_OF_SIGNALS_FOR_NEW_SUBSCRIBER, filteredEvents);

            // Create an initial set of signals for the user
            Logger.info("Found "+(initializationSignals!=null ? initializationSignals.size() : 0)+" signals for origin ("+origin+")");
            for (NotificationEventSignal signal: initializationSignals) {
                NotificationEventSignalUser signalUser = new NotificationEventSignalUser(User.findByUUID(UUID.fromString(sub.getUserId())),signal);
                signalUser.insert();
            }
        } catch (Exception e) {
            Logger.debug("Exception initializing signals for user...");
            Logger.debug(e.getMessage());
        }
    }
}