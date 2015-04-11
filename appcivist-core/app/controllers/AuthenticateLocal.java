package controllers;

import play.mvc.Result;
import service.PlayAuthenticateLocal;

import com.feth.play.module.pa.controllers.Authenticate;

public class AuthenticateLocal extends Authenticate {

	private static final String PAYLOAD_KEY = "p";

	public static Result authenticate(final String provider) {
		noCache(response());

		final String payload = request().getQueryString(PAYLOAD_KEY);
		return PlayAuthenticateLocal.handleAuthentication(provider, ctx(),
				payload);
	}

	// public static Result logout(){
	// play.mvc.Http.Context.current().session().clear();
	//
	// try {
	// //getting the request field from anon class
	// Field requestField =
	// play.mvc.Http.Context.current().request().getClass().getDeclaredField("req$2");
	// play.api.mvc.Request requestInstance = (play.api.mvc.Request)
	// requestField.get(play.mvc.Http.Context.current().request());
	// // getting the session field
	// Field sessionField =
	// requestInstance.getClass().getDeclaredField("session");
	// sessionField.setAccessible(true);
	// sessionField.set(requestInstance, null);
	// } catch (SecurityException e) {
	// Logger.error(e.getMessage());
	// } catch (NoSuchFieldException e) {
	// Logger.error(e.getMessage());
	// }catch (IllegalAccessException e) {
	// Logger.error(e.getMessage());
	// }
	//
	//
	//
	// return com.feth.play.module.pa.controllers.Authenticate.logout();
	// }

}
