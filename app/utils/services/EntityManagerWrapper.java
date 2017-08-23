package utils.services;

import models.User;
import models.transfer.PreferenceTransfer;
import play.Logger;
import play.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utils.GlobalData;

import java.util.HashMap;

/**
 * Created by ggaona on 21/8/17.
 */
public class EntityManagerWrapper {

    private final String USERS = "users/";
    private final String IDENTITIES = "identities/";
    private final String PREFERENCES = "preferences/";

    private static final long DEFAULT_TIMEOUT = 20000;


    public WSResponse getUser(User user) {

        String pathParams = user.getUuidAsString();
        try {
            WSRequest holder = getWSHolder(USERS, "GET", pathParams, "");
            Logger.info("ENTITY MANAGER: Getting user: " + holder.getUrl());
            F.Promise<WSResponse> promise = wsSend(holder);
            WSResponse response = promise.get(DEFAULT_TIMEOUT);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a user
     *
     * @param user {
     *             "userId": "<value of appcivistUserUUID>",
     *             "name": "<value of appcivist User's Name and Lastname",
     *             "lang": "<value of appcivist User Lang"
     *             }
     * @return
     */
    public void createUser(User user) throws Exception {

        HashMap<String, String> u = new HashMap<>();
        u.put("userId", user.getUuidAsString());
        u.put("name", user.getName());
        u.put("lang", user.getLang());

        WSRequest holder = getWSHolder(USERS, "POST", u);
        Logger.info("ENTITY MANAGER: Creating user: " + holder.getUrl());
        F.Promise<WSResponse> promise = wsSend(holder);
        WSResponse response = promise.get(DEFAULT_TIMEOUT);

        if (response.getStatus() == 200) {
            Logger.info("ENTITY MANAGER: user created successfully");

        } else {
            throw new Exception(
                    "Error while creating user at entity manager " +
                            response.asJson().toString());
        }

    }

    public void updateIdentities(User user, String serviceName, String identity, Boolean value) throws Exception {

        HashMap<String, Object> u = new HashMap<>();

        u.put("serviceId", serviceName);
        u.put("userId", user.getUuidAsString());
        if (identity != null) {
            u.put("identity", identity);
        }
        if (value != null) {
            u.put("enabled", value);
        }

        WSRequest holder = getWSHolder(IDENTITIES, "PUT", u);
        Logger.info("ENTITY MANAGER: Updating identities: " + holder.getUrl());
        F.Promise<WSResponse> promise = wsSend(holder);
        WSResponse response = promise.get(DEFAULT_TIMEOUT);

        Logger.info("ENTITY MANAGER: Identity: " + u);

        if (response.getStatus() == 200) {
            Logger.info("ENTITY MANAGER: " + serviceName + " created successfully");

        } else {

            throw new Exception("Error while updating user identities at entity manager: " +
                    response.asJson().toString());
        }
    }


    /**
     * Private WS Sender based on a holder
     *
     * @param holder
     * @return
     */
    private F.Promise<WSResponse> wsSend(WSRequest holder) {
        F.Promise<WSResponse> promise = holder.execute().map(
                new F.Function<WSResponse, WSResponse>() {
                    public WSResponse apply(WSResponse response) {
                        return response;
                    }
                });
        return promise;
    }

    private WSRequest getWSHolder(String endpoint, String method, String pathParams, String queryParams) {
        pathParams = pathParams != null && !pathParams.isEmpty() ? pathParams : "";
        queryParams = queryParams != null && !queryParams.isEmpty() ? queryParams : "";
        String notificationServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_URI_ENTITY_MANAGER);

        WSRequest holder = WS.url(notificationServerUrl + endpoint + pathParams + queryParams);
        holder.setMethod(method);
        return holder;

    }

    /**
     * @param endpoint
     * @param method
     * @return
     */
    private WSRequest getWSHolder(String endpoint, String method, Object body) {
        String notificationServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_URI_ENTITY_MANAGER);
        WSRequest holder = WS.url(notificationServerUrl + endpoint);
        holder.setMethod(method);
        holder.setBody(Json.toJson(body));
        return holder;
    }

    public void updateAutoSubcriptions(PreferenceTransfer preferenceUpdate, User subscriber) throws Exception{


        WSRequest holder = getWSHolder(PREFERENCES + subscriber.getUuidAsString(), "PUT", preferenceUpdate);
        Logger.info("ENTITY MANAGER: Updating preferences: " + holder.getUrl());
        F.Promise<WSResponse> promise = wsSend(holder);
        WSResponse response = promise.get(DEFAULT_TIMEOUT);


        if (response.getStatus() == 200) {
            Logger.info("ENTITY MANAGER: " + PREFERENCES + " created successfully");

        } else {

            throw new Exception("Error while updating user preferences at entity manager: " +
                    response.asJson().toString());
        }
    }
}
