package schedulers;

import akka.actor.ActorSystem;
import delegates.NotificationsDelegate;
import enums.SpaceTypes;
import enums.SubscriptionTypes;
import exceptions.ConfigurationException;
import models.Campaign;
import models.Subscription;
import play.Logger;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

/**
 * Created by yohanna on 28/10/17.
 */
public class NewsletterCreatorSchedule extends DailySchedule {

    @Inject
    public NewsletterCreatorSchedule(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.initialize(18, 53, "NewsletterCreator");
    }

    @Override
    public void executeProcess() {

        List<Subscription> subscriptionList = Subscription.findBySubscriptionAndSpaceType(SubscriptionTypes.NEWSLETTER,
                SpaceTypes.CAMPAIGN, SpaceTypes.IDEA);
        for (Subscription sub : subscriptionList) {
            String spaceId = sub.getSpaceId();
            Boolean newRequired = NotificationsDelegate.checkIfNewNewsletterIsRequired(spaceId);
            if (newRequired) {
                Campaign campaign = Campaign.readByUUID(UUID.fromString(spaceId));
                try {
                    NotificationsDelegate.newNewsletterInCampaign(campaign, UUID.fromString(sub.getUserId()));
                } catch (ConfigurationException e) {
                    Logger.error("Error creating newsletter", e);
                }
            }

        }

    }
}
