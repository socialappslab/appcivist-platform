package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.PlayAuthenticate;
import enums.ConfigTargets;
import enums.ResourceSpaceTypes;
import enums.ResponseStatus;
import enums.UserProfileConfigsTypes;
import http.Headers;
import io.swagger.annotations.*;
import models.*;
import models.transfer.AutoSuscriptionTransfer;
import models.transfer.PreferenceTransfer;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.services.EntityManagerWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static play.data.Form.form;

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
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "Assembly id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "config_form", value = "configuration details in body", dataType = "models.Config", paramType = "body", required = true),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
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
                        GlobalData.CONFIG_CREATE_MSG_ERROR, "\nAssembly does not exists"));
                return badRequest(Json.toJson(responseBody));
            }

            newConfig.setTargetUuid(a.getUuid());
            newConfig.setConfigTarget(ConfigTargets.ASSEMBLY);
            newConfig = Config.create(newConfig);
            if (newConfig == null) {
                responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.CONFIG_CREATE_MSG_ERROR, "\nConfiguration '" + key + "' does not exists"));
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
        HashMap<String, String> map = new HashMap<String, String>();
        for (Config c : configs
                ) {
            String key = c.getKey();
            String value = c.getValue();
            map.put(key, value);
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
                    .toJson(new TransferResponseStatus("No config found with uuid " + uuid + " in space with id " + sid)));
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
        HashMap<String, String> map = new HashMap<String, String>();
        for (Config c : configs
                ) {
            String key = c.getKey();
            String value = c.getValue();
            map.put(key, value);
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
            @ApiImplicitParam(name = "config_map", value = "configuration key value json map", dataType = "String", paramType = "body", required = true),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfSpace", meta = SecurityModelConstants.SPACE_RESOURCE_PATH)
    public static Result updateSpaceConfig(Long sid) {
        JsonNode requestBody = request().body().asJson();
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> result = mapper.convertValue(requestBody, HashMap.class);
        ConfigValues updatedConfigValues = new ConfigValues(result);
        ConfigValues refreshedConfigValues = new ConfigValues();
        TransferResponseStatus responseBody = new TransferResponseStatus();
        if (result == null || result.keySet() == null || result.values() == null) {
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
        for (String key : updatedConfigValues.getConfigs().keySet()) {
            Object value = updatedConfigValues.getConfigs().get(key);
            List<Config> updateConfigs = new ArrayList<Config>();
            ConfigTargets configTargets = null;
            UUID uuid = null;
            if (ResourceSpaceTypes.ASSEMBLY.equals(resourceSpace.getType())) {
                configTargets = ConfigTargets.ASSEMBLY;
                uuid = resourceSpace.getAssemblyResources() == null ? null : resourceSpace.getAssemblyResources().getUuid();
            } else if (ResourceSpaceTypes.CAMPAIGN.equals(resourceSpace.getType())) {
                configTargets = ConfigTargets.CAMPAIGN;
                uuid = resourceSpace.getCampaign() == null ? null : resourceSpace.getCampaign().getUuid();
            } else if (ResourceSpaceTypes.COMPONENT.equals(resourceSpace.getType())) {
                configTargets = ConfigTargets.COMPONENT;
                uuid = resourceSpace.getComponent() == null ? null : resourceSpace.getComponent().getUuid();
            } else if (ResourceSpaceTypes.CONTRIBUTION.equals(resourceSpace.getType())) {
                configTargets = ConfigTargets.CONTRIBUTION;
                uuid = resourceSpace.getContribution() == null ? null : resourceSpace.getContribution().getUuid();
            } else if (ResourceSpaceTypes.WORKING_GROUP.equals(resourceSpace.getType())) {
                configTargets = ConfigTargets.WORKING_GROUP;
                uuid = resourceSpace.getWorkingGroupResources() == null ? null : resourceSpace.getWorkingGroupResources().getUuid();
            }
            if (configTargets == null || uuid == null) {
                responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.CONFIG_CREATE_MSG_ERROR, "\nEntity of type " + resourceSpace.getType() + " does not exists"));
                return badRequest(Json.toJson(responseBody));
            }

            updateConfigs = Config.findByTypeAndKey(uuid, configTargets, key);
            if (updateConfigs == null || updateConfigs.size() == 0) {
                // Add the config
                Config newConf = new Config();
                newConf.setConfigTarget(configTargets);
                newConf.setKey(key);
                newConf.setValue(value.toString());
                newConf.setTargetUuid(uuid);
                newConf = Config.create(newConf);
                ResourceSpace rs = ResourceSpace.read(sid);
                rs.getConfigs().add(newConf);
                rs.update();
                refreshedConfigValues.getConfigs().put(newConf.getKey(),newConf.getValue());
            } else {
                for (Config conf : updateConfigs) {
                    conf.setValue(value.toString());
                    configList.add(conf);
                }
            }

        }
        for (Config conf : configList) {
            Config.update(conf);
            refreshedConfigValues.getConfigs().put(conf.getKey(),conf.getValue());
        }
        return ok(Json.toJson(refreshedConfigValues));

    }

    /**
     * POST       /api/space/:sid/config
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = TransferResponseStatus.class, produces = "application/json", value = "Add configuration to the resource space")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sid", value = "Resource Space id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "config_map", value = "configuration key value json map", dataType = "String", paramType = "body", required = true),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfSpace", meta = SecurityModelConstants.SPACE_RESOURCE_PATH)
    public static Result createSpaceConfig(Long sid) {
        User configCreator = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        JsonNode requestBody = request().body().asJson();
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> result = mapper.convertValue(requestBody, HashMap.class);
        ConfigValues newConfigValues = new ConfigValues(result);
        TransferResponseStatus responseBody = new TransferResponseStatus();
        if (result == null || result.keySet() == null || result.values() == null) {
            responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("Invalid JSON");
            return badRequest(Json.toJson(responseBody));
        }
        List<Config> configList = new ArrayList<Config>();
        for (String key : newConfigValues.getConfigs().keySet()) {
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
            if (newConfig.getDefinition() == null) {
                ConfigDefinition cd = ConfigDefinition.findByKey(newConfig.getKey());
                if (cd != null) {
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
        for (Config conf : configList
                ) {
            Config.create(conf);
        }
        responseBody.setStatusMessage("OK");
        return ok(Json.toJson(responseBody));

    }

    /**
     * PUT       /api/space/:sid/config
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = TransferResponseStatus.class, produces = "application/json",
            value = "Update user configs")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sid", value = "Resource Space id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "config_map", value = "configuration key value json map", dataType = "String", paramType = "body", required = true),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfSpace", meta = SecurityModelConstants.SPACE_RESOURCE_PATH)
    public static Result updateUserConfig(Long id) {
        // Get the user record of the creator
        User subscriber = null;
        EntityManagerWrapper wrapper = new EntityManagerWrapper();

        try {
            subscriber = User.findByUserId(id);
        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            " Can't find User with id: " + id)));


        }

        if (subscriber == null) {
            return badRequest(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.BADREQUEST,
                            " User with id : " + id + " doesn't exists")));
        }

        //Check user registration
        try {
            WSResponse response = wrapper.getUser(subscriber);

            Logger.info("ENTITY MANAGER: GET user response =>" + response.getStatus());
            // Relay response to requestor
            if (response.getStatus() == 200) {
                Logger.info("ENTITY MANAGER: user exist => " + response.getBody().toString());
            } else if (response.getStatus() == 404) {
                Logger.info("ENTITY MANAGER: user doesnt exist. Creating user");
                wrapper.createUser(subscriber);

            } else {
                return Controller.internalServerError(Json.toJson(TransferResponseStatus.errorMessage(
                        "Error while checking user at entity manager",
                        response.asJson().toString())));

            }
        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            e.getMessage())));


        }

        // insert configurations
        JsonNode json = request().body().asJson();
        HashMap<String, Object> configs = Json.fromJson(json, HashMap.class);
        List<Config> actualUserConfigs = Config.findByUser(subscriber.getUuid());
        PreferenceTransfer preferenceUpdate = null;
        AutoSuscriptionTransfer subcription = null;

        Ebean.beginTransaction();
        try {
            for (String key : configs.keySet()) {
                String value = configs.get(key).getClass().equals(Boolean.class) ? String.valueOf(configs.get(key)) : (String) configs.get(key);
                if (UserProfileConfigsTypes.entities.contains(key) ||
                        UserProfileConfigsTypes.otherPreferences.contains(key) ||
                        UserProfileConfigsTypes.preferencesNewsletter.keySet().contains(key)) {
                    Boolean updated = false;
                    for (Config actual : actualUserConfigs) {
                        if (actual.getKey().equals(key)) {
                            //Update config
                            actual.setValue(value);
                            actual.update();
                            updated = true;
                            break;
                        }
                    }
                    if (!updated) {
                        //Save a new config
                        Config config = new Config();
                        config.setTargetUuid(subscriber.getUuid());
                        config.setValue(value);
                        config.setConfigTarget(ConfigTargets.USER);
                        config.setKey(key);
                        config.save();
                    }

                    //Update email configuration in entity manager
                    if (UserProfileConfigsTypes.entities.contains(key)) {
                        updateIdentities(key, value, subscriber);
                    }

                    if (UserProfileConfigsTypes.preferencesNewsletter.keySet().contains(key)) {
                        Logger.info(" --- FOUND: " + key);
                        if (preferenceUpdate == null) {
                            preferenceUpdate = new PreferenceTransfer(subscriber.getUuidAsString());

                        }
                        if (key.equals("notifications.default.service")) {
                            preferenceUpdate.setDefaultService(value);
                        } else {
                            if (subcription == null) {
                                subcription = new AutoSuscriptionTransfer( "campaign-newsletter");
                                subcription.setIdentity(subscriber.getEmail());
                                preferenceUpdate.getAutoSusbcriptions().add(subcription);
                            }
                            Class clazz = subcription.getClass();


                            for (Field f : clazz.getDeclaredFields()) {
                                f.setAccessible(true);
                                if (f.getName().equals(UserProfileConfigsTypes.preferencesNewsletter.get(key))) {
                                    try {
                                        f.set(subcription, value);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }


                        }
                    }


                } else {
                    // return bad request
                    return badRequest(Json
                            .toJson(new TransferResponseStatus(
                                    ResponseStatus.BADREQUEST,
                                    " Config type: " + key + " not allowed")));
                }
            }
            Logger.info(" --- Update PReferences: " + preferenceUpdate);
            if (preferenceUpdate != null && !preferenceUpdate.getAutoSusbcriptions().isEmpty()) {
                //update preferences
                Logger.info(" --- creating auto subscription: " + preferenceUpdate);
                wrapper.updateAutoSubcriptions(preferenceUpdate, subscriber);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            e.getMessage())));
        }
        Ebean.commitTransaction();


        return ok();

    }


    private static void updateIdentities(String key, String value, User subscriber) throws Exception {
        EntityManagerWrapper wrapper = new EntityManagerWrapper();


        if (key.equals("notifications.service.email.identity")) {
            String email = value;
            if (email == null || email.isEmpty()) {
                email = subscriber.getEmail();
            }
            wrapper.updateIdentities(subscriber,
                    "email",
                    email,
                    null);
        }

        if (key.equals("notifications.service.email")) {
            Boolean serviceEmailConfig = Boolean.valueOf(value);
            wrapper.updateIdentities(subscriber,
                    "email",
                    null,
                    serviceEmailConfig);

        }


        if (key.equals("notifications.service.facebook-messenger.identity")) {
            String facebookIdentity = value;
            wrapper.updateIdentities(subscriber,
                    "facebookmessenger",
                    facebookIdentity,
                    null);

        }

        if (key.equals("notifications.service.facebook-messenger")) {
            Boolean configValue = Boolean.valueOf(value);
            wrapper.updateIdentities(subscriber,
                    "facebookmessenger",
                    null,
                    configValue);

        }


        if (key.equals("notifications.service.twitter.identity")) {
            String identity = value;
            wrapper.updateIdentities(subscriber,
                    "twitter",
                    identity,
                    null);


        }

        if (key.equals("notifications.service.twitter-messenger")) {
            Boolean configValue = Boolean.valueOf(value);
            wrapper.updateIdentities(subscriber,
                    "twitter",
                    null,
                    configValue);

        }


    }

    /**
     * PUT       /api/space/:sid/config
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = TransferResponseStatus.class, produces = "application/json",
            value = "Get user configs")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sid", value = "Resource Space id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "config_map", value = "configuration key value json map", dataType = "String", paramType = "body", required = true),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfSpace", meta = SecurityModelConstants.SPACE_RESOURCE_PATH)
    public static Result getUserConfig(Long id) {
        // Get the user record of the creator
        User subscriber = null;
        try {
            subscriber = User.findByUserId(id);
        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            " Can't find User with id: " + id)));


        }

        if (subscriber == null) {
            return badRequest(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.BADREQUEST,
                            " User with id : " + id + " doesn't exists")));
        }

        List<Config> actualUserConfigs = Config.findByUser(subscriber.getUuid());
        HashMap<String, Object> configs = new HashMap<>();

        for (Config conf : actualUserConfigs) {
            if (conf.getValue().equals("true") || conf.getValue().equals("false")) {
                configs.put(conf.getKey(), new Boolean(conf.getValue()));
            } else {
                configs.put(conf.getKey(), conf.getValue());
            }
        }
        return ok(Json.toJson(configs));

    }
}
