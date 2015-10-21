package controllers;

import static play.data.Form.form;
import http.Headers;

import java.util.List;
import java.util.UUID;

import models.VotingBallotRegistrationForm;
import delegates.VotingBallotDelegate;
import models.Membership;
import models.SecurityRole;
import models.TokenAction;
import models.TokenAction.Type;
import models.User;
import models.transfer.MembershipTransfer;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.Pair;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

	

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




