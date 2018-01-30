package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.feth.play.module.pa.PlayAuthenticate;
import delegates.NotificationsDelegate;
import http.Headers;
import io.swagger.annotations.*;
import models.ResourceSpace;
import models.Subscription;
import models.User;
import models.transfer.TransferResponseStatus;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.GlobalData;

import java.util.List;

@Api(value = "06 subscriptions: Subscriptions management")
@With(Headers.class)
public class Subscriptions extends Controller {

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Return all subscriptions in SID", httpMethod = "GET")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No subscriptions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result findByResourceSpace(Long sid) {
        ResourceSpace rs = ResourceSpace.read(sid);
        if(rs == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The resource space doesn't exist")));
        }
        List<Subscription> aRet = Subscription.findSubscriptionBySpaceId(rs.getUuidAsString());
        if(aRet.isEmpty()) {
            return notFound(Json.toJson(new TransferResponseStatus("No subscriptions found")));
        }
        return ok(Json.toJson(aRet));
    }

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Return a subscription", httpMethod = "GET")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No subscription found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result findByResourceSpaceAndId(Long sid, Long subid) {
        ResourceSpace rs = ResourceSpace.read(sid);
        if(rs == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The resource space doesn't exist")));
        }
        Subscription aRet = Subscription.findSubscriptionBySpaceIdAndId(rs.getUuidAsString(), subid);
        if (aRet == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The subscription doesn't exist")));
        }
        return ok(Json.toJson(aRet));
    }

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Unsubscribe to receive notifications for events in resource space", httpMethod = "POST")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result deleteSubscription(Long sid, Long subid) {
        // TODO: review the manageSubscriptionToResourceSpace method
        User subscriber = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        ResourceSpace rs = ResourceSpace.read(sid);
        Subscription aRet = Subscription.findSubscriptionBySpaceIdAndId(rs.getUuidAsString(), subid);
        if(aRet == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The subscription doesn't exist")));
        }
        aRet.delete();
        return NotificationsDelegate.manageSubscriptionToResourceSpace("UNSUBSCRIBE", rs, "email", subscriber);    }
}
