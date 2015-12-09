package delegates;
import java.util.UUID;

//import models.VotingBallot;
import models.VotingBallotRegistrationForm;

public class SecretSignatureDelegate {
	//this function doesn't take params as were are just testing things
	@SuppressWarnings("null")
	public static UUID verifyRegistrationGenerateSignature() {
		VotingBallotRegistrationForm dummyform = null;
		return dummyform.getUuid();
		
	}
	
	public static void getRegistrationResult(String password, String signature) {
		///how should I finish the transfer part
		
	}


}
