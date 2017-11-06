package schedulers;

import akka.actor.ActorSystem;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by ggaona on 19/9/17.
 */
public abstract class DailySchedule {

    ActorSystem actorSystem;
    ExecutionContext executionContext;


    public void initialize(Integer hour, Integer minute, String processName) {

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(new Date());
        calStart.set(Calendar.HOUR_OF_DAY, hour);
        calStart.set(Calendar.MINUTE, minute);
        calStart.set(Calendar.SECOND, 0);


        Date startDate = calStart.getTime();
        Date now = new Date();

        Long delay = startDate.getTime() - now.getTime();

        if (delay < 0) {
            calStart.add(Calendar.DATE, 1);

            delay = calStart.getTime().getTime() - now.getTime();
        }

        delay = TimeUnit.MILLISECONDS.toMinutes(delay);

        System.out.println("Calendarized process " + processName + " at:"
                + calStart.get(Calendar.HOUR) + ":"
                + calStart.get(Calendar.MINUTE)
                + " Delay: " + delay);


        this.actorSystem.scheduler().schedule(
                Duration.create(delay, TimeUnit.MINUTES), // initialDelay
                //Duration.create(1, TimeUnit.SECONDS), // initialDelay
                Duration.create(1, TimeUnit.DAYS), // interval
                () -> {
                    this.executeProcess();
                },
                this.executionContext
        );
    }

    public abstract void executeProcess();

    int getConfigOrElse(String key, int def) {
        com.typesafe.config.Config config = ConfigFactory.load();
        try {
            return config.getInt(key);
        } catch (ConfigException.Missing e) {
            return def;
        }
    }
}
