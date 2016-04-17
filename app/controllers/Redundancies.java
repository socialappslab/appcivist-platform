package controllers;

import static play.data.Form.form;
import http.Headers;

import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import enums.ResponseStatus;


//@Api(value = "/redundancy", description = "find similar contributions to encourage collaboration between activists with similar ideas")
@With(Headers.class)
public class Redundancies extends Controller {

	
	public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);

	

	//@ApiOperation(httpMethod = "GET", response = Redundancy.class, produces = "application/json", value = "Find keywords", notes = "Find a contribution's keywords by searching for repitition")
	//@ApiResponses(value = { @ApiResponse(code = 404, message = "No keywords found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "Contributin ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	//@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.REDUNDANCY_RESOURCE_PATH)
	public static Result find_keywords(Long id) {
		ArrayList<String> list = RedundanciesDelegate.find_keywords(id);
		
		
		Logger.info("match list size:" + RedundanciesDelegate.getKeywordsTable().toString());
		Logger.info("keywords table:" + list.size());
		for (int i =0; i < list.size(); i++) {

			Logger.info("keyword:" + list.get(i));

		}



		Set set = RedundanciesDelegate.getKeywordsTable().entrySet();
	    Iterator it = set.iterator();
	    while (it.hasNext()) {
	      Map.Entry entry = (Map.Entry) it.next();
	      Logger.info("Keywords Hashtable entries:" + entry.getKey() + " : " + entry.getValue());
	    }




		return list != null ? ok(Json.toJson(list)) : notFound(Json
				.toJson(new TransferResponseStatus(ResponseStatus.NODATA,
						"No keywords found")));
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "Contributin ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	//@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.REDUNDANCY_RESOURCE_PATH)
	public static Result match_keywords(Long id) {
		Long contri_id;
		List<Contribution> contributions = Contribution.findAll();
		

		for(int i = 0; i < contributions.size(); i++ ) {
			contri_id = contributions.get(i).getContributionId();
			RedundanciesDelegate.find_keywords(contri_id);
		}
		
		ArrayList<Long> list = RedundanciesDelegate.match_keywords(id);



		Logger.info("contri size" + contributions.size());
		Logger.info("current contri ID: " + id);

		Set set2 = RedundanciesDelegate.getKeywordsTable().entrySet();
	    Iterator it2 = set2.iterator();
	    while (it2.hasNext()) {
	      Map.Entry entry2 = (Map.Entry) it2.next();
	      Logger.info("Keywords Hashtable entries:" + entry2.getKey() + " : " + entry2.getValue());
	    }

	    Set set3 = RedundanciesDelegate.getContributionsTable().entrySet();
	    Iterator it3 = set3.iterator();
	    while (it3.hasNext()) {
	      Map.Entry entry3 = (Map.Entry) it3.next();
	      Logger.info("Contributions ID Hashtable entries:" + entry3.getKey() + " : " + entry3.getValue());
	    }

		Set set = RedundanciesDelegate.getSimilarContriTable().entrySet();
	    Iterator it = set.iterator();
	    while (it.hasNext()) {
	      Map.Entry entry = (Map.Entry) it.next();
	      Logger.info("Hashtable entries:" + entry.getKey() + " : " + entry.getValue());
	    }




		// Logger.info("keyword table:" + RedundanciesDelegate.getKeywordsTable().toString());
		// Logger.info("match list size:" + RedundanciesDelegate.getSimilarContriTable().size());
		// Logger.info("similar table list :" + RedundanciesDelegate.getSimilarContriTable().toString());
//		Logger.info("the similar values:" + RedundanciesDelegate.getSimilarContriTable().values().toArray().get(1));
		return list != null ? ok(Json.toJson(list)) : notFound(Json
				.toJson(new TransferResponseStatus(ResponseStatus.NODATA,
						"No contributions match found")));
	}



	// 1. Redundancy class is a Delegate (put it in delegates package)
	// 2. Existing contributions in the database can be Retrieved using the Contribution model 
	// 3. Version 1: keywords tables and contributions/keywords hashmap will be created in memory
	// 4. Version 2: think about how keyword table and  contributions/keywords hashmap  should be store in the database to accelerate queries 
	          // construct these tables for each new contribution when they are created 
	// 5. Version 3: service that updates tables every once and a whle










}