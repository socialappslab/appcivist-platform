package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;

import http.Headers;
import models.Campaign;
import models.ComponentInstance;
import models.User;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.GlobalData;
import models.transfer.TransferResponseStatus;

import java.util.List;

import static play.data.Form.form;

@With(Headers.class)
public class CampaignComponents extends Controller {

	public static final Form<ComponentInstance> CAMPAIGN_COMPONENT_FORM = form(ComponentInstance.class);

	@SubjectPresent
	public static Result findCampaignComponents(Long aid, Long campaignId) {
		List<ComponentInstance> phases = ComponentInstance.findAll(campaignId);
		return ok(Json.toJson(phases));
	}

	@SubjectPresent
	public static Result findCampaignComponent(Long aid, Long campaignId,
			Long phaseId) {
		ComponentInstance campaignPhase = ComponentInstance.read(campaignId, phaseId);
		return ok(Json.toJson(campaignPhase));
	}

	@SubjectPresent
	public static Result deleteCampaignComponent(Long aid, Long campaignId,
			Long phaseId) {
		ComponentInstance.delete(campaignId, phaseId);
		return ok();
	}

	@SubjectPresent
	public static Result updateCampaignComponent(Long aid, Long campaignId,
			Long phaseId) {
		// 1. read the campaignPhase data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<ComponentInstance> newCampaignPhaseForm = CAMPAIGN_COMPONENT_FORM
				.bindFromRequest();

		if (newCampaignPhaseForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CAMPAIGN_PHASE_CREATE_MSG_ERROR,
					newCampaignPhaseForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {

			ComponentInstance newCampaignPhase = newCampaignPhaseForm.get();

			TransferResponseStatus responseBody = new TransferResponseStatus();

			newCampaignPhase.setComponentInstanceId(phaseId);
			ComponentInstance.update(newCampaignPhase);
			Logger.info("Updating phase in campaign =>" + campaignId);
			Logger.debug("=> " + newCampaignPhaseForm.toString());

			responseBody.setNewResourceId(newCampaignPhase.getComponentInstanceId());
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CAMPAIGN_PHASE_CREATE_MSG_SUCCESS,
					newCampaignPhase.getComponentInstanceId()));
			responseBody.setNewResourceURL(GlobalData.CAMPAIGN_PHASE_BASE_PATH
					+ "/" + newCampaignPhase.getComponentInstanceId());

			return ok(Json.toJson(responseBody));
		}
	}

	@SubjectPresent
	public static Result createCampaignComponent(Long aid, Long campaignId) {
		// 1. obtaining the user of the requestor
		User campaignPhaseCreator = User
				.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
		// 2. read the new campaign data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<ComponentInstance> newCampaignPhaseForm = CAMPAIGN_COMPONENT_FORM
				.bindFromRequest();

		if (newCampaignPhaseForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CAMPAIGN_PHASE_CREATE_MSG_ERROR,
					newCampaignPhaseForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			ComponentInstance newCampaignPhase = newCampaignPhaseForm.get();
			if (newCampaignPhase.getLang() == null)
				newCampaignPhase.setLang(campaignPhaseCreator.getLanguage());
			Campaign campaign = Campaign.read(campaignId);
			TransferResponseStatus responseBody = new TransferResponseStatus();
			ComponentInstance.create(campaignId, newCampaignPhase);
			campaign.getResources().getComponents().add(newCampaignPhase);
			campaign.update();
			Logger.info("Creating new campaign Phase");
			Logger.debug("=> " + newCampaignPhaseForm.toString());

			responseBody.setNewResourceId(newCampaignPhase.getComponentInstanceId());
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CAMPAIGN_PHASE_CREATE_MSG_SUCCESS,
					newCampaignPhase.getComponentInstanceId()));
			responseBody.setNewResourceURL(GlobalData.CAMPAIGN_PHASE_BASE_PATH
					+ "/" + newCampaignPhase.getComponentInstanceId());

			return ok(Json.toJson(responseBody));
		}
	}
}
