package controllers;

import java.util.UUID;

import models.VotingBallotRegistrationForm;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import delegates.VotingBallotDelegate;

public class VotingServices extends Controller {

	public static Result getVotingRegistrationForm(UUID uuid) {
		VotingBallotRegistrationForm vbf = VotingBallotDelegate.getVotingRegistrationFormByUUID(uuid);
		return ok(Json.toJson(vbf));

	}
	
	/* verfies values provided by the user for each field in the VotingBallotRegistrationForm **/

//	public static Result verify() {
//
//		read the Form Fields
//		Call on the secretSignatureDelegate to process based on the fields
//		check if the the signature is valid?
//		return that signature as a JSON
//
//	}	
}




