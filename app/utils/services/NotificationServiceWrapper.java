package utils.services;

import models.transfer.NotificationSignalTransfer;
import play.Logger;
import play.Play;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

public class NotificationServiceWrapper {

	private String notificationServerUrl = "";
	private String notificationApiKey = "";
	private final String EVENTS = "/events";
	private final String SIGNALS = "/signals";
	private final String SUBSCRIPTIONS = "/subscriptions";
	private static final long DEFAULT_TIMEOUT = 10000;
	
	
	public NotificationServiceWrapper() {
		this.notificationServerUrl = Play.application().configuration().getString("appcivist.services.notification.default.serverBaseUrl");
		this.notificationApiKey = Play.application().configuration().getString("appcivist.services.notification.default.apiKey");
	}

	public WSResponse sendNotificationSignal(NotificationSignalTransfer notificationSignal) {
		WSRequest holder = WS.url(notificationServerUrl + SIGNALS);
		holder.setMethod("POST");
		holder.setBody(Json.toJson(notificationSignal));
		
		Logger.info("NOTIFICATION: Sending notification signal to notification service: "+holder.getUrl());
		Promise<WSResponse> promise = holder.execute().map(
				new Function<WSResponse, WSResponse>() {
					public WSResponse apply(WSResponse response) {
						return response;
					}
				});
		WSResponse response = promise.get(DEFAULT_TIMEOUT);
		return response;
	}
}
