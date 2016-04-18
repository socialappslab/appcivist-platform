package utils;

import java.util.Calendar;

import models.Log;

public class LogActions {
	public static void logActivity(String u, String a, String r) {
		Log l = new Log();
		l.setUser(u);
		l.setAction(a);
		l.setPath(r);
		l.setTime(Calendar.getInstance().getTime());
		LogActions.logActivity(l);
	}
	
	public static Log logActivity(Log l) {
		l.save();
		l.refresh();
		return l;
	}
}
