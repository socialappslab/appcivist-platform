package controllers;

import static play.data.Form.form;
import http.Headers;

import java.util.List;
import java.util.UUID;

import models.Assembly;
import models.Membership;
import models.MembershipAssembly;
import models.MembershipGroup;
import models.MembershipInvitation;
import models.SecurityRole;
import models.TokenAction;
import models.TokenAction.Type;
import models.User;
import models.WorkingGroup;
import models.transfer.InvitationTransfer;
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
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.feth.play.module.pa.PlayAuthenticate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import delegates.MembershipsDelegate;
import enums.MembershipCreationTypes;
import enums.MembershipStatus;
import enums.MembershipTypes;
import enums.MyRoles;
import enums.ResponseStatus;
import exceptions.MembershipCreationException;

@Api(value = "membership: Assembly and Working Group membership management", description = "Assembly and Working Group membership management endpoints. A membership connects a user to either a working group or an assembly, assigning him a role that is used for authorization purposes")
@With(Headers.class)
public class Memberships extends Controller {

	public static final Form<MembershipTransfer> TRANSFER_MEMBERSHIP_FORM = form(MembershipTransfer.class);
	public static final Form<InvitationTransfer> TRANSFER_INVITATION_FORM = form(InvitationTransfer.class);
	public static final Form<MembershipInvitation> MEMBERSHIP_INVITATION_FORM = form(MembershipInvitation.class);
	public static final Form<Membership> MEMBERSHIP_FORM = form(Membership.class);
	public static final Form<SecurityRole> ROLE_FORM = form(SecurityRole.class);

	/**
	 * The membership invitation/request timeout in seconds Defaults to 4 weeks
	 * (24 hours * 30 days * 60 minutes * 60 seconds)
	 */
	public static final Long MEMBERSHIP_EXPIRATION_TIMEOUT = new Long(
			30 * 14 * 3600);

	/**
	 * TODO: marked for review and removal if not needed
	 * 
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Membership.class, produces = "application/json", value = "Create a membership with any desired status within an Assembly or a Group. Endpoint available only to ADMINS.")
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"), 
		@ApiImplicitParam(name = "Membership Simplified Object", value = "The membership to create", dataType="models.transfer.MembershipTransfer", paramType="Body")
		})
	@ApiResponses({
			@ApiResponse(code = ACCEPTED, message = "Membership was created", response = Membership.class),
			@ApiResponse(code = BAD_REQUEST, message = "Membership body has errors", response = TransferResponseStatus.class) })
	@Restrict({ @Group(GlobalData.ADMIN_ROLE) })
	public static Result createMembership() {
		// 1. Read User data related to the Requestor
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. Read the body of the request
		final Form<MembershipTransfer> newMembershipForm = TRANSFER_MEMBERSHIP_FORM
				.bindFromRequest();

		if (newMembershipForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					newMembershipForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			MembershipTransfer newMembership = newMembershipForm.get();
			String targetCollection = newMembership.getTargetCollection();
			Long targetCollectionId = targetCollection.toUpperCase().equals(
					"GROUP") ? newMembership.getGroupId() : newMembership
					.getAssemblyId();

			try {
				Ebean.beginTransaction();
				Result r = createMembership(requestor, targetCollection,
						targetCollectionId, newMembership.getType(),
						newMembership.getUserId(), newMembership.getEmail(),
						newMembership.getDefaultRoleId(),
						newMembership.getDefaultRoleName());
				Ebean.commitTransaction();
				return r;
			} catch (MembershipCreationException e) {
				Ebean.rollbackTransaction();
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
						"Error: "+e.getMessage()));
				e.printStackTrace();
				return internalServerError(Json.toJson(responseBody));
			}
		}
	}

	/**
	 * Find membership record for user identified by its ID
	 * 
	 * @param uid
	 *            User's ID
	 * @param type
	 *            Type of membership filter (assembly, group)
	 * @return User's Membership record
	 */
	@ApiOperation(httpMethod = "GET", response = Membership.class, responseContainer = "List", produces = "application/json", value = "Read user memberships by User ID and Membership TYPE", notes = "This endpoint is only accessible to ADMIN users and to the User identified by the provided UUID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User or Memberships not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	@Dynamic(value = "OnlyMeAndAdmin", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result findMembershipByUser(
			@ApiParam(name = "uid", value = "User's ID") Long uid, 
			@ApiParam(name = "type", value = "Type of memberships to read", allowableValues = "assembly,group") String type) {
		User u = User.findByUserId(uid);
		if (u == null)
			return notFound(Json.toJson(new TransferResponseStatus(
					ResponseStatus.NODATA, "User with ID = " + uid
							+ " not found")));

		// TODO: include UUID already in the Membership model
		String membershipType = "";
		if (type != null && !type.isEmpty()) {
			switch (type.toLowerCase()) {
			case "assembly":
				membershipType = "ASSEMBLY";
				break;
			case "group":
				membershipType = "GROUP";
				break;
			}
		}

		List<Membership> memberships = Membership.findByUser(u, membershipType);
		if (memberships == null || memberships.isEmpty())
			return notFound(Json.toJson(new TransferResponseStatus(
					ResponseStatus.NODATA, "No memberships for user with ID = "
							+ uid)));
		else
			return ok(Json.toJson(memberships));
	}

