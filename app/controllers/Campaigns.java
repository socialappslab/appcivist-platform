package controllers;

import static play.data.Form.form;
import http.Headers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.Assembly;
import models.Campaign;
import models.CampaignTemplate;
import models.Membership;
import models.MembershipAssembly;
import models.ResourceSpace;
import models.User;
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

@Api(value="/campaign",description="Campaign Making Service: create and manage assembly campaigns")
@With(Headers.class)
public class Campaigns extends Controller {

	public static final Form<Campaign> CAMPAIGN_FORM = form(Campaign.class);

	@ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer = "List", produces = "application/json", value = "List campaigns of an Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "aid", value = "Assembly owner id", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findCampaignsByAssemblyId(Long aid) {
		List<Campaign> campaigns = Assembly.findCampaigns(aid);
		return campaigns != null && !campaigns.isEmpty() ? ok(Json
				.toJson(campaigns)) : ok(Json
				.toJson(new TransferResponseStatus("No campaign found")));
	}

	@ApiOperation(httpMethod = "GET", response = Campaign.class, produces = "application/json", value = "Get campaign by ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
		@ApiImplicitParam(name = "aid", value = "Assembly owner numerical id", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "cid", value = "Campaign numerical id", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findCampaignByAssemblyId(Long aid, Long campaignId) {
		Campaign campaign = Campaign.read(campaignId);
		return campaign != null ? ok(Json
				.toJson(campaign)) : ok(Json
				.toJson(new TransferResponseStatus("No campaign found")));
	}

	@ApiOperation(httpMethod = "DELETE", response = Campaign.class, produces = "application/json", value = "Get campaign by ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
		@ApiImplicitParam(name = "aid", value = "Assembly owner numerical id", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "cid", value = "Campaign numerical id", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteCampaign(Long aid, Long campaignId) {
		Campaign.delete(campaignId);
		return ok();
	}

	@ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Update campaign by ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
		@ApiImplicitParam(name = "aid", value = "Assembly owner numerical id", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "cid", value = "Campaign numerical id", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "new campaign form", value = "Campaign in json", dataType = "models.Campaign", paramType = "body"),
		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateCampaign(Long aid, Long campaignId) {
		// 1. read the campaign data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Campaign> newCampaignForm = CAMPAIGN_FORM.bindFromRequest();

		if (newCampaignForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
					newCampaignForm.errorsAsJson()));
			Logger.info("Error pdating campaign");
			Logger.debug("=> " + newCampaignForm.errorsAsJson());
			return badRequest(Json.toJson(responseBody));
		} else {
			Campaign updatedCampaign = newCampaignForm.get();
			updatedCampaign.setCampaignId(campaignId);
			updatedCampaign.update();
			Logger.info("Updating campaign");
			Logger.debug("=> " + newCampaignForm.toString());
			return ok(Json.toJson(updatedCampaign));
		}
	}

	@ApiOperation(httpMethod = "POST", response = Campaign.class, produces = "application/json", value = "Get campaign by ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
		@ApiImplicitParam(name = "aid", value = "Assembly owner numerical id", dataType = "Long", paramType = "path"),
		@ApiImplicitParam(name = "new campaign form", value = "Campaign in json", dataType = "models.Campaign", paramType = "body"),
		@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createCampaignInAssembly(Long aid) {
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
			if (newCampaign.getLang() == null) 
				newCampaign.setLang(campaignCreator.getLanguage());
			
			Logger.info("Creating new campaign");
			Logger.debug("=> " + newCampaignForm.toString());

			
			// Adding the new campaign to the Assembly Resource Space
			Campaign.create(newCampaign);
			ResourceSpace assemblyResources = Assembly.read(aid).getResources();
			assemblyResources.addCampaign(newCampaign);
			assemblyResources.update();
			newCampaign.refresh();
			return ok(Json.toJson(newCampaign));
		}
	}
	
	@ApiOperation(httpMethod = "GET", response = UpdateTransfer.class, responseContainer="List", produces = "application/json", value = "Update user information", notes = "Updates user information")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response=TransferResponseStatus.class) })
	@ApiImplicitParams({
		@ApiImplicitParam(name="uuid", value="Assembly's UUID", dataType="java.util.UUID", paramType="path"),
		@ApiImplicitParam(name="SESSION_KEY", value="User's session authentication key", dataType="String", paramType="header"),
		@ApiImplicitParam(name="filter", value="Filter value", dataType="String", paramType="query", allowableValues="ongoing,past,future,all", defaultValue="ongoing")
	})
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result findCampaignsByAssemblyUUID(UUID uuid, String filter) {
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
				return notFound(Json.toJson(new TransferResponseStatus("No campaign found")));
		}
	}
	
	@ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer="List", produces = "application/json", value = "Update user information", notes = "Updates user information")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No Campaign Found", response=TransferResponseStatus.class) })
	@ApiImplicitParams({
		@ApiImplicitParam(name="uid", value="User's ID", dataType="Long", paramType="path"),
		@ApiImplicitParam(name="SESSION_KEY", value="User's session authentication key", dataType="String", paramType="header"),
		@ApiImplicitParam(name="filter", value="Filter value", dataType="String", paramType="query", allowableValues="ongoing,past,future,all", defaultValue="ongoing")
	})
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result campaignsByUserId(Long uid, String filter) {
		if (filter==null || filter.equals("ongoing")) {
			return ongoingCampaignsByUserId(uid);
		} else if (filter.equals("past")) {
			return internalServerError(Json.toJson(new TransferResponseStatus("Not implemented")));
		} else if (filter.equals("future")) {
			return internalServerError(Json.toJson(new TransferResponseStatus("Not implemented")));
		} else {
			User u = User.read(uid);
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
	
	@ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer="List", produces = "application/json", value = "Update user information", notes = "Updates user information")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No Campaign Found", response=TransferResponseStatus.class) })
	@ApiImplicitParams({
		@ApiImplicitParam(name="uuid", value="User's UUID", dataType="java.util.UUID", paramType="path"),
		@ApiImplicitParam(name="SESSION_KEY", value="User's session authentication key", dataType="String", paramType="header"),
		@ApiImplicitParam(name="filter", value="Filter value", dataType="String", paramType="query", allowableValues="ongoing,past,future,all", defaultValue="ongoing")
	})
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result campaignsByUserUuid(UUID uuid, String filter) {
		if (filter==null || filter.equals("ongoing")) {
			return ongoingCampaignsByUserUuid(uuid);
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
			else return notFound(Json.toJson(new TransferResponseStatus("No ongoing campaigns")));
		}
	}
	
	
	@ApiOperation(httpMethod = "GET", response = CampaignTemplate.class, responseContainer = "List", produces = "application/json", value = "Update user information", notes = "Updates user information")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No Campaign Found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"), })
	@SubjectPresent
	public static Result findCampaignTemplates() {
		List<CampaignTemplate> cts = CampaignTemplate.findAll();
		if (cts != null && !cts.isEmpty())
			return ok(Json.toJson(cts));
		else
			return notFound(Json.toJson(new TransferResponseStatus(
					"No campaign templates")));

	}
	
	// Private not exposed Methods
	private static Result ongoingCampaignsByUserUuid(UUID uuid) {
		User u = User.findByUUID(uuid);
		return ongoingCampaignsByUser(u);
	}

	private static Result ongoingCampaignsByUserId(Long uid) {
		User u = User.read(uid);
		return ongoingCampaignsByUser(u);
	}
	
	private static Result ongoingCampaignsByUser(User u) {
		List<Membership> assemblyMemberships = Membership.findByUser(u, "ASSEMBLY");
		List<Campaign> ongoingCampaigns = new ArrayList<Campaign>();
		
		for (Membership membership : assemblyMemberships) {
			Assembly a = ((MembershipAssembly) membership).getAssembly();
			ongoingCampaigns.addAll(Campaign.getOngoingCampaignsFromAssembly(a));
		}
		
		if (!ongoingCampaigns.isEmpty()) return ok(Json.toJson(ongoingCampaigns));
		else
			return notFound(Json.toJson(new TransferResponseStatus("No ongoing campaigns")));
	}

	
	private static Result ongoingCampaignsByAssembly(UUID uuid) {
		Assembly a = Assembly.readByUUID(uuid);
		List<Campaign> ongoingCampaigns = new ArrayList<Campaign>();
		ongoingCampaigns.addAll(Campaign.getOngoingCampaignsFromAssembly(a));
		if (!ongoingCampaigns.isEmpty()) return ok(Json.toJson(ongoingCampaigns));
		else
			return notFound(Json.toJson(new TransferResponseStatus("No ongoing campaigns")));
	}
}
