package controllers;

import com.feth.play.module.pa.providers.AuthProvider;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.Campaign;
import models.transfer.TransferResponseStatus;
import play.libs.Json;
import play.mvc.*;
import play.mvc.Result;
import service.PlayAuthenticateLocal;
import com.feth.play.module.pa.controllers.Authenticate;
import http.Headers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@With(Headers.class)
public class AuthenticateLocal extends Authenticate {

	private static final String PAYLOAD_KEY = "p";

	public static Result authenticate(final String provider) {
		noCache(response());

		final String payload = request().getQueryString(PAYLOAD_KEY);
		return PlayAuthenticateLocal.handleAuthentication(provider, ctx(), payload);
	}

	@ApiOperation(httpMethod = "GET", responseContainer = "List", produces = "application/json", value = "List of authentication providers")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "No provider found", response = TransferResponseStatus.class)})
	public static Result getProviders() {
		List<Map<String, Object>> toRet = new ArrayList<>();
		for (AuthProvider a : AuthProvider.Registry.getProviders()) {
			Map<String, Object> provider = new HashMap<>();
			provider.put("url", a.getUrl());
			provider.put("key", a.getKey());
			toRet.add(provider);
		}
		if(toRet.isEmpty()) {
			return notFound();
		}
		return ok(Json.toJson(toRet));
	}
}
