package controllers;

import play.*;
import play.mvc.*;
import views.html.*;
import http.Headers;

@With(Headers.class)
public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";


	
	public static Result index() {
		return ok(index.render());
	}

	public static Result checkPreFlight(String path) {
		Logger.debug("--> OPTIONS Preflight REQUEST");
		response().setHeader("Access-Control-Allow-Origin", "*");
		response().setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, OPTIONS");
		response()
				.setHeader("Access-Control-Allow-Headers",
						"accept, origin, Content-type, x-json, x-prototype-version, x-requested-with, SESSION_KEY");
		return ok();
	}
}
