package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.feth.play.module.pa.PlayAuthenticate;
import delegates.NotificationsDelegate;
import enums.SubscriptionTypes;
import http.Headers;
import io.swagger.annotations.*;
import models.ResourceSpace;
import models.Subscription;
import models.User;
import models.transfer.TransferResponseStatus;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.GlobalData;

import java.util.List;
import java.util.UUID;

import static play.data.Form.form;

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

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Unsubscribe to receive notifications for event", httpMethod = "DELETE")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No subscription found", response = TransferResponseStatus.class)})
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
        return NotificationsDelegate.manageSubscriptionToResourceSpace("UNSUBSCRIBE", rs, "email", subscriber);
    }

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Update subscription", httpMethod = "PUT")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result updateSubscription(Long sid, Long subid) {
        Form<Subscription> subscriptionForm = form(Subscription.class).bindFromRequest();
        if (subscriptionForm.hasErrors()) {
            return badRequest(Json.toJson(TransferResponseStatus.badMessage("Subscription error", subscriptionForm.errorsAsJson().toString())));
        }
        Subscription newSubscription = subscriptionForm.get();
        ResourceSpace rs = ResourceSpace.read(sid);
        if(rs == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The resource space doesn't exist")));
        }
        Subscription old = Subscription.findSubscriptionBySpaceIdAndId(rs.getUuidAsString(), subid);
        if( old == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The subscription doesn't exist")));
        }
        return ok(Json.toJson(Subscription.update(newSubscription,  old)));
    }


    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Return a subscription", httpMethod = "GET")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No subscription found", response = TransferResponseStatus.class)})
    public static Result findByByResourceSpaceUUIDAndIdentifier(String suuid, String identifier) {

        ResourceSpace rs = ResourceSpace.readByUUID(UUID.fromString(suuid));
        if(rs == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The resource space doesn't exist")));
        }
        Subscription aRet = Subscription.findSubscriptionBySpaceIdAndIdentifier(suuid, identifier);
        if (aRet == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The subscription doesn't exist")));
        }
        return ok(Json.toJson(aRet));
    }

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Subscribe to receive notifications for events in resource space", httpMethod = "POST")
    public static Result subscribeByResourceSpaceUUIDAndIdentifier(String suuid, String identifier) {
        User subscriber = new User();
        subscriber.setEmail(identifier);
        ResourceSpace rs = ResourceSpace.readByUUID(UUID.fromString(suuid));
        if(rs == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The resource space doesn't exist")));
        }
        Subscription old = Subscription.findSubscriptionBySpaceIdAndIdentifier(suuid, identifier);
        if (old != null) {
            return badRequest(Json.toJson(TransferResponseStatus.badMessage("Subscription error", "The email is already subscribed to the resource space")));
        }
        Subscription sub = new Subscription();
        sub.setSpaceId(rs.getUuidAsString());
        sub.setSubscriptionType(SubscriptionTypes.REGULAR);
        sub.setSpaceType(rs.getType());
        sub.setDefaultIdentity(identifier);
        sub.insert();
        return NotificationsDelegate.manageSubscriptionToResourceSpace("SUBSCRIBE", rs, "email", subscriber);
    }

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Update subscription", httpMethod = "PUT")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class)})
    public static Result updateByResourceSpaceUUIDAndIdentifier(String suuid, String identifier) {
        Form<Subscription> subscriptionForm = form(Subscription.class).bindFromRequest();
        if (subscriptionForm.hasErrors()) {
            return badRequest(Json.toJson(TransferResponseStatus.badMessage("Subscription error", subscriptionForm.errorsAsJson().toString())));
        }
        Subscription newSubscription = subscriptionForm.get();
        ResourceSpace rs = ResourceSpace.readByUUID(UUID.fromString(suuid));
        Subscription old = Subscription.findSubscriptionBySpaceIdAndIdentifier(suuid, identifier);
        if(rs == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The resource space doesn't exist")));
        }
        if( old == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The subscription doesn't exist")));
        }
        return ok(Json.toJson(Subscription.update(newSubscription,  old)));
    }

    @ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Unsubscribe to receive notifications for event", httpMethod = "DELETE")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No subscription found", response = TransferResponseStatus.class)})
    public static Result unsuscribeByResourceSpaceUUIDAndIdentifier(String suuid, String identifier) {
        ResourceSpace rs = ResourceSpace.readByUUID(UUID.fromString(suuid));
        Subscription aRet = Subscription.findSubscriptionBySpaceIdAndIdentifier(rs.getUuidAsString(), identifier);
        if(aRet == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The subscription doesn't exist")));
        }
        aRet.delete();
        User subscriber = new User();
        subscriber.setEmail(identifier);
        return NotificationsDelegate.manageSubscriptionToResourceSpace("UNSUBSCRIBE", rs, "email", subscriber);
    }

}
