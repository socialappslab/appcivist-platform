package utils;

import java.util.Calendar;

import models.Log;
import play.Play;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

public class LogActions {
	public static void logActivity(String u, String a, String r) {
		Boolean logActions = Play.application().configuration().getBoolean(GlobalData.CONFIG_USER_ACTIONS_LOGGING);
		if (logActions) {
			Log l = new Log();
			l.setUser(u);
			l.setAction(a);
			l.setPath(r);
			l.setTime(Calendar.getInstance().getTime());
			LogActions.logActivity(l);
		}
	}
	public static void logActivity(String u, String a, String r, String rt, String ruuid) {
		Boolean logActions = Play.application().configuration().getBoolean(GlobalData.CONFIG_USER_ACTIONS_LOGGING);
		if (logActions) {
			Log l = new Log();
			l.setUser(u);
			l.setAction(a);
			l.setPath(r);
			l.setResourceType(rt);
			l.setResourceUuid(ruuid);
			l.setTime(Calendar.getInstance().getTime());
			LogActions.logActivity(l);
		}
	}
	public static Log logActivity(Log l) {
		Boolean logActions = Play.application().configuration().getBoolean(GlobalData.CONFIG_USER_ACTIONS_LOGGING);
		if (logActions) {
			l.save();
			l.refresh();
		}
		return l;
	}
	public static void logActivity(String user_id, Context ctx) {
		Boolean logActions = Play.application().configuration().getBoolean(GlobalData.CONFIG_USER_ACTIONS_LOGGING);
		if (logActions) {
			Log l = new Log();
			l.setUser(user_id);
			l.setAction(ctx.request().method());
			l.setPath(ctx.request().uri());
			l.setRemoteAddress(ctx.request().remoteAddress());
			l.setTime(Calendar.getInstance().getTime());
			LogActions.logActivity(l);
		}
	}
	public static void logActivity(String user_id, Request req) {
		Boolean logActions = Play.application().configuration().getBoolean(GlobalData.CONFIG_USER_ACTIONS_LOGGING);
		if (logActions) {
			Log l = new Log();
			l.setUser(user_id);
			l.setAction(req.method());
			l.setPath(req.uri());
			l.setRemoteAddress(req.remoteAddress());
			l.setTime(Calendar.getInstance().getTime());
			LogActions.logActivity(l);
		}
	}
	
}
