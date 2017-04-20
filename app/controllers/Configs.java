package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.PlayAuthenticate;
import enums.ResourceSpaceTypes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import enums.ConfigTargets;
import http.Headers;
import models.*;
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

import java.util.*;

@Api(value = "10 configuration: configuration management", description = "Configurations management")
@With(Headers.class)
public class Configs extends Controller {

	public static final Form<Config> CONFIG_FORM = form(Config.class);
	public static final Form<ConfigValues> CONFIG_VALUES_FORM = form(ConfigValues.class);

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

	/**
	 * GET       /api/space/:sid/config
	 *
	 * @param sid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = HashMap.class, responseContainer = "List", produces = "application/json",
			value = "Get configs in a Resource Space")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
	@SubjectPresent
	public static Result findSpaceConfigs(Long sid) {
		ResourceSpace resourceSpace = ResourceSpace.read(sid);
		List<Config> configs = resourceSpace.getConfigs();
		HashMap<String,String> map = new HashMap<String,String>();
		for (Config c:configs
			 ) {
			String key= c.getKey();
			String value = c.getValue();
			map.put(key,value);
		}
		return ok(Json.toJson(map));
	}

	/**
	 * GET       /api/space/:sid/config/:uuid
	 *
	 * @param sid
	 * @param uuid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Config.class, produces = "application/json",
			value = "Get config by id in a Resource Space")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
	@SubjectPresent
	public static Result findSpaceConfigById(Long sid, UUID uuid) {
		ResourceSpace resourceSpace = ResourceSpace.findByConfig(sid, uuid);
		if (resourceSpace == null) {
			return notFound(Json
					.toJson(new TransferResponseStatus("No config found with uuid "+uuid+ " in space with id "+sid)));
		} else {
			Config resourceSpaceConfig = Config.read(uuid);
			return ok(Json.toJson(resourceSpaceConfig));
		}
	}

	/**
	 * GET       /api/space/:uuid/config/public
	 *
	 * @param uuid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = HashMap.class, responseContainer = "List", produces = "application/json",
			value = "Get configs in a Resource Space")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
//	@SubjectPresent
	public static Result findSpaceConfigsPublic(UUID uuid) {
		ResourceSpace resourceSpace = ResourceSpace.readByUUID(uuid);
		List<Config> configs = resourceSpace.getConfigs();
		HashMap<String,String> map = new HashMap<String,String>();
		for (Config c:configs
				) {
			String key= c.getKey();
			String value = c.getValue();
			map.put(key,value);
		}
		return ok(Json.toJson(map));
	}

	/**
	 * PUT       /api/space/:sid/config
	 *
	 * @param sid
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = TransferResponseStatus.class, produces = "application/json",
			value = "Update configs in a Resource Space")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "sid", value = "Resource Space id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "config_map", value = "configuration key value json map", dataType = "String", paramType = "body", required = true)})
//			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
//	@SubjectPresent
	public static Result updateSpaceConfig(Long sid) {
			JsonNode requestBody = request().body().asJson();
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, String> result = mapper.convertValue(requestBody, HashMap.class);
			ConfigValues updatedConfig = new ConfigValues(result);
			TransferResponseStatus responseBody = new TransferResponseStatus();
			if (result == null || result.keySet()==null || result.values()==null) {
				responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage("Invalid JSON");
				return badRequest(Json.toJson(responseBody));
			}
			List<Config> configList = new ArrayList<Config>();
			ResourceSpace resourceSpace = ResourceSpace.read(sid);
			if (resourceSpace == null) {
				responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.CONFIG_CREATE_MSG_ERROR, "\nResource Space does not exists"));
				return badRequest(Json.toJson(responseBody));
			}
			for(String key : updatedConfig.getConfigs().keySet()) {
				Object value = updatedConfig.getConfigs().get(key);
				List<Config> updateConfigs = new ArrayList<Config>();
				if (ResourceSpaceTypes.ASSEMBLY.equals(resourceSpace.getType())) {
					updateConfigs = Config.findByTypeAndKey(resourceSpace.getAssemblyResources().getUuid(),ConfigTargets.ASSEMBLY,key);
				} else if (ResourceSpaceTypes.CAMPAIGN.equals(resourceSpace.getType())) {
					updateConfigs = Config.findByTypeAndKey(resourceSpace.getCampaign().getUuid(),ConfigTargets.CAMPAIGN,key);
				} else if (ResourceSpaceTypes.COMPONENT.equals(resourceSpace.getType())) {
					updateConfigs = Config.findByTypeAndKey(resourceSpace.getComponent().getUuid(),ConfigTargets.COMPONENT,key);
				} else if (ResourceSpaceTypes.CONTRIBUTION.equals(resourceSpace.getType())) {
					updateConfigs = Config.findByTypeAndKey(resourceSpace.getContribution().getUuid(),ConfigTargets.CONTRIBUTION,key);
				} else if (ResourceSpaceTypes.WORKING_GROUP.equals(resourceSpace.getType())) {
					updateConfigs = Config.findByTypeAndKey(resourceSpace.getWorkingGroupResources().getUuid(),ConfigTargets.WORKING_GROUP,key);
				}
				if (updateConfigs ==null || updateConfigs.size()==0) {
					responseBody = new TransferResponseStatus();
					responseBody.setStatusMessage(Messages.get(
							GlobalData.CONFIG_CREATE_MSG_ERROR, "\nConfiguration Key '" + key + "' does not exists"));
					return badRequest(Json.toJson(responseBody));
				}
				for (Config conf: updateConfigs
						) {
					conf.setValue(value.toString());
					configList.add(conf);
				}

			}
			for (Config conf: configList
					) {
				Config.update(conf);
			}
			responseBody.setStatusMessage("OK");
			return ok(Json.toJson(updatedConfig));

	}

	/**
	 * POST       /api/space/:sid/config
	 *
	 * @param sid
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = TransferResponseStatus.class, produces = "application/json", value = "Add configuration to the resource space")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "sid", value = "Resource Space id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "config_map", value = "configuration key value json map", dataType = "String", paramType = "body", required = true)})
//			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
//	@SubjectPresent
	public static Result createSpaceConfig(Long sid) {
		User configCreator = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));
		JsonNode requestBody = request().body().asJson();
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String> result = mapper.convertValue(requestBody, HashMap.class);
		ConfigValues newConfigValues = new ConfigValues(result);
		TransferResponseStatus responseBody = new TransferResponseStatus();
		if (result == null || result.keySet()==null || result.values()==null) {
			responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage("Invalid JSON");
			return badRequest(Json.toJson(responseBody));
		}
		List<Config> configList = new ArrayList<Config>();
		for(String key : newConfigValues.getConfigs().keySet()) {
			Object value = newConfigValues.getConfigs().get(key);
			Config newConfig = new Config();
			if (newConfig.getLang() == null)
				newConfig.setLang(configCreator.getLanguage());
			ResourceSpace resourceSpace = ResourceSpace.read(sid);
			newConfig.setKey(key);
			newConfig.setValue(value.toString());
			if (resourceSpace == null) {
				responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.CONFIG_CREATE_MSG_ERROR, "\nResource Space does not exists"));
				return badRequest(Json.toJson(responseBody));
			}

			if (ResourceSpaceTypes.ASSEMBLY.equals(resourceSpace.getType())) {
				newConfig.setConfigTarget(ConfigTargets.ASSEMBLY);
				newConfig.setTargetUuid(resourceSpace.getAssemblyResources().getUuid());
			} else if (ResourceSpaceTypes.CAMPAIGN.equals(resourceSpace.getType())) {
				newConfig.setConfigTarget(ConfigTargets.CAMPAIGN);
				newConfig.setTargetUuid(resourceSpace.getCampaign().getUuid());
			} else if (ResourceSpaceTypes.COMPONENT.equals(resourceSpace.getType())) {
				newConfig.setConfigTarget(ConfigTargets.COMPONENT);
				newConfig.setTargetUuid(resourceSpace.getComponent().getUuid());
			} else if (ResourceSpaceTypes.CONTRIBUTION.equals(resourceSpace.getType())) {
				newConfig.setConfigTarget(ConfigTargets.CONTRIBUTION);
				newConfig.setTargetUuid(resourceSpace.getContribution().getUuid());
			} else if (ResourceSpaceTypes.WORKING_GROUP.equals(resourceSpace.getType())) {
				newConfig.setConfigTarget(ConfigTargets.WORKING_GROUP);
				newConfig.setTargetUuid(resourceSpace.getWorkingGroupResources().getUuid());
			}
			if (newConfig.getDefinition()==null) {
				ConfigDefinition cd = ConfigDefinition.findByKey(newConfig.getKey());
				if(cd != null) {
					newConfig.setDefinition(cd);
				} else {
					responseBody = new TransferResponseStatus();
					responseBody.setStatusMessage(Messages.get(
							GlobalData.CONFIG_CREATE_MSG_ERROR, "\nConfiguration definition '" + key + "' does not exists"));
					return badRequest(Json.toJson(responseBody));
				}
			}
			configList.add(newConfig);
		}
		for (Config conf: configList
			 ) {
			Config.create(conf);
		}
		responseBody.setStatusMessage("OK");
		return ok(Json.toJson(responseBody));

	}
}
