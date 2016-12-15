package security;

import models.User;
import models.transfer.TransferResponseStatus;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;

import controllers.Secured;
import enums.ResponseStatus;
import play.Logger;
import play.api.mvc.Session;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Http.Context;
import play.mvc.Result;
import scala.collection.immutable.Map;
import service.PlayAuthenticateLocal;
import utils.LogActions;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;

import java.util.Optional;

/**
 * Default authorization handler
 * 
 * @author cdparra
 *
 */
public class MyDeadboltHandler extends AbstractDeadboltHandler {

	public F.Promise<Optional<Result>> beforeAuthCheck(final Context context) {
		// returning Result=null means that everything is OK. Return a real result if
		// you want a redirect to a login page or
		// somewhere else

		if (Secured.isLoggedIn(context)) {
			String play_session = context.request().getHeader("SESSION_KEY");
			Map<String, String> values = Session.decode(play_session);
			if (values.size() > 0) {
				String user_id = values.get(PlayAuthenticateLocal.USER_KEY).get();
				LogActions.logActivity(user_id, context);
			}
			return F.Promise.promise(() -> Optional.ofNullable(null));
			
		} else {
			// user is not logged in

			// call this if you want to redirect your visitor to the page that
			// was requested before sending him to the login page
			// if you don't call this, the user will get redirected to the page
			// defined by your resolver
			final String originalUrl = context.request().uri();
			Logger.info("AUTH: unauthorized access");
			return Promise.promise(() -> Optional.of(forbidden(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.UNAUTHORIZED,
							"You need to log in first, to view '" + originalUrl
									+ "'")))));
		}

	}

	public Promise<Optional<Subject>> getSubject(Context context) {
		if (!Secured.isLoggedIn(context))
			return F.Promise.promise(() -> Optional.ofNullable(null));

		final AuthUserIdentity u = PlayAuthenticate.getUser(context);
		return Promise.promise(() -> Optional.ofNullable(User
				.findByAuthUserIdentity(u)));
	}

	@Override
	public Promise<Result> onAuthFailure(Context context, String content) {
		return Promise.promise(() -> forbidden(Json
				.toJson(new TransferResponseStatus(ResponseStatus.UNAUTHORIZED,
						"Authentication failed: " + content))));
	}

	public Promise<Optional<DynamicResourceHandler>> getDynamicResourceHandler(
			Context context) {
		return F.Promise.promise(() -> Optional
				.of(new MyDynamicResourceHandler()));
	}
}
