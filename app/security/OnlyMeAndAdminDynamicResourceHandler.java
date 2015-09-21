package security;

import java.util.UUID;

import models.User;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;

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
										allowed[0] = true;
									} else {
										subjectOption.ifPresent(subject -> {
											String path = context.request().path();
										    Long requestedResourceId = MyDynamicResourceHandler.getIdFromPath(path, meta);
										    UUID requestedResourceUUID = null;
										    if(requestedResourceId<0) {
										    	requestedResourceUUID = MyDynamicResourceHandler.getUUIDFromPath(path, meta);
										    }									    
										    User u = User.findByUserName(subject.getIdentifier());
										    Long requestorId = u.getUserId();
										    UUID requestorUUID = u.getUuid();
										    
										    Logger.debug("Checking relationship of '"+meta+"' and user");
										    
										    // TODO: check permission of user on resource
										    
										    Logger.debug("--> userId = "+requestorId);
										    Logger.debug("--> userUUID = "+requestorUUID);
										    Logger.debug("--> requestedResourceId = "+requestedResourceId);
										    Logger.debug("--> requestedResourceUUID = "+requestedResourceUUID);
										    Logger.debug("--> type of resource= "+meta);
										    Logger.debug("Checking for path "+meta+requestedResourceId);
										    
										    Long requestedId = MyDynamicResourceHandler.getIdFromPath(path, SecurityModelConstants.USER_RESOURCE_PATH);
										    UUID requestedUUID = null;
										    if(requestedId<0) {
										    	requestedUUID = MyDynamicResourceHandler.getUUIDFromPath(path, SecurityModelConstants.USER_RESOURCE_PATH);
											    allowed[0] = requestorUUID == requestedUUID;
										    } else {
											    allowed[0] = requestorId == requestedId;
										    }
										});
									}
									return allowed[0];
							  });
	}
}
