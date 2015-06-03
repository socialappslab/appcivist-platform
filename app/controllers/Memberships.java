package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;

import enums.MembershipCreationTypes;
import enums.MembershipStatus;
import models.Membership;
import models.transfer.TransferResponseStatus;
import models.transfer.TransferMembership;
import models.Assembly;
import models.AssemblyMembership;
import models.GroupMembership;
import models.User;
import models.WorkingGroup;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.*;
import providers.MyLoginUsernamePasswordAuthUser;
import providers.MyUsernamePasswordAuthProvider;
import utils.GlobalData;
import static play.data.Form.form;

public class Memberships extends Controller {

	public static final Form<TransferMembership> MEMBERSHIP_FORM = form(TransferMembership.class);
	/**
	 * The membership invitation/request timeout in seconds Defaults to 4 weeks
	 * (24 hours * 30 days * 60 minutes * 60 seconds)
	 */
	public static final Long MEMBERSHIP_EXPIRATION_TIMEOUT = new Long(
			30 * 14 * 3600);

	@Security.Authenticated(Secured.class)
	public static Result createMembership() {
		// 1. obtaining the user of the requestor
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<TransferMembership> newMembershipForm = MEMBERSHIP_FORM
				.bindFromRequest();

		if (newMembershipForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					newMembershipForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			TransferMembership newMembership = newMembershipForm.get();
			return createMembership(requestor,
					newMembership.getTargetCollection(), newMembership.getId(),
					newMembership.getType(), newMembership.getUserId(),
					newMembership.getEmail());
		}
	}
	
	/**
	 * POST /<assembly or group>/:id/membership/:type
	 * 
	 * @param membershipType
	 * @return
	 */
	public static Result createMembership(User requestor,
			String targetCollection, Long id, String membershipType,
			Long userId, String userEmail) {

		// 4. Read the target Group or Assembly from the Database depending
		// on the targetCollection
		WorkingGroup targetWorkingGroup = targetCollection.equals("group") ? WorkingGroup
				.read(id) : null;
		Assembly targetAssembly = targetCollection.equals("assembly") ? Assembly
				.read(id) : null;
		// 5.Create the correct type of membership depending on the
		// targetCollection
		Membership m = targetCollection.equals("group") ? new GroupMembership()
				: new AssemblyMembership();

		// 6. Make sure either the assembly or the group exists before
		// proceeding
		if (targetAssembly == null && targetWorkingGroup == null) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					" The target " + targetCollection + " " + id
							+ " does not exist"));
			return unauthorized(Json.toJson(responseBody));
		}

		// 7. Set who created the membership
		m.setCreator(requestor);

		// 8. check if user exists
		User targetUser = User.findByUserId(userId);
		if (targetUser == null) {
			// TODO create membership invitations for not members
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					" The target user " + userId + " does not exist"));
			return unauthorized(Json.toJson(responseBody));
		}

		m.setUser(targetUser);
		m.setLang(targetUser.getLocale());
		m.setExpiration((System.currentTimeMillis() + 1000 * MEMBERSHIP_EXPIRATION_TIMEOUT));

		// 9. check if the membership of this user on this assembly/group
		// already exists
		boolean mExists = m.checkIfExists();

		if (!mExists) {
			// 8. Set the initial status of the new membership depending of
			// the type
			if (membershipType.toUpperCase().equals(
					MembershipCreationTypes.INVITATION)) {

				// 8. Check if the creator is authorized
				if (Membership.userCanInvite(requestor, targetAssembly)) {
					m.setStatus(MembershipStatus.INVITED);
					MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider
							.getProvider();
					provider.sendMembershipInvitationEmail(m, targetCollection);
					Membership.create(m);
					return ok(Json.toJson(m));

				} else {
					return unauthorized("You don't have the role to send invitations");
				}
			} else if (membershipType.toUpperCase().equals(
					MembershipCreationTypes.REQUEST)) {
				m.setStatus(MembershipStatus.REQUESTED);
				Membership.create(m);
				return ok(Json.toJson(m));
			} else if (membershipType.toUpperCase().equals(
					MembershipCreationTypes.SUBSCRIPTION)) {
				m.setStatus(MembershipStatus.FOLLOWING);
				Membership.create(m);
				return ok(Json.toJson(m));
			} else {
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
						" The request did not specified whether it was an invitation or a join request"));
				return unauthorized(Json.toJson(responseBody));
			}
		} else {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					" The target user " + userId
							+ " has already a membership to this "
							+ targetCollection));
			return unauthorized(Json.toJson(responseBody));
		}

	}

	//GET    /api/membership/:id                            controllers.Memberships.readMembership(id: Long)
	@Security.Authenticated(Secured.class)
	public static Result readMembership(Long id) {
		Membership m = Membership.read(id);
		if (m!=null){
			return ok(Json.toJson(m));	
		} else {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage("There is no membership with ID = "+id);
			return notFound(Json.toJson(responseBody));
		}
	}
	
	//PUT    /api/membership/:id                            controllers.Memberships.update(id: Long)
	@Security.Authenticated(Secured.class)
	public static Result update(Long id) {
		// TODO: IMPLEMENT
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage("Not implemented yet");
		return notFound(Json.toJson(responseBody));
	}

	//DELETE /api/membership/:id                            controllers.Memberships.delete(id: Long)
	@Security.Authenticated(Secured.class)
	public static Result delete(Long id) {
		// TODO: IMPLEMENT
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage("Not implemented yet");
		return notFound(Json.toJson(responseBody));
	} 

	//GET    /api/membership/verify/:token                  controllers.Memberships.verifyMembership(token: String) 
	@Security.Authenticated(Secured.class)
	public static Result verifyMembership(String token) {
		// check the user who is accepting the invitation is
		// TODO
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage("Not implemented yet");
		return notFound(Json.toJson(responseBody));
	}
}