	/**
	 * Get the list of invitations to the target Group or Assembly
	 * 
	 * @param targetId
	 *            id of the target group or assembly
	 * @param status
	 *            the status of the invitation (INVITED, ACCEPTED, REJECTED)
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = MembershipInvitation.class, responseContainer = "List", produces = "application/json", value = "Get the list of invitations to the target Group or Assembly")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	@ApiResponses({
			@ApiResponse(code = ACCEPTED, message = "Invitations found", response = MembershipInvitation.class),
			@ApiResponse(code = NOT_FOUND, message = "Invitations not found", response = TransferResponseStatus.class) })
	public static Result listInvitations(
			@ApiParam(name = "targetId", value = "Working Group or Assembly Id") Long targetId, 
			@ApiParam(name = "status", value = "Invitation Status", allowableValues = "INVITED, ACCEPTED, REJECTED") String status) {
		
		List<MembershipInvitation> invitations = MembershipInvitation
				.findByTargetIdAndStatus(targetId, status);
		if (invitations != null && invitations.isEmpty()) {
			TransferResponseStatus response = new TransferResponseStatus(
					ResponseStatus.NODATA, "No invitations with status '"
							+ status
							+ "' found for the target collection with id: "
							+ targetId);
			return notFound(Json.toJson(response));
		} else {
			return ok(Json.toJson(invitations));
		}
	}

	/**
	 * Read and invitation by the Token
	 * 
	 * @param token
	 *            invitation token
	 * @return the invitation record
	 */
	@ApiOperation(httpMethod = "GET", response = MembershipInvitation.class, produces = "application/json", value = "Read an invitation by Token")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	public static Result readInvitation(
			@ApiParam(name = "token", value = "Invitation Token") UUID token) {
		TokenAction ta = TokenAction.findByToken(token.toString(),
				TokenAction.Type.MEMBERSHIP_INVITATION);
		if (ta != null && ta.isValid()) {
			MembershipInvitation mi = MembershipInvitation.findByToken(token);
			return ok(Json.toJson(mi));
		} else {
			TransferResponseStatus response = new TransferResponseStatus(
					ResponseStatus.NOTAVAILABLE, "Token is no longer valid");
			return notFound(Json.toJson(response));
		}
	}

