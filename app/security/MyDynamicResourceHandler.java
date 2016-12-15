package security;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;

public class MyDynamicResourceHandler implements DynamicResourceHandler {

    private static final Map<String, Optional<DynamicResourceHandler>> HANDLERS = new HashMap<String, Optional<DynamicResourceHandler>>();

    private static final DynamicResourceHandler DENY = new DynamicResourceHandler() {
        @Override
        public F.Promise<Boolean> isAllowed( String s,
        		                             String s1,
        		                             DeadboltHandler deadboltHandler, 
        		                             Context context) {
            return F.Promise.pure(false);        
        }

        @Override
        public F.Promise<Boolean> checkPermission( String s,
                                                   DeadboltHandler deadboltHandler, 
                                                   Context context) {
            return F.Promise.pure(false);
        }
    };

	static {
		// for each, the "meta" propery is used to pass down information on how to recognize to what
		// set of resources we are referring
		HANDLERS.put("MemberOfGroup", Optional.of(new GroupDynamicResourceHandler())); 
		HANDLERS.put("MemberOfAssembly", Optional.of(new AssemblyDynamicResourceHandler())); 
		HANDLERS.put("CoordinatorOfGroup", Optional.of(new GroupDynamicResourceHandler())); 
		HANDLERS.put("CoordinatorOfAssembly", Optional.of(new AssemblyDynamicResourceHandler())); 
		HANDLERS.put("ModeratorOfAssembly", Optional.of(new AssemblyDynamicResourceHandler()));
		HANDLERS.put("ModeratorOfGroup", Optional.of(new GroupDynamicResourceHandler())); 
		HANDLERS.put("GroupMemberIsExpert", Optional.of(new GroupDynamicResourceHandler())); 
		HANDLERS.put("AssemblyMemberIsExpert", Optional.of(new AssemblyDynamicResourceHandler())); 
		HANDLERS.put("CanInviteToGroup", Optional.of(new GroupDynamicResourceHandler())); 
		HANDLERS.put("CanInviteToAssembly", Optional.of(new AssemblyDynamicResourceHandler())); 
		HANDLERS.put("OnlyMe", Optional.of(new OnlyMeDynamicResourceHandler()));
		HANDLERS.put("OnlyMeAndAdmin", Optional.of(new OnlyMeAndAdminDynamicResourceHandler())); 
		HANDLERS.put("MemberOrListed", Optional.of(new AssemblyProfileDynamicResourceHandler())); 
		HANDLERS.put("AuthorOfContribution", Optional.of(new ContributionDynamicResourceHandler())); 
		HANDLERS.put("Anonymous", Optional.of(new AnonymousDynamicResourceHandler())); 
	}

    @Override
    public Promise<Boolean> checkPermission(String permissionValue,
            DeadboltHandler deadboltHandler, Context ctx) {
        return deadboltHandler.getSubject(ctx)
                .map(subjectOption -> {
                    final boolean[] permissionOk = {false};
                    subjectOption.ifPresent(subject -> {
                        List<? extends Permission> permissions = subject.getPermissions();
                        for (Iterator<? extends Permission> iterator = permissions.iterator(); !permissionOk[0] && iterator.hasNext(); )
                        {
                            Permission permission = iterator.next();
                            permissionOk[0] = permission.getValue().contains(permissionValue);
                        }
                    });

                    return permissionOk[0];
                });
    
    }

    @Override
    public Promise<Boolean>  isAllowed(final String name, 
                                       final String meta, 
                                       final DeadboltHandler deadboltHandler, 
                                       final Context ctx) {
        return HANDLERS.get(name)
                       .orElseGet(() -> {
                            Logger.error("No handler available for " + name);
                            return DENY;
                       })
                       .isAllowed(name, 
                                     meta, 
                                     deadboltHandler, 
                                     ctx);
    }
    
    public static Long getIdFromPath(String path, String id_from){
        String id = StringUtils.substringAfter(path, id_from);
        if(StringUtils.contains(id, "/"))
            id = id.split("/")[0];
        try {
			return Long.parseLong(id);
		} catch (Exception e) {
			return new Long(-1);
		}
    }
    
    public static UUID getUUIDFromPath(String path, String id_from){
        String id = StringUtils.substringAfter(path, id_from);
        if(StringUtils.contains(id, "/"))
            id = id.split("/")[0];
        return id !=null ? UUID.fromString(id) : null;
    }
}
