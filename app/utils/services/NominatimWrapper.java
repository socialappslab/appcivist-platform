package utils.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import play.Logger;
import play.Play;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class NominatimWrapper {
	private static String FORMAT = "json";
	private static String GEOJSON = "1";
	private static String BASE_URL = "http://nominatim.openstreetmap.org/search/";
	private static final long DEFAULT_TIMEOUT = 10000;

	public NominatimWrapper() {
		super();
	}

	public static JsonNode geoCode(String query) {
		try {
			String encodedQuery = URLEncoder.encode(query, "UTF-8");
			WSRequest holder = WS.url(BASE_URL);
			holder.setQueryParameter("format", FORMAT);
			holder.setQueryParameter("q", encodedQuery);
			holder.setQueryParameter("polygon_geojson", GEOJSON);
			holder.setMethod("GET");

			Promise<WSResponse> promise = holder.execute().map(
					new Function<WSResponse, WSResponse>() {
						public WSResponse apply(WSResponse response) {
							return response;
						}
					});
			WSResponse response = promise.get(DEFAULT_TIMEOUT);
			if (response.getStatus() == 200) {
				return response.asJson();
			}
			return null;
		} catch (UnsupportedEncodingException e) {
			Logger.info("Nominatim API Failed because of exception in query URL: "
					+ e.getLocalizedMessage());
			return null;
		}
	}
}
