package security;

import java.util.List;

import models.Membership;
import models.MembershipAssembly;
import models.SecurityRole;
import models.User;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import enums.MyRoles;

public class AssemblyDynamicResourceHandler extends AbstractDynamicResourceHandler {

	public Promise<Boolean> checkPermission(String permissionValue,
			DeadboltHandler deadboltHandler, Context ctx) {
		// TODO Auto-generated method stub
		return super.checkPermission(permissionValue, deadboltHandler, ctx);
	}

	@Override
	public Promise<Boolean> isAllowed(String name, String meta,
			DeadboltHandler deadboltHandler, Context context) {

		 return deadboltHandler.getSubject(context)
                               .map(subjectOption -> {
                            	   final boolean[] allowed = {false};
                            	   if (new DeadboltAnalyzer().hasRole(subjectOption, "ADMIN")) {
                            		   allowed[0] = true;
                            	   } else {
                            		   subjectOption.ifPresent(subject -> {
                            			   User u = User.findByUserName(subject.getIdentifier());
                            			   Logger.debug("Checking membership of User in "+meta+"...");
                            			   Logger.debug("--> userName = " + u.getUsername());
                            			   String path = context.request().path();
                            			   Long assemblyId = MyDynamicResourceHandler.getIdFromPath(path, meta);
                            			   Logger.debug("--> assemblyId= " + assemblyId);
                            			   Membership m = MembershipAssembly.findByUserAndAssemblyIds(u.getUserId(), assemblyId);
                        				   
                        				     if (m!=null && name.equals("CoordinatorOfAssembly")) {
                        					     List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.COORDINATOR.getName());
                        					     allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();
                        				     } else if (m!=null && name.equals("AssemblyMemberIsExpert")) {
                        					     List<SecurityRole> membershipRoles = m.getRoles();
                        					     for (SecurityRole r : membershipRoles) {
                        						     if(r.getName().equals(MyRoles.EXPERT.getName())) {
                        							     allowed[0] = true;
                        						     }
                        					     }
                        				     } else if (m!=null && name.equals("ModeratorOfAssembly")) {
                        					     List<SecurityRole> membershipRoles = m.getRoles();
                        					     for (SecurityRole r : membershipRoles) {
                        						     if(r.getName().equals(MyRoles.MODERATOR.getName())) {
                        							     allowed[0] = true;
                        						     }
                        					     }
                        				     } else 
                        					   allowed[0] = m!=null;
                            			   
                            		   });
                            	   }
                            	   return allowed[0];
                               });	
	}
}
