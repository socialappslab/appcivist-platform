package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

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

	/**
	 * Controller action added to support CORS requests
	 * 
	 * @param path
	 * @return
	 */
	public static Result checkPreFlight(String path) {
		Logger.debug("--> OPTIONS Preflight REQUEST");
		response().setHeader("Access-Control-Allow-Origin", "*");
		response().setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, OPTIONS");
		response()
				.setHeader("Access-Control-Allow-Headers",
						"accept, origin, Content-type, x-json, x-prototype-version, x-requested-with, SESSION_KEY");
		return ok();
	}
	
	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}
}
