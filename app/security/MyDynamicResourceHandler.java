package security;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;

public class MyDynamicResourceHandler implements DynamicResourceHandler {

	private static final Map<String, DynamicResourceHandler> HANDLERS = new HashMap<String, DynamicResourceHandler>();

	static {
		HANDLERS.put("MemberOfGroup", new GroupDynamicResourceHandler()); // for this, meta must be AssemblyId
		HANDLERS.put("MemberOfAssembly", new AssemblyDynamicResourceHandler()); // for this, meta must be GroupId
		HANDLERS.put("CoordinatorOfGroup", new GroupDynamicResourceHandler()); // for this, meta must be AssemblyId
		HANDLERS.put("CoordinatorOfAssembly", new AssemblyDynamicResourceHandler()); // for this, meta must be GroupId
		HANDLERS.put("CanInviteToGroup", new GroupDynamicResourceHandler()); // for this, meta must be AssemblyId
		HANDLERS.put("CanInviteToAssembly", new AssemblyDynamicResourceHandler()); // for this, meta must be GroupId
	}

	@Override
	public boolean checkPermission(String permissionValue,
			DeadboltHandler deadboltHandler, Context ctx) {
		Subject s = (Subject) deadboltHandler.getSubject(ctx);
		List<? extends Permission> permissions = s.getPermissions();
		final boolean[] permissionOk = { false };
		for (Iterator<? extends Permission> iterator = permissions.iterator(); !permissionOk[0]
				&& iterator.hasNext();) {
			Permission permission = iterator.next();
			permissionOk[0] = permission.getValue().contains(permissionValue);
		}
		return permissionOk[0];
	}

	@Override
	public boolean isAllowed(String name, String meta,
			DeadboltHandler deadboltHandler, Context ctx) {
		DynamicResourceHandler handler = HANDLERS.get(name);
		boolean result = false;
		if (handler == null) {
			Logger.error("No handler available for " + name);
		} else {
			result = handler.isAllowed(name, meta, deadboltHandler, ctx);
		}
		return result;
	}

}
