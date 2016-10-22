package controllers;

import static play.data.Form.form;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.ContributionTypes;
import http.Headers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import models.Assembly;
import models.Campaign;
import models.Membership;
import models.MembershipAssembly;
import models.Resource;
import models.User;
import models.misc.Views;
import models.transfer.CampaignSummaryTransfer;
import models.transfer.CampaignTransfer;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.twirl.api.Content;
import security.SecurityModelConstants;
import utils.GlobalData;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.feth.play.module.pa.PlayAuthenticate;

import delegates.CampaignDelegate;
import delegates.ResourcesDelegate;
import enums.ResourceTypes;

@Api(value = "03 campaign: Campaign Management", description = "Campaign Making Service: create and manage assembly campaigns")
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
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findCampaignsByAssemblyId(@ApiParam(name = "aid", value = "Assembly ID") Long aid) {
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
	@ApiOperation(httpMethod = "GET", response = Campaign.class, produces = "application/json", value = "Read campaign by campaign and assembly IDs")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findCampaignByAssemblyId(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid, 
			@ApiParam(name = "cid", value = "Campaign ID") Long campaignId) {
		Campaign campaign = Campaign.read(campaignId);
		return campaign != null ? ok(Json.toJson(campaign)) : ok(Json
				.toJson(new TransferResponseStatus("No campaign found")));
	}
	
	@ApiOperation(httpMethod = "GET", response = Campaign.class, produces = "application/json", value = "Read campaign by Universal ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	public static Result findCampaignByUUID(@ApiParam(name = "uuid", value = "Campaign Universal ID (UUID)") UUID uuid) {
		try{

			Campaign summary = CampaignDelegate.getCampaignSummary(uuid);
			if(summary == null){
				return ok(Json
						.toJson(new TransferResponseStatus("No campaign found")));
			}
			//We have to show only ideas, discussions or proposals
			summary.setContributions(summary.getContributions().stream().filter(c -> {
				if(c.getType().equals(ContributionTypes.IDEA) || c.getType().equals(ContributionTypes.DISCUSSION)
						|| c.getType().equals(ContributionTypes.PROPOSAL)){
					return true;
				}else {
					return false;
				}
			}).collect(Collectors.toList()));
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			String result = mapper.writerWithView(Views.Public.class)
					.writeValueAsString(summary);

			Content ret = new Content() {
				@Override public String body() { return result; }
				@Override public String contentType() { return "application/json"; }
			};

			return Results.ok(ret);
		}catch(Exception e){
			return badRequest(Json.toJson(Json
					.toJson(new TransferResponseStatus("Error processing request"))));
		}
//		CampaignSummaryTransfer summary = CampaignDelegate.getCampaignSummary(uuid);
//		ObjectMapper mapper = new ObjectMapper();
//		String result = mapper.writerWithView(Views.Public.class)
//				.writeValueAsString(summary);
//		return summary  != null ? ok(Json.toJson(summary)) : ok(Json
//				.toJson(new TransferResponseStatus("No campaign found")));
	}


	/**
	 * GET /api/ballot/:uuid/campaign
	 * Read campaign by assembly ID and campaign ID
	 * @param aid
	 * @param campaignId
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = CampaignSummaryTransfer.class, responseContainer="List", produces = "application/json", value = "Read campaign by voting ballot Universal ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	public static Result findCampaignsByBallot(@ApiParam(name = "uuid", value = "Ballot Universal ID")  UUID uuid) {
		List<CampaignSummaryTransfer> campaignSummaries = CampaignDelegate.findByBindingBallot(uuid);
		return campaignSummaries != null ? ok(Json.toJson(campaignSummaries)) : ok(Json
				.toJson(new TransferResponseStatus("No campaign found")));
	}
	
	/**
	 * DELETE /api/assembly/:aid/campaign/:cid
	 * Delete campaign by ID
	 * @param aid
	 * @param campaignId
	 * @return
	 */
	@ApiOperation(httpMethod = "DELETE", response = Campaign.class, produces = "application/json", value = "Delete campaign by campaign and assembly IDs", notes="Only for COORDINATOS of assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteCampaign(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid, 
			@ApiParam(name = "cid", value = "Campaign ID") Long campaignId) {
		// TODO: implement soft delete
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
	@ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Update a campaign by its ID and the assembly ID", notes="Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Campaign object", value = "Campaign in json", dataType = "models.Campaign", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateCampaign(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid, 
			@ApiParam(name = "cid", value = "Campaign ID") Long campaignId) {
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
	@ApiOperation(httpMethod = "POST", response = CampaignTransfer.class, produces = "application/json", value = "Create a new Campaign", notes="Only for COORDINATORS. The templates will be used to import all the resources from a list of existing campaigns to the new")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Campaign simplified object", value = "Campaign in json", dataType = "models.transfer.CampaignTransfer", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createCampaignInAssembly(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid, 
			@ApiParam(name="templates", value="List of campaign ids (separated by comma) to use as template for the current campaign") String templates) {
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
						campaignTransfer, campaignCreator, aid, templates);
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
			notes = "Only for MEMBERS of the assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Campaigns not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
	})
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result findCampaignsByAssemblyUUID(
			@ApiParam(name = "uuid", value = "Assembly's Universal ID") UUID uuid, 
			@ApiParam(name = "filter", value = "Filter campaign by status (ongoing, past, upcoming, all)", allowableValues = "ongoing,past,future,all", defaultValue = "ongoing") String filter) {
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
			notes = "Only for SELF")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No Campaign Found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", 
					paramType = "header")
	})
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result campaignsByUserId(
			@ApiParam(name = "uid", value = "User's ID") Long uid, 
			@ApiParam(name = "filter", value = "Filter campaign by status (ongoing, past, upcoming, all)", allowableValues = "ongoing,past,future,all", defaultValue = "ongoing") String filter) {
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
			notes = "Only for SELF")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No Campaign Found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
	})
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result campaignsByUserUuid(
			@ApiParam(name = "uuid", value = "User's Universal ID") UUID uuid, 
			@ApiParam(name = "filter", value = "Filter campaign by status (ongoing, past, upcoming, all)", allowableValues = "ongoing,past,future,all", defaultValue = "ongoing") String filter) {
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
	@ApiOperation(httpMethod = "GET", response = URL.class, responseContainer = "List", produces = "application/json", value = "Get list of available campaign templates", notes = "Get list of available campaign templates")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No Campaign Template Found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"), })
	@SubjectPresent
	public static Result findCampaignTemplates() {
		// Must return resource, padTransfer, writable url or read only url??
		List<Resource> cts = Resource.findByResourceType(ResourceTypes.CONTRIBUTION_TEMPLATE);
		if (cts != null && !cts.isEmpty()) {
			List<URL> urls = cts.stream().map(sc -> sc.getUrl()).collect(Collectors.toList());
			//return ok(Json.toJson(cts));
			return ok(Json.toJson(urls));
		} else {
			return notFound(Json.toJson(new TransferResponseStatus(
					"No campaign templates")));
		}
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
	private static Result ongoingCampaignsByUserId(
			@ApiParam(name="id", value="User ID") Long uid) {
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

	/** TODO For templates and contribution, the resources (and related pad) not confirmed after x time must be eliminated **/

	/**
	 * POST /api/campaign/template
	 * Create a new Resource CONTRIBUTION_TEMPLATE
	 * @param text
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Resource.class, value = "Create a new Contribution Template for the campaign", notes="Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createCampaignTemplatePad(
			// TODO: move text to body of request
			@ApiParam(name="text", value="text for the template") String text) {
		User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
		Resource res = ResourcesDelegate.createResource(campaignCreator, text, ResourceTypes.CONTRIBUTION_TEMPLATE);
		if (res != null) {
			return ok(Json.toJson(res));
		} else {
			return internalServerError("The HTML text is malformed.");
		}

	}

	/**
	 * PUT /api/campaign/template
	 * Confirm a Resource CONTRIBUTION_TEMPLATE
	 * @param rid
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", value = "Confirm Contribution Template")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result confirmCampaignTemplatePad(
			@ApiParam(name = "Resource ID", value = "Contribution Template") Long rid) {
		Resource res = ResourcesDelegate.confirmResource(rid);
		return ok(Json.toJson(res));
	}
}
