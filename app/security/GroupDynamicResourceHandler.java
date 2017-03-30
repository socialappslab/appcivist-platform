package security;

import java.util.List;

import enums.ManagementTypes;
import enums.MembershipTypes;
import enums.MyRoles;
import models.MembershipGroup;
import models.Membership;
import models.MembershipInvitation;
import models.SecurityRole;
import models.User;
import models.WorkingGroup;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;

public class GroupDynamicResourceHandler extends AbstractDynamicResourceHandler {

  public Promise<Boolean> checkPermission(String permissionValue,
      DeadboltHandler deadboltHandler, Context ctx) {
    // TODO Auto-generated method stub
    return super.checkPermission(permissionValue, deadboltHandler, ctx);
  }

  @Override
  public Promise<Boolean> isAllowed(String rule, String resource,
      DeadboltHandler deadboltHandler, Context context) {

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
                                           Long groupId = MyDynamicResourceHandler.getIdFromPath(path, resource);
                                           Logger.debug("Checking membership of User in "+resource+"...");
                                           Logger.debug("--> userName = " + u.getUsername());
                                           Logger.debug("--> assemblyId= " + groupId);
                                     Membership m = MembershipGroup.findByUserAndGroupId(u.getUserId(), groupId);
                                     WorkingGroup wg = WorkingGroup.read(groupId);
                                           Boolean groupNotOpen = !wg.getProfile().getManagementType().equals(ManagementTypes.OPEN);
                                           if(wg.getIsTopic()){
                                              groupNotOpen = false;
                                           }
                                     if (m!=null && rule.equals("CoordinatorOfGroup") && groupNotOpen) {
                                               List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.COORDINATOR.getName());
                                               allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();
                                           } else if (m!=null && rule.equals("GroupMemberIsExpert") && groupNotOpen) {
                                               List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.EXPERT.getName());
                                               allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();
                                           } else if (m!=null && rule.equals("ModeratorOfGroup") && groupNotOpen) {
                                               List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.MODERATOR.getName());
                                               allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();                                           
                                           } else {
                                             allowed[0] = m!=null; 
                                             if(!allowed[0]) {
                                               // Check if the user has been invited. In which case, it will be considered a member
                                               MembershipInvitation mi = MembershipInvitation.findByUserIdTargetIdAndType(u.getUserId(), groupId, MembershipTypes.GROUP);
                                               allowed[0] =  mi!=null;
                                             }
                                           }
                                   });
                                 }
                                 return allowed[0];
                               });  
  }

}
