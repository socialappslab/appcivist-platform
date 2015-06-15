package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import http.Headers;
import models.Campaign;
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
import models.transfer.TransferResponseStatus;

import java.util.List;

import static play.data.Form.form;

@With(Headers.class)
public class Campaigns extends Controller{

    public static final Form<Campaign> CAMPAIGN_FORM = form(Campaign.class);

    @Security.Authenticated(Secured.class)
    public static Result findCampaigns(){
        List<Campaign> campaigns = Campaign.findAll();
        return ok(Json.toJson(campaigns));
    }

    @Security.Authenticated(Secured.class)
    public static Result findCampaign(Long campaignId){
        Campaign campaign = Campaign.read(campaignId);
        return ok(Json.toJson(campaign));
    }

    @Security.Authenticated(Secured.class)
    public static Result deleteCampaign(Long campaignId){
        Campaign.delete(campaignId);
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public static Result updateCampaign(Long campaignId) {
        // 1. read the campaign data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Campaign> newCampaignForm = CAMPAIGN_FORM.bindFromRequest();

        if (newCampaignForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_CREATE_MSG_ERROR, newCampaignForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            Campaign newCampaign = newCampaignForm.get();

            TransferResponseStatus responseBody = new TransferResponseStatus();

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
    public static Result createCampaign() {
        // 1. obtaining the user of the requestor
        User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));

        // 2. read the new campaign data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Campaign> newCampaignForm = CAMPAIGN_FORM.bindFromRequest();

        if (newCampaignForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_CREATE_MSG_ERROR, newCampaignForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            Campaign newCampaign = newCampaignForm.get();

            if(newCampaign.getLang() == null)
                newCampaign.setLang(campaignCreator.getLanguage());

            TransferResponseStatus responseBody = new TransferResponseStatus();

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
    }
}
