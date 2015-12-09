package delegates;

import java.util.UUID;

import models.VotingBallot;
import models.VotingBallotRegistrationForm;

//import java.util.UUID;


public class VotingCandidateDelegate {
	public static VotingCandidate getVotingCandidateByUUID(UUID uuid) {
		VotingBallot v = VotingBallot.queryByUUID(uuid);
		VotingCandidate out = null;
		if (v!=null) 
//			TODO:Ask if this is okay since it returns a list
			out = v.getCandidates();

		return out;
	}
}
