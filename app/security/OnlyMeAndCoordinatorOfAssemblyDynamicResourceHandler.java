package security;

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import models.Contribution;
import models.User;
import play.Logger;
import play.libs.F;
import play.mvc.Http;

import java.util.UUID;

import static security.CoordinatorOrAuthorDynamicResourceHandler.checkIfCoordinator;

public class OnlyMeAndCoordinatorOfAssemblyDynamicResourceHandler extends AbstractDynamicResourceHandler {

    @Override
    public F.Promise<Boolean> checkPermission(String permissionValue,
                                              DeadboltHandler deadboltHandler, Http.Context ctx) {
        //
        return super.checkPermission(permissionValue, deadboltHandler, ctx);
    }

    @Override
    public F.Promise<Boolean> isAllowed(String name, String meta,
                                        DeadboltHandler deadboltHandler, Http.Context context) {


        return deadboltHandler.getSubject(context)
                .map( subjectOption -> {
                    final boolean[] allowed = {false};
                    subjectOption.ifPresent(subject -> {
                        String path = context.request().path();
                        User u = User.findByUserName(subject.getIdentifier());
                        if (u!=null) {
                            u.setSessionLanguage();
                            Long requestorId = u.getUserId();
                            UUID requestorUUID = u.getUuid();
                            Logger.info("Checking relationship of...");
                            Logger.info("--> userId = " + requestorId);
                            Logger.info("--> userUUID = " + requestorUUID);
                            Logger.info("--> type of resource= " + meta);
                            Long requestedId = MyDynamicResourceHandler.getIdFromPath(path,
                                    SecurityModelConstants.AUTHOR_RESOURCE_PATH);
                            UUID requestedUUID;
                            //check only me
                            if (requestedId < 0) {
                                requestedUUID = MyDynamicResourceHandler.getUUIDFromPath(path,
                                        SecurityModelConstants.AUTHOR_RESOURCE_PATH);
                                allowed[0] = requestorUUID.equals(requestedUUID);
                                Logger.info("Allowed = " + allowed[0]);
                            } else {
                                allowed[0] = requestorId.equals(requestedId);
                                Logger.info("Allowed = " + allowed[0]);
                            }

                            //if the author is not itself check if it is the coordinator
                            if (!allowed[0]) {
                                Contribution contribution = CoordinatorOrAuthorDynamicResourceHandler
                                        .getContributionFromPath(path, meta);
                                checkIfCoordinator(contribution, allowed, u);
                            }
                        }
                    });
                    return allowed[0];
                });
    }
}
