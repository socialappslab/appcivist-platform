package service.facebook;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import play.Application;
import play.Configuration;
import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Http.Request;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.google.inject.Inject;
import service.facebook.FacebookAuthInfoLocal;

public class FacebookAuthProviderLocal extends
        OAuth2AuthProvider<FacebookAuthUserLocal, FacebookAuthInfoLocal> {

    private static final String MESSAGE = "message";
    private static final String ERROR = "error";
    private static final String FIELDS = "fields";

    public static final String PROVIDER_KEY = "facebook";

    private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
    private static final String USER_INFO_FIELDS_SETTING_KEY = "userInfoFields";

    @Inject
    public FacebookAuthProviderLocal(Application app) {
        super(app);
    }

    public static abstract class SettingKeys extends
            OAuth2AuthProvider.SettingKeys {
        public static final String DISPLAY = "display";
    }

    public static abstract class FacebookConstants extends Constants {
        public static final String DISPLAY = "display";
    }

    @Override
    protected FacebookAuthUserLocal transform(FacebookAuthInfoLocal info, final String state)
            throws AuthException {

        final String url = getConfiguration().getString(
                USER_INFO_URL_SETTING_KEY);
        final String fields = getConfiguration().getString(
                USER_INFO_FIELDS_SETTING_KEY);
        final WSResponse r = WS
                .url(url)
                .setQueryParameter(OAuth2AuthProvider.Constants.ACCESS_TOKEN,
                        info.getAccessToken())
                .setQueryParameter(FIELDS, fields)
                .get().get(getTimeout());

        final JsonNode result = r.asJson();
        if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
            throw new AuthException(result.get(ERROR).get(MESSAGE).asText());
        } else {
            Logger.debug(result.toString());
            return new FacebookAuthUserLocal(result, info, state);
        }
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    protected FacebookAuthInfoLocal buildInfo(final WSResponse r) throws AccessTokenException {

        final JsonNode respJson = r.asJson();

        if (r.getStatus() >= 400) {
            throw new AccessTokenException(r.asJson().get(ERROR).get(MESSAGE).asText());

        } else {

            if (respJson.size() < 2) {
                throw new AccessTokenException("At least two values were expected, but got " + respJson.size());
            }

            return new FacebookAuthInfoLocal(respJson);
        }
    }

    @Override
    protected List<NameValuePair> getAuthParams(final Configuration c,
                                                final Request request, final String state) throws AuthException {
        final List<NameValuePair> params = super.getAuthParams(c, request, state);

        if (c.getString(SettingKeys.DISPLAY) != null) {
            params.add(new BasicNameValuePair(FacebookConstants.DISPLAY, c
                    .getString(SettingKeys.DISPLAY)));
        }

        return params;
    }
}
