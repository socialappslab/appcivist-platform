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
import models.User;
import models.transfer.CampaignSummaryTransfer;
import models.transfer.CampaignTransfer;
import models.transfer.TransferResponseStatus;
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

import com.avaje.ebean.Ebean;
import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import delegates.CampaignDelegate;

@Api(value = "/campaign", description = "Campaign Making Service: create and manage assembly campaigns")
@With(Headers.class)
public class Campaigns extends Controller {

	public static final Form<Campaign> CAMPAIGN_FORM = form(Campaign.class);
	public static final Form<CampaignTransfer> CAMPAIGN_TRANSFER_FORM = form(CampaignTransfer.class);

	/**
	 * GET /api/assembly/:aid/campaign
	 * List campaigns of an Assembly
	 * @param aid Assembly Id
	 * @return
	 */
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

	/**
	 * GET /api/assembly/:aid/campaign/:cid
	 * Read campaign by assembly ID and campaign ID
	 * @param aid
	 * @param campaignId
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Campaign.class, produces = "application/json", value = "Get campaign by ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly owner numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Campaign numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findCampaignByAssemblyId(Long aid, Long campaignId) {
		Campaign campaign = Campaign.read(campaignId);
		return campaign != null ? ok(Json.toJson(campaign)) : ok(Json
				.toJson(new TransferResponseStatus("No campaign found")));
	}
	
	@ApiOperation(httpMethod = "GET", response = Campaign.class, produces = "application/json", value = "Get campaign by UUID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uuid", value = "Campaign Universal ID (UUID)", dataType = "java.util.UUID", paramType = "path") })
	public static Result findCampaignByUUID(UUID uuid) {
		CampaignSummaryTransfer summary = CampaignDelegate.getCampaignSummary(uuid);
		return summary  != null ? ok(Json.toJson(summary)) : ok(Json
				.toJson(new TransferResponseStatus("No campaign found")));
	}

	/**
	 * DELETE /api/assembly/:aid/campaign/:cid
	 * Delete campaign by ID
	 * @param aid
	 * @param campaignId
	 * @return
	 */
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

	/**
	 * PUT /api/assembly/:aid/campaign/:cid
	 * Update campaign by ID
	 * @param aid
	 * @param campaignId
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Delete campaign by ID")
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

	/**
	 * POST /api/assembly/:aid/campaign
	 * Create a new Campaign
	 * @param aid
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Campaign.class, produces = "application/json", value = "Create a new Campaign")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly owner numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "New Campaign Form", value = "Campaign in json", dataType = "models.Campaign", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	// TODO re-enable the control for creating campaigns once it works
	// @Dynamic(value = "CoordinatorOfAssembly", meta =
	// SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createCampaignInAssembly(Long aid) {
		try {
			Ebean.beginTransaction();
			// 1. obtaining the user of the requestor
			User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate
					.getUser(session()));
			// 2. read the new campaign data from the body
			// another way of getting the body content => request().body().asJson()
			final Form<CampaignTransfer> newCampaignForm = CAMPAIGN_TRANSFER_FORM
					.bindFromRequest();

			if (newCampaignForm.hasErrors()) {
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
						newCampaignForm.errorsAsJson()));
				Ebean.rollbackTransaction();
				return badRequest(Json.toJson(responseBody));
			} else {
				CampaignTransfer campaignTransfer = newCampaignForm.get();
				CampaignTransfer newCampaign = CampaignDelegate.create(
						campaignTransfer, campaignCreator, aid);
				Ebean.commitTransaction();
				return ok(Json.toJson(newCampaign));
			}
		} catch (Exception e) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
					"There was an internal error: "+e.getMessage()));
			Ebean.rollbackTransaction();
			e.printStackTrace();
			return badRequest(Json.toJson(responseBody));
		}
	}

	/**
	 * GET /api/assembly/:uuid/campaign
	 * Given an Assembly Universal ID (uuid), return its campaigns
	 * @param uuid
	 * @param filter
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer = "List", produces = "application/json", 
			value = "Given an Assembly Universal ID (uuid), return its campaigns", 
			notes = "Given an Assembly Universal ID (uuid), return its campaigns")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Campaigns not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uuid", value = "Assembly's UUID", dataType = "java.util.UUID", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", 
					paramType = "header"),
			@ApiImplicitParam(name = "filter", value = "Filter campaign by status (ongoing, past, upcoming, all)", dataType = "String", 
					paramType = "query", allowableValues = "ongoing,past,future,all", defaultValue = "ongoing") 
	})
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result findCampaignsByAssemblyUUID(UUID uuid, String filter) {
		if (filter == null || filter.equals("ongoing")) {
			return ongoingCampaignsByAssembly(uuid);
		} else if (filter.equals("past")) {
			return internalServerError(Json.toJson(new TransferResponseStatus(
					"Not implemented")));
		} else if (filter.equals("future")) {
			return internalServerError(Json.toJson(new TransferResponseStatus(
					"Not implemented")));
		} else {
			Assembly a = Assembly.readByUUID(uuid);
			List<Campaign> campaigns = a.getResources().getCampaigns();
			if (!campaigns.isEmpty())
				return ok(Json.toJson(campaigns));
			else
				return notFound(Json.toJson(new TransferResponseStatus(
						"No campaign found")));
		}
	}

	/**
	 * GET /api/user/:uid/campaign
	 * Given a users local ID (uid), returns an array of campaigns that are organized by that user's assemblies
	 */
	@ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer = "List", produces = "application/json", 
			value = "Given a users local ID (uid), returns an array of campaigns that are organized by that user's assemblies", 
			notes = "Given a users local ID (uid), returns an array of campaigns that are organized by that user's assemblies")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No Campaign Found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", 
					paramType = "header"),
			@ApiImplicitParam(name = "filter", value = "Filter campaign by status (ongoing, past, upcoming, all)", dataType = "String", 
					paramType = "query", allowableValues = "ongoing,past,future,all", defaultValue = "ongoing") 
	})
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result campaignsByUserId(Long uid, String filter) {
		if (filter == null || filter.equals("ongoing")) {
			return ongoingCampaignsByUserId(uid);
		} else if (filter.equals("past")) {
			return internalServerError(Json.toJson(new TransferResponseStatus(
					"Not implemented")));
		} else if (filter.equals("future")) {
			return internalServerError(Json.toJson(new TransferResponseStatus(
					"Not implemented")));
		} else {
			User u = User.read(uid);
			List<Membership> assemblyMemberships = Membership.findByUser(u,
					"ASSEMBLY");
			List<Campaign> campaigns = new ArrayList<Campaign>();
			for (Membership membership : assemblyMemberships) {
				Assembly a = ((MembershipAssembly) membership).getAssembly();
				campaigns.addAll(a.getResources().getCampaigns());
			}
			if (!campaigns.isEmpty())
				return ok(Json.toJson(campaigns));
			else
				return notFound(Json.toJson(new TransferResponseStatus(
						"No ongoing campaigns")));
		}
	}

	/**
	 * GET /api/user/:uuid/campaign
	 * Given a users universal ID (uuid), returns an array of campaigns that are organized by that user's assemblies
	 */
	@ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer = "List", produces = "application/json", 
			value = "Given a users universal ID (uuid), returns an array of campaigns that are organized by that user's assemblies", 
			notes = "Given a users universal ID (uuid), returns an array of campaigns that are organized by that user's assemblies")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No Campaign Found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uuid", value = "User's UUID", dataType = "java.util.UUID", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
			@ApiImplicitParam(name = "filter", value = "Filter campaign by status (ongoing, past, upcoming, all)", dataType = "String", paramType = "query", allowableValues = "ongoing,past,future,all", defaultValue = "ongoing") })
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result campaignsByUserUuid(UUID uuid, String filter) {
		if (filter == null || filter.equals("ongoing")) {
			return ongoingCampaignsByUserUuid(uuid);
		} else if (filter.equals("past")) {
			return internalServerError(Json.toJson(new TransferResponseStatus(
					"Not implemented")));
		} else if (filter.equals("future")) {
			return internalServerError(Json.toJson(new TransferResponseStatus(
					"Not implemented")));
		} else {
			User u = User.findByUUID(uuid);
			List<Membership> assemblyMemberships = Membership.findByUser(u,
					"ASSEMBLY");
			List<Campaign> campaigns = new ArrayList<Campaign>();
			for (Membership membership : assemblyMemberships) {
				Assembly a = ((MembershipAssembly) membership).getAssembly();
				campaigns.addAll(a.getResources().getCampaigns());
			}
			if (!campaigns.isEmpty())
				return ok(Json.toJson(campaigns));
			else
				return notFound(Json.toJson(new TransferResponseStatus(
						"No ongoing campaigns")));
		}
	}

	/**
	 * GET /api/campaign/template Get list of available campaign templates
	 * 
	 * @return JSON array with the list of campaign templates
	 */
	@ApiOperation(httpMethod = "GET", response = CampaignTemplate.class, responseContainer = "List", produces = "application/json", value = "Get list of available campaign templates", notes = "Get list of available campaign templates")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No Campaign Template Found", response = TransferResponseStatus.class) })
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
	/**
	 * Get campaigns that are ongoing given an User's Universal ID (uuid)
	 * @param uuid User's Universal ID (uuid)
	 * @return
	 */
	private static Result ongoingCampaignsByUserUuid(UUID uuid) {
		User u = User.findByUUID(uuid);
		return ongoingCampaignsByUser(u);
	}

	/**
	 * Get campaigns that are ongoing given an User's Local ID (uid)
	 * @param uid User's Local Id
	 * @return
	 */
	private static Result ongoingCampaignsByUserId(Long uid) {
		User u = User.read(uid);
		return ongoingCampaignsByUser(u);
	}

	/**
	 * Get campaigns that are ongoing given an User object
	 * @param u User's object
	 * @return
	 */
	private static Result ongoingCampaignsByUser(User u) {
		List<Membership> assemblyMemberships = Membership.findByUser(u,
				"ASSEMBLY");
		List<Campaign> ongoingCampaigns = new ArrayList<Campaign>();

		for (Membership membership : assemblyMemberships) {
			Long aid = ((MembershipAssembly) membership).getAssembly()
					.getAssemblyId();
			List<Campaign> ongoing = Campaign
					.getOngoingCampaignsFromAssembly(aid);
			if (ongoing != null)
				ongoingCampaigns.addAll(ongoing);
		}

		if (!ongoingCampaigns.isEmpty())
			return ok(Json.toJson(ongoingCampaigns));
		else
			return notFound(Json.toJson(new TransferResponseStatus(
					"No ongoing campaigns")));
	}

	/**
	 * Get campaigns that are ongoing given an Assembly's Universal ID (uuid)
	 * @param uuid
	 * @return
	 */
	private static Result ongoingCampaignsByAssembly(UUID uuid) {
		Assembly a = Assembly.readByUUID(uuid);
		List<Campaign> ongoingCampaigns = new ArrayList<Campaign>();
		ongoingCampaigns.addAll(Campaign.getOngoingCampaignsFromAssembly(a));
		if (!ongoingCampaigns.isEmpty())
			return ok(Json.toJson(ongoingCampaigns));
		else
			return notFound(Json.toJson(new TransferResponseStatus(
					"No ongoing campaigns")));
	}
}
