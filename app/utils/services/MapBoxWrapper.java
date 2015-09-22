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

public class MapBoxWrapper {
	private static String TOKEN = "";
	private static String TOKEN_NAME = "access_token";
	private static String VERSION = "/v4";
	private static String BASE_URL = "https://api.mapbox.com" + VERSION;
	private static final long DEFAULT_TIMEOUT = 10000;

	// Mapbox dataset
	private static String DATASET_PLACES = "/mapbox.places";

	// Include other operations
	private static String OPERATION_GEOCODE = "/geocode";

	static {
		TOKEN = Play.application().configuration()
				.getString("appcivist.services.mapboxapi.token");
	}

	public MapBoxWrapper() {
		super();
	}

	public static String geoCode(String query) {
		try {
			String encodedQuery = URLEncoder.encode(query, "UTF-8");
			WSRequest holder = WS.url(BASE_URL + OPERATION_GEOCODE + DATASET_PLACES + "/" + encodedQuery+".json");
			holder.setQueryParameter(TOKEN_NAME, TOKEN);
			holder.setMethod("GET");
			
			Logger.info("Getting geo-coding information from URL: "+holder.getUrl());
			Logger.info("Getting geo-coding information with TOKEN: "+TOKEN);
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
		} catch (UnsupportedEncodingException e) {
			Logger.info("MapBox API Failed because of exception in query URL: "
					+ e.getLocalizedMessage());
			return null;
		}
	}
}
