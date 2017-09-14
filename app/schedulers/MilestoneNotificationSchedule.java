package schedulers;

import akka.actor.ActorSystem;
import delegates.NotificationsDelegate;
import enums.NotificationEventName;
import enums.ResourceSpaceTypes;
import models.Campaign;
import models.ComponentMilestone;
import models.ResourceSpace;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Schedule process that create signals according to:
 * Milestones that are due in one week
 * Milestones that are due in one day
 * <p>
 * Created by ggaona on 12/9/17.
 */
public class MilestoneNotificationSchedule {

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    @Inject
    public MilestoneNotificationSchedule(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.initialize();
    }

    private void initialize() {

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(new Date());
        calStart.set(Calendar.HOUR_OF_DAY, 10);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);




        Date startDate = calStart.getTime();
        Date now = new Date();

        Long delay = startDate.getTime() - now.getTime();

        if (delay < 0) {
            calStart.add(Calendar.DATE, 1);

            delay = calStart.getTime().getTime() - now.getTime();
        }

        delay = TimeUnit.MILLISECONDS.toMinutes(delay);

        System.out.println("Calendarized milestone process at: "
                + calStart.get(Calendar.HOUR) + ":"
                + calStart.get(Calendar.MINUTE)
                + " Delay: " + delay);


        this.actorSystem.scheduler().schedule(
                Duration.create(delay, TimeUnit.MINUTES), // initialDelay
                //Duration.create(1, TimeUnit.SECONDS), // initialDelay
                Duration.create(1, TimeUnit.DAYS), // interval
                () -> {
                    this.signalMilestones();
                },
                this.executionContext
        );
    }

    /**
     * Find milestone
     */
    public void signalMilestones() {
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

        this.createNotifications(oneDay,NotificationEventName.MILESTONE_UPCOMING_IN_A_DAY);
        this.createNotifications(oneWeek,NotificationEventName.MILESTONE_UPCOMING_IN_A_WEEK);


    }

    public  void createNotifications(List<ComponentMilestone> milestones, NotificationEventName eventName){
        //Milestones that are due in one week
        for (ComponentMilestone mile : milestones) {
            //Find Parent Campaign
            for (ResourceSpace space : mile.getContainingSpaces()) {
                if (space.getType().equals(ResourceSpaceTypes.COMPONENT)) {

                    for (ResourceSpace parent : space.getComponent().getContainingSpaces()) {
                        //signalNotification(ResourceSpaceTypes originType, NotificationEventName eventName,
                        //models.AppCivistBaseModel origin, AppCivistBaseModel resource)
                        System.out.println("Parent of: " + mile.getUuidAsString() + " is " + parent.getType());
                        NotificationsDelegate.signalNotification(parent.getType(),
                                eventName, parent.getCampaign(), mile);
                    }


                }
            }

        }
    }


}
