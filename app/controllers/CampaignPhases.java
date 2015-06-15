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
import models.transfer.TransferResponseStatus;

import java.util.List;

import static play.data.Form.form;

@With(Headers.class)
public class CampaignPhases extends Controller{

    public static final Form<CampaignPhase> CAMPAIGN_PHASE_FORM = form(CampaignPhase.class);

    @Security.Authenticated(Secured.class)
    public static Result findCampaignPhases(Long campaignId){
        List<CampaignPhase> phases = CampaignPhase.findAll(campaignId);
        return ok(Json.toJson(phases));
    }

    @Security.Authenticated(Secured.class)
    public static Result findCampaignPhase(Long campaignId, Long phaseId){
        CampaignPhase campaignPhase = CampaignPhase.read(campaignId, phaseId);
        return ok(Json.toJson(campaignPhase));
    }

    @Security.Authenticated(Secured.class)
    public static Result deleteCampaignPhase(Long campaignId, Long phaseId){
        CampaignPhase.delete(campaignId,phaseId);
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public static Result updateCampaignPhase(Long campaignId, Long phaseId) {
        // 1. read the campaignPhase data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<CampaignPhase> newCampaignPhaseForm = CAMPAIGN_PHASE_FORM.bindFromRequest();

        if (newCampaignPhaseForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_PHASE_CREATE_MSG_ERROR, newCampaignPhaseForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            CampaignPhase newCampaignPhase = newCampaignPhaseForm.get();

            TransferResponseStatus responseBody = new TransferResponseStatus();

            newCampaignPhase.setPhaseId(phaseId);
            CampaignPhase camp2 = CampaignPhase.find.ref(phaseId);
            newCampaignPhase.update();
            Logger.info("Updating phase in campaign =>" + campaignId);
            Logger.debug("=> " + newCampaignPhaseForm.toString());

            responseBody.setNewResourceId(newCampaignPhase.getPhaseId());
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_PHASE_CREATE_MSG_SUCCESS,
                    newCampaignPhase.getPhaseId()));
            responseBody.setNewResourceURL(GlobalData.CAMPAIGN_PHASE_BASE_PATH + "/" + newCampaignPhase.getPhaseId());

            return ok(Json.toJson(responseBody));
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result createCampaignPhase(Long campaignId) {
        // 1. obtaining the user of the requestor
        User campaignPhaseCreator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));

        // 2. read the new campaign data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<CampaignPhase> newCampaignPhaseForm = CAMPAIGN_PHASE_FORM.bindFromRequest();

        if (newCampaignPhaseForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_PHASE_CREATE_MSG_ERROR, newCampaignPhaseForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            CampaignPhase newCampaignPhase = newCampaignPhaseForm.get();

            if(newCampaignPhase.getLang() == null)
                newCampaignPhase.setLang(campaignPhaseCreator.getLanguage());

            TransferResponseStatus responseBody = new TransferResponseStatus();

            CampaignPhase.create(campaignId, newCampaignPhase);
            Logger.info("Creating new campaign Phase");
            Logger.debug("=> " + newCampaignPhaseForm.toString());

            responseBody.setNewResourceId(newCampaignPhase.getPhaseId());
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_PHASE_CREATE_MSG_SUCCESS,
                    newCampaignPhase.getPhaseId()));
            responseBody.setNewResourceURL(GlobalData.CAMPAIGN_PHASE_BASE_PATH+"/"+newCampaignPhase.getPhaseId());

            return ok(Json.toJson(responseBody));
        }
    }
}
