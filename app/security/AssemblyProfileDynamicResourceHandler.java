package security;

import java.util.List;
import java.util.UUID;

import enums.Visibility;
import enums.MyRoles;
import models.Assembly;
import models.AssemblyProfile;
import models.MembershipAssembly;
import models.Membership;
import models.User;
import models.SecurityRole;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;

public class AssemblyProfileDynamicResourceHandler extends AbstractDynamicResourceHandler {

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
                            			   Logger.info("Checking membership of User in "+meta+"...");
                            			   Logger.debug("--> userName = " + u.getUsername());
                            			   String path = context.request().path();
                            			   Long assemblyId = MyDynamicResourceHandler.getIdFromPath(path, meta);
                            			   UUID assemblyUuid = null;
                            			   
										   if(assemblyId<0) {
											   assemblyUuid = MyDynamicResourceHandler.getUUIDFromPath(path, meta);
											   Logger.debug("--> No assemblyId, using assemblyUuid = " + assemblyUuid);
											   allowed[0]=Assembly.isAssemblyListed(assemblyUuid);
										   } else {
											   Logger.debug("--> assemblyId= " + assemblyId);
											   allowed[0]=Assembly.isAssemblyListed(assemblyId);
										   }
	                        			   
										   // If the Assembly is not listed, then we only care if the user is member
										   if(!allowed[0]) {
											   Membership m = null;
											   if (assemblyId<0) m = MembershipAssembly.findByUserAndAssemblyUuid(u.getUserId(), assemblyUuid);
											   else m = MembershipAssembly.findByUserAndAssemblyIds(u.getUserId(), assemblyId);
											   allowed[0] = m!=null;
										   }
                            		   });
                            	   }
                            	   return allowed[0];
                               });	
	}
}
