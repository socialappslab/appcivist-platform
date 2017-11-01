package schedulers;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import delegates.NotificationsDelegate;
import enums.SpaceTypes;
import enums.SubscriptionTypes;
import exceptions.ConfigurationException;
import models.Campaign;
import models.Subscription;
import models.WorkingGroup;
import play.Logger;
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
        Config config = ConfigFactory.load();
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        int hour, minute;
        try {
            hour = config.getInt("appcivist.schedule.newsletter.hour");
            minute = config.getInt("appcivist.schedule.newsletter.minute");
        } catch (ConfigException.Missing e) {
            hour = 16;
            minute = 0;
        }
        this.initialize(hour, minute, "NewsletterCreator");
    }

    @Override
    public void executeProcess() {

        List<Subscription> subscriptionList = Subscription.findBySubscriptionAndSpaceType(SubscriptionTypes.NEWSLETTER,
                SpaceTypes.CAMPAIGN, SpaceTypes.WORKING_GROUP);
        for (Subscription sub : subscriptionList) {
            String spaceId = sub.getSpaceId();
            Boolean newRequired = NotificationsDelegate.checkIfNewNewsletterIsRequired(spaceId);
            if (newRequired) {
                try {
                    Logger.info("Newsletter Creator Scheduler for uuid " +spaceId);
                    if(sub.getSpaceType().equals(SpaceTypes.CAMPAIGN)) {
                        Campaign campaign = Campaign.readByUUID(UUID.fromString(spaceId));
                        NotificationsDelegate.newNewsletterInCampaign(campaign, UUID.fromString(sub.getUserId()));
                    } else {
                        WorkingGroup workingGroup = WorkingGroup.readByUUID(UUID.fromString(spaceId));
                        NotificationsDelegate.newNewsletterInWorkingGroup(workingGroup, UUID.fromString(sub.getUserId()));
                    }
                } catch (ConfigurationException e) {
                    Logger.error("Error creating newsletter", e);
                }
            }

        }

    }
}
