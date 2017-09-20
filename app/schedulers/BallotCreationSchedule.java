package schedulers;

import akka.actor.ActorSystem;
import enums.*;
import models.*;
import scala.concurrent.ExecutionContext;
import utils.GlobalData;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

        this.initialize(15, 7, "Ballot Creation");
    }


    @Override
    public void executeProcess() {
        System.out.println("Executing ballot");

        //Get Components of type voting with starting day = today

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(new Date());
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(new Date());
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);

        System.out.println("Searching Component Start day between: "+ calStart.getTime() + " and " +calEnd.getTime() );

        List<Component> components = Component.findVotingByStartingDay(calStart.getTime(), calEnd.getTime());
        System.out.println("Found "+ components.size() + " COMPONENT to create Voting ballots");

        //Find all campaigns related and create ballot
        for (Component component : components) {
            for (ResourceSpace spaces : component.getContainingSpaces()) {
                if (spaces.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {


                    Campaign campaign = spaces.getCampaign();

                    System.out.println("Creating ballot for campaing:"+ campaign.getCampaignId());


                    //If campaign config campaign.include.all.published.proposals === TRUE,
                    // change status of PUBLISHED to INBALLOT before creating ballot.
                    Config publisheProposal = spaces
                            .getConfigByKey(
                                    GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM);

                    //Campaign related, creating Ballot
                    // 6. Create a decision ballot associated with this component and add it to the campaign
                    Ballot ballot = new Ballot();
                    Date startBallot = component.getStartDate();
                    Date endBallot = component.getEndDate();

                    // if component has no start date, use now
                    startBallot = startBallot != null ? startBallot : Calendar.getInstance().getTime();

                    // if component has no end date, use 30 days after startDate
                    Calendar c = Calendar.getInstance();
                    c.setTime(startBallot);
                    c.add(Calendar.DATE, 30);
                    endBallot = endBallot != null ? endBallot : c.getTime();

                    ballot.setStartsAt(startBallot);
                    ballot.setEndsAt(endBallot);
                    ballot.setPassword(campaign.getUuidAsString());


                    Config publishedProposal = component.getResourceSpace()
                            .getConfigByKey(
                                    GlobalData.CAMPAIGN_INCLUDE_PUBLISHED_PROPOSAL);
                    Boolean published = false;
                    if (publishedProposal != null && Boolean.valueOf(publishedProposal.getValue())) {
                        //Update Campaign status or ballot
                        //Create a BallotCandidate for ever Contribution.type = PROPOSAL
                        published = true;
                    }

                    //Creating candidates
                    createBallotCandidates(campaign,ballot,published);


                    Config votingSystemConfig = component.getResourceSpace()
                            .getConfigByKey(
                                    GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM);

                    VotingSystemTypes vtype = votingSystemConfig != null ? VotingSystemTypes
                            .valueOf(votingSystemConfig.getValue())
                            : VotingSystemTypes.PLURALITY;
                    ballot.setVotingSystemType(vtype);
                    ballot.setRequireRegistration(false);
                    ballot.setUserUuidAsSignature(true);
                    ballot.setDecisionType("BINDING");
                    ballot.setComponent(component);
                    ballot.save();
                    ballot.refresh();
                    campaign.setCurrentBallotAsString(ballot.getUuid().toString());
                    campaign.getResources().addBallot(ballot);

                    //Adding configurations to ballot
                    addBallotConfigurations(component.getConfigs(),ballot);



                }
            }
        }
    }

    private void createBallotCandidates(Campaign campaign, Ballot ballot, Boolean publishedProposal) {
        List<Contribution> contributions = campaign.getContributions();
        Boolean hasCandidates = false;
        for(Contribution c : contributions){
            if(c.getType()!=null && c.getType().equals(ContributionTypes.PROPOSAL)){
                //if config campaign.include.all.published.proposals === TRUE,
                // change status of PUBLISHED to INBALLOT
                hasCandidates=true;
                System.out.println("Creating BallotCandidate for Contribution "+ c.getTitle() + "=="+ c.getContributionId() );
                if(publishedProposal){
                    c.setStatus(ContributionStatus.INBALLOT);
                    c.update();
                }

                //Creating candidate
                BallotCandidate candidate = new BallotCandidate();
                candidate.setBallotId(ballot.getId());
                candidate.setCandidateType(BallotCandidateTypes.CAMPAIGN);
                candidate.setCandidateUuid(c.getUuid());
                candidate.insert();

            }
        }
        //If ballot has not candidates, then set status to draft
        if(!hasCandidates){
            ballot.setStatus(BallotStatus.DRAFT);
        }
    }

    public void addBallotConfigurations(List<Config> configs ,  Ballot ballot){
        // Add Ballot configurations
        for (Config config : configs) {
            BallotConfiguration ballotConfig = new BallotConfiguration();
            ballotConfig.setBallotId(ballot.getId());
            ballotConfig.setKey(config.getKey());
            ballotConfig.setValue(config.getValue());
            ballotConfig.save();
            if (config.getKey().equals("component.voting.ballot.password")) {
                BallotRegistrationField brf = new BallotRegistrationField();
                brf.setBallotId(ballot.getId());
                brf.setDescription("The password used by non-users to vote on proposals through the voting ballot");
                brf.setExpectedValue(config.getValue());
                brf.setName("Ballot Password");
                brf.setPosition(0);
                brf.save();
                ballot.setPassword(config.getValue());
                ballot.update();
            }
        }

    }
}
