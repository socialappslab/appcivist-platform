package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import io.swagger.annotations.*;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.api.mvc.CookieBaker;
import play.api.mvc.Session;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.*;
//import play.mvc.Http.Session;
import play.mvc.Http.Context;
import service.PlayAuthenticateLocal;
import service.RecaptchaVerify;
import utils.GlobalData;
import utils.LogActions;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import http.Headers;

@With(Headers.class)
public class Secured extends Security.Authenticator {

	@ApiOperation(response = TransferResponseStatus.class, produces = "application/json", value = "Verify recaptcha", httpMethod = "POST")
	@ApiResponses(value = {@ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class)})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
	@Restrict({@Group(GlobalData.USER_ROLE)})
	public static Result verifyRecaptcha(String k) {
		Logger.info("RECIBIDO: " + k);
		RecaptchaVerify rv = new RecaptchaVerify();
		Result response = rv.verifyRecaptcha(k);
		return response;
	}

	/**
	 * getUsername returns the User's ID (i.e., its email for password providers)
	 */
	@Override
	public String getUsername(Context ctx) {
		Secured.handleAuth(ctx);
		final AuthUser u = PlayAuthenticate.getUser(ctx.session());
		if (u != null) {
			return u.getId();
		} else {
			return null;
		}
	}

	/**
	 * Checks the SESSION_KEY to see it has expired or not
	 * @param ctx
	 * @return
	 */
	public static boolean isLoggedIn(Context ctx) {
		Secured.handleAuth(ctx);
		final AuthUser u = PlayAuthenticate.getUser(ctx.session());
		return u != null;
	}

	@Override
	public Result onUnauthorized(final Context ctx) {
		// ctx.flash().put(Application.FLASH_MESSAGE_KEY,
		// "Nice try, but you need to log in first!");
		// return redirect(routes.Application.index());
		return forbidden("you need to log in first!");
	}
	
	/**
	 * Analyzes the SESSION_KEY contained in the body of the request (as a header)
	 * and checks if it's properly signed and if it hasn't expired
	 * 
	 * @param ctx Context object containing the request
	 */
	public static void handleAuth(final Context ctx) {
		String play_session = ctx.request().getHeader("SESSION_KEY");
		Logger.debug("AUTH: authenticating session header: " + play_session);
		if (play_session != null && !"".trim().equals(play_session)) {
			try {
				scala.collection.immutable.Map<String, String> values = (scala.collection.immutable.Map<String, String>) Session.decode(play_session);
				if (values.size() > 0) {
					String user_exp = values.get(PlayAuthenticateLocal.EXPIRES_KEY).get();
					String provider_id = values.get(PlayAuthenticateLocal.PROVIDER_KEY).get();
					String user_id = values.get(PlayAuthenticateLocal.USER_KEY).get();

					Logger.debug("AUTH: session expiring code: " + user_exp);
					Logger.debug("AUTH: session provider id: " + provider_id);
					Logger.debug("AUTH: session user id: " + user_id);
					Logger.debug("AUTH: request uri: " + ctx.request().uri());

					ctx.session().put(PlayAuthenticateLocal.EXPIRES_KEY, user_exp);
					ctx.session().put(PlayAuthenticateLocal.PROVIDER_KEY, provider_id);
					ctx.session().put(PlayAuthenticateLocal.USER_KEY, user_id);
				} else {
					Logger.debug("AUTH: session key decoding not successful");
				}
				Logger.debug("AUTH: added to the session: " + ctx.session());
			} catch (Exception e) {
				Logger.error(e.getMessage());
			}
		} else {
			if (ctx.request().cookie("SESSION_KEY") == null) {
				ctx.session().clear();
			}
		}
	}
}