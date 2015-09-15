package controllers;

import static play.data.Form.form;
import http.Headers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.Assembly;
import models.Campaign;
import models.Membership;
import models.MembershipAssembly;
import models.User;
import models.transfer.MembershipTransfer;
import models.transfer.TransferResponseStatus;
import models.transfer.UpdateTransfer;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.SecurityModelConstants;
import utils.GlobalData;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Api(value="/campaign",description="Campaign management endpoints")
@With(Headers.class)
public class Campaigns extends Controller {

	public static final Form<Campaign> CAMPAIGN_FORM = form(Campaign.class);

	@ApiOperation(httpMethod = "GET", response = MembershipTransfer.class, produces = "application/json", value = "List campaigns of an Assembly")
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findCampaigns(Long aid) {
		List<Campaign> campaigns = Campaign.findByAssembly(aid);
		return ok(Json.toJson(campaigns));
	}

	// TODO create dynamic auth handler that controlls the campaigns belong to
	// the specified assembly
	// that's why assembly id is always a parameter
	@SubjectPresent
	public static Result findCampaign(Long aid, Long campaignId) {
		Campaign campaign = Campaign.read(campaignId);
		return ok(Json.toJson(campaign));
	}

	@SubjectPresent
	public static Result deleteCampaign(Long aid, Long campaignId) {
		Campaign.delete(campaignId);
		return ok();
	}

	@SubjectPresent
	public static Result updateCampaign(Long aid, Long campaignId) {
		// 1. read the campaign data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Campaign> newCampaignForm = CAMPAIGN_FORM.bindFromRequest();

		if (newCampaignForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
					newCampaignForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Campaign newCampaign = newCampaignForm.get();
			TransferResponseStatus responseBody = new TransferResponseStatus();
			if (Campaign.readByTitle(newCampaign.getTitle()) > 0) {
				Logger.info("Campaign already exists");
			} else {
				newCampaign.setCampaignId(campaignId);
				newCampaign.update();
				Logger.info("Updating campaign");
				Logger.debug("=> " + newCampaignForm.toString());
				responseBody.setNewResourceId(newCampaign.getCampaignId());
				responseBody.setStatusMessage(Messages.get(
						GlobalData.CAMPAIGN_CREATE_MSG_SUCCESS,
						newCampaign.getTitle()));
				responseBody.setNewResourceURL(GlobalData.CAMPAIGN_BASE_PATH
						+ "/" + newCampaign.getCampaignId());
			}
			return ok(Json.toJson(responseBody));
		}
	}

