package security;

import models.User;
import models.transfer.TransferResponseStatus;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;

import controllers.Secured;
import enums.ResponseStatus;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Http.Context;
import play.mvc.Result;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;

public class MyDeadboltHandler extends AbstractDeadboltHandler {

	@Override
	public F.Promise<Result> beforeAuthCheck(Context context) {
		if (Secured.isLoggedIn(context)) {
			// user is logged in
			return null;
		} else {
			// user is not logged in

			// call this if you want to redirect your visitor to the page that
			// was requested before sending him to the login page
			// if you don't call this, the user will get redirected to the page
			// defined by your resolver
			final String originalUrl = context.request().uri();

			//			context.flash().put("error",
			//					"You need to log in first, to view '" + originalUrl + "'");
			// return redirect(PlayAuthenticate.getResolver().login());
			
			return Promise.promise(() -> forbidden(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.UNAUTHORIZED,
							"You need to log in first, to view '" + originalUrl
									+ "'"))));
		}
		
	}

	@Override
	public Promise<Subject> getSubject(Context context) {
		if (!Secured.isLoggedIn(context)) {
			return null;
		}

		final AuthUserIdentity u = PlayAuthenticate.getUser(context);
		return Promise.promise(() -> User.findByAuthUserIdentity(u));
		//return User.findByAuthUserIdentity(u);

	}

	@Override
	public Promise<Result> onAuthFailure(Context context, String content) {
		return Promise.promise(() -> forbidden(Json
				.toJson(new TransferResponseStatus(ResponseStatus.UNAUTHORIZED,
						"Authentication failed"))));
	}

	@Override
	public DynamicResourceHandler getDynamicResourceHandler(Context context) {
		return new MyDynamicResourceHandler();
	}

}
