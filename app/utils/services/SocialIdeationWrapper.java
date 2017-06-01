package utils.services;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import play.Logger;
import play.Play;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

public class SocialIdeationWrapper {
	public static String TOKEN = "";
	private static String TOKEN_NAME = "Token";
	private static String VERSION = "/v4";
	public static String BASE_URL = "";
	private static final long DEFAULT_TIMEOUT = 10000;

	// Mapbox dataset
	// private static String DATASET_PLACES = "/mapbox.places";

	// Include other operations
	// private static String OPERATION_GEOCODE = "/geocode";

	// static {
	// 	TOKEN = Play.application().configuration()
	// 			.getString("appcivist.services.mapboxapi.token");
	// }

	public SocialIdeationWrapper() {
		super();
	}

	public static String createUser(String fbToken, String fbUid, String initiativeUrl) {
		try {
			// String encodedQuery = URLEncoder.encode(query, "UTF-8");
			WSRequest holder = WS.url(BASE_URL+"/login_fb");
			holder.setHeader(TOKEN_NAME, TOKEN);
			holder.setQueryParameter("access_token", fbToken);
			holder.setQueryParameter("user_id", fbUid);
			holder.setQueryParameter("initiative_url", initiativeUrl);
			holder.setMethod("GET");
			
			// Logger.info("Getting geo-coding information from URL: "+holder.getUrl());
			// Logger.info("Getting geo-coding information with TOKEN: "+TOKEN);
			Promise<WSResponse> promise = holder.execute().map(
					new Function<WSResponse, WSResponse>() {
						public WSResponse apply(WSResponse response) {
							return response;
						}
					});
			WSResponse response = promise.get(DEFAULT_TIMEOUT);
			if (response.getStatus() == 200) {
				return response.asJson().toString();
			}
			return null;
		} catch (Exception e) {
			Logger.info("SocialIdeation API Failed because of exception in query URL: "
					+ e.getLocalizedMessage());
			return null;
		}
	}
}
