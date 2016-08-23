package controllers;

import static play.data.Form.form;
import http.Headers;

import java.text.SimpleDateFormat;
import java.util.Date;

import models.Log;
import models.User;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.LogActions;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import views.html.*;

@Api(value="/")
@With(Headers.class)
public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";
	public static final Form<Log> LOG_FORM = form(Log.class);

	public static Result index() {
		return ok(index.render());
	}

	/**
	 * Controller action added to support CORS requests
	 * 
	 * @param path
	 * @return
	 */
	public static Result checkPreFlight(String path) {
		Logger.debug("--> OPTIONS Preflight REQUEST");
		response().setHeader("Access-Control-Allow-Origin", "*");
		response().setHeader("Allow", "*");
		response().setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, OPTIONS");
		response().setHeader("Access-Control-Allow-Headers","Accept, Origin, Content-Type, X-Json, X-Prototype-Version, X-Requested-With, Referer, User-Agent, SESSION_KEY");
		return ok();
	}
	
	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

	@ApiOperation(produces="text/html", value="API Swagger-UI documentation", httpMethod="GET")
	public static Result swaggerDocs() {
		return ok(swagger.render());
	}
	
	/** POST       /api/log
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Log.class, produces = "application/json", value = "Log an action")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "log", value = "Body of Log in JSON", required = true, dataType = "models.Log", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@SubjectPresent
	public static Result logActivity() {
		User user = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
		final Form<Log> newLogForm = LOG_FORM.bindFromRequest();
		if (newLogForm.hasErrors()) {
			return internalServerError(Json.toJson(new TransferResponseStatus("Log is not valid")));
		} else {
			Log newLog = newLogForm.get();
			if (user !=null) {
				newLog.setUser(user.getEmail());
				LogActions.logActivity(newLog);
				return ok(Json.toJson(newLog));	
			} else {
				return notFound(Json.toJson(new TransferResponseStatus("User's session was null")));
			}
		}
	}
	
	/** POST       /api/log/public
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Log.class, produces = "application/json", value = "Log an action")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "log", value = "Body of Log in JSON", required = true, dataType = "models.Log", paramType = "body") })
	public static Result logActivityPublic() {
		final Form<Log> newLogForm = LOG_FORM.bindFromRequest();
		if (newLogForm.hasErrors()) {
			return internalServerError(Json.toJson(new TransferResponseStatus("Log is not valid")));
		} else {
			Log newLog = newLogForm.get();
			String userEmail = newLog.getUser();
			User user = null;
			if (userEmail !=null ){
				user = User.findByEmail(userEmail);	
			}
			if (user !=null) {
				newLog.setUser(user.getEmail());
				LogActions.logActivity(newLog);
				return ok(Json.toJson(newLog));	
			} else {
				return notFound(Json.toJson(new TransferResponseStatus("User's email does not exist in our database")));
			}
		}
	}
	
}
