package security;

import models.User;
import play.Logger;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;

public class AssemblyDynamicResourceHandler extends AbstractDynamicResourceHandler {

	@Override
	public boolean checkPermission(String permissionValue,
			DeadboltHandler deadboltHandler, Context ctx) {
		// TODO Auto-generated method stub
		return super.checkPermission(permissionValue, deadboltHandler, ctx);
	}

	@Override
	public boolean isAllowed(String name, String meta,
			DeadboltHandler deadboltHandler, Context ctx) {
		Subject subject = (Subject) deadboltHandler.getSubject(ctx);
		boolean allowed = false;
		
//		// TODO remove ADMINS when app is ready for production
//		// if the user is an ADMIN user, we allow everything
//		if(DeadboltAnalyzer.hasRole(subject, "ADMIN")) {
//			Logger.debug("Requested by an ADMIN...");
//			allowed = true;
//		} else {
			User u = User.findByUserName(subject.getIdentifier());
			Logger.debug("Checking membership of User in Assembly...");
			Logger.debug("--> userName = " + u.getUsername());
			Logger.debug("--> assemblyId= " + meta);
			// TODO how to put the id of the assembly in the meta?? 
//		}
		
		
		
		return super.isAllowed(name, meta, deadboltHandler, ctx);
	}

}
