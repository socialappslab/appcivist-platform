package utils.services;

import exceptions.ConfigurationException;
import models.Subscription;
import models.transfer.NotificationEventTransfer;
import models.transfer.NotificationSignalTransfer;
import models.transfer.NotificationSubscriptionTransfer;
import play.Logger;
import play.Play;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utils.GlobalData;

import java.util.ArrayList;

public class NotificationServiceWrapper {

    private String notificationServerUrl = "";
    private String notificationApiKey = "";
    private final String EVENTS = "/events";
    private final String SIGNALS = "/signals";
    private final String SUBSCRIPTIONS = "/subscriptions";
    private final String ENDPOINT = "/endpoint";
    private static final long DEFAULT_TIMEOUT = 10000;


    public NotificationServiceWrapper() throws ConfigurationException {
        this.notificationServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_NOTIFICATION_DEFAULT_SERVER_BASE);
        this.notificationApiKey = Play.application().configuration().getString(GlobalData.CONFIG_NOTIFICATION_DEFAULT_API_KEY);
        ArrayList<String> missing = new ArrayList<>();
        if(this.notificationServerUrl == null){
            missing.add(GlobalData.CONFIG_NOTIFICATION_DEFAULT_SERVER_BASE);
        }
        if(this.notificationApiKey == null){
            missing.add(GlobalData.CONFIG_NOTIFICATION_DEFAULT_API_KEY);
        }
        if (missing.size()>0) {
            throw new ConfigurationException(missing.toString());
        }
    }

    /**
     * Create an Event
     * curl -i -X POST -H 'Content-Type: application/json' -d '{"eventId" : "1234_NEW_CONTRIBUTION_IDEA", "title": "New Widget Build Available"}' http://<hostname>/events
     *
     * @param notificationEvent
     * @return
     */
    public WSResponse createNotificationEvent(NotificationEventTransfer notificationEvent) {
        WSRequest holder = getWSHolder(EVENTS, "POST", notificationEvent);
        Logger.info("NOTIFICATION: Creating notification EVENT  in notification service: " + holder.getUrl() + "---" +notificationEvent.getTitle() );
        Promise<WSResponse> promise = wsSend(holder);
        WSResponse response = promise.get(DEFAULT_TIMEOUT);
        return response;
    }

    /**
     * Subscribe to an Event
     * curl -i -X POST -H 'Content-Type: application/json' -d '{"eventId": "1234_NEW_CONTRIBUTION_IDEA", "alertEndpoint": "account@mail.com", "endpointType" : "email"}'   // TODO: tp://<hostname>/subscriptions
     *
     * @param notificationSubscription
     * @return
     */
    @Deprecated
    public WSResponse createNotificationSubscription(NotificationSubscriptionTransfer notificationSubscription) {
        WSRequest holder = getWSHolder(SUBSCRIPTIONS, "POST", notificationSubscription);
        Logger.info("NOTIFICATION: Creating notification SUBSCRIPTION in notification service: " + holder.getUrl());
        Promise<WSResponse> promise = wsSend(holder);
        WSResponse response = promise.get(DEFAULT_TIMEOUT);
        return response;
    }

    /**
     * Subscribe to an Event
     * curl -i -X POST -H 'Content-Type: application/json' -d '{"eventId": "1234_NEW_CONTRIBUTION_IDEA", "alertEndpoint": "account@mail.com", "endpointType" : "email"}'   // TODO: tp://<hostname>/subscriptions
     *
     * @param notificationSubscription
     * @return
     */
    public WSResponse createNotificationSubscription(Subscription notificationSubscription) {
        WSRequest holder = getWSHolder(SUBSCRIPTIONS, "POST", notificationSubscription);
        Logger.info("NOTIFICATION: Creating notification SUBSCRIPTION in notification service: " + holder.getUrl());
        Promise<WSResponse> promise = wsSend(holder);
        WSResponse response = promise.get(DEFAULT_TIMEOUT);
        return response;
    }

    /**
     * Signal an Event
     * curl -i -XPOST -H 'Content-Type: application/json'
     * -d '{"eventId": "1234_NEW_CONTRIBUTION_IDE", "title": "New IDEA in Quality of Life WG", "text" : "Notification text", "data": "Build ID: dget-2241"}' http://<hostname>/signals
     *
     * @param notificationSignal
     * @return
     */

    public WSResponse sendNotificationSignal(NotificationSignalTransfer notificationSignal) {
        WSRequest holder = getWSHolder(SIGNALS, "POST", notificationSignal);
        Logger.info("NOTIFICATION: Sending notification signal to notification service: " + holder.getUrl());
        Promise<WSResponse> promise = wsSend(holder);
        WSResponse response = promise.get(DEFAULT_TIMEOUT);
        return response;
    }

    /**
     * Delete a Subscription by eventId and alertEndpoint
     * curl -i -X DELETE -H 'Content-Type: application/json' http://<hostname>/subscriptions/<eventId>/<alertEndpoint>
     *
     * @param notificationSignal
     * @return
     */
    public WSResponse deleteSubscription(NotificationSubscriptionTransfer notificationSubscription) {
        String eventId = notificationSubscription.getEventId();
        String alertEndpoint = notificationSubscription.getAlertEndpoint();
        String pathParams = "/" + eventId + "/" + alertEndpoint;
        WSRequest holder = getWSHolder(SUBSCRIPTIONS, "DELETE", pathParams, "");
        Logger.info("NOTIFICATION: Deleting subscription: " + holder.getUrl());
        Promise<WSResponse> promise = wsSend(holder);
        WSResponse response = promise.get(DEFAULT_TIMEOUT);
        return response;
    }


    /**
     * List Subscriptions per alertEndpoint
     * curl -i -X GET -H 'Content-Type: application/json' http://<hostname>/subscriptions/endpoint/<alertEndpoint>
     *
     * @param alertEndpoint
     * @return
     */
    public WSResponse listSubscriptionPerAlertEndpoint(String alertEndpoint) {
        String pathParams = "/" + alertEndpoint;
        WSRequest holder = getWSHolder(SUBSCRIPTIONS + ENDPOINT, "GET", pathParams, "");
        Logger.info("NOTIFICATION: Listing subscriptions for alertEndpoint: " + holder.getUrl());
        Promise<WSResponse> promise = wsSend(holder);
        WSResponse response = promise.get(DEFAULT_TIMEOUT);
        return response;
    }

    // TODO: Delete Subscription
    // curl -i -X DELETE http://<hostname>/subscriptions/<subscription-id>
    // TODO: Delete Event
    // curl -i -X DELETE http://<hostname>/events/<event-id>
    // TODO: Delete Subscription
    // curl -i -X DELETE http://<hostname>/subscriptions/<subscription-id>
    // TODO: Modify Event
    // curl -i -X PUT -H 'Content-Type: application/json' -d '{"description": "Brand new description"}' http://<hostname>/events/<event-id>	

    /**
     * Private WS Sender based on a holder
     *
     * @param holder
     * @return
     */
    private Promise<WSResponse> wsSend(WSRequest holder) {
        Promise<WSResponse> promise = holder.execute().map(
                new Function<WSResponse, WSResponse>() {
                    public WSResponse apply(WSResponse response) {
                        return response;
                    }
                });
        return promise;
    }

    /**
     * @param endpoint
     * @param method
     * @return
     */
    private WSRequest getWSHolder(String endpoint, String method, Object body) {
        WSRequest holder = WS.url(notificationServerUrl + endpoint);
        holder.setMethod(method);
        holder.setHeader("AUTH", notificationApiKey);
        holder.setBody(Json.toJson(body));
        return holder;
    }

    private WSRequest getWSHolder(String endpoint, String method, String pathParams, String queryParams) {
        pathParams = pathParams != null && !pathParams.isEmpty() ? pathParams : "";
        queryParams = queryParams != null && !queryParams.isEmpty() ? queryParams : "";
        WSRequest holder = WS.url(notificationServerUrl + endpoint + pathParams + queryParams);
        holder.setMethod(method);
        holder.setHeader("AUTH", notificationApiKey);
        return holder;
    }

    private WSRequest getWSHolder(String endpoint, String method, String pathParams, String queryParams, Object body) {
        pathParams = pathParams != null && !pathParams.isEmpty() ? "/" + pathParams : "";
        queryParams = queryParams != null && !queryParams.isEmpty() ? "?" + queryParams : "";
        WSRequest holder = WS.url(notificationServerUrl + endpoint + pathParams + queryParams);
        holder.setMethod(method);
        holder.setHeader("AUTH", notificationApiKey);
        holder.setBody(Json.toJson(body));
        return holder;
    }

}
