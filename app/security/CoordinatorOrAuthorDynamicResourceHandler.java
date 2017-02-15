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
				 			  if (u!=null) u.setSessionLanguage();
				 			   Logger.debug("Checking membership of User in "+meta+"...");
				 			   Logger.debug("--> userName = " + u.getUsername());
				 			   String path = context.request().path();
				 			   UUID contributionUuid = MyDynamicResourceHandler.getUUIDFromPath(path, meta);
				 			   Logger.debug("--> contributionUuid = " + contributionUuid);
				 			   Contribution contribution = Contribution.readByUUID(contributionUuid);
							   if (contribution!=null) {
								   Long contributionId = contribution.getContributionId();
								   allowed[0] = Contribution.isUserAuthor(u,contributionId);
								   if(allowed[0]==false){
										   Long assemblyId = null;
										   Assembly a = null;
										   AssemblyProfile ap = null;
										   Membership m = null;
										   assemblyId = contribution.getAssemblyId();
										   a = Assembly.read(assemblyId);
										   if (a!=null) {
											   m = MembershipAssembly.findByUserAndAssemblyIds(u.getUserId(), assemblyId);
											   ap = a.getProfile();
										   }

										   Boolean assemblyNotOpen = true;
										   if (ap!=null) {
											   assemblyNotOpen = ap.getManagementType().equals(ManagementTypes.OPEN);

										   }
										   if (m!=null && assemblyNotOpen) {
											   Logger.debug("AUTHORIZATION --> Checking if user is Coordinator");
											   List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.COORDINATOR.getName());
											   allowed[0] = membershipRoles != null && !membershipRoles.isEmpty();
										   }
								   }
							   } else {
								   allowed[0] = false;
							   }
				 		   });
				 	   }
				 	   return allowed[0];
				  });	
	}
}