	@SubjectPresent
	public static Result createCampaign(Long aid) {
		// 1. obtaining the user of the requestor
		User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));
		// 2. read the new campaign data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Campaign> newCampaignForm = CAMPAIGN_FORM.bindFromRequest();

		if (newCampaignForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
					newCampaignForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Campaign newCampaign = newCampaignForm.get();
			newCampaign.setAssemblyResourceSet(Assembly.read(aid).getResources());
			if (newCampaign.getLang() == null)
				newCampaign.setLang(campaignCreator.getLanguage());
			TransferResponseStatus responseBody = new TransferResponseStatus();
			if (Campaign.readByTitle(newCampaign.getTitle()) > 0) {
				Logger.info("Campaign already exists");
			} else {
				Campaign.create(newCampaign);
				Logger.info("Creating new campaign");
				Logger.debug("=> " + newCampaignForm.toString());
				responseBody.setNewResourceId(newCampaign.getCampaignId());
				responseBody.setStatusMessage(Messages.get(
						GlobalData.CAMPAIGN_CREATE_MSG_SUCCESS,
						newCampaign.getTitle()));
				responseBody.setNewResourceURL(GlobalData.CAMPAIGN_BASE_PATH
						+ "/" + newCampaign.getCampaignId());
			}
			return ok(Json.toJson(responseBody));
		}
	}
	
	@ApiOperation(httpMethod = "GET", response = UpdateTransfer.class, responseContainer="List", produces = "application/json", value = "Update user information", notes = "Updates user information")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response=TransferResponseStatus.class) })
	@ApiImplicitParams({
		//@ApiImplicitParam(name="user", value="user", dataType="String", defaultValue="user", paramType = "path"),
		@ApiImplicitParam(name="uuid", value="Assembly's UUID", dataType="java.util.UUID", paramType="path"),
		@ApiImplicitParam(name="SESSION_KEY", value="User's session authentication key", dataType="String", paramType="header"),
		@ApiImplicitParam(name="filter", value="Filter value", dataType="String", paramType="query", allowableValues="ongoing,past,future,all", defaultValue="ongoing")
	})
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result campaignsByAssembly(UUID uuid, String filter) {
		if (filter==null || filter.equals("ongoing")) {
			return ongoingCampaignsByAssembly(uuid);
		} else if (filter.equals("past")) {
			return internalServerError(Json.toJson(new TransferResponseStatus("Not implemented")));
		} else if (filter.equals("future")) {
			return internalServerError(Json.toJson(new TransferResponseStatus("Not implemented")));
		} else {
			Assembly a = Assembly.readByUUID(uuid);
			List<Campaign> campaigns = a.getResources().getCampaigns();
			if (!campaigns.isEmpty()) return ok(Json.toJson(campaigns));
			else
				return notFound(Json.toJson(new TransferResponseStatus("No ongoing campaigns")));
		}
	}
	
	@ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer="List", produces = "application/json", value = "Update user information", notes = "Updates user information")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No Campaign Found", response=TransferResponseStatus.class) })
	@ApiImplicitParams({
		//@ApiImplicitParam(name="user", value="user", dataType="String", defaultValue="user", paramType = "path"),
		@ApiImplicitParam(name="uuid", value="User's UUID", dataType="java.util.UUID", paramType="path"),
		@ApiImplicitParam(name="SESSION_KEY", value="User's session authentication key", dataType="String", paramType="header"),
		@ApiImplicitParam(name="filter", value="Filter value", dataType="String", paramType="query", allowableValues="ongoing,past,future,all", defaultValue="ongoing")
	})
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result campaignsByUser(UUID uuid, String filter) {
		if (filter==null || filter.equals("ongoing")) {
			return ongoingCampaignsByUser(uuid);
		} else if (filter.equals("past")) {
			return internalServerError(Json.toJson(new TransferResponseStatus("Not implemented")));
		} else if (filter.equals("future")) {
			return internalServerError(Json.toJson(new TransferResponseStatus("Not implemented")));
		} else {
			User u = User.findByUUID(uuid);
			List<Membership> assemblyMemberships = Membership.findByUser(u, "ASSEMBLY");
			List<Campaign> campaigns = new ArrayList<Campaign>();
			for (Membership membership : assemblyMemberships) {
				Assembly a = ((MembershipAssembly) membership).getAssembly();
				campaigns.addAll(a.getResources().getCampaigns());
			}
			if (!campaigns.isEmpty()) return ok(Json.toJson(campaigns));
			else
				return notFound(Json.toJson(new TransferResponseStatus("No ongoing campaigns")));
		}
	}
	
	private static Result ongoingCampaignsByUser(UUID uuid) {
		User u = User.findByUUID(uuid);
		List<Membership> assemblyMemberships = Membership.findByUser(u, "ASSEMBLY");
		List<Campaign> ongoingCampaigns = new ArrayList<Campaign>();
		
		for (Membership membership : assemblyMemberships) {
			Assembly a = ((MembershipAssembly) membership).getAssembly();
			ongoingCampaigns.addAll(Campaign.extractOngoingCampaignsFromAssembly(a));
		}
		
		if (!ongoingCampaigns.isEmpty()) return ok(Json.toJson(ongoingCampaigns));
		else
			return notFound(Json.toJson(new TransferResponseStatus("No ongoing campaigns")));
	}

	
	
	
	
	private static Result ongoingCampaignsByAssembly(UUID uuid) {
		Assembly a = Assembly.readByUUID(uuid);
		List<Campaign> ongoingCampaigns = new ArrayList<Campaign>();
		ongoingCampaigns.addAll(Campaign.extractOngoingCampaignsFromAssembly(a));
		if (!ongoingCampaigns.isEmpty()) return ok(Json.toJson(ongoingCampaigns));
		else
			return notFound(Json.toJson(new TransferResponseStatus("No ongoing campaigns")));
	}
}
