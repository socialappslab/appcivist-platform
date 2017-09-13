package schedulers;

import com.google.inject.AbstractModule;

/**
 * Created by ggaona on 12/9/17.
 */
public class TaskModule extends AbstractModule {

    @Override
    protected void configure() {
        System.out.println("Calendarized TaskModule");
        bind(MilestoneNotificationSchedule.class).asEagerSingleton();
    }
}