package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import http.Headers;
import models.Campaign;
import models.CampaignPhase;
import models.User;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import utils.GlobalData;
import utils.ResponseStatusBean;

import java.util.List;

import static play.data.Form.form;

@With(Headers.class)
public class CampaignPhases extends Controller{

    public static final Form<CampaignPhase> CAMPAIGN_PHASE_FORM = form(CampaignPhase.class);

    //@Security.Authenticated(Secured.class)
    public static Result findCampaignPhases(Long campaignId){
        List<CampaignPhase> phases = CampaignPhase.findAll(campaignId);
        return ok(Json.toJson(phases));
    }

    //@Security.Authenticated(Secured.class)
    public static Result findCampaignPhase(Long campaignId, Long phaseId){
        CampaignPhase campaignPhase = CampaignPhase.read(campaignId, phaseId);
        return ok(Json.toJson(campaignPhase));
    }

    //@Security.Authenticated(Secured.class)
    public static Result deleteCampaignPhase(Long campaignId, Long phaseId){
        CampaignPhase.delete(campaignId,phaseId);
        return ok();
    }
/*
    @Security.Authenticated(Secured.class)
    public static Result updateCampaignPhase(Long campaignId, Long phaseId) {
        // 1. read the campaign data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Campaign> newCampaignForm = CAMPAIGN_FORM.bindFromRequest();

        if (newCampaignForm.hasErrors()) {
            ResponseStatusBean responseBody = new ResponseStatusBean();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_CREATE_MSG_ERROR, newCampaignForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            Campaign newCampaign = newCampaignForm.get();

            ResponseStatusBean responseBody = new ResponseStatusBean();

            if( Campaign.readByTitle(newCampaign.getTitle()) > 0 ){
                Logger.info("Campaign already exists");
            }
            else {
                newCampaign.setCampaignId(campaignId);
                newCampaign.update();
                Logger.info("Updating campaign");
                Logger.debug("=> " + newCampaignForm.toString());

                responseBody.setNewResourceId(newCampaign.getCampaignId());
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.CAMPAIGN_CREATE_MSG_SUCCESS,
                        newCampaign.getTitle()));
                responseBody.setNewResourceURL(GlobalData.CAMPAIGN_BASE_PATH + "/" + newCampaign.getCampaignId());
            }

            return ok(Json.toJson(responseBody));
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result createCampaignPhase(Long campaignId) {
        // 1. obtaining the user of the requestor
        User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));

        // 2. read the new campaign data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Campaign> newCampaignForm = CAMPAIGN_FORM.bindFromRequest();

        if (newCampaignForm.hasErrors()) {
            ResponseStatusBean responseBody = new ResponseStatusBean();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_CREATE_MSG_ERROR, newCampaignForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            Campaign newCampaign = newCampaignForm.get();

            if(newCampaign.getLang() == null)
                newCampaign.setLang(campaignCreator.getLocale());

            ResponseStatusBean responseBody = new ResponseStatusBean();

            if( Campaign.readByTitle(newCampaign.getTitle()) > 0 ){
                Logger.info("Campaign already exists");
            }
            else{
                Campaign.create(newCampaign);
                Logger.info("Creating new campaign");
                Logger.debug("=> " + newCampaignForm.toString());

                responseBody.setNewResourceId(newCampaign.getCampaignId());
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.CAMPAIGN_CREATE_MSG_SUCCESS,
                        newCampaign.getTitle()));
                responseBody.setNewResourceURL(GlobalData.CAMPAIGN_BASE_PATH+"/"+newCampaign.getCampaignId());
            }

            return ok(Json.toJson(responseBody));
        }
    }*/
}
