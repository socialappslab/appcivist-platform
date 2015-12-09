package delegates;


import java.util.UUID;

import models.VotingBallotRegistrationForm;
import models.VotingBallot;
import models.VotingBallotVote;
import models.transfer.VotingBallotTransfer;


public class VotingBallotDelegate {

	public static VotingBallotRegistrationForm getVotingRegistrationFormByUUID(UUID uuid) {
		//List<VotingBallot> vbl = new ArrayList();
		VotingBallot v = VotingBallot.queryByUUID(uuid);
		VotingBallotRegistrationForm out = null;
		if (v!=null) 
			out = v.getRegistrationForm();
		return out;
	}
	
	public static VotingBallot getVotingBallotByUUID(UUID uuid) {
		
		
		// TODO:implement the below
		return VotingBallot.queryByUUID(uuid);
	}

	public static VotingBallotTransfer getBallotAndVote(UUID uuid,
			String signature) {
		// step 1: get the ballot 
		VotingBallot vb = VotingBallotDelegate.getVotingBallotByUUID(uuid);
		
		// setp 2: get the vote
		VotingBallotVote vbv = VotingBallotDelegate.getVotingBallotVote(signature);
		
		VotingBallotTransfer result = new VotingBallotTransfer();
		result.setBallot(vb);
		result.setVote(vbv);
		
		return result;
	}

	private static VotingBallotVote getVotingBallotVote(String signature) {
		//TODO: IMPLEMENT THIS
		return VotingBallotVote.findBySignature(signature);
	}
}