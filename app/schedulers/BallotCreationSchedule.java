package schedulers;

import akka.actor.ActorSystem;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;

/**
 * Automatically create the ballot and its candidates when the voting stage starts, putting
 * as candidates contributions with status INBALLOT.
 * If campaign config campaign.include.all.published.proposals === TRUE,
 * change status of PUBLISHED to INBALLOT before creating ballot.
 * Created by ggaona on 19/9/17.
 */
public class BallotCreationSchedule extends DailySchedule {


    @Inject
    public BallotCreationSchedule(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.initialize(22, 50, "Ballot Creation");
    }



    @Override
    public void executeProcess() {
        System.out.println("Executing ballot");
    }
}
