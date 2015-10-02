package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import enums.ConfigTargets;
import http.Headers;
import models.Assembly;
import models.Config;
import models.User;
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
import static play.data.Form.form;

import java.util.List;
import java.util.UUID;

@With(Headers.class)
public class Configs extends Controller {

	public static final Form<Config> CONFIG_FORM = form(Config.class);

	@SubjectPresent
	public static Result findConfigs(Long aid) {
		Assembly a = Assembly.read(aid);
		List<Config> configs = Config.findByAssembly(a.getUuid());
		return ok(Json.toJson(configs));
	}

	@SubjectPresent
	public static Result findConfig(Long aid, String configUuid) {
		UUID uuid = UUID.fromString(configUuid);
		Assembly a = Assembly.read(aid);
		Config config = Config.read(a.getUuid(), uuid);
		return ok(Json.toJson(config));
	}

	@SubjectPresent
	public static Result deleteConfig(Long aid, String configUuid) {
		UUID uuid = UUID.fromString(configUuid);
		Assembly a = Assembly.read(aid);
		Config.delete(a.getUuid(), uuid);
		return ok();
	}

	@SubjectPresent
	public static Result updateConfig(Long aid, String configUuid) {
		final Form<Config> newConfigForm = CONFIG_FORM.bindFromRequest();
		if (newConfigForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CONFIG_CREATE_MSG_ERROR,
					newConfigForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Config updatedConfig = newConfigForm.get();
			TransferResponseStatus responseBody = new TransferResponseStatus();
			// make sure the config that's being updated is id = configId
			updatedConfig.setUuid(UUID.fromString(configUuid));
			updatedConfig = Config.update(updatedConfig);
			
			Logger.info("Updating config");
			Logger.debug("=> " + newConfigForm.toString());

			responseBody.setNewResourceUuid(updatedConfig.getUuid());
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CONFIG_CREATE_MSG_SUCCESS,
					updatedConfig.getKey()));
			responseBody.setNewResourceURL(GlobalData.CONFIG_BASE_PATH + "/"
					+ updatedConfig.getUuid());
			return ok(Json.toJson(responseBody));
		}
	}

	@ApiOperation(httpMethod = "POST", response = Assembly.class, produces = "application/json", value = "Add configuration to the assembly", notes = "If assembly is coordinated, only COORDINATORS can access this encpoint")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "config_form", value = "configuration details in body", dataType = "models.Config", paramType = "body", required = true),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createConfig(Long aid) {
		// 1. obtaining the user of the requestor
		User configCreator = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));
	//	Assembly assembly = Assembly.read(aid);
		
		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Config> newConfigForm = CONFIG_FORM.bindFromRequest();

		if (newConfigForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CONFIG_CREATE_MSG_ERROR,
					newConfigForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			TransferResponseStatus responseBody = new TransferResponseStatus();			
			Config newConfig = newConfigForm.get();
			if (newConfig.getLang() == null)
				newConfig.setLang(configCreator.getLanguage());
			Assembly a = Assembly.read(aid);
			String key = newConfig.getKey();
			if (a == null) {
				responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.CONFIG_CREATE_MSG_ERROR,"\nAssembly does not exists"));
				return badRequest(Json.toJson(responseBody));
			}
		
			newConfig.setTargetUuid(a.getUuid());
			newConfig.setConfigTarget(ConfigTargets.ASSEMBLY);
			newConfig = Config.create(newConfig);
			if(newConfig==null) {
				responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.CONFIG_CREATE_MSG_ERROR,"\nConfiguration '"+key+"' does not exists"));
				return badRequest(Json.toJson(responseBody));					
			}
			Logger.info("Creating new config");
			Logger.debug("=> " + newConfigForm.toString());

			responseBody.setNewResourceUuid(newConfig.getUuid());
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CONFIG_CREATE_MSG_SUCCESS, newConfig.getKey()));
			responseBody.setNewResourceURL(GlobalData.CONFIG_BASE_PATH + "/"
					+ newConfig.getUuid());

			return ok(Json.toJson(responseBody));
		}
	}
}
