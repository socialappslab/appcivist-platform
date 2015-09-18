package controllers;


import java.util.List;

import models.location.Location;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import play.libs.Json;
import play.mvc.*;
import http.Headers;

@Api(value="/")
@With(Headers.class)
public class Locations extends Controller {
	@ApiOperation(produces="application/json", value="Simple search of existing locations", httpMethod="GET")
	public static Result findLocations(String query) {
		List<Location> locationList = Location.findByQuery(query);
		// TODO: add results from searching in MapBox
		return ok(Json.toJson(locationList));
	}
}
