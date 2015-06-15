package controllers;

import com.feth.play.module.pa.PlayAuthenticate;

import http.Headers;
import models.Config;
import models.User;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import utils.GlobalData;
import static play.data.Form.form;

import java.util.List;

@With(Headers.class)
public class Configs extends Controller {

    public static final Form<Config> CONFIG_FORM = form(Config.class);

    @Security.Authenticated(Secured.class)
    public static Result findConfigs(){
        List<Config> configs = Config.findAll();
        return ok(Json.toJson(configs));
    }

    @Security.Authenticated(Secured.class)
    public static Result findConfig(Long configId){
        Config config = Config.read(configId);
        return ok(Json.toJson(config));
    }

    @Security.Authenticated(Secured.class)
    public static Result deleteConfig(Long configId){
        Config.delete(configId);
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public static Result updateConfig(Long configId) {
        // 1. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Config> newConfigForm = CONFIG_FORM.bindFromRequest();

        if (newConfigForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONFIG_CREATE_MSG_ERROR, newConfigForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            Config newConfig = newConfigForm.get();

            TransferResponseStatus responseBody = new TransferResponseStatus();

            if( Config.readByKey(newConfig.getKey()) > 0 ){
                Logger.info("Config already exists");
            }
            else {
                newConfig.setConfigId(configId);
                newConfig.update();
                Logger.info("Updating config");
                Logger.debug("=> " + newConfigForm.toString());

                responseBody.setNewResourceId(newConfig.getConfigId());
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.CONFIG_CREATE_MSG_SUCCESS,
                        newConfig.getKey()));
                responseBody.setNewResourceURL(GlobalData.CONFIG_BASE_PATH + "/" + newConfig.getConfigId());
            }

            return ok(Json.toJson(responseBody));
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result createConfig() {
        // 1. obtaining the user of the requestor
        User configCreator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Config> newConfigForm = CONFIG_FORM.bindFromRequest();

        if (newConfigForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONFIG_CREATE_MSG_ERROR,newConfigForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            Config newConfig = newConfigForm.get();

            if(newConfig.getLang() == null)
                newConfig.setLang(configCreator.getLanguage());

            TransferResponseStatus responseBody = new TransferResponseStatus();

            if( Config.readByKey(newConfig.getKey()) > 0 ){
                Logger.info("Config already exists");
            }
            else{
                Config.create(newConfig);
                Logger.info("Creating new config");
                Logger.debug("=> " + newConfigForm.toString());

                responseBody.setNewResourceId(newConfig.getConfigId());
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.CONFIG_CREATE_MSG_SUCCESS,
                        newConfig.getKey()));
                responseBody.setNewResourceURL(GlobalData.CONFIG_BASE_PATH+"/"+newConfig.getConfigId());
            }

            return ok(Json.toJson(responseBody));
        }
    }
}
