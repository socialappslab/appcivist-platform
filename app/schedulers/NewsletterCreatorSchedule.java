package schedulers;

import akka.actor.ActorSystem;
import delegates.NotificationsDelegate;
import enums.ResourceSpaceTypes;
import enums.SubscriptionTypes;
import exceptions.ConfigurationException;
import models.Campaign;
import models.ResourceSpace;
import models.Subscription;
import models.WorkingGroup;
import play.Logger;
import providers.MyUsernamePasswordAuthProvider;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

/**
 * Automatically create newsletter from {@link Campaign} or {@link WorkingGroup}
 * each 7 or configured amount of days.
 * Created by yohanna on 28/10/17.
 */
public class NewsletterCreatorSchedule extends DailySchedule {

    @Inject
    public NewsletterCreatorSchedule(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize(getConfigOrElse("appcivist.schedule.newsletter.hour", 16),
                getConfigOrElse("appcivist.schedule.newsletter.minute", 0),
                "NewsletterCreator");
    }

    @Override
    public void executeProcess() {

        Logger.info("NOTIFICATION SCHEDULER ");
        List<Subscription> subscriptionList = Subscription.findBySubscriptionAndSpaceType(SubscriptionTypes.NEWSLETTER,
                ResourceSpaceTypes.CAMPAIGN, ResourceSpaceTypes.WORKING_GROUP);
        for (Subscription sub : subscriptionList) {
            String spaceId = sub.getSpaceId();
            Boolean newRequired = NotificationsDelegate.checkIfNewNewsletterIsRequired(spaceId);
            if (newRequired) {
                try {
                    Logger.info("Newsletter Creator Scheduler for uuid " +spaceId);
                    if(sub.getSpaceType().equals(ResourceSpaceTypes.CAMPAIGN)) {
                        ResourceSpace rs = ResourceSpace.readByUUID(UUID.fromString(spaceId));
                        Campaign campaign = rs.getCampaign();
                        NotificationsDelegate.newNewsletterInCampaign(campaign, UUID.fromString(sub.getUserId()));
                    } else {
                        ResourceSpace rs = ResourceSpace.readByUUID(UUID.fromString(spaceId));
                        WorkingGroup workingGroup = rs.getWorkingGroupResources();
                        if(workingGroup != null) {
                            NotificationsDelegate.newNewsletterInWorkingGroup(workingGroup, UUID.fromString(sub.getUserId()));
                        }
                    }
                } catch (Exception e) {
                    Logger.error("Error creating newsletter", e);
                }
            }

        }

    }
}
