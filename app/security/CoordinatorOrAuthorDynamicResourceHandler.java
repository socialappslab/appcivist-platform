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
							UUID contributionUuid = MyDynamicResourceHandler.getUUIDFromPath(path, meta);
							Contribution contribution = null;
							if (contributionUuid != null) {
								Logger.debug("--> contributionUuid = " + contributionUuid);
								contribution = Contribution.readByUUID(contributionUuid);
							} else {
								Long contributionId = MyDynamicResourceHandler.getIdFromPath(path, meta);
								Logger.debug("--> contributionId = " + contributionId);
								contribution = Contribution.read(contributionId);
							}

							if (contribution != null) {
								Long contributionId = contribution.getContributionId();
								// first we ask if the user is author of this proposal
								allowed[0] = Contribution.isUserAuthor(u, contributionId);

								// if user is not author, we ask if the user is coordinator of one of the spaces
								// in which it has been added
								if (allowed[0] == false) {
                                    Logger.debug("AUTHORIZATION:User is not author of contribution = " + contributionId);

                                    Long assemblyId = null;
									Assembly a = null;
									AssemblyProfile ap = null;
									Long groupId = null;
									WorkingGroup wg = null;
									WorkingGroupProfile wgp = null;
									Membership m = null;

									// we look at all the resource spaces that contain the contribution
									// if the user is a coordinator of any of them, we will return a positive authorization
									List<ResourceSpace> containingSpaces = contribution.getContainingSpaces();
                                    Logger.debug("AUTHORIZATION:Contribution has been added to this number of spaces = " + containingSpaces!=null ? containingSpaces.size()+"" : "0");
                                    for (ResourceSpace s : containingSpaces) {
										if (s.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
											Campaign c = s.getCampaign();
                                            Logger.debug("AUTHORIZATION: Checking assemblies of campaign = " + c.getCampaignId());
											List<Long> assemblyIds = c.getAssemblies();
                                            for (Long aid : assemblyIds) {
                                                Logger.debug("AUTHORIZATION: Checking if user is coordinator of assembly = " + aid);
												a = Assembly.read(aid);
												if (a != null) {
													m = MembershipAssembly.findByUserAndAssemblyIds(u.getUserId(), aid);
													ap = a.getProfile();
												}
												Boolean assemblyIsOpen = false;
												if (ap != null) {
													assemblyIsOpen = ap.getManagementType().equals(ManagementTypes.OPEN);
												}
												if (m != null && !assemblyIsOpen) {
													Logger.debug("AUTHORIZATION: Checking if user is Coordinator");
													List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.COORDINATOR.getName());
                                                    allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();
													if (allowed[0]) {
                                                        Logger.debug("AUTHORIZATION: User is coordinator of assembly = " + aid);
														assemblyId = aid;
														break;
													}
                                                } else if (assemblyIsOpen) {
												    // If assemblyIsOpen, then only authors are allowed to edit, there are no coordinators
                                                    Logger.debug("AUTHORIZATION: Assembly is OPEN. There are no coordinators of assembly = " + aid);
                                                } else if (m == null) {
                                                    Logger.debug("AUTHORIZATION: User is NOT member of assembly = " + aid);
                                                }
											}
											if (allowed[0]) {
												break;
											}
										} else if (s.getType().equals(ResourceSpaceTypes.ASSEMBLY)) {
											a = s.getAssemblyResources();
											assemblyId = a.getAssemblyId();
                                            Logger.debug("AUTHORIZATION: Checking if user is coordinator of assembly = " + assemblyId);
											if (a != null) {
												m = MembershipAssembly.findByUserAndAssemblyIds(u.getUserId(), assemblyId);
												ap = a.getProfile();
											}
											Boolean assemblyIsOpen = false;
											if (ap != null) {
												assemblyIsOpen = ap.getManagementType().equals(ManagementTypes.OPEN);
											}
											if (m != null && !assemblyIsOpen) {
                                                Logger.debug("AUTHORIZATION: Checking if user is Coordinator");
												List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.COORDINATOR.getName());
												allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();
												if (allowed[0]) {
                                                    Logger.debug("AUTHORIZATION: User is coordinator of assembly = " + assemblyId);
                                                } else {
                                                    Logger.debug("AUTHORIZATION: User is NOT coordinator of assembly = " + assemblyId);
                                                }
											} else if (assemblyIsOpen) {
                                                // If assemblyIsOpen, then only authors are allowed to edit, there are no coordinators
                                                Logger.debug("AUTHORIZATION: Assembly is OPEN. There are no coordinators of assembly = " + assemblyId);
                                            } else if (m == null) {
                                                Logger.debug("AUTHORIZATION: User is NOT member of assembly = " + assemblyId);
                                            }
										} else if (s.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
                                            wg = s.getWorkingGroupResources();
											groupId = wg.getGroupId();
                                            Logger.debug("AUTHORIZATION: Checking if user is coordinator of group = " + groupId);
											if (wg != null) {
												m = MembershipGroup.findByUserAndGroupId(u.getUserId(), groupId);
												wgp = wg.getProfile();
											}
											Boolean groupIsOpen = false;
											if (ap != null) {
												groupIsOpen = wgp.getManagementType().equals(ManagementTypes.OPEN);
											}
											if (m != null && !groupIsOpen) {
												Logger.debug("AUTHORIZATION --> Checking if user is Coordinator");
												List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.COORDINATOR.getName());
												allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();
												if (allowed[0]) {
                                                    Logger.debug("AUTHORIZATION: User is coordinator of group = " + groupId);
                                                } else {
                                                    Logger.debug("AUTHORIZATION: User is NOT coordinator of group = " + groupId);
                                                }
											} else if (groupIsOpen) {
                                                // If assemblyIsOpen, then only authors are allowed to edit, there are no coordinators
                                                Logger.debug("AUTHORIZATION: Group is OPEN. There are no coordinators of group = " + groupId);
                                            } else if (m == null) {
                                                Logger.debug("AUTHORIZATION: User is NOT member of group = " + groupId);
                                            }
										}
									}
									if (containingSpaces!=null && containingSpaces.size() == 0) {
                                        Logger.debug("AUTHORIZATION: Contribution is no resource space");
                                    }
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
}
