package models.transfer;

import models.VotingBallot;
import models.VotingBallotVote;

public class VotingBallotTransfer {
	private VotingBallot ballot;
	private VotingBallotVote vote;
	public VotingBallot getBallot() {
		return ballot;
	}
	public void setBallot(VotingBallot ballot) {
		this.ballot = ballot;
	}
	public VotingBallotVote getVote() {
		return vote;
	}
	public void setVote(VotingBallotVote vote) {
		this.vote = vote;
	}
	
}
