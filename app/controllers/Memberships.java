package controllers;

import static play.data.Form.form;

import java.util.List;

import models.Assembly;
import models.AssemblyMembership;
import models.GroupMembership;
import models.Membership;
import models.SecurityRole;
import models.TokenAction;
import models.TokenAction.Type;
import models.User;
import models.WorkingGroup;
import models.transfer.TransferMembership;
import models.transfer.TransferResponseStatus;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import providers.MyUsernamePasswordAuthProvider;
import utils.GlobalData;
import play.Logger;

import com.feth.play.module.pa.PlayAuthenticate;

import enums.MembershipCreationTypes;
import enums.MembershipRoles;
import enums.MembershipStatus;
import enums.ResponseStatus;

public class Memberships extends Controller {

	public static final Form<TransferMembership> TRANSFER_MEMBERSHIP_FORM = form(TransferMembership.class);
	public static final Form<Membership> MEMBERSHIP_FORM = form(Membership.class);
	public static final Form<SecurityRole> ROLE_FORM = form(SecurityRole.class);

	/**
	 * The membership invitation/request timeout in seconds Defaults to 4 weeks
	 * (24 hours * 30 days * 60 minutes * 60 seconds)
	 */
	public static final Long MEMBERSHIP_EXPIRATION_TIMEOUT = new Long(
			30 * 14 * 3600);


	// TODO: TEST
	@Security.Authenticated(Secured.class)
	public static Result createMembership() {
		// 1. obtaining the user of the requestor
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<TransferMembership> newMembershipForm = TRANSFER_MEMBERSHIP_FORM
				.bindFromRequest();

		if (newMembershipForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					newMembershipForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			TransferMembership newMembership = newMembershipForm.get();
			String targetCollection = newMembership.getTargetCollection();
			Long targetCollectionId = targetCollection.toUpperCase().equals("GROUP") ? newMembership.getGroupId() : newMembership.getAssemblyId();
			
			return createMembership(requestor,
					targetCollection, targetCollectionId,
					newMembership.getType(), newMembership.getUserId(),
					newMembership.getEmail(), newMembership.getDefaultRoleId(), newMembership.getDefaultRoleName());
		}
	}

