package controllers;


import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import models.location.Location;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import play.Play;
import play.libs.Json;
import play.mvc.*;
import http.Headers;
import utils.GlobalData;
import utils.services.MapBoxWrapper;
import utils.services.NominatimWrapper;

@Api(value="location", hidden=true)
@With(Headers.class)
public class Locations extends Controller {
	@ApiOperation(produces="application/json", value="Simple search of existing locations", httpMethod="GET")
	public static Result findLocations(String query) {
		System.out.println("findLocations");
		List<Location> locationList = Location.findByQuery(query);
		// TODO: add results from searching in MapBox
		return ok(Json.toJson(locationList));
	}

	/**
	 * Endpoint location/gejson?q=locationString for geocoding service wrapper
	 * @param queryLocation
	 * @return
	 */
	@ApiOperation(produces="application/json", value="Get the geojson (or list of geojson) by query", httpMethod="GET")
	public static Result getGeojson(String queryLocation) {
		System.out.println("getGeojson");
		String geocodingService = Play.application().configuration().getString(GlobalData.GEOCODING_SERVICE);
		if (geocodingService.equals("nominatim")) {
			return ok (NominatimWrapper.geoCode(queryLocation));
		} else if (geocodingService.equals("mapbox")) {
			return ok(Json.toJson(MapBoxWrapper.geoCode(queryLocation))); // don't persist
		} else {
			return ok("There are no coincidences.");
		}
	}

	/**
	 * Enpoint location/gejson for locations' geojson attribute update
	 * @return
	 */
	@ApiOperation(produces="application/json", value="Update geojson of existing locations", httpMethod="GET")
	public static Result updateLocationGeojson() {
		System.out.println("updateLocationGeojson");
		String geocodingService = Play.application().configuration().getString(GlobalData.GEOCODING_SERVICE);
		if (geocodingService.equals("nominatim")) {
			List<Location> locationList = Location.find.all();
			List<Location> locationListTest = locationList.subList(0, 1);
			for (Location location : locationListTest) {
//				String serializedLocation = "";
//				serializedLocation += location.getPlaceName() != null && !location.getPlaceName().isEmpty() ? location.getPlaceName() : "";
//				serializedLocation += location.getStreet() != null && !location.getStreet().isEmpty() ? " " + location.getStreet() : "";
//				serializedLocation += location.getCity() != null && !location.getCity().isEmpty() ? " " + location.getCity() : "";
//				serializedLocation += location.getState() != null && !location.getState().isEmpty() ? " " + location.getState() : "";
//				serializedLocation += location.getZip() != null && !location.getZip().isEmpty() ? " " + location.getZip() : "";
//				serializedLocation += location.getCountry() != null && !location.getCountry().isEmpty() ? " " + location.getCountry() : "";

				if (location.getPlaceName() != null) {
					JsonNode resultLocation = NominatimWrapper.geoCode(location.getPlaceName());

					ArrayNode geojsonArr;
					ArrayNode additionalInfoArr;

					if (resultLocation.isArray()) {
						// split additional info and geojson for each result
						ArrayNode arr = (ArrayNode) resultLocation;
						geojsonArr = new ObjectMapper().createArrayNode();
						additionalInfoArr = new ObjectMapper().createArrayNode();
						for (int a = 0; a < arr.size(); a++) {
							JsonNode json = arr.get(a);
							geojsonArr.add(json.get("geojson"));

							Location.createAdditionalInfo(additionalInfoArr, json);
						}

					} else {
						// split additional info and geojson
						geojsonArr = new ObjectMapper().createArrayNode();
						additionalInfoArr = new ObjectMapper().createArrayNode();
						JsonNode json = resultLocation;
						geojsonArr.add(json.get("geojson"));

						Location.createAdditionalInfo(additionalInfoArr, json);
					}
					location.setGeoJson(geojsonArr.toString());
					location.setAdditionInfo(additionalInfoArr.toString());
					location.update();
				}
			}
			return ok("Locations successfully updated");
		} else {
			return forbidden("You cannot update locations' geojson attribute with " + geocodingService + " as geocoding service");
		}
	}
}
