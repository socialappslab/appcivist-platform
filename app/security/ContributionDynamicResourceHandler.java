package security;

import java.util.List;


import models.Contribution;
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

public class ContributionDynamicResourceHandler extends AbstractDynamicResourceHandler {

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
				 			   Logger.debug("Checking membership of User in "+meta+"...");
				 			   Logger.debug("--> userName = " + u.getUsername());
				 			   String path = context.request().path();
				 			   Long contributionId = MyDynamicResourceHandler.getIdFromPath(path, meta);
				 			   Logger.debug("--> contributionId = " + contributionId);
		 			   
						       if (name.equals("AuthorOfContribution")) {
						    	   allowed[0] = Contribution.isUserAuthor(u,contributionId);
						       } else { 
							       allowed[0] = false;
						       }
				 			   
				 		   });
				 	   }
				 	   return allowed[0];
				  });	
	}
}
