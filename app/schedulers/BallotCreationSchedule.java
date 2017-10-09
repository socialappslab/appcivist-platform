package schedulers;

import akka.actor.ActorSystem;
import enums.*;
import models.*;
import play.Logger;
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

        Integer hour = 3;
        Integer minute = 0;
        String processName = "Ballot Creation";
        this.initialize(hour, minute, processName);
    }

    @Override
    public void executeProcess() {
        createBallot();
    }

    private void createBallot() {
        Logger.info("Executing ballot");

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

        Logger.info("Searching Component Start day between: "+ calStart.getTime() + " and " +calEnd.getTime() );

        List<Component> components = Component.findVotingByStartingDay(calStart.getTime(), calEnd.getTime());
        Logger.info("Found "+ components.size() + " COMPONENT to create Voting ballots");

        //Find all campaigns related and create ballot
        for (Component component : components) {
            for (ResourceSpace spaces : component.getContainingSpaces()) {
                if (spaces.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
                    Campaign campaign = spaces.getCampaign();
                    Logger.info("Creating ballot for campaing:" + campaign.getCampaignId());

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
                    ballot.setPassword(campaign.getShortname());

                    Config publishedProposal = component.getResourceSpace()
                            .getConfigByKey(
                                    GlobalData.CONFIG_CAMPAIGN_INCLUDE_PUBLISHED_PROPOSAL);
                    Boolean published = false;
                    if (publishedProposal != null && Boolean.valueOf(publishedProposal.getValue())) {
                        //Update Campaign status or ballot
                        //Create a BallotCandidate for ever Contribution.type = PROPOSAL
                        published = true;
                    }

                    Config ballotEntityType = component.getResourceSpace()
                            .getConfigByKey(
                                    GlobalData.CONFIG_COMPONENT_VOTING_BALLOT_ENTITY_TYPE);

                    String entityType = ballotEntityType != null ? ballotEntityType.getValue() : null;
                    ballot.setEntityType(entityType != null ? entityType : "PROPOSAL");

                    Config votingSystemConfig = component.getResourceSpace()
                            .getConfigByKey(
                                    GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM);

                    VotingSystemTypes vtype = votingSystemConfig != null ? VotingSystemTypes
                            .valueOf(votingSystemConfig.getValue())
                            : VotingSystemTypes.DISTRIBUTED;
                    String votesLimit = "5";
                    String votesLimitMeaning = "TOKENS";
                    if (vtype.equals(VotingSystemTypes.PLURALITY)) {
                        votesLimitMeaning = "SELECTIONS"; // user can give vote on up to 'votesLimit' candidates
                        Config votingVotesLimitConfig = component.getResourceSpace()
                                .getConfigByKey(
                                        GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM_PLURALITY_TYPE);
                        votesLimit = votingVotesLimitConfig != null ? votingVotesLimitConfig.getValue() : votesLimit;
                    } else if (vtype.equals(VotingSystemTypes.DISTRIBUTED)) {
                        votesLimitMeaning = "TOKENS"; // user can distribute up to 'votesLimit' points among candidates
                        Config votingVotesLimitConfig = component.getResourceSpace()
                                .getConfigByKey(
                                        GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM_DISTRIBUTED_POINTS);
                        votesLimit = votingVotesLimitConfig != null ? votingVotesLimitConfig.getValue() : votesLimit;
                    } else if (vtype.equals(VotingSystemTypes.RANGE)) {
                        votesLimitMeaning = "RANGE"; // user can assign scores to candidates in the range of 'votesLimit' (min-max)
                        Config votingVotesLimitConfig = component.getResourceSpace()
                                .getConfigByKey(
                                        GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM_RANGE_MAX_SCORE);
                        votesLimit = votingVotesLimitConfig != null ? votingVotesLimitConfig.getValue() : votesLimit;
                    } else if (vtype.equals(VotingSystemTypes.RANKED)) {
                        votesLimitMeaning = "SELECTIONS"; // user can give vote on up to 'votesLimit' candidates
                        Config votingVotesLimitConfig = component.getResourceSpace()
                                .getConfigByKey(
                                        GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM_RANKED_NUMBER_PROPOSALS);
                        votesLimit = votingVotesLimitConfig != null ? votingVotesLimitConfig.getValue() : votesLimit;
                    }
                    ballot.setVotesLimit(votesLimit);
                    ballot.setVotesLimitMeaning(VotesLimitMeanings.valueOf(votesLimitMeaning));
                    ballot.setVotingSystemType(vtype);
                    ballot.setRequireRegistration(false);
                    ballot.setUserUuidAsSignature(true);
                    ballot.setDecisionType("BINDING");
                    ballot.setComponent(component);
                    ballot.save();
                    ballot.refresh();
                    campaign.setCurrentBallotAsString(ballot.getUuid().toString());
                    campaign.getResources().addBallot(ballot);
                    campaign.getResources().update();
                    campaign.update();

                    //Creating candidates
                    createBallotCandidates(campaign, ballot, published);

                    //Adding configurations to ballot
                    addBallotConfigurations(component.getConfigs(), ballot);
                }
            }
        }
    }

    private void createBallotCandidates(Campaign campaign, Ballot ballot, Boolean publishedProposal) {
        List<Contribution> contributions = campaign.getContributions();
        Boolean hasCandidates = false;
        for(Contribution c : contributions) {
            ContributionTypes ballotEntityType = ContributionTypes.valueOf(ballot.getEntityType());
            if(c.getType()!=null && c.getType().equals(ballotEntityType)){
                //if config campaign.include.all.published.proposals === TRUE,
                // change status of PUBLISHED to INBALLOT
                hasCandidates=true;
                Logger.info("Creating BallotCandidate for Contribution "+ c.getTitle() + "=="+ c.getContributionId() );
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
