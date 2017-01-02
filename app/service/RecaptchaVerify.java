package service;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.ConfigurationException;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import utils.GlobalData;

import java.util.ArrayList;

/**
 * Created by vanessa on 01/01/17.
 */
public class RecaptchaVerify {
    String serverURL;
    String secret;
    static long DEFAULT_TIMEOUT = 5000;

    public RecaptchaVerify() {
        secret = Play.application().configuration().getString(GlobalData.CONFIG_RECAPTCHA_SECRET);
        serverURL = Play.application().configuration().getString(GlobalData.CONFIG_RECAPTCHA_SERVER_URL);
    }

    public Result verifyRecaptcha(String hash) {
        try {
            verifyLocalParameters();
            WSRequest holder = WS.url(serverURL);
            holder.setMethod("GET");
            holder.setQueryParameter("secret", secret);
            holder.setQueryParameter("response", hash);
            //RecaptchaRequest rr = new RecaptchaRequest(secret, hash);
            //holder.setBody(Json.toJson(rr));
            //Logger.info("Verifing Recaptcha: " + Json.toJson(rr));
            F.Promise<WSResponse> promise = holder.execute().map(
                    new F.Function<WSResponse, WSResponse>() {
                        public WSResponse apply(WSResponse response) {
                            return response;
                        }
                    });
            WSResponse response = promise.get(DEFAULT_TIMEOUT);
            Logger.info(String.valueOf(response.getStatus()));
            Logger.info(response.getBody());
            return Controller.ok(response.asJson());
        } catch (ConfigurationException e) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.MISSING_CONFIGURATION, e.getMessage()));
            Logger.error("Configuration error: ", e);
            return Controller.internalServerError(Json.toJson(responseBody));
        }
    }

    public boolean verifyLocalParameters() throws ConfigurationException {
        ArrayList<String> missing = new ArrayList<>();
        if (this.secret == null) {
            missing.add(GlobalData.CONFIG_RECAPTCHA_SECRET);
        }
        if (this.serverURL == null) {
            missing.add(GlobalData.CONFIG_RECAPTCHA_SERVER_URL);
        }
        if (missing.size() > 0) {
            throw new ConfigurationException(missing.toString());
        }
        return true;
    }

    public static class RecaptchaRequest {
        String secret;
        String response;
        String remoteip;

        public RecaptchaRequest(String secret, String response) {
            this.secret = secret;
            this.response = response;
        }

        public RecaptchaRequest(String secret, String response, String remoteip) {
            secret = secret;
            response = response;
            remoteip = remoteip;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public String getRemoteip() {
            return remoteip;
        }

        public void setRemoteip(String remoteip) {
            this.remoteip = remoteip;
        }

        @Override
        public String toString() {
            return "RecaptchaRequest{" +
                    "secret='" + secret + '\'' +
                    ", response='" + response + '\'' +
                    ", remoteip='" + remoteip + '\'' +
                    '}';
        }
    }

}