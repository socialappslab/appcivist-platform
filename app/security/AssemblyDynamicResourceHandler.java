package security;

import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import enums.ManagementTypes;
import enums.MembershipTypes;
import enums.MyRoles;
import models.*;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Http.Context;

import java.util.List;

public class AssemblyDynamicResourceHandler extends AbstractDynamicResourceHandler {

    public Promise<Boolean> checkPermission(String permissionValue,
            DeadboltHandler deadboltHandler, Context ctx) {
        // TODO Auto-generated method stub
        return super.checkPermission(permissionValue, deadboltHandler, ctx);
    }

    // TODO: we need two dynamic handlers for /membership: one for GET and DELETE, and one for PUT
    @Override
    public Promise<Boolean> isAllowed(String rule, String resource, DeadboltHandler deadboltHandler, Context context) {

        return deadboltHandler.getSubject(context).map(subjectOption ->
            {
                Logger.debug("AUTHORIZATION: Checking for " + context.request().method() + " on " + context.request().path());
                final boolean[] allowed = {false};
                if (new DeadboltAnalyzer().hasRole(subjectOption, "ADMIN")) {
                    allowed[0] = true;
                } else {
                    subjectOption.ifPresent(subject ->
                    {

                        User u = User.findByUserName(subject.getIdentifier());
                        Logger.debug("User " + u.getName());
                        if (u != null)
                            u.setSessionLanguage();
                        String path = context.request().path();
                        String method = context.request().method();
                        String res = resource;
                        Long assemblyId = null;
                        Long groupId = null;
                        Assembly a = null;
                        AssemblyProfile ap = null;
                        Membership m = null;
                        WorkingGroup wg = null;
                        WorkingGroupProfile wgp = null;
                        Boolean isGroupMembership = false;
                        Boolean requestorIsOwnerOfMembership = false;
                        if (resource.equals(SecurityModelConstants.MEMBERSHIP_RESOURCE_PATH)) {
                            // We are dealing with a Membership record
                            // - The user of the membership can read and delete
                            // - Only Assembly/WG Coordinators can edit the Status of the Membership

                            Long membershipId = MyDynamicResourceHandler.getIdFromPath(path, resource);
                            Logger.debug("AUTHORIZATION: Checking user of requested membership = " + membershipId);
                            m = Membership.read(membershipId);

                            if (m != null && m.getMembershipType().equals("ASSEMBLY")) {
                                // We are dealing with an ASSEMBLY membership
                                // - The user of the membership can read and delete
                                // - Only Assembly Coordinators can edit the Membership
                                MembershipAssembly mAssembly = (MembershipAssembly) m;
                                a = mAssembly.getAssembly();
                                assemblyId = a.getAssemblyId();
                                isGroupMembership = false;
                            } else {
                                // We are dealing with a WORKING GROUP membership
                                // - The user of the membership can read and delete
                                // - Only Assembly and WG coordinators can edit the Membership
                                Logger.debug("AUTHORIZATION: Membership of Working Group");
                                MembershipGroup mGroup = (MembershipGroup) m;
                                m = mGroup;
                                wg = mGroup.getWorkingGroup();
                                groupId = wg.getGroupId();
                                isGroupMembership = true;
                            }

                            if (m != null && u.getUserId() != m.getUser().getUserId()) {
                                // user who called the endpoint is NOT the same as the one in the Membership record
                                // use the caller membership
                                Logger.debug("AUTHORIZATION: Membership does not belong to requestor");
                                m = isGroupMembership ? MembershipGroup.findByUserAndGroup(u, wg) : MembershipAssembly.findByUserAndAssembly(u, a);
                            } else {
                                // user is same as membership, therefore only GET and DELETE are allowed
                                Logger.debug("AUTHORIZATION: Membership belongs to requestor. Allow only GET and " +
                                        "DELETE if requestor is not COORDINATOR.");
                                requestorIsOwnerOfMembership = true;
                            }
                        } else {
                            Logger.debug("AUTHORIZATION: Checking membership of User in " + resource + "...");
                            if (resource != null && resource.equals("assembly/")) {
                                assemblyId = MyDynamicResourceHandler.getIdFromPath(path, resource);
                                a = Assembly.read(assemblyId);
                                m = MembershipAssembly.findByUserAndAssembly(u, a);
                                isGroupMembership = false;
                            } else if ((resource != null && resource.equals("group/"))) {
                                groupId = MyDynamicResourceHandler.getIdFromPath(path, resource);
                                wg = WorkingGroup.read(groupId);
                                m = MembershipGroup.findByUserAndGroup(u, wg);
                                isGroupMembership = true;
                            }
                        }

                        if (a != null) {
                            ap = a.getProfile();
                        } else if (wg != null) {
                            wgp = wg.getProfile();
                        }

                        Boolean targetCollectionIsOpen = false;
                        if (ap != null) {
                            targetCollectionIsOpen = ap.getManagementType().equals(ManagementTypes.OPEN);
                        } else if (wgp != null) {
                            targetCollectionIsOpen = wgp.getManagementType().equals(ManagementTypes.OPEN);
                        }

                        // TODO: move this logic into a new Dynamic Handler called CoordinatorOfGroupOrAssembly and keep only COORDINATOR of assembly here
                        if (m != null && rule.equals("CoordinatorOfAssembly") && !targetCollectionIsOpen) {
                            Logger.debug("AUTHORIZATION --> Checking if user" + m.getUser().getUserId() + " is Coordinator");
                            List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.COORDINATOR.getName());
                            allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();

                            if (!allowed[0] && isGroupMembership) {
                                List<Long> assemblyIDs = wg.getAssemblies();
                                for (Long aid : assemblyIDs) {
                                    Assembly groupAssembly = Assembly.read(aid);
                                    allowed[0] = MembershipAssembly.hasRole(u, groupAssembly, MyRoles.COORDINATOR);
                                    if (allowed[0]) {
                                        break;
                                    }
                                }
                            }
                            if (!allowed[0]) {
                                if (requestorIsOwnerOfMembership) {
                                    allowed[0] = method.equals("GET") || method.equals("DELETE");
                                }
                            }
                        } else if (m != null && rule.equals("AssemblyMemberIsExpert")) {
                            Logger.debug("AUTHORIZATION --> Checking if user is Expert");
                            allowed[0] = MembershipAssembly.hasRole(u, m.getTargetAssembly(), MyRoles.EXPERT);
                        } else if (m != null && rule.equals("ModeratorOfAssembly") && !targetCollectionIsOpen) {
                            Logger.debug("AUTHORIZATION --> Checking if user is Moderator");
                            allowed[0] = MembershipAssembly.hasRole(u, m.getTargetAssembly(), MyRoles.MODERATOR);
                        } else {
                            Logger.debug("AUTHORIZATION --> Checking if user is Member");
                            allowed[0] = m != null;
                            if (!allowed[0]) {
                                Logger.debug("AUTHORIZATION --> Checking if user has at least an Invitation");
                                // Check if the user has been invited. In which case, it will be considered a member
                                MembershipInvitation mi = MembershipInvitation.findByUserIdTargetIdAndType(u.getUserId(), assemblyId, MembershipTypes.ASSEMBLY);
                                allowed[0] = mi != null;
                            }
                        }
                        Logger.debug("--> User authorization for " + resource + " " + assemblyId + " is " + allowed[0]);
                    });
                }
                Logger.debug("Allowed " + allowed[0]);
                return allowed[0];

        });

    }
}
