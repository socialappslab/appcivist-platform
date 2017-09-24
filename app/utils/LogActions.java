package utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

import models.Log;
import play.Logger;
import play.Play;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

public class LogActions {
	
	public static void logActivity(String u, String a, String r) {
		Boolean logActions = Play.application().configuration().getBoolean(GlobalData.CONFIG_USER_ACTIONS_LOGGING);
		if (logActions!=null && logActions) {
			Log l = new Log();
			l.setUser(LogActions.anonymizeUserId(u));
			l.setAction(a);
			l.setPath(r);
			l.setTime(Calendar.getInstance().getTime());
			LogActions.logActivity(l);
		}
	}

	public static void logActivity(String u, String a, String r, String rt, String ruuid) {
		Boolean logActions = Play.application().configuration().getBoolean(GlobalData.CONFIG_USER_ACTIONS_LOGGING);
		if (logActions!=null && logActions) {
			Log l = new Log();
			l.setUser(LogActions.anonymizeUserId(u));
			l.setAction(a);
			l.setPath(r);
			l.setResourceType(rt);
			l.setResourceUuid(ruuid);
			l.setTime(Calendar.getInstance().getTime());
			LogActions.logActivity(l);
		}
	}

	public static void logActivity(String user_id, Context ctx) {
		Boolean logActions = Play.application().configuration().getBoolean(GlobalData.CONFIG_USER_ACTIONS_LOGGING);
		if (logActions!=null && logActions) {
			Log l = new Log();
			l.setUser(LogActions.anonymizeUserId(user_id));
			l.setAction(ctx.request().method());
			l.setPath(ctx.request().uri());
			l.setRemoteAddress(ctx.request().remoteAddress());
			l.setTime(Calendar.getInstance().getTime());
			l.setRemoteHost(ctx.request().getHeader("UI_PATH"));
			l.setComment(LogActions.extractOtherInfoFromRequest(ctx.request()));
			if (ctx.request().uri().equals("/api/log/front")) {
				l.setComment(ctx.request().body().asJson().toString());	
			}
			LogActions.logActivity(l);
		}
	}

	public static void logActivity(String user_id, Request req) {
		Boolean logActions = Play.application().configuration().getBoolean(GlobalData.CONFIG_USER_ACTIONS_LOGGING);
		
		if (logActions!=null && logActions) {
			Log l = new Log();
			l.setUser(LogActions.anonymizeUserId(user_id));
			l.setAction(req.method());
			l.setPath(req.uri());
			l.setRemoteAddress(req.remoteAddress());
			l.setTime(Calendar.getInstance().getTime());
			l.setRemoteHost(req.getHeader("UI_PATH"));
			l.setComment(LogActions.extractOtherInfoFromRequest(req));
			if (req.uri().equals("/api/log/front")) {
				l.setComment(req.body().asJson().toString());	
			}
			LogActions.logActivity(l);
		}
	}

	public static Log logActivity(Log l) {
		Boolean logActions = Play.application().configuration().getBoolean(GlobalData.CONFIG_USER_ACTIONS_LOGGING);
		if (logActions!=null && logActions) {
			l.save();
			l.refresh();
		}
		return l;
	}

	private static String extractOtherInfoFromRequest(Request req) {
		return "{'User-Agent': '"+req.getHeader("User-Agent")+"','Accept-Language':'"+req.getHeader("Accept-Language")+"'}";
	}

	public static String exceptionStackTraceToString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
	/**
	 * Replaces the user id (represented by the email) by the user's UUID (which is not identifiable by itself in the logs). 
	 * TODO: to provide full anonymization, replace the use of the UUID with a Hash function. 
	 * @param email
	 * @return
	 */
	private static String anonymizeUserId(String email) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Logger.error("Error hashing user id: "+e.getLocalizedMessage());
			return email;
		}
	    md.update(email.getBytes());
	    byte[] digest = md.digest();
	    String myHash = DatatypeConverter
	      .printHexBinary(digest);
	    return myHash;
	}
}
