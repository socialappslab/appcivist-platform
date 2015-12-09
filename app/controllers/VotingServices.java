package controllers;

import static play.data.Form.form;

import java.util.UUID;

import models.Assembly;
import models.VotingBallot;
import models.VotingBallotRegistrationField;
import models.VotingBallotRegistrationForm;
import models.VotingBallotVote;
import models.transfer.TransferResponseStatus;
import models.transfer.VotingBallotTransfer;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.GlobalData;
import delegates.VotingBallotDelegate;
import delegates.VotingCandidate;

public class VotingServices extends Controller {

	public static final Form<VotingBallotRegistrationForm> REGISTRATION_FORM = form(VotingBallotRegistrationForm.class);

	/**
	 * GET /ballot/{uuid}/registration => Retrieves the VotingBallotRegistrationForm, with its fields
	 * @param uuid
	 * @return
	 */
	public static Result getVotingRegistrationForm(UUID uuid) {

		VotingBallotRegistrationForm vbf = VotingBallotDelegate
				.getVotingRegistrationFormByUUID(uuid);
		return ok(Json.toJson(vbf));

	}

	/**
	 * POST /ballot/{uuid}/registration 
	 * => Verifies the values provided by the user foe each field in the 
	 * VotingBallotRegistrationForm and responds with both the password for the 
	 * VotingBallot and a specific generated "signature" for the voter. 
	 * The signature will be used by the voter to retrieve its personal vote.
 	 * @param uuid
	 * @return
	 */
	public static Result Register(UUID uuid) {
		// 1. Check the VotingBallot with uuid exists, if not, return not found
		// 404 error

		// 2. Read the registration form values from the body and
		// another way of getting the body content => request().body().asJson()
		final Form<VotingBallotRegistrationForm> newRegForm = REGISTRATION_FORM
				.bindFromRequest();

		if (newRegForm.hasErrors()) {
			return badRequest(Json.toJson(TransferResponseStatus.badMessage(
					Messages.get(GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
							newRegForm.errorsAsJson()), newRegForm
							.errorsAsJson().toString())));
		} else {
			VotingBallotRegistrationForm newRegistration = newRegForm.get();
			
			// TODO: make this a TransferClass (meaning put it in the transfer package as a standalone class)
			class Signature {
				public String signature;
			}
			
			Signature userSign = new Signature();

			// 2.1. verify values and generate a signature for the voter
			// Delegate.verifyRegistrationGenerateSignature()
			// -> v1.0 => just return a random string (for example, a random
			// UUID)
			// -> v2.0 => implement the logic but only after a first 1.0 version
			// of the endpoint works
			// example of logic to move to the delegate
			String signatureString = null;
			for (VotingBallotRegistrationField receivedField : newRegistration
					.getFields()) {
				// using the name of the field (field.getFieldName()) and its
				// value(field.getValue()) in the fields
				// if there is a expectedValue, check they match with the field
				// value
				// concatenate all these fieldNames and values
				String ObtainedString = receivedField.getValue();

				VotingBallotRegistrationField fieldDefinition = VotingBallotRegistrationField
						.read(receivedField
								.getVotingBallotRegistrationFieldId());

				if (receivedField.getValue() != null) {
					if (fieldDefinition.getExpectedValue() != null
							&& !fieldDefinition.getExpectedValue().isEmpty()) {

						if (fieldDefinition.getExpectedValue().equals(
								receivedField.getValue())) {
							signatureString += ObtainedString;
						} else {
							// / throw an exception
						}
					} else {
						signatureString += ObtainedString;
				
					}
				}
				
				signatureString = signatureString.hashCode() + "";
			}

			userSign.signature = signatureString;
			if (signatureString !=null) {
				return ok(Json.toJson(userSign));		
			} else {
				return internalServerError("There was an error generating the signature");
			}
		}

		// TODO: move all the logic to create the signature to SecretSignatureDelegate. 
		// 3. Get the password from the VotingBallot and the signature, and ask
		// the delegate to create the response
		// Delegate.getRegistrationResult(password, signature) =>
		// VotingBallotRegistrationTransfer ?????
		// return that as JSON

		// TO-DO ask Cristian about below: SHOULD i make user a global var ?????
//		String pswd = userBallot.getPassword();
//		String sig = user.getSignature();
//
//		// return as Json
//		return ok(Json.toJson(SecretSignatureDelegate.getRegistrationResult(pswd, sig)));
	}
	
	public static Result getVotingBallot(UUID uuid, String signature) {
		VotingBallotTransfer result = VotingBallotDelegate.getBallotAndVote(uuid,signature);
		return ok(Json.toJson(result));
		
	}
	
	
	public static Result createVotingBallotVote(UUID uuid, String signature) {
		// use that to create the signature for the user
					// create a VotingBallotVote with the signature
					// TO DO: Ask how to create this object
					// 2.2. Using the signature, create a VotingBallotVote for the voter
								// and save it
								
					VotingBallotVote userBallot = new VotingBallotVote();
					
					userBallot.setSignature(signature);
					userBallot.setStatus("DRAFT");
					
					//Implement Readby UUID
					userBallot.setBallot(VotingBallot.readByUUID(uuid));
					
					VotingBallotVote.create(userBallot);
					return ok(Json.toJson(result));

	}
	public static Result updateVotingBallotVote(long voteid, String signature){
		VotingBallotVote updateUserBallot = new VotingBallotVote();
		updateUserBallot.setSignature(signature);
		updateUserBallot.setStatus("DRAFT");
		//can this be out put
		updateUserBallot.setBallot(VotingBallot.read(voteid));
		VotingBallotVote.update(updateUserBallot);
		return ok(Json.toJson(result));
	}
}
