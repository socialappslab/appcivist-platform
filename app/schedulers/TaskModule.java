package schedulers;

import com.google.inject.AbstractModule;
import play.Logger;

/**
 * Created by ggaona on 12/9/17.
 */
public class TaskModule extends AbstractModule {

    @Override
    protected void configure() {
        Logger.info("Configuring schedulers.TaskModule...");
        bind(MilestoneNotificationSchedule.class).asEagerSingleton();
        bind(VotingBallotsNotificationSchedule.class).asEagerSingleton();
        bind(BallotCreationSchedule.class).asEagerSingleton();

    }
}