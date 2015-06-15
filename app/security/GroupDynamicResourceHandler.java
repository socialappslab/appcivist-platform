package security;

import play.mvc.Http.Context;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;

public class GroupDynamicResourceHandler extends AbstractDynamicResourceHandler {

	@Override
	public boolean checkPermission(String permissionValue,
			DeadboltHandler deadboltHandler, Context ctx) {
		// TODO Auto-generated method stub
		return super.checkPermission(permissionValue, deadboltHandler, ctx);
	}

	@Override
	public boolean isAllowed(String name, String meta,
			DeadboltHandler deadboltHandler, Context ctx) {
		// TODO Auto-generated method stub
		return super.isAllowed(name, meta, deadboltHandler, ctx);
	}

}
