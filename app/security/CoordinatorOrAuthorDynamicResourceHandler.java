package security;

import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import enums.ManagementTypes;
import enums.MembershipTypes;
import enums.MyRoles;
import enums.ResourceSpaceTypes;
import models.*;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Http.Context;

import java.util.List;
import java.util.UUID;

public class CoordinatorOrAuthorDynamicResourceHandler extends AbstractDynamicResourceHandler {

	public Promise<Boolean> checkPermission(String permissionValue,
			DeadboltHandler deadboltHandler, Context ctx) {
		// TODO Auto-generated method stub
		return super.checkPermission(permissionValue, deadboltHandler, ctx);
	}

	@Override
	public Promise<Boolean> isAllowed(String name, String meta,
									  DeadboltHandler deadboltHandler, Context context) {

		return deadboltHandler
				.getSubject(context)
				.map(subjectOption -> {
					final boolean[] allowed = {false};
					if (new DeadboltAnalyzer().hasRole(subjectOption, "ADMIN")) {
						allowed[0] = true;
					} else {
						subjectOption.ifPresent(subject -> {
							User u = User.findByUserName(subject.getIdentifier());
							if (u != null) u.setSessionLanguage();
							Logger.debug("Checking membership of User in " + meta + "...");
							Logger.debug("--> userName = " + u.getUsername());
							String path = context.request().path();
							Contribution contribution = getContributionFromPath(path, meta);
							if (contribution != null) {
								Long contributionId = contribution.getContributionId();
								// first we ask if the user is author of this proposal
								allowed[0] = Contribution.isUserAuthor(u, contributionId);

								// if user is not author, we ask if the user is coordinator of one of the spaces
								// in which it has been added
								if (!allowed[0]) {
                                    Logger.debug("AUTHORIZATION:User is not author of contribution = " + contributionId);

                                    checkIfCoordinator(contribution, allowed, u);
								}
							} else {
                                Logger.debug("AUTHORIZATION: FALSE");
                                allowed[0] = false;
							}
						});
					}
					return allowed[0];
				});
	}

	public static Contribution getContributionFromPath(String path, String meta) {
		UUID contributionUuid = MyDynamicResourceHandler.getUUIDFromPath(path, meta);
		Boolean metaIsResourceSpace = false;
		if (meta.equals("space/")) {
			metaIsResourceSpace = true;
		}

		Contribution contribution;
		if (contributionUuid != null) {
			Logger.debug("--> contributionUuid = " + contributionUuid);
			if (metaIsResourceSpace) {
				Logger.info("Authentication of authorship is on the resource space of the contribution. Getting contribution from resource space id...");
				ResourceSpace rs = ResourceSpace.readByUUID(contributionUuid);
				contribution = rs.getContribution();
			} else {
                Logger.info("Authentication of authorship is on the contribution. Getting contribution by id...");
                contribution = Contribution.readByUUID(contributionUuid);
			}
		} else {
			Long contributionId = MyDynamicResourceHandler.getIdFromPath(path, meta);
			Logger.debug("--> contributionId = " + contributionId);
			if (metaIsResourceSpace) {
				Logger.info("Authentication of authorsip is on the resource space of the contribution. Getting contribution from resource space id...");
				ResourceSpace rs = ResourceSpace.read(contributionId);
				contribution = rs.getContribution();
			} else {
                Logger.info("Authentication of authorship is on the contribution. Getting contribution by id...");
				contribution = Contribution.read(contributionId);
			}
		}
		return contribution;
	}

