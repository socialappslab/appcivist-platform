package security;

import java.util.UUID;

import models.User;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;

// Currently, it only works when meta="User"
public class OnlyMeAndAdminDynamicResourceHandler extends AbstractDynamicResourceHandler {

	@Override
	public Promise<Boolean> checkPermission(String permissionValue,
			DeadboltHandler deadboltHandler, Context ctx) {
		// 
		return super.checkPermission(permissionValue, deadboltHandler, ctx);
	}

	/**
	 * Allow access to a user resource only if himself is requesting
	 */
	@Override
	public Promise<Boolean> isAllowed(String name, String meta,
			DeadboltHandler deadboltHandler, Context context) {
		
		return deadboltHandler.getSubject(context)
							  .map( subjectOption -> {
								  final boolean[] allowed = {false};
									if (new DeadboltAnalyzer().hasRole(subjectOption, "ADMIN")) {
									    Logger.debug("AUTHORIZATION: User is ADMIN");
										allowed[0] = true;
									} else {
										subjectOption.ifPresent(subject -> {
											// 1. Get user associated to the auth token
											User u = User.findByUserName(subject.getIdentifier());
										    if (u!=null) u.setSessionLanguage();
										    Long requestorId = u.getUserId();
										    UUID requestorUUID = u.getUuid();
										    Logger.debug("AUTHORIZATION: Requestor user identified by: " + (requestorId == null ? requestorUUID : requestorId));
										    
											// 2. Get the Requested resource ID or UUID as it is specified in the request PATH
											
											String path = context.request().path();
										    Long requestedResourceId = MyDynamicResourceHandler.getIdFromPath(path, meta);
										    UUID requestedResourceUUID = null;
										    if(requestedResourceId<0) {
										    	requestedResourceUUID = MyDynamicResourceHandler.getUUIDFromPath(path, meta);
										    }									    
										    
										    Logger.debug("AUTHORIZATION: Requested resource '"+meta+"' identified as: " + (requestedResourceId == null ? requestedResourceUUID : requestedResourceId));
										    Logger.debug("AUTHORIZATION: Checking for path /"+meta+(requestedResourceId == null ? requestedResourceUUID : requestedResourceId));
										    
										    // TODO 3. Add control to check whether the User owns the Resource when meta!=USER
										    
										    // 4. Check that the user in the session is the user identified in the endpoint /path
										    Long requestedUserId = null;
										    UUID requestedUserUUID = null;
										    if (meta.equals(SecurityModelConstants.USER_RESOURCE_PATH)) {
										    	requestedUserId = requestedResourceId;
										    } else {
										    	requestedUserId = MyDynamicResourceHandler.getIdFromPath(path, SecurityModelConstants.USER_RESOURCE_PATH);
										    }
										    
										    if(requestedUserId<0) {
										    	requestedUserUUID = MyDynamicResourceHandler.getUUIDFromPath(path, SecurityModelConstants.USER_RESOURCE_PATH);
											    allowed[0] = requestorUUID.equals(requestedUserUUID);
											    Logger.debug("AUTHORIZATION: Allowed = "+allowed[0]);	
										    } else {
											    allowed[0] = requestorId.equals(requestedUserId);
											    Logger.debug("AUTHORIZATION: Allowed = "+allowed[0]);	
										    }
										});
									}
									return allowed[0];
							  });
	}
}
