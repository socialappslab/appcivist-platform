package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

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
import security.SecurityModelConstants;
import utils.GlobalData;
import models.transfer.TransferResponseStatus;

import java.util.List;

import static play.data.Form.form;

@Api(value="/component",description="Component Instance Services: create and manage component instances")
@With(Headers.class)
public class CampaignComponents extends Controller {

	public static final Form<ComponentInstance> CAMPAIGN_COMPONENT_FORM = form(ComponentInstance.class);

	@ApiOperation(httpMethod = "GET", response = ComponentInstance.class, responseContainer = "List", produces = "application/json", value = "List components in assembly campaign")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "aid", value = "Owning assembly ID", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "campaignId", value = "Owning campaign ID", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findCampaignComponents(Long aid, Long campaignId) {
		List<ComponentInstance> components = ComponentInstance.findAll(campaignId);
		return ok(Json.toJson(components));
	}

	@ApiOperation(httpMethod = "GET", response = ComponentInstance.class, produces = "application/json", value = "List components in assembly campaign")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "aid", value = "Owning assembly ID", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "campaignId", value = "Owning campaign ID", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "componentId", value = "Component ID", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findCampaignComponent(Long aid, Long campaignId,
			Long componentId) {
		ComponentInstance campaignPhase = ComponentInstance.read(campaignId, componentId);
		return ok(Json.toJson(campaignPhase));
	}

	@ApiOperation(httpMethod = "DELETE", response = ComponentInstance.class, produces = "application/json", value = "List components in assembly campaign")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "aid", value = "Owning assembly ID", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "campaignId", value = "Owning campaign ID", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "componentId", value = "Component ID", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteCampaignComponent(Long aid, Long campaignId,
			Long phaseId) {
		ComponentInstance.delete(campaignId, phaseId);
		return ok();
	}

	@ApiOperation(httpMethod = "PUT", response = ComponentInstance.class, produces = "application/json", value = "List components in assembly campaign")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "aid", value = "Owning assembly ID", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "campaignId", value = "Owning campaign ID", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "componentId", value = "Component ID", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
		public static Result updateCampaignComponent(Long aid, Long campaignId,
			Long phaseId) {
		// 1. read the campaignPhase data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<ComponentInstance> newCampaignComponentForm = CAMPAIGN_COMPONENT_FORM
				.bindFromRequest();

		if (newCampaignComponentForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CAMPAIGN_PHASE_CREATE_MSG_ERROR,
					newCampaignComponentForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			ComponentInstance newCampaignComponent = newCampaignComponentForm.get();
			newCampaignComponent.setComponentInstanceId(phaseId);
			ComponentInstance.update(newCampaignComponent);
			Logger.info("Updating phase in campaign =>" + campaignId);
			Logger.debug("=> " + newCampaignComponentForm.toString());


			return ok(Json.toJson(newCampaignComponent));
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
