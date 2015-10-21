package delegates;


import java.util.UUID;
import models.VotingBallotRegistrationForm;


import models.VotingBallot;


public class VotingBallotDelegate {

	public static VotingBallotRegistrationForm getVotingRegistrationFormByUUID(UUID uuid) {
		//List<VotingBallot> vbl = new ArrayList();
		VotingBallot v = VotingBallot.queryByUUID(uuid);
		VotingBallotRegistrationForm out = null;
		if (v!=null) 
			out = v.getRegistrationForm();

		return out;
	}
}