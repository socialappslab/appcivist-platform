package security;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;

public class OnlyMeDynamicResourceHandler extends AbstractDynamicResourceHandler {

	@Override
	public boolean checkPermission(String permissionValue,
			DeadboltHandler deadboltHandler, Context ctx) {
		// 
		return super.checkPermission(permissionValue, deadboltHandler, ctx);
	}

	/**
	 * Allow access to a user resource only if himself is requesting
	 */
	@Override
	public boolean isAllowed(String name, String meta,
			DeadboltHandler deadboltHandler, Context context) {
		Promise<Subject> subjectPromise = deadboltHandler.getSubject(context);
		Subject subject = subjectPromise.get(0);
		boolean allowed = false;
		if (DeadboltAnalyzer.hasRole(subject, "ADMIN")) {
			return true;
		} else {
			String path = context.request().path();
			Long requestedResourceId = MyDynamicResourceHandler.getIdFromPath(path, meta);
			Long requestorId = new Long(subject.getIdentifier());
			
			Logger.debug("Checking relationship of...");
			Logger.debug("--> userId = "+requestorId);
			Logger.debug("--> requestedResourceId = "+requestedResourceId);
			Logger.debug("--> type of resource= "+meta);
			Logger.debug("Checking for path "+meta+requestedResourceId);
			
			Long requestedId = MyDynamicResourceHandler.getIdFromPath(path, SecurityModelConstants.USER_RESOURCE_PATH);
			
			allowed = requestorId == requestedId;
		}
		return allowed;
	}
}
