package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;

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

	@SubjectPresent
	public static Result createConfig(Long aid) {
		// 1. obtaining the user of the requestor
		User configCreator = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

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

			Config newConfig = newConfigForm.get();

			if (newConfig.getLang() == null)
				newConfig.setLang(configCreator.getLanguage());

			TransferResponseStatus responseBody = new TransferResponseStatus();

			if (Config.readByKey(newConfig.getKey()) > 0) {
				Logger.info("Config already exists");
			} else {
				Assembly a = Assembly.read(aid);
				newConfig.setTargetUuid(a.getUuid());
				newConfig.setConfigTarget(ConfigTargets.ASSEMBLY);
				Config.create(newConfig);
				Logger.info("Creating new config");
				Logger.debug("=> " + newConfigForm.toString());

				responseBody.setNewResourceUuid(newConfig.getUuid());
				responseBody.setStatusMessage(Messages.get(
						GlobalData.CONFIG_CREATE_MSG_SUCCESS,
						newConfig.getKey()));
				responseBody.setNewResourceURL(GlobalData.CONFIG_BASE_PATH
						+ "/" + newConfig.getUuid());
			}

			return ok(Json.toJson(responseBody));
		}
	}
}
