package delegates;

import java.util.ArrayList;
import java.util.List;

import models.Assembly;
import models.Membership;
import models.MembershipAssembly;
import models.MembershipGroup;
import models.SecurityRole;
import models.User;
import models.WorkingGroup;
import models.transfer.AssemblySummaryTransfer;
import models.transfer.TransferResponseStatus;

import org.dozer.DozerBeanMapper;

import controllers.Memberships;
import enums.MembershipCreationTypes;
import enums.MembershipStatus;
import enums.MyRoles;
import enums.ResponseStatus;
import play.Play;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import utils.GlobalData;
import utils.Pair;

public class MembershipsDelegate {

	public static DozerBeanMapper mapper;
	static {
		List<String> mappingFiles = Play.application().configuration()
				.getStringList("appcivist.dozer.mappingFiles");
		mapper = new DozerBeanMapper(mappingFiles);
	}

	public static Pair<Membership, TransferResponseStatus> createMembership(
			User requestor, String targetCollection, Long targetCollectionId,
			String membershipType, Long userId, String userEmail,
			Long defaultRoleId, String defaultRoleName) {

		WorkingGroup targetWorkingGroup = targetCollection.toUpperCase()
				.equals("GROUP") ? WorkingGroup.read(targetCollectionId) : null;
		Assembly targetAssembly = targetCollection.toUpperCase().equals(
				"ASSEMBLY") ? Assembly.read(targetCollectionId) : null;
		// 5.Create the correct type of membership depending on the
		// targetCollection
		Membership m = targetCollection.toUpperCase().equals("GROUP") ? new MembershipGroup()
				: new MembershipAssembly();

		TransferResponseStatus errorBody = new TransferResponseStatus();

		// 6. Make sure either the assembly or the group exists before
		// proceeding
		if (targetAssembly == null && targetWorkingGroup == null) {
			errorBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					" The target " + targetCollection + " "
							+ targetCollectionId + " does not exist"));
			errorBody.setResponseStatus(ResponseStatus.BADREQUEST);
			return new Pair<Membership, TransferResponseStatus>(null, errorBody);
		}

		// 7. Set who created the membership
		m.setCreator(requestor);

		// 8. check if user exists
		User targetUser = userId == null ? User.findByEmail(userEmail) : User
				.findByUserId(userId);
		if (targetUser == null) {
			// TODO create membership invitations for not members
			errorBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					" The target user " + userId + " does not exist"));
			errorBody.setResponseStatus(ResponseStatus.BADREQUEST);
			return new Pair<Membership, TransferResponseStatus>(null, errorBody);
		}

		m.setUser(targetUser);
		m.setLang(targetUser.getLanguage());
		m.setExpiration((System.currentTimeMillis() + 1000 * Memberships.MEMBERSHIP_EXPIRATION_TIMEOUT));
		if (targetCollection.toUpperCase().equals("GROUP")) {
			((MembershipGroup) m).setWorkingGroup(targetWorkingGroup);
		} else {
			((MembershipAssembly) m).setAssembly(targetAssembly);
		}

		// 9. check if the membership of this user on this assembly/group
		// already exists
		boolean mExists = targetCollection.toUpperCase().equals("GROUP") ? MembershipGroup
				.checkIfExists(m) : MembershipAssembly.checkIfExists(m);

		SecurityRole role = SecurityRole.findByName(MyRoles.MEMBER.toString());
		if (defaultRoleId != null)
			role = SecurityRole.read(defaultRoleId);
		else if (defaultRoleName != null)
			m.getRoles().add(role);

		m.getRoles().add(role);

		if (!mExists) {
			// 8. Set the initial status of the new membership depending of
			// the type
			if (membershipType.toUpperCase().equals(
					MembershipCreationTypes.INVITATION.toString())) {

				Boolean userCanInvite = targetCollection.toUpperCase().equals(
						"ASSEMBLY") ? Membership.userCanInvite(requestor,
						targetAssembly) : Membership.userCanInvite(requestor,
						targetWorkingGroup);
				// 8. Check if the creator is authorized
				if (userCanInvite) {
					m.setStatus(MembershipStatus.INVITED);
					Membership.create(m);
					m.refresh();
					MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider
							.getProvider();
					provider.sendMembershipInvitationEmail(m, targetCollection);
					return new Pair<Membership, TransferResponseStatus>(m, null);

				} else {
					errorBody.setStatusMessage(Messages.get(
							GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
							" The user is not authorized"));
					errorBody.setResponseStatus(ResponseStatus.UNAUTHORIZED);
					return new Pair<Membership, TransferResponseStatus>(null,
							errorBody);
				}
			} else if (membershipType.toUpperCase().equals(
					MembershipCreationTypes.REQUEST.toString())) {
				m.setStatus(MembershipStatus.REQUESTED);
				Membership.create(m);
				return new Pair<Membership, TransferResponseStatus>(m, null);
			} else if (membershipType.toUpperCase().equals(
					MembershipCreationTypes.SUBSCRIPTION.toString())) {
				m.setStatus(MembershipStatus.FOLLOWING);
				Membership.create(m);
				return new Pair<Membership, TransferResponseStatus>(m, null);
			} else {
				errorBody
						.setStatusMessage(Messages
								.get(GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
										" The request did not specified whether it was an invitation or a join request"));
				errorBody.setResponseStatus(ResponseStatus.UNAUTHORIZED);
				return new Pair<Membership, TransferResponseStatus>(null,
						errorBody);
			}
		} else {
			errorBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					" The target user " + userId
							+ " has already a membership to this "
							+ targetCollection));
			errorBody.setResponseStatus(ResponseStatus.UNAUTHORIZED);
			return new Pair<Membership, TransferResponseStatus>(null, errorBody);
		}
	}
	
	/**
	 * Checks is the user who sent the request is actually the user of this membership
	 * @param requestor
	 * @param m
	 * @return
	 */
	public static Boolean isMembershipOfRequestor(User requestor,
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
	public static Membership requestorMembership(User requestor, Membership m) {
		// is requestor the user in this membership?
		if (requestor.equals(m.getUser())) {
			return m;
		} else {
			if (m.getMembershipType().equals("ASSEMBLY")) {
				return MembershipAssembly.findByUserAndAssembly(requestor,
						((MembershipAssembly) m).getAssembly());
			} else {
				return MembershipGroup.findByUserAndGroup(requestor,
						((MembershipGroup) m).getWorkingGroup());
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
	public static Boolean requestorIsCoordinator(User requestor, Membership m) {
		return requestorHasRole(requestor, m, MyRoles.COORDINATOR);
	}

	/**
	 * Check if the requestor has a given Role in the group or assembly associated to the membership whose 
	 * ID is given on the request
	 * @param requestor
	 * @param m
	 * @return
	 */
	public static Boolean requestorHasRole(User requestor, Membership m,
			MyRoles role) {
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
