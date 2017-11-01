package schedulers;

import akka.actor.ActorSystem;
import delegates.NotificationsDelegate;
import enums.NotificationEventName;
import enums.ResourceSpaceTypes;
import models.Ballot;
import models.Campaign;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Schedule process that create signals according to:
 * voting ballots that end in one month
 * voting ballots that end in one week
 * voting ballots that end in one day
 * <p>
 * Created by ggaona on 12/9/17.
 */
public class VotingBallotsNotificationSchedule extends DailySchedule {


    @Inject
    public VotingBallotsNotificationSchedule(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialize(
                getConfigOrElse("appcivist.schedule.votingBallot.hour",9),
                getConfigOrElse("appcivist.schedule.votingBallot.minute",0),
                "Voting Ballots Notification");
    }


    /**
     * Find ballots and create notifications
     */
    public void executeProcess() {
        System.out.println("Calendarized Voting Ballot notifications ");
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(new Date());
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.add(Calendar.DATE, 1);

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(new Date());
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.add(Calendar.DATE, 1);

        List<Campaign> oneDayStart = currentBallotStart(calStart.getTime(), calEnd.getTime());
        List<Campaign> oneDayEnd = currentBallotEnd(calStart.getTime(), calEnd.getTime());

        System.out.println("Start date: " + calStart.getTime().toString()
                + " End: " + calEnd.getTime().toString()
                + " Start Found: " + oneDayStart.size()
                + " End Found:" + oneDayEnd.size());


        //Move 1 week later
        calStart.add(Calendar.DATE, 6);
        calEnd.add(Calendar.DATE, 6);


        List<Campaign> oneWeekStart = currentBallotStart(calStart.getTime(), calEnd.getTime());
        List<Campaign> oneWeekEnd = currentBallotEnd(calStart.getTime(), calEnd.getTime());

        System.out.println("Start oneWeek: " + calStart.getTime().toString()
                + " End: " + calEnd.getTime().toString()
                + " Start Found: " + oneWeekStart.size()
                + " End Found:" + oneWeekEnd.size());

        //Move 2 month later
        //Move 1 week later
        calStart.add(Calendar.DATE, 23);
        calEnd.add(Calendar.DATE, 23);

        List<Campaign> oneMonthStart = currentBallotStart(calStart.getTime(), calEnd.getTime());
        List<Campaign> oneMonthEnd = currentBallotEnd(calStart.getTime(), calEnd.getTime());

        System.out.println("Start oneMONTH: " + calStart.getTime().toString()
                + " End: " + calEnd.getTime().toString()
                + " Start Found: " + oneMonthStart.size()
                + " End Found:" + oneMonthEnd.size());

        this.createNotifications(oneDayStart, NotificationEventName.BALLOT_UPCOMING_IN_A_DAY);
        this.createNotifications(oneDayEnd, NotificationEventName.BALLOT_ENDING_IN_A_DAY);


        this.createNotifications(oneWeekStart, NotificationEventName.BALLOT_UPCOMING_IN_A_WEEK);
        this.createNotifications(oneWeekEnd, NotificationEventName.BALLOT_ENDING_IN_A_WEEK);


        this.createNotifications(oneMonthStart, NotificationEventName.BALLOT_UPCOMING_IN_A_MONTH);
        this.createNotifications(oneMonthEnd, NotificationEventName.BALLOT_ENDING_IN_A_MONTH);

    }

    public void createNotifications(List<Campaign> campaigns, NotificationEventName eventName) {
        for (Campaign campaign : campaigns) {
            //System.out.println("Parent of: " + mile.getUuidAsString() + " is " + parent.getType());

            NotificationsDelegate.signalNotification(ResourceSpaceTypes.CAMPAIGN,
                    eventName, campaign, campaign);
        }
    }

    public List<Campaign> currentBallotStart(Date start, Date end) {
        List<Ballot> ballots = Ballot.getBalltoByDateStart(start, end);
        List<Campaign> campaigns = new ArrayList<>();
        for (Ballot b : ballots) {
            List<Campaign> c = Campaign.findByCurrentBallotUUID(b.getUuid());
            campaigns.addAll(c);
        }
        return campaigns;
    }

    public List<Campaign> currentBallotEnd(Date start, Date end) {
        List<Ballot> ballots = Ballot.getBalltoByDateEnd(start, end);
        List<Campaign> campaigns = new ArrayList<>();
        for (Ballot b : ballots) {
            List<Campaign> c = Campaign.findByCurrentBallotUUID(b.getUuid());
            campaigns.addAll(c);
        }
        return campaigns;
    }


}
