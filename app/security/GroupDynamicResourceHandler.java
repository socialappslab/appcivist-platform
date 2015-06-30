package security;

import models.GroupMembership;
import models.Membership;
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
                            			   Long groupId = MyDynamicResourceHandler.getIdFromPath(path, SecurityModelConstants.USER_RESOURCE_PATH);
                            			   Logger.debug("--> groupId= " + groupId);
                            			   // TODO: add visibility to group
                            			   // WorkingGroup g = WorkingGroup.read(groupId);
                            			   Membership m = GroupMembership.findByUserAndGroupId(u.getUserId(), groupId);
                            			   allowed[0] = m!=null;						    
                            		   });
                            	   }
                            	   return allowed[0];
                               });	
	}

}
