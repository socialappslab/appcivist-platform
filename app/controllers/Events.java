package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feth.play.module.pa.PlayAuthenticate;

import com.google.gson.JsonArray;
import delegates.CampaignDelegate;
import delegates.NotificationsDelegate;
import delegates.ResourcesDelegate;
import enums.*;
import exceptions.ConfigurationException;
import http.Headers;
import io.swagger.annotations.*;
import models.*;
import models.misc.InitialDataConfig;
import models.misc.Views;
import models.transfer.CampaignSummaryTransfer;
import models.transfer.CampaignTransfer;
import models.transfer.TransferResponseStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import play.*;
import play.data.Form;
import play.i18n.Messages;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.twirl.api.Content;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.LogActions;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static play.data.Form.form;

public class Events extends Controller {
	/** GET /api/space/:sid/theme
	* Always include in your comments the related endpoint as
	* a back reference
	* @Api* annotations are for documentation. 
	* They come from the import io.swagger.annotations package 
	*/
    @ApiOperation(httpMethod = "GET", response = Event.class, responseContainer = "List", value = "Lists existing events")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No event found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    // the following is a play-authenticate annotation 
    // will make sure the session key 
	// is related to an existing user 
	@SubjectPresent 
    public static Result listEvents(
	@ApiParam(name = "date1", value = "Date 1") Date date1, 
	@ApiParam(name = "date2", value = "Date 2") Date date2) {
        
	// Here we use the model to do the queries
	List<Event> eventList = Event.eventBetweenDates(date1,date2);
        
	// Here we controll the responses we will send, based on // the result of the query
	if (eventList == null || eventList.isEmpty()) {
	// notFound, ok, and other similar methods are from 
	// the play Result API that makes it easier to build 
	// and HTTP response without having to worry about 
	// the details like the HTTP response code
	    return notFound(Json.toJson(new TransferResponseStatus("No Event found between these dates")));
	// TransferResponseStatus is a model we 
	// created that is not persisted, but 
	// we use it to communicate responses that 
	// are not persisted entities 
	// (e.g., error messages)
    } else {
    // ok is the default method to build a success 
    // response. We pass the object that will go in the 
    // body of the response. In this case, we use the 
    // Json class and its method toJson to serialize the 
    // object to Json. This class comes from 
    // play.libs.Json;
            return ok(Json.toJson(eventList));
        }
    }
//POST PARA LA CREACION DEL PERSONAL
	@ApiOperation(nickname = "event", httpMethod = "POST", response = Event.class, produces = "application/json", value = "Create new Event")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Event not found", response = TransferResponseStatus.class) })
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result createEvent(Long uid) {
		final Form<Event> newUserForm = EVENT_FORM
				.bindFromRequest();
		if (newEventForm.hasErrors())
			return badRequest(Json.toJson(TransferResponseStatus
					.badMessage("Errors in form", newEventForm
							.errorsAsJson().toString())));
		else {
			Event newEvent = newEvent.get();
			newEvent.save();
			newEvent.refresh();
			Logger.info("Creating Event: "+ newEvent.getEventId());
			Logger.debug("=> " + newEventForm.toString());
			return ok(Json.toJson(newEventProfile));
		}
	}

}