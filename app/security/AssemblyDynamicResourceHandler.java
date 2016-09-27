package security;

import java.util.List;

import models.*;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import enums.ManagementTypes;
import enums.MembershipTypes;
import enums.MyRoles;

public class AssemblyDynamicResourceHandler extends AbstractDynamicResourceHandler {

    public Promise<Boolean> checkPermission(String permissionValue,
            DeadboltHandler deadboltHandler, Context ctx) {
        // TODO Auto-generated method stub
        return super.checkPermission(permissionValue, deadboltHandler, ctx);
    }

    @Override
    public Promise<Boolean> isAllowed(String rule, String resource, DeadboltHandler deadboltHandler, Context context) {
        return deadboltHandler.getSubject(context)
                               .map(subjectOption -> {
                                   final boolean[] allowed = {false};
                                   if (new DeadboltAnalyzer().hasRole(subjectOption, "ADMIN")) {
                                       allowed[0] = true;
                                   } else {
                                       subjectOption.ifPresent(subject -> {
                                           User u = User.findByUserName(subject.getIdentifier());
                                           if (u!=null) u.setSessionLanguage();
                                           String path = context.request().path();
                                           Long assemblyId = null;
                                           Assembly a = null;
                                           Boolean isMembershipGroup = false;
                                           AssemblyProfile ap = null;
                                           Membership m = null;
                                           if (rule.equals("CoordinatorOfAssembly")) {
                                               Long membershipId = MyDynamicResourceHandler.getIdFromPath(path, resource);
                                               Membership membership = Membership.read(membershipId);
                                               if (membership.getMembershipType().equals("ASSEMBLY")) {
                                                   MembershipAssembly mAssembly = (MembershipAssembly) MembershipAssembly.read(membershipId);
                                                   assemblyId = mAssembly.getAssembly().getAssemblyId();
                                                   a = Assembly.read(assemblyId);
                                               } else {
                                                   MembershipGroup mGroup = (MembershipGroup) MembershipGroup.read(membershipId);
                                                   // if one group has many assemblies or no one, assemblyId and assembly are null
                                                   // and m is de membership of the request
                                                   m = mGroup;
                                               }
                                           } else {
                                               assemblyId = MyDynamicResourceHandler.getIdFromPath(path, resource);
                                               a = Assembly.read(assemblyId);
                                           }
                                           Logger.debug("Checking membership of User in "+resource+"...");
                                           Logger.debug("--> userName = " + u.getUsername());
                                           Logger.debug("--> assemblyId= " + assemblyId);
                                           if (a!=null) {
                                               m = MembershipAssembly.findByUserAndAssemblyIds(u.getUserId(), assemblyId);
                                               ap = a.getProfile();
                                           }

                                           Boolean assemblyNotOpen = true;
                                           if (ap!=null) {
                                        	   assemblyNotOpen = ap.getManagementType().equals(ManagementTypes.OPEN);
                                           }
                                           if (m!=null && rule.equals("CoordinatorOfAssembly") && assemblyNotOpen) {
                                               Logger.debug("--> Checking if user is Coordinator");
                                               List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.COORDINATOR.getName());
                                               allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();
                                           } else if (m!=null && rule.equals("AssemblyMemberIsExpert") && assemblyNotOpen) {
                                               Logger.debug("--> Checking if user is Expert");
                                               List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.EXPERT.getName());
                                               allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();
                                           } else if (m!=null && rule.equals("ModeratorOfAssembly") && assemblyNotOpen) {
                                               Logger.debug("--> Checking if user is Moderator");
                                               List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.MODERATOR.getName());
                                               allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();                                           
                                           } else {
                                             Logger.debug("--> Checking if user is Member");
                                             allowed[0] = m!=null; 
                                             if(!allowed[0]) {
                                                 Logger.debug("--> Checking if user has at least an Invitation");
                                            	 // Check if the user has been invited. In which case, it will be considered a member
                                            	 MembershipInvitation mi = MembershipInvitation.findByUserIdTargetIdAndType(u.getUserId(), assemblyId, MembershipTypes.ASSEMBLY);
                                            	 allowed[0] =  mi!=null;
                                             }
                                           }
                                           Logger.debug("--> User authorization for "+resource+" "+assemblyId+" is "+allowed[0]);
                                       });
                                   }
                                   return allowed[0];
                               });    
    }
}
