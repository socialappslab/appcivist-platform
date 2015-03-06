package controllers;

import java.io.UnsupportedEncodingException;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Secured.class)
public class Restricted extends Controller {

	@Security.Authenticated(Secured.class)
	public static Result index() {

		// return ok(restricted.render(localUser));
//		final models.User localUser = Application.getLocalUser(session());
		final play.mvc.Http.Cookie cookie = play.mvc.Http.Context.current()
				.request().cookie("PLAY_SESSION");
//		UserAuthenticatedBean user = new UserAuthenticatedBean();
//		user.setName(localUser.getFname());
//		user.setLastName(localUser.getSname());
//		user.setId(localUser.getId());
//		user.setEmail(localUser.getEmail());
//		user.setPicture(localUser.getPic());
//		if(cookie != null){
//			user.setCookieDomain(cookie.domain());
//			user.setCookieHttpOnly(cookie.httpOnly());
//			user.setCookieName(cookie.name());
//			user.setCookiePath(cookie.path());
//			user.setCookieSecure(cookie.secure());
//			user.setCookieValue(cookie.value());
//		}
//		response().setContentType("text/plain; charset=UTF-8");
//		play.Play.application().configuration().endefaultWebEncoding
//		play.mvc.Http.Context.current().response().encoding.
//		response().setContentType("text/html; charset=utf-8");
//		WS.url(controllers.routes.Restricted.id("#" + cookie.name() +"=" + cookie.value()).url()).
		String url_decoded = "";
		try {
			url_decoded = java.net.URLDecoder.decode(controllers.routes.Restricted.id("!#" + cookie.name() +"=" + cookie.value()).url(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return redirect(url_decoded);
//		return ok(toJson(user));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result id(String id){
//		response().setContentType("text/html; charset=utf-8");
		return ok();
	}
	
}