	public static void checkIfCoordinator(Contribution contribution, final boolean[] allowed, User u) {

		Long assemblyId = null;
		Assembly a = null;
		AssemblyProfile ap = null;
		Long groupId = null;
		WorkingGroup wg = null;
		WorkingGroupProfile wgp = null;
		boolean isCoordinator = false;


		// we look at all the resource spaces that contain the contribution
		// if the user is a coordinator of any of them, we will return a positive authorization
		List<ResourceSpace> containingSpaces = contribution.getContainingSpaces();
		Logger.debug("AUTHORIZATION:Contribution has been added to this number of spaces = " +  containingSpaces.size()+"");
		for (ResourceSpace s : containingSpaces) {
			if (s.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
				Campaign c = s.getCampaign();
				Logger.debug("AUTHORIZATION: Checking assemblies of campaign = " + c.getCampaignId());
				List<Long> assemblyIds = c.getAssemblies();
				for (Long aid : assemblyIds) {
					Logger.debug("AUTHORIZATION: Checking if user is coordinator of assembly = " + aid);
					a = Assembly.read(aid);
					if (a != null) {
						ap = a.getProfile();
						isCoordinator = MembershipAssembly.hasRole(u, a, MyRoles.COORDINATOR);
					}
					Boolean assemblyIsOpen = false;
					if (ap != null) {
						assemblyIsOpen = ap.getManagementType().equals(ManagementTypes.OPEN);
					}
					if (isCoordinator && !assemblyIsOpen) {
						allowed[0] = true;
						Logger.debug("AUTHORIZATION: User is coordinator of assembly = " + aid);
						break;
					} else if (assemblyIsOpen) {
						// If assemblyIsOpen, then only authors are allowed to edit, there are no coordinators
						Logger.debug("AUTHORIZATION: Assembly is OPEN. There are no coordinators of assembly = " + aid);
					} else  {
						Logger.debug("AUTHORIZATION: User is NOT COORDINATOR of assembly = " + aid);
					}
				}
				if (allowed[0]) {
					break;
				}
			} else if (s.getType().equals(ResourceSpaceTypes.ASSEMBLY)) {
				a = s.getAssemblyResources();
				assemblyId = a.getAssemblyId();
				Logger.debug("AUTHORIZATION: Checking if user is coordinator of assembly = " + assemblyId);
				isCoordinator = MembershipAssembly.hasRole(u, a, MyRoles.COORDINATOR);
				ap = a.getProfile();
				Boolean assemblyIsOpen = false;
				if (ap != null) {
					assemblyIsOpen = ap.getManagementType().equals(ManagementTypes.OPEN);
				}
				if (isCoordinator && !assemblyIsOpen) {
					allowed[0] = true;
					if (allowed[0]) {
						Logger.debug("AUTHORIZATION: User is coordinator of assembly = " + assemblyId);
					} else {
						Logger.debug("AUTHORIZATION: User is NOT coordinator of assembly = " + assemblyId);
					}
				} else if (assemblyIsOpen) {
					// If assemblyIsOpen, then only authors are allowed to edit, there are no coordinators
					Logger.debug("AUTHORIZATION: Assembly is OPEN. There are no coordinators of assembly = " + assemblyId);
				}
			} else if (s.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
				wg = s.getWorkingGroupResources();
				groupId = wg.getGroupId();
				Logger.debug("AUTHORIZATION: Checking if user is coordinator of group = " + groupId);
				isCoordinator = MembershipGroup.hasRole(u, wg, MyRoles.COORDINATOR);
				wgp = wg.getProfile();
				Boolean groupIsOpen = false;
				if (ap != null) {
					groupIsOpen = wgp.getManagementType().equals(ManagementTypes.OPEN);
				}
				if (isCoordinator && !groupIsOpen) {
					allowed[0] = true;
					if (allowed[0]) {
						Logger.debug("AUTHORIZATION: User is coordinator of group = " + groupId);
					} else {
						Logger.debug("AUTHORIZATION: User is NOT coordinator of group = " + groupId);
					}
				} else if (groupIsOpen) {
					// If assemblyIsOpen, then only authors are allowed to edit, there are no coordinators
					Logger.debug("AUTHORIZATION: Group is OPEN. There are no coordinators of group = " + groupId);
				}
			}
		}
		if (containingSpaces.size() == 0) {
			Logger.debug("AUTHORIZATION: Contribution is no resource space");
		}
	}

}