	/**
	 * Read a membership by ID
	 * 
	 * @param id
	 * @return
	 */
	@Security.Authenticated(Secured.class)
	public static Result readMembership(Long id) {
		Membership m = Membership.read(id);
		if (m != null) {
			List<SecurityRole> roles = m.getRoles();
			Logger.debug("Membership roles: #" +roles.size() +" = "+ roles.toString());
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
	@Security.Authenticated(Secured.class)
	public static Result readMembershipRoles(Long id) {
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
	@Security.Authenticated(Secured.class)
	public static Result addMembershipRole(Long id) {
		Membership m = Membership.read(id);
		if (m != null) {
			// TODO move all the role checking to another common place
			User requestor = User.findByAuthUserIdentity(PlayAuthenticate
					.getUser(session()));
			Boolean authorization = false;
			// what's the requestor membership in the group/assembly related to
			// this membership
			Membership requestorMembership = requestorMembership(requestor, m);

			if (requestorMembership != null) {
				authorization = requestorIsCoordinator(requestor,
						requestorMembership);
			} else {
				return unauthorized(Json.toJson(new TransferResponseStatus(
						ResponseStatus.UNAUTHORIZED,
						"Requestor is not member of this "
								+ m.getMembershipType())));
			}

			if (authorization) {
				final Form<SecurityRole> newRoleForm = ROLE_FORM.bindFromRequest();
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
								+ MembershipRoles.COORDINATOR)));
			}

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
	@Security.Authenticated(Secured.class)
	public static Result deleteMembershipRole(Long id, Long rid) {
		Membership m = Membership.read(id);
		if (m != null) {
			// TODO move all the role checking to another common place
			// TODO move all the role checking to another common place
			User requestor = User.findByAuthUserIdentity(PlayAuthenticate
					.getUser(session()));
			Boolean authorization = false;
			// what's the requestor membership in the group/assembly related to
			// this membership
			Membership requestorMembership = requestorMembership(requestor, m);

			if (requestorMembership != null) {
				authorization = requestorIsCoordinator(requestor,
						requestorMembership);
			} else {
				return unauthorized(Json.toJson(new TransferResponseStatus(
						ResponseStatus.UNAUTHORIZED,
						"Requestor is not member of this "
								+ m.getMembershipType())));
			}

			if (authorization) {
				
				if(m.getRoles().size()>1) {
					SecurityRole membershipRole = SecurityRole.read(rid);
					m.getRoles().remove(membershipRole);
					m.update();
					m.refresh();
					return ok(Json.toJson(m));
				} else {
					// leave always at least one Role				
					return badRequest(Json.toJson(new TransferResponseStatus(
					ResponseStatus.BADREQUEST, "Memberships must have at least one role")));
				}
			} else {
				return unauthorized(Json.toJson(new TransferResponseStatus(
						ResponseStatus.UNAUTHORIZED, "Requestor is not"
								+ MembershipRoles.COORDINATOR)));
			}
		} else {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage("There is no membership with ID = "
					+ id);
			return notFound(Json.toJson(responseBody));
		}
	}

	// PUT /api/membership/:id controllers.Memberships.update(id: Long)
	@Security.Authenticated(Secured.class)
	public static Result updateMembershipStatus(Long id, String status) {
		String upStatus = status.toUpperCase();
		Membership m = Membership.read(id);
		// TODO move all the role checking to another common place
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));
		Boolean authorization = false;
		// what's the requestor membership in the group/assembly related to
		// this membership
		Membership requestorMembership = requestorMembership(requestor, m);

		if (requestorMembership != null) {
			authorization = requestorIsCoordinator(requestor,
					requestorMembership);
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
							+ MembershipRoles.COORDINATOR)));
		}
	}

	// DELETE /api/membership/:id controllers.Memberships.delete(id: Long)
	@Security.Authenticated(Secured.class)
	public static Result deleteMembership(Long id) {
		Membership m = Membership.read(id);
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// Any user can delete their own memberships
		if (isMembershipOfRequestor(requestor, m)) {
			m.delete();
			return ok(Json.toJson(new TransferResponseStatus(ResponseStatus.OK,
					"Membership was deleted")));
		} else {
			// Also COORDINATORS of the associated assembly/group can delete
			// memberships
			Boolean authorization = false;

			// what's the requestor membership in the group/assembly related to
			// this membership
			Membership requestorMembership = requestorMembership(requestor, m);

			if (requestorMembership != null) {
				authorization = requestorIsCoordinator(requestor,
						requestorMembership);
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
								+ MembershipRoles.COORDINATOR)));
			}

		}
	}

	// TODO: TEST
	// GET /api/membership/verify/:token
	// controllers.Memberships.verifyMembership(token: String)
	public static Result verifyMembership(Long id, String token) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final TokenAction ta = Users.tokenIsValid(token, Type.MEMBERSHIP_INVITATION);
		if (ta == null) {
			return badRequest(Json.toJson(Messages.get("playauthenticate.token.error.message")));
			// TODO content negotiation: if content-type is HTML, render the response in HTML
			// return badRequest(no_token_or_invalid.render());
		}
		
		final String email = ta.targetUser.getEmail();
		Membership.verify(id, ta.targetUser);
		return ok(Json.toJson(Messages.get("playauthenticate.verify_email.success", email)));		
	}

	/****************************************************************************************************************
	 * Not exposed methods
	 ****************************************************************************************************************/


	/**
	 * General create membership method (not exposed in the API)
	 * 
	 * @param membershipType
	 * @return
	 */
	protected static Result createMembership(User requestor,
			String targetCollection, Long targetCollectionId, String membershipType,
			Long userId, String userEmail, Long defaultRoleId, String defaultRoleName) {

		// 4. Read the target Group or Assembly from the Database depending
		// on the targetCollection
		WorkingGroup targetWorkingGroup = targetCollection.toUpperCase().equals("GROUP") ? WorkingGroup
				.read(targetCollectionId) : null;
		Assembly targetAssembly = targetCollection.toUpperCase().equals("ASSEMBLY") ? Assembly
				.read(targetCollectionId) : null;
		// 5.Create the correct type of membership depending on the
		// targetCollection
		Membership m = targetCollection.toUpperCase().equals("GROUP") ? new GroupMembership()
				: new AssemblyMembership();

		// 6. Make sure either the assembly or the group exists before
		// proceeding
		if (targetAssembly == null && targetWorkingGroup == null) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					" The target " + targetCollection + " " + targetCollectionId
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
		m.setLang(targetUser.getLanguage());
		m.setExpiration((System.currentTimeMillis() + 1000 * MEMBERSHIP_EXPIRATION_TIMEOUT));
		if(targetCollection.toUpperCase().equals("GROUP")) {
			((GroupMembership) m).setWorkingGroup(targetWorkingGroup);
		} else {
			((AssemblyMembership) m).setAssembly(targetAssembly);
		}
		
		// 9. check if the membership of this user on this assembly/group
		// already exists
		boolean mExists = targetCollection.toUpperCase().equals("GROUP") ? GroupMembership.checkIfExists(m) :
				AssemblyMembership.checkIfExists(m);

		SecurityRole role = SecurityRole.findByName(MembershipRoles.MEMBER.toString());
		if(defaultRoleId != null) 
			role = SecurityRole.read(defaultRoleId);
		else if (defaultRoleName != null)
			m.getRoles().add(role);

		m.getRoles().add(role);
		
		if (!mExists) {
			// 8. Set the initial status of the new membership depending of
			// the type
			if (membershipType.toUpperCase().equals(
					MembershipCreationTypes.INVITATION.toString())) {

				Boolean userCanInvite = targetCollection.toUpperCase().equals("ASSEMBLY") ? Membership.userCanInvite(requestor, targetAssembly) : Membership.userCanInvite(requestor, targetWorkingGroup);
				// 8. Check if the creator is authorized
				if (userCanInvite) {
					m.setStatus(MembershipStatus.INVITED);
					Membership.create(m);
					m.refresh();
					MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider
							.getProvider();
					provider.sendMembershipInvitationEmail(m, targetCollection);
					return ok(Json.toJson(m));

				} else {
					return unauthorized("You don't have the role to send invitations");
				}
			} else if (membershipType.toUpperCase().equals(
					MembershipCreationTypes.REQUEST.toString())) {
				m.setStatus(MembershipStatus.REQUESTED);
				Membership.create(m);
				return ok(Json.toJson(m));
			} else if (membershipType.toUpperCase().equals(
					MembershipCreationTypes.SUBSCRIPTION.toString())) {
				m.setStatus(MembershipStatus.FOLLOWING);
				Membership.create(m);
				return ok(Json.toJson(m));
			} else {
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody
						.setStatusMessage(Messages
								.get(GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
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

	/**
	 * Checks is the user who sent the request is actually the user of this membership
	 * @param requestor
	 * @param m
	 * @return
	 */
	protected static Boolean isMembershipOfRequestor(User requestor,
			Membership m) {
		// is requestor the user in this membership?
		if (requestor.equals(m.getUser())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get the Membership of the requestor, associated to the group or assembly of the membership whose 
	 * ID is given on the request
	 * @param requestor
	 * @param m
	 * @return
	 */
	protected static Membership requestorMembership(User requestor, Membership m) {
		// is requestor the user in this membership?
		if (requestor.equals(m.getUser())) {
			return m;
		} else {
			if (m.getMembershipType().equals("ASSEMBLY")) {
				return AssemblyMembership.findByUserAndAssembly(requestor,
						((AssemblyMembership) m).getAssembly());
			} else {
				return GroupMembership.findByUserAndGroup(requestor,
						((GroupMembership) m).getWorkingGroup());
			}
		}

	}


	/**
	 * Check if the requestor is COORDINATOR of the group or assembly associated to the membership whose 
	 * ID is given on the request
	 * @param requestor
	 * @param m
	 * @return
	 */
	protected static Boolean requestorIsCoordinator(User requestor, Membership m) {
		return requestorHasRole(requestor, m, MembershipRoles.COORDINATOR);
	}

	/**
	 * Check if the requestor has a given Role in the group or assembly associated to the membership whose 
	 * ID is given on the request
	 * @param requestor
	 * @param m
	 * @return
	 */
	protected static Boolean requestorHasRole(User requestor, Membership m,
			MembershipRoles role) {
		if (m != null) {
			for (SecurityRole requestorRole : m.getRoles()) {
				if (requestorRole.getName().equals(role.toString())) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

}