	/**
	 * Searches for an user membership in an assembly. If no membership exists,
	 * searches for a pending invitation.
	 * 
	 * @param aid
	 *            The assembly id
	 * @param uid
	 *            The user id
	 * @return the user's assembly or a notFound response
	 */
	@ApiOperation(httpMethod = "GET", response = Membership.class, produces = "application/json", value = "Read membership record of an user within a specified assembly")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	@ApiResponses({
			@ApiResponse(code = OK, message = "Membership found", response = Membership.class),
			@ApiResponse(code = NOT_FOUND, message = "Membership not found", response = TransferResponseStatus.class) })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result readUserMembershipInAssembly(
			@ApiParam(name = "aid", value = "Assembly Id") Long aid, 
			@ApiParam(name = "uid", value = "User Id") Long uid) {

		Membership ma = MembershipAssembly.findByUserAndAssemblyIds(uid, aid);
		if (ma == null) {
			// If there is no Membership record created (either REQUESTED or
			// ACCEPTED), check if there is a pending invitation
			MembershipInvitation mi = MembershipInvitation
					.findByUserIdTargetIdAndType(uid, aid,
							MembershipTypes.ASSEMBLY);

			if (mi == null) {
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage("Membership of user " + uid
						+ " in assembly " + aid + " does not exist");
				responseBody.setResponseStatus(ResponseStatus.NODATA);
				return notFound(Json.toJson(responseBody));
			} else {
				// Create a temporary "membership" object to send as response
				ma = new Membership();
				ma.setStatus(MembershipStatus.INVITED);
				ma.setMembershipType("INVITATION");
				ma.setRoles(mi.getRoles());
				ma.setTargetAssembly(mi.getTargetAssembly());
				ma.setInvitationToken(mi.getToken().token);
				return ok(Json.toJson(ma));
			}
		} else {
			return ok(Json.toJson(ma));
		}
	}

	/**
	 * Searches for an user membership in a group. If no membership exists,
	 * searches for a pending invitation.
	 * 
	 * @param aid
	 *            The assembly id
	 * @param uid
	 *            The user id
	 * @return the user's assembly or a notFound response
	 */
	@ApiOperation(httpMethod = "GET", response = Membership.class, produces = "application/json", value = "Read membership record of a user within a group", notes = "Only available to COORDINATORS")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	@ApiResponses({
			@ApiResponse(code = OK, message = "Membership found", response = Membership.class),
			@ApiResponse(code = NOT_FOUND, message = "Membership not found", response = TransferResponseStatus.class) })
	@Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result readUserMembershipInGroup(
			@ApiParam(name = "gid", value = "Working Group Id") Long gid, 
			@ApiParam(name = "uid", value = "User's id") Long uid) {

		Membership ma = MembershipGroup.findByUserAndGroupId(uid, gid);

		if (ma == null) {
			// If there is no Membership record created (either REQUESTED or
			// ACCEPTED), check if there is a pending invitation
			MembershipInvitation mi = MembershipInvitation
					.findByUserIdTargetIdAndType(uid, gid,
							MembershipTypes.GROUP);

			if (mi == null) {
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage("Membership of user " + uid
						+ " in group " + gid + " does not exist");
				return notFound(Json.toJson(responseBody));
			} else {
				// Create a temporary "membership" object to send as response
				ma = new Membership();
				ma.setStatus(MembershipStatus.INVITED);
				ma.setMembershipType("INVITATION");
				ma.setRoles(mi.getRoles());
				ma.setTargetAssembly(mi.getTargetAssembly());
				ma.setInvitationToken(mi.getToken().token);
				return ok(Json.toJson(ma));
			}
		} else {
			return ok(Json.toJson(ma));
		}
	}

