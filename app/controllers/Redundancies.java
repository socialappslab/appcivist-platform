package controllers;

import static play.data.Form.form;
import http.Headers;

import java.util.List;

import models.Assembly;
import models.ComponentInstance;
import models.Contribution;
import models.ResourceSpace;
import models.User;
import models.WorkingGroup;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.SecurityModelConstants;
import utils.GlobalData;
import be.objectify.deadbolt.java.actions.Dynamic;

import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import delegates.ContributionsDelegate;
import delegates.RedundanciesDelegate;
import enums.ContributionTypes;

//@Api(value = "/redundancy", description = "find similar contributions to encourage collaboration between activists with similar ideas")
@With(Headers.class)
public class Redundancies extends Controller {

	public static final Form<Redundancy> REDUNDANCY_FORM = form(Redundancy.class);
	public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);

	// @ApiOperation(httpMethod = "GET", response = Redundancy.class, produces = "application/json", value = "Get Contribution's keywords", notes = "Get Contribution's keywords by its Contribution ID")
	// @ApiResponses(value = { @ApiResponse(code = 404, message = "No keywords found", response = TransferResponseStatus.class) })
	// @ApiImplicitParams({
	// 		@ApiImplicitParam(name = "id", value = "Contribution id", dataType = "Long", paramType = "path"),
	// 		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	// //@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.REDUNDANCY_RESOURCE_PATH)
	// public static Result findKeywordsByID(Long id) {
	// 	Redundancy a = Redundancy.findKeywordsByID(id);
	// 	return a != null ? ok(Json.toJson(a)) : notFound(Json
	// 			.toJson(new TransferResponseStatus(ResponseStatus.NODATA,
	// 					"No Contribution with ID = " + id)));
	// }

	@ApiOperation(httpMethod = "GET", response = Redundancy.class, produces = "application/json", value = "Find keywords", notes = "Find a contribution's keywords by searching for repitition")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No keywords found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "Contribution text", dataType = "String", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	//@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.REDUNDANCY_RESOURCE_PATH)
	public static Result find_keywords(Long id) {
		ArrayList<String> list = RedundanciesDelegate.find_keywords(id);
		return list != null ? ok(Json.toJson(list)) : notFound(Json
				.toJson(new TransferResponseStatus(ResponseStatus.NODATA,
						"No keywords found")));
	}



	// 1. Redundancy class is a Delegate (put it in delegates package)
	// 2. Existing contributions in the database can be Retrieved using the Contribution model 
	// 3. Version 1: keywords tables and contributions/keywords hashmap will be created in memory
	// 4. Version 2: think about how keyword table and  contributions/keywords hashmap  should be store in the database to accelerate queries 
	          // construct these tables for each new contribution when they are created 
	// 5. Version 3: service that updates tables every once and a whle










}