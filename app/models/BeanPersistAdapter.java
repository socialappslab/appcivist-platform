package models;

import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistRequest;
import delegates.NotificationsDelegate;
import enums.NotificationEventName;
import enums.ResourceSpaceTypes;
import play.libs.F;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by ggaona on 29/8/17.
 */
public class BeanPersistAdapter implements BeanPersistController {


    @Override
    public int getExecutionOrder() {
        return 0;
    }

    @Override
    public boolean isRegisterFor(Class<?> cls) {
        return true;
    }

    @Override
    public boolean preInsert(BeanPersistRequest<?> request) {
        return true;
    }

    @Override
    public boolean preUpdate(BeanPersistRequest<?> request) {
        return true;
    }

    @Override
    public boolean preDelete(BeanPersistRequest<?> request) {
        return true;
    }

    @Override
    public void postInsert(BeanPersistRequest<?> request) {

        //System.out.println("=== POST INSERT CALL == " + request.getBean().getClass() );
        //System.out.println(" == INSTANCE OF CAMPAIGN: " + (request.getBean() instanceof Campaign) );
        //CAMPAIGN
        if (request.getBean() instanceof Campaign) {
            this.notifyCampaign(request, NotificationEventName.NEW_CAMPAIGN);

        }

        if (request.getBean() instanceof WorkingGroup) {
            this.notifyWorkingGroup(request, NotificationEventName.NEW_WORKING_GROUP);

        }
        if (request.getBean() instanceof BallotVote) {
            this.notifyBallotVote(request, NotificationEventName.NEW_VOTING_BALLOT);
        }

        if (request.getBean() instanceof ComponentMilestone) {
            this.notifyMilestone(request, NotificationEventName.NEW_MILESTONE);
        }

        if (request.getBean() instanceof Contribution) {
            Contribution c = (Contribution) request.getBean();
            this.notifyContribution(request, NotificationsDelegate.getNewContributionEventName(c));
        }

        if (request.getBean() instanceof ContributionFeedback) {
            this.notifyContributionFeedback(request, NotificationEventName.NEW_CONTRIBUTION_FEEDBACK);
        }

        if (request.getBean() instanceof ContributionHistory) {
            this.notifyContributionHistory(request, NotificationEventName.UPDATED_CONTRIBUTION_HISTORY);
        }

        if (request.getBean() instanceof Membership) {
            this.notifyMemberShip(request, NotificationEventName.MEMBER_JOINED);
        }


    }


    @Override
    public void postUpdate(BeanPersistRequest<?> request) {
        //CAMPAIGN
        if (request.getBean() instanceof Campaign) {
            this.notifyCampaign(request, NotificationEventName.UPDATED_CAMPAIGN);

        }

        if (request.getBean() instanceof WorkingGroup) {
            this.notifyWorkingGroup(request, NotificationEventName.UPDATED_WORKING_GROUP);

        }

        if (request.getBean() instanceof BallotVote) {
            this.notifyBallotVote(request, NotificationEventName.UPDATED_VOTING_BALLOT);
        }

        if (request.getBean() instanceof ComponentMilestone) {
            this.notifyMilestone(request, NotificationEventName.UPDATED_MILESTONE);
        }

        if (request.getBean() instanceof Contribution) {
            Contribution c = (Contribution) request.getBean();
            this.notifyContribution(request, NotificationsDelegate.getUpdatedContributionEventName(c));
        }

        if (request.getBean() instanceof ContributionFeedback) {
            this.notifyContributionFeedback(request, NotificationEventName.UPDATED_CONTRIBUTION_FEEDBACK);
        }

    }

    private void notifyContributionFeedback(BeanPersistRequest<?> request, NotificationEventName eventName) {
        ContributionFeedback feedback = (ContributionFeedback) request.getBean();
        Contribution c = Contribution.read(feedback.getContributionId());
        for (Long campId : c.getCampaignIds()) {
            Campaign campaign = Campaign.read(campId);
            NotificationsDelegate.signalNotification(ResourceSpaceTypes.CAMPAIGN, eventName, campaign, feedback);
        }

    }

