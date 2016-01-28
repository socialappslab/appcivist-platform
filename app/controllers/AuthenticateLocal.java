package controllers;

import play.mvc.*;
import play.mvc.Result;
import service.PlayAuthenticateLocal;
import com.feth.play.module.pa.controllers.Authenticate;
import http.Headers;

@With(Headers.class)
public class AuthenticateLocal extends Authenticate {

	private static final String PAYLOAD_KEY = "p";

	public static Result authenticate(final String provider) {
		noCache(response());

		final String payload = request().getQueryString(PAYLOAD_KEY);
		return PlayAuthenticateLocal.handleAuthentication(provider, ctx(), payload);
	}
}