	/**
	 * Creates an invitation to join an assembly and sends email to the invited
	 * user
	 * 
	 * @param aid
	 *            id of the assembly
	 * @return the invitation record
	 */
	@ApiOperation(httpMethod = "POST", response = InvitationTransfer.class, produces = "application/json", value = "Create and send an Invitation to join an Assembly", notes="Only available to COORDINATORS")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
			@ApiImplicitParam(name = "Invation Object", value = "Invitation details", dataType = "models.transfer.InvitationTransfer", paramType = "body") 
	})
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createSendInvitationToJoinAssembly(@ApiParam(name = "aid", value = "Assembly Id") Long aid) {
		// 1. Obtaining the user of the requestor
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));
		// 2. Read the invitation data from the body
		final Form<InvitationTransfer> newInvitationForm = TRANSFER_INVITATION_FORM
				.bindFromRequest();

		if (newInvitationForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					newInvitationForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Ebean.beginTransaction();
			InvitationTransfer newInvitation;
			MembershipInvitation mi = null;
			try {
				newInvitation = newInvitationForm.get();
				newInvitation.setTargetId(aid);
				newInvitation.setTargetType("ASSEMBLY");
				mi = createAndSendInvitation(requestor, newInvitation);
			} catch (Exception e) {
				Ebean.rollbackTransaction();
				e.printStackTrace();
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
						e.getMessage()));
				return internalServerError(Json.toJson(responseBody));
			}
			Ebean.commitTransaction();
			return ok(Json.toJson(mi));
		}
	}

	@ApiOperation(httpMethod = "PUT", response = InvitationTransfer.class, produces = "application/json", value = "Resend invitation")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
			@ApiImplicitParam(name = "iid", value = "Invitation id", dataType = "Long", paramType = "path") })
	@SubjectPresent
	public static Result reSendInvitation(Long iid) {
		MembershipInvitation mi = MembershipInvitation.read(iid);
		Long targetId = mi.getTargetId();
		MembershipTypes type = mi.getTargetType();
		String invitationBody = type == MembershipTypes.ASSEMBLY ? Assembly
				.read(targetId).getInvitationEmail() : WorkingGroup.read(
				targetId).getInvitationEmail();
		mi.sendInvitationEmail(invitationBody, mi.getToken().token);
		return ok(Json.toJson(mi));
	}

	/**
	 * Creates an invitation to join a group and sends email to the invited user
	 * 
	 * @param gid
	 *            id of the group
	 * @return the invitation record
	 */
	@ApiOperation(httpMethod = "POST", response = InvitationTransfer.class, produces = "application/json", value = "Create and send an invitation to join a Group to a non-AppCivist user ")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	@Dynamic(value = "CoordinatorOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result createSendInvitationToJoinGroup(@ApiParam(name = "gid", value = "Working Group Id") Long gid) {
		// 1. Obtaining the user of the requestor
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));
		// 2. Read the invitation data from the body
		final Form<InvitationTransfer> newInvitationForm = TRANSFER_INVITATION_FORM
				.bindFromRequest();

		if (newInvitationForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					newInvitationForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Ebean.beginTransaction();
			InvitationTransfer newInvitation;
			MembershipInvitation mi = null;
			try {
				newInvitation = newInvitationForm.get();
				newInvitation.setTargetId(gid);
				newInvitation.setTargetType("GROUP");
				mi = createAndSendInvitation(requestor, newInvitation);
			} catch (Exception e) {
				Ebean.rollbackTransaction();
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
						e.getMessage()));
				return internalServerError(Json.toJson(responseBody));
			}
			Ebean.commitTransaction();
			return ok(Json.toJson(mi));
		}
	}

	/**
	 * Update invitation status
	 * @param token
	 * @param answer
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = User.class, responseContainer = "List", produces = "application/json", value = "Update invitation status")
	public static Result answerInvitation(
			@ApiParam(name = "token", value = "Invitation token") UUID token, 
			@ApiParam(name = "answer", value = "Answer to the Invitation", allowableValues = "ACCEPT, REJECT") String answer) {
		Ebean.beginTransaction();
		MembershipStatus response = answer.equals("ACCEPT") ? MembershipStatus.ACCEPTED
				: MembershipStatus.REJECTED;

		// 1. Verify the token
		MembershipInvitation mi = MembershipInvitation.findByToken(token);
		final TokenAction ta = Users.tokenIsValid(token.toString(),
				Type.MEMBERSHIP_INVITATION);
		if (ta == null) {
			Ebean.rollbackTransaction();
			return badRequest(Json.toJson(TransferResponseStatus.badMessage(
					Messages.get("playauthenticate.token.error.message"),
					"Token is null")));
		}
		ta.delete();
		// 2. Read Invitation
		User invitedUser = null;
		if (mi.getUserId() != null) {
			invitedUser = User.read(mi.getUserId());
		} else {
			Ebean.rollbackTransaction();
			return internalServerError(Json.toJson(TransferResponseStatus
					.badMessage("User has not signuped yet", "")));
		}

		if (response.equals(MembershipStatus.ACCEPTED)) {
			// 4. Create Membership
			try {
				MembershipInvitation.acceptAndCreateMembershipByToken(mi,
						invitedUser);
			} catch (Exception e) {
				Ebean.rollbackTransaction();
				e.printStackTrace();
				return internalServerError(Json.toJson(TransferResponseStatus
						.badMessage(Messages.get("Error has occurred: "),
								e.getMessage())));
			}
			Ebean.commitTransaction();
			return ok(Json.toJson("DONE"));
		} else {
			Ebean.commitTransaction();
			// 5. Update Invitation record
			MembershipInvitation.rejectAndUpdateInvitationByToken(mi,
					invitedUser);
			return ok(Json.toJson("DONE"));
		}
	}

	@ApiOperation(httpMethod = "POST", response = Membership.class, produces = "application/json", value = "Create a membership request for an Assembly or a Group")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"), 
			@ApiImplicitParam(name = "Membership simplified object", value = "A membership simplified object with details of the target assembly or group and the user requesting membership", dataType="models.transfer.MembershipTransfer", paramType = "body")
	})
	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result createMembershipRequest(@ApiParam(name = "targetId", value="ID of the target Assembly or Group") Long targetId) {
		// 1. obtaining the user of the requestor
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<MembershipTransfer> newMembershipForm = TRANSFER_MEMBERSHIP_FORM
				.bindFromRequest();

		if (newMembershipForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					newMembershipForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			MembershipTransfer newMembership = newMembershipForm.get();
			String targetCollection = newMembership.getTargetCollection();
			Long targetCollectionId = targetId;
			// Allow only requests and subscriptions to non-members
			if (!newMembership.getType().equals(
					MembershipCreationTypes.INVITATION.toString())) {
				try {
					Ebean.beginTransaction();
					Result r = createMembership(requestor, targetCollection,
							targetCollectionId, newMembership.getType(),
							newMembership.getUserId(),
							newMembership.getEmail(),
							newMembership.getDefaultRoleId(),
							newMembership.getDefaultRoleName());
					Ebean.commitTransaction();
					return r;
				} catch (MembershipCreationException e) {
					Ebean.rollbackTransaction();
					TransferResponseStatus responseBody = new TransferResponseStatus();
					responseBody.setStatusMessage(Messages.get(
							GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
							"Error: " + e.getMessage()));
					e.printStackTrace();
					return internalServerError(Json.toJson(responseBody));
				}

			} else {
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
						"A non member cannot create an invitation membership"));
				return internalServerError(Json.toJson(responseBody));
			}
		}
	}

	/**
	 * Read a membership by ID
	 * 
	 * @param id
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Membership.class, produces = "application/json", value = "Read a membership record by its ID")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	@Security.Authenticated(Secured.class) // TODO: secured this by limiting the reading of specific membership by ids to OnlyMeAndAdmin
	public static Result readMembership(@ApiParam(name="id", value="Membership ID") Long id) {
		Membership m = Membership.read(id);
		if (m != null) {
			List<SecurityRole> roles = m.getRoles();
			Logger.debug("Membership roles: #" + roles.size() + " = "
					+ roles.toString());
			return ok(Json.toJson(m));
		} else {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage("There is no membership with ID = "
					+ id);
			return notFound(Json.toJson(responseBody));
		}
	}

	/**
	 * Read the roles assigned to a specific membership by ID
	 * 
	 * @param id
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = SecurityRole.class, responseContainer="List", produces = "application/json", value = "Read roles assigned to a specific membership record by its ID")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	@Security.Authenticated(Secured.class)
	public static Result readMembershipRoles(@ApiParam(name="id", value="Membership ID") Long id) {
		Membership m = Membership.read(id);
		if (m != null) {
			List<SecurityRole> roles = m.getRoles();
			return roles != null ? ok(Json.toJson(roles)) : notFound(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.NOTAVAILABLE,
							"No roles for membership" + id)));
		} else {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage("There is no membership with ID = "
					+ id);
			return notFound(Json.toJson(responseBody));
		}
	}

	/**
	 * Add a Role to the membership (only Coordinators of the Assembly/Group)
	 * 
	 * private Long roleId; private String name;
	 * 
	 * @param id
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Membership.class, produces = "application/json", value = "Add a role to a membership identified by its ID", notes="Only for COORDINATORS")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	// TODO: implement @Dynamic(value="CoordinatorOfAssemblyOrGroup", meta=SecurityModelConstants.MEMBERSHIP_RESOURCE_PATH)
	@Security.Authenticated(Secured.class)
	public static Result addMembershipRole(@ApiParam(value="Membership ID", name="id") Long id) {
		Membership m = Membership.read(id);
		if (m != null) {
			User requestor = User.findByAuthUserIdentity(PlayAuthenticate
					.getUser(session()));
			Boolean authorization = false;
			Membership requestorMembership = MembershipsDelegate
					.requestorMembership(requestor, m);

			// TODO move authorization to a DynamicResourceHandler
			if (requestorMembership != null) {
				authorization = MembershipsDelegate.requestorIsCoordinator(
						requestor, requestorMembership);
			} else {
				return unauthorized(Json.toJson(new TransferResponseStatus(
						ResponseStatus.UNAUTHORIZED,
						"Requestor is not member of this "
								+ m.getMembershipType())));
			}

			if (authorization) {
				final Form<SecurityRole> newRoleForm = ROLE_FORM
						.bindFromRequest();
				if (newRoleForm.hasErrors()) {
					TransferResponseStatus responseBody = new TransferResponseStatus();
					responseBody
							.setStatusMessage("There was an error in the role included in the request"
									+ newRoleForm.errorsAsJson());
					return badRequest(Json.toJson(responseBody));
				} else {
					SecurityRole newRole = newRoleForm.get();
					Long roleId = newRole.getRoleId();
					String roleName = newRole.getName();

					SecurityRole role = SecurityRole.read(roleId);
					if (role == null) {
						role = SecurityRole.findByName(roleName);
					}

					if (role != null) {
						m.getRoles().add(role);
						m.update();
						m.refresh();
						return ok(Json.toJson(m));
					} else {
						return internalServerError(Json
								.toJson(new TransferResponseStatus(
										ResponseStatus.NOTAVAILABLE,
										"The role you are trying to add ("
												+ roleName + ")"
												+ " does not exist ")));
					}
				}

			} else {
				return unauthorized(Json.toJson(new TransferResponseStatus(
						ResponseStatus.UNAUTHORIZED, "Requestor is not"
								+ MyRoles.COORDINATOR)));
			}

		} else {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage("There is no membership with ID = "
					+ id);
			return notFound(Json.toJson(responseBody));
		}
	}

	/**
	 * Delete a membership role (only Coordinators of the Assembly/Group)
	 * 
	 * private Long roleId; private String name;
	 * 
	 * @param id
	 * @return
	 */
	@ApiOperation(httpMethod = "DELETE", response = Membership.class, produces = "application/json", value = "Delete a role from a membership identified by its ID", notes="Only for COORDINATORS")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	// TODO: implement @Dynamic(value="CoordinatorOfAssemblyOrGroup", meta=SecurityModelConstants.MEMBERSHIP_RESOURCE_PATH)
	@Security.Authenticated(Secured.class)
	public static Result deleteMembershipRole(
			@ApiParam(name="id", value="Membership ID") Long id, 
			@ApiParam(name="rid", value="Role ID") Long rid) {
		Membership m = Membership.read(id);
		if (m != null) {
			// TODO move all the role checking to another common place
			// TODO move all the role checking to another common place
			User requestor = User.findByAuthUserIdentity(PlayAuthenticate
					.getUser(session()));
			Boolean authorization = false;
			// what's the requestor membership in the group/assembly related to
			// this membership
			Membership requestorMembership = MembershipsDelegate
					.requestorMembership(requestor, m);

			if (requestorMembership != null) {
				authorization = MembershipsDelegate.requestorIsCoordinator(
						requestor, requestorMembership);
			} else {
				return unauthorized(Json.toJson(new TransferResponseStatus(
						ResponseStatus.UNAUTHORIZED,
						"Requestor is not member of this "
								+ m.getMembershipType())));
			}

			if (authorization) {

				if (m.getRoles().size() > 1) {
					SecurityRole membershipRole = SecurityRole.read(rid);
					m.getRoles().remove(membershipRole);
					m.update();
					m.refresh();
					return ok(Json.toJson(m));
				} else {
					// leave always at least one Role
					return badRequest(Json.toJson(new TransferResponseStatus(
							ResponseStatus.BADREQUEST,
							"Memberships must have at least one role")));
				}
			} else {
				return unauthorized(Json.toJson(new TransferResponseStatus(
						ResponseStatus.UNAUTHORIZED, "Requestor is not"
								+ MyRoles.COORDINATOR)));
			}
		} else {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage("There is no membership with ID = "
					+ id);
			return notFound(Json.toJson(responseBody));
		}
	}

	// PUT /api/membership/:id controllers.Memberships.update(id: Long)
	@ApiOperation(httpMethod = "PUT", response = Membership.class, produces = "application/json", value = "Update status of a MEMBERSHIP", notes="Only for COORDINATORS")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	// TODO: implement @Dynamic(value="CoordinatorOfAssemblyOrGroup", meta=SecurityModelConstants.MEMBERSHIP_RESOURCE_PATH)
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.MEMBERSHIP_RESOURCE_PATH)
	public static Result updateMembershipStatus(
			@ApiParam(name="id", value="Membership ID") Long id, 
			@ApiParam(name="status", value="New Membership Status") String status) {
		String upStatus = status.toUpperCase();
		Membership m = Membership.read(id);
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));
		Boolean authorization = false;
		// what's the requestor membership in the group/assembly related to
		// this membership
		Membership requestorMembership = MembershipsDelegate
				.requestorMembership(requestor, m);

		// TODO: authorization no longer needed here, see the DynamicResourceHandler CoordinatorOfAssembly
		if (requestorMembership != null) {
			authorization = MembershipsDelegate.requestorIsCoordinator(
					requestor, requestorMembership);
		} else {
			return unauthorized(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.UNAUTHORIZED,
							"Requestor is not member of this "
									+ m.getMembershipType())));
		}

		if (authorization) {
			m.setStatus(MembershipStatus.valueOf(upStatus));
			m.update();
			m.refresh();
			return ok(Json.toJson(m));
		} else {
			return unauthorized(Json.toJson(new TransferResponseStatus(
					ResponseStatus.UNAUTHORIZED, "Requestor is not"
							+ MyRoles.COORDINATOR)));
		}
	}

	// DELETE /api/membership/:id controllers.Memberships.delete(id: Long)
	@ApiOperation(httpMethod = "DELETE", response = TransferResponseStatus.class, produces = "application/json", value = "Delete a MEMBERSHIP", notes="Only for COORDINATORS and the User of the membership")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	// TODO: move authorization logic to a DynamicResourceHandler "CoordinatorOrSelf"
	@Security.Authenticated(Secured.class)
	public static Result deleteMembership(
			@ApiParam(name="id", value="Membership ID") Long id) {
		Membership m = Membership.read(id);
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// Any user can delete their own memberships
		if (MembershipsDelegate.isMembershipOfRequestor(requestor, m)) {
			m.delete();
			return ok(Json.toJson(new TransferResponseStatus(ResponseStatus.OK,
					"Membership was deleted")));
		} else {
			// Also COORDINATORS of the associated assembly/group can delete memberships
			Boolean authorization = false;

			// what's the requestor membership in the group/assembly related to this membership
			Membership requestorMembership = MembershipsDelegate.requestorMembership(requestor, m);

			if (requestorMembership != null) {
				authorization = MembershipsDelegate.requestorIsCoordinator(
						requestor, requestorMembership);
			} else {
				return unauthorized(Json.toJson(new TransferResponseStatus(
						ResponseStatus.UNAUTHORIZED,
						"Requestor is not member of this "
								+ m.getMembershipType())));
			}

			if (authorization) {
				m.delete();
				return ok(Json.toJson(new TransferResponseStatus(
						ResponseStatus.OK, "Membership was deleted")));
			} else {
				return unauthorized(Json.toJson(new TransferResponseStatus(
						ResponseStatus.UNAUTHORIZED, "Requestor is not"
								+ MyRoles.COORDINATOR)));
			}

		}
	}

	// GET /api/membership/verify/:token
	// controllers.Memberships.verifyMembership(token: String)
	@ApiOperation(httpMethod = "GET", response = TransferResponseStatus.class, produces = "application/json", value = "Delete a MEMBERSHIP", notes="Only for COORDINATORS and the User of the membership")
	// TODO: move authorization logic to a DynamicResourceHandler "CoordinatorOrSelf"
	public static Result verifyMembership(
			@ApiParam(name="id", value="Membership ID") Long id, 
			@ApiParam(name="token", value="Membership invitation token") String token) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final TokenAction ta = Users.tokenIsValid(token,
				Type.MEMBERSHIP_INVITATION);
		if (ta == null) {
			return badRequest(Json.toJson(Messages
					.get("playauthenticate.token.error.message")));
			// TODO content negotiation: if content-type is HTML, render the
			// response in HTML
			// return badRequest(no_token_or_invalid.render());
		}

		final String email = ta.targetUser.getEmail();
		Membership.verify(id, ta.targetUser);
		return ok(Json.toJson(Messages.get(
				"playauthenticate.verify_email.success", email)));
	}

	/****************************************************************************************************************
	 * Not exposed methods
	 ****************************************************************************************************************/
	/**
	 * General create membership method (not exposed in the API)
	 * 
	 * @param membershipType
	 * @return
	 * @throws MembershipCreationException
	 */
	protected static Result createMembership(User requestor,
			String targetCollection, Long targetCollectionId,
			String membershipType, Long userId, String userEmail,
			Long defaultRoleId, String defaultRoleName)
			throws MembershipCreationException {

		Pair<Membership, TransferResponseStatus> result = MembershipsDelegate
				.createMembership(requestor, targetCollection,
						targetCollectionId, membershipType, userId, userEmail,
						defaultRoleId, defaultRoleName);
		Membership m = result.getFirst();
		TransferResponseStatus r = result.getSecond();
		if (r == null) {
			return ok(Json.toJson(m));
		} else {
			if (r.getResponseStatus().equals(ResponseStatus.BADREQUEST))
				return badRequest(Json.toJson(r));
			else if (r.getResponseStatus().equals(ResponseStatus.NODATA))
				return notFound(Json.toJson(r));
			else if (r.getResponseStatus().equals(ResponseStatus.NOTAVAILABLE))
				return notFound(Json.toJson(r));
			else if (r.getResponseStatus().equals(ResponseStatus.SERVERERROR))
				return internalServerError(Json.toJson(r));
			else
				return unauthorized(Json.toJson(r));
		}
	}

	/**
	 * General create invitation method (not exposed in the API)
	 * 
	 * @param creator
	 * @param invitation
	 * @return
	 * @throws MembershipCreationException
	 */
	private static MembershipInvitation createAndSendInvitation(User creator,
			InvitationTransfer invitation) throws MembershipCreationException {
		MembershipInvitation membershipInvitation = MembershipInvitation
				.create(invitation, creator);
		return membershipInvitation;
	}
}