    private void notifyContribution(BeanPersistRequest<?> request, NotificationEventName newContributionEventName) {
        Contribution c = (Contribution) request.getBean();
        for (ResourceSpace r : c.getContainingSpaces()) {
            NotificationsDelegate.signalNotification(r.getType(), newContributionEventName, r, c);
        }
    }

    private void notifyContributionHistory(BeanPersistRequest<?> request, NotificationEventName updatedContributionHistory) {
        ContributionHistory history = (ContributionHistory) request.getBean();


        for (ResourceSpace r : history.getRelatedResourceSpaces()) {

            NotificationsDelegate.signalNotification(
                    r.getType(),
                    updatedContributionHistory,
                    r,
                    history);

        }


    }


    /**
     * Whenever an update or created milestone
     *
     * @param request
     * @param updatedMilestone
     */
    private void notifyMilestone(BeanPersistRequest<?> request, NotificationEventName updatedMilestone) {
        ComponentMilestone entity = (ComponentMilestone) request.getBean();
        for (ResourceSpace c : entity.getContainingSpaces()) {
            NotificationsDelegate.signalNotification(c.getType(), updatedMilestone, c, entity);

        }

    }

    private void notifyBallotVote(BeanPersistRequest<?> request, NotificationEventName newVotingBallot) {
        Ballot entity = (Ballot) request.getBean();
        List<Campaign> campaigns = Campaign.findByCurrentBallotUUID(entity.getUuid());
        System.out.println("=== NOTIFICATION FOR == " + newVotingBallot+ " CAMPAINGS: " + campaigns.size());

        for (Campaign c : campaigns) {
            NotificationsDelegate.signalNotification(ResourceSpaceTypes.CAMPAIGN, newVotingBallot, c, c);

        }

    }

    /**
     * Working Group Notification
     *
     * @param request
     * @param eventName
     */
    private void notifyWorkingGroup(BeanPersistRequest<?> request, NotificationEventName eventName) {
        WorkingGroup entity = (WorkingGroup) request.getBean();

        for (ResourceSpace s : entity.getContainingSpaces()) {
            switch (s.getType()) {
                case ASSEMBLY:
                    NotificationsDelegate.signalNotification(s.getType(), eventName, s.getAssemblyResources(), entity);
                    break;
                case CAMPAIGN:
                    NotificationsDelegate.signalNotification(s.getType(), eventName, s.getCampaign(), entity);
                    break;
            }
        }

    }

    /**
     * New Campaign event
     *
     * @param request
     */
    private void notifyCampaign(BeanPersistRequest<?> request, NotificationEventName eventName) {
        Campaign entity = (Campaign) request.getBean();

        //List<Assembly> assemblies = Assembly.findAssemblyFromCampaign(entity.getUuid());

        for (ResourceSpace rs  : entity.getContainingSpaces()) {
            System.out.println("=== NotificationsDelegate FOR == " + rs.getAssemblies().size() + " " + rs.getResourceSpaceId()  + " " + rs.getAssemblyResources());


                NotificationsDelegate.signalNotification(rs.getType(), eventName, rs.getAssemblyResources(), entity);

        }

    }

    private void notifyMemberShip(BeanPersistRequest<?> request, NotificationEventName eventName){

        System.out.println("=== ADAPTER notifyMemberShip == " + request.getBean().getClass());

        Membership m = (Membership) request.getBean();
        if(m.getTargetAssembly() != null){
            NotificationsDelegate.signalNotification(ResourceSpaceTypes.ASSEMBLY, eventName, m.getTargetAssembly(), m);
        }
        if(m.getTargetGroup() != null){
            NotificationsDelegate.signalNotification(ResourceSpaceTypes.WORKING_GROUP, eventName, m.getTargetGroup(), m);
        }

    }


    @Override
    public void postDelete(BeanPersistRequest<?> request) {

    }

    @Override
    public void postLoad(Object bean, Set<String> includedProperties) {

    }
}
