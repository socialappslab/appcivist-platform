package controllers;

import play.Logger;
import play.api.mvc.Session;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

public class Secured extends Security.Authenticator {

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
	
	public static boolean isLoggedIn(Context ctx) {
		Secured.handleAuth(ctx);
		
		final AuthUser u = PlayAuthenticate.getUser(ctx.session());
		return u != null;
//		if (u != null) {
//			return u.getId();
//		} else {
//			return null;
//		}
	}

	@Override
	public Result onUnauthorized(final Context ctx) {
//		ctx.flash().put(Application.FLASH_MESSAGE_KEY, "Nice try, but you need to log in first!");
//		return redirect(routes.Application.index());
		return forbidden("you need to log in first!");
	}
	
	public static void handleAuth(final Context ctx){
		String play_session = ctx.request().getHeader("PLAY_SESSION");
		Logger.debug("****** play session header: " + play_session);
		if(play_session != null && !"".trim().equals(play_session)){
			
			try {
				scala.collection.immutable.Map<String, String> values = Session.decode(play_session);
				if(values.size() > 0 ){
					ctx.session().put("pa.u.exp", values.get("pa.u.exp").get());
					ctx.session().put("pa.p.id", values.get("pa.p.id").get());
					ctx.session().put("pa.u.id", values.get("pa.u.id").get());
				}
				//find the way to do it automatically

				
				//getting the request field from anon class
				
//				Field requestField = ctx.session().getClass().getDeclaredField("req$2");
//				play.api.mvc.Request requestInstance = (play.api.mvc.Request) requestField.get(ctx.request());
//				// getting the session field
//				Field sessionField = requestInstance.getClass().getDeclaredField("session");
//				sessionField.setAccessible(true);
//				Session sessionInstance = (Session) sessionField.get(requestInstance);
//				//getting the data from the session
//				Field dataField = sessionInstance.getClass().getDeclaredField("data");
//				dataField.setAccessible(true);
//				dataField.set(sessionInstance, Session.decode(play_session));
				Logger.debug("added to the session: " + ctx.session());
			} catch (Exception e) {
				Logger.error(e.getMessage());
			} 
//				catch (NoSuchFieldException e) {
//				Logger.error(e.getMessage());
//			}catch (IllegalAccessException e) {
//				Logger.error(e.getMessage());
//			}
			
			
			
		}
//		else {
//			if(ctx.request().cookie("PLAY_SESSION") == null){
//				ctx.session().clear();
//			}
//		}
	}
}