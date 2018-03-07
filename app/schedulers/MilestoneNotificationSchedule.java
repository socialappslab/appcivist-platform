package schedulers;

import akka.actor.ActorSystem;
import delegates.NotificationsDelegate;
import enums.NotificationEventName;
import enums.ResourceSpaceTypes;
import enums.SubscriptionTypes;
import models.ComponentMilestone;
import models.ResourceSpace;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Schedule process that create signals according to:
 * Milestones that are due in one week
 * Milestones that are due in one day
 * <p>
 * Created by ggaona on 12/9/17.
 */
public class MilestoneNotificationSchedule extends DailySchedule {


    @Inject
    public MilestoneNotificationSchedule(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize(
                getConfigOrElse("appcivist.schedule.milestoneNotification.hour",22),
                getConfigOrElse("appcivist.schedule.milestoneNotification.minute",0),
                "MilestoneNotification");
    }


    /**
     * Find milestone
     */
    public void executeProcess() {
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(new Date());
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.add(Calendar.DATE, 1);
        Date startDate = calStart.getTime();

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(new Date());
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.add(Calendar.DATE, 1);
        Date endDate = calEnd.getTime();

        List<ComponentMilestone> oneDay = ComponentMilestone.getMilestoneByDate(startDate, endDate);
        System.out.println("Start date" + startDate.toString() + " End: " + endDate.toString() + " Found: " + oneDay.size());


        //Move 1 week later
        calStart.add(Calendar.DATE, 6);
        calEnd.add(Calendar.DATE, 6);


        List<ComponentMilestone> oneWeek = ComponentMilestone.getMilestoneByDate(calStart.getTime(), calEnd.getTime());

        this.createNotifications(oneDay, NotificationEventName.MILESTONE_UPCOMING_IN_A_DAY);
        this.createNotifications(oneWeek, NotificationEventName.MILESTONE_UPCOMING_IN_A_WEEK);


    }

    public void createNotifications(List<ComponentMilestone> milestones, NotificationEventName eventName) {
        //Milestones that are due in one week
        List<Long> resourcesIds = new ArrayList<>();
        for (ComponentMilestone mile : milestones) {
            //Find Parent Campaign
            for (ResourceSpace space : mile.getContainingSpaces()) {
                if (space.getType().equals(ResourceSpaceTypes.COMPONENT)) {

                    for (ResourceSpace parent : space.getComponent().getContainingSpaces()) {
                        //signalNotification(ResourceSpaceTypes originType, NotificationEventName eventName,
                        //models.AppCivistBaseModel origin, AppCivistBaseModel resource)
                        if(resourcesIds.contains(parent.getResourceSpaceId())) {
                            continue;
                        }
                        resourcesIds.add(parent.getResourceSpaceId());
                        System.out.println("Parent of: " + mile.getUuidAsString() + " is " + parent.getType());
                        NotificationsDelegate.signalNotification(parent.getType(),
                                eventName, parent.getCampaign(), mile, SubscriptionTypes.REGULAR, null);
                    }


                }
            }

        }
    }


}
