package controllers;

import static play.data.Form.form;
import http.Headers;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import models.Assembly;
import models.Ballot;
import models.BallotCandidate;
import models.Campaign;
import models.Component;
import models.Contribution;
import models.ContributionFeedback;
import models.ContributionStatistics;
import models.ContributionTemplate;
import models.MembershipInvitation;
import models.Resource;
import models.ResourceSpace;
import models.User;
import models.WorkingGroup;
import models.WorkingGroupProfile;
import models.transfer.InvitationTransfer;
import models.transfer.PadTransfer;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.With;
import security.SecurityModelConstants;
import utils.GlobalData;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import delegates.ContributionsDelegate;
import enums.ContributionTypes;
import enums.ManagementTypes;
import enums.ResourceSpaceTypes;
import enums.ResponseStatus;
import enums.SupportedMembershipRegistration;
import exceptions.MembershipCreationException;

@Api(value = "/contribution", description = "Contribution Making Service: contributions by citizens to different spaces of civic engagement")
@With(Headers.class)
public class Contributions extends Controller {

	public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);
	public static final Form<ContributionFeedback> CONTRIBUTION_FEEDBACK_FORM = form(ContributionFeedback.class);
	public static final Form<Resource> ATTACHMENT_FORM = form(Resource.class);

	/**
	 * GET       /api/assembly/:aid/contribution
	 * @param aid
	 * @param space
	 * @param type
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "space", value = "Resource space name within assembly", dataType = "String", paramType = "query", allowableValues = "forum,resources", defaultValue = "forum"),
			@ApiImplicitParam(name = "type", value = "Type of contributions", dataType = "String", paramType = "query", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = ""),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findAssemblyContributions(Long aid, String space,
			String type) {
		Assembly a = Assembly.read(aid);
		ResourceSpace rs = null;
		if (a != null) {
			if (space != null && space.equals("forum"))
				rs = a.getForum();
			else
				rs = a.getResources();
		}
		List<Contribution> contributions = ContributionsDelegate
				.findContributionsInResourceSpace(rs, type, null);
		
		return contributions != null ? ok(Json.toJson(contributions))
				: notFound(Json.toJson(new TransferResponseStatus(
						"No resource space for assembly: " + aid)));
	}

	/**
	 * GET       /api/assembly/:aid/campaign/:cid/component/:ciid/contribution
	 * @param aid
	 * @param cid
	 * @param ciid
	 * @param type
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Campaign id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "ciid", value = "Component id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "type", value = "Type of contributions", dataType = "String", paramType = "query", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = ""),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findCampaignComponentContributions(Long aid, Long cid, Long ciid, String type) {
		Component c = Component.read(cid, ciid);
		ResourceSpace rs = null;
		if (c != null) {
		
			rs = c.getResourceSpace();
		}
		List<Contribution> contributions = ContributionsDelegate
				.findContributionsInResourceSpace(rs, type, null);
		return contributions != null ? ok(Json.toJson(contributions))
				: notFound(Json.toJson(new TransferResponseStatus(
						"No contributions for {assembly, campaign, component}: " + aid+", "+cid+", "+ciid)));
	}

	/**
	 * GET 	     /api/assembly/:aid/campaign/:cid/contribution
	 * @param aid
	 * @param cid
	 * @param type
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in a Campaign")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Campaign id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "type", value = "Type of contributions", dataType = "String", paramType = "query", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = ""),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findCampaignContributions(Long aid, Long cid, String type) {
		Campaign c = Campaign.read(cid);
		ResourceSpace rs = null;
		if (c != null) {
			rs = c.getResources();
		}
		List<Contribution> contributions = ContributionsDelegate
				.findContributionsInResourceSpace(rs, type, null);
		return contributions != null ? ok(Json.toJson(contributions))
				: notFound(Json.toJson(new TransferResponseStatus(
						"No contributions for {assembly, campaign}: " + aid+", "+cid)));
	}
	
	/**
	 * GET       /api/assembly/:aid/group/:gid/contribution
	 * @param aid
	 * @param gid
	 * @param space
	 * @param type
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "gid", value = "Group id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "space", value = "Resource space name within assembly", dataType = "String", paramType = "query", allowableValues = "resources,forum", defaultValue = ""),
			@ApiImplicitParam(name = "type", value = "Type of contributions", dataType = "String", paramType = "query", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = ""),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findAssemblyGroupContributions(Long aid, Long gid,
			String space, String type) {
		WorkingGroup wg = WorkingGroup.read(gid);
		ResourceSpace rs = null;
		if (wg != null) {
			if (space != null && space.equals("forum"))
				rs = wg.getForum();
			else
				rs = wg.getResources();
		}
		List<Contribution> contributions = ContributionsDelegate
				.findContributionsInResourceSpace(rs, type, null);
		return contributions != null ? ok(Json.toJson(contributions))
				: notFound(Json.toJson(new TransferResponseStatus(
						"No resource space for assembly: " + aid)));
	}

	/**
	 * GET       /api/assembly/:aid/contribution/:cid
	 * @param aid
	 * @param contributionId
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findContribution(Long aid, Long contributionId) {
		Contribution contribution = Contribution.read(contributionId);
		return ok(Json.toJson(contribution));
	}

	/**
	 * GET       /api/space/:sid/contribution
	 * @param sid
	 * @param type
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in a Resource Space")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "sid", value = "Resource Space id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "type", value = "Type of contributions", dataType = "String", paramType = "query", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = ""),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@SubjectPresent
	public static Result findResourceSpaceContributions(Long sid, String type) {
		ResourceSpace rs = ResourceSpace.read(sid);
		List<Contribution> contributions = ContributionsDelegate
				.findContributionsInResourceSpace(rs, type, null);
		return contributions != null ? ok(Json.toJson(contributions))
				: notFound(Json.toJson(new TransferResponseStatus(
						"No contributions for {resource space}: " + sid+", type="+type)));
	}
	
	/**
	 * GET       /api/assembly/:aid/campaign/:cid/contribution/:coid/stats
	 * @param aid
	 * @param cid
	 * @param stid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = ContributionStatistics.class, responseContainer="List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Campaign id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "coid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result readContributionStats(Long aid, Long cid, Long coid) {
		try {
			ContributionStatistics stats = new ContributionStatistics(coid);
			return ok(Json.toJson(stats));
		} catch (Exception e) {
			return internalServerError(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.SERVERERROR,
							"Error reading contribution stats: " + e.getMessage())));
		}
	}
	
	/**
	 * GET       /api/assembly/:aid/contribution/:cid/padid
	 * @param aid
	 * @param contributionId
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = String.class, produces = "application/json", value = "Get the padId of a Contribution")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findContributionPadId(Long aid, Long contributionId) {
		Contribution c = Contribution.read(contributionId);
		if (c!=null) {
			Resource pad = c.getExtendedTextPad();
			String padId = pad.getPadId();
			PadTransfer p = new PadTransfer();
			p.setPadId(padId);
			if(padId!=null) {
				return ok(Json.toJson(p));	
			} else {
				return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No Pad id for this Contribution")));
			}
		} 
		return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Contribution with ID "+contributionId+ " not found")));
	}

	/**
	 * GET       /api/assembly/:aid/contribution/:cid/comments
	 * @param aid
	 * @param contributionId
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = String.class, produces = "application/json", value = "Get the padId of a Contribution")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findContributionComments(Long aid, Long contributionId) {
		Contribution c = Contribution.read(contributionId);
		if (c!=null) {
			List<Contribution> comments = Contribution.readCommentsOfSpace(c.getResourceSpaceId());
			if(comments!=null) {
				return ok(Json.toJson(comments));	
			} else {
				return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No comments on this contribution")));
			}
		} 
		return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Contribution with ID "+contributionId+ " not found")));
	}
	
	/**
	 * GET       /api/contribution/:uuid
	 * @param uuid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Contribution.class, produces = "application/json", value = "Get contribution by its Universal ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uuid", value = "Contribution's Universal Id (UUID)", dataType = "java.util.UUID", paramType = "path") })
	// TODO: add API token support, some API enpoints must be available only for registered clients
	public static Result findContributionByUUID(UUID uuid) {
		Contribution contribution = Contribution.readByUUID(uuid);
		return ok(Json.toJson(contribution));
	}
	
	/* CREATE ENDPOINTS 
	 * TODO: reduce complexity by removing uncessary create methods
	 */
	/**
	 * POST       /api/space/:sid/contribution
	 * @param sid
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "sid", value = "Resource Space id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@SubjectPresent
	public static Result createContributionInResourceSpaceWithId(Long sid) {
		// 1. obtaining the user of the requestor
		User author = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (newContributionForm.hasErrors()) {
			return contributionCreateError(newContributionForm);
		} else {

			Contribution newContribution = newContributionForm.get();
			ContributionTypes type = newContribution.getType();
			if (type == null) {
				type = ContributionTypes.COMMENT;
			}

			ResourceSpace rs = ResourceSpace.read(sid);
			ContributionTemplate template = null;

			if(rs!=null &&newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
				List<ContributionTemplate> templates = rs.getTemplates();
				if (templates!=null && !templates.isEmpty())
					template = rs.getTemplates().get(0);				
			}
			newContribution.setContextUserId(author.getUserId());
			Contribution c;
			try {
				c = createContribution(newContribution, author, type, template, rs);
			} catch (Exception e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error when creating Contribution: " + e.toString())));
			}
			if (c != null) {
				rs.addContribution(c);
				rs.update();
			}
			return ok(Json.toJson(c));
		}
	}

	/**
	 * POST      /api/assembly/:aid/contribution
	 * @param aid
	 * @param space
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "space", value = "Resource space name within assembly", dataType = "String", paramType = "query", allowableValues = "resources,forum", defaultValue = ""),
			@ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createAssemblyContribution(Long aid, String space) {
		// 1. obtaining the user of the requestor
		User author = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (newContributionForm.hasErrors()) {
			return contributionCreateError(newContributionForm);
		} else {
			Contribution newContribution = newContributionForm.get();
			ContributionTypes type = newContribution.getType();
			if (type == null) {
				type = ContributionTypes.COMMENT;
			}

			Assembly a = Assembly.read(aid);
			ResourceSpace rs = space != null && space.equals("forum") ? a
					.getForum() : a.getResources();
			
			ContributionTemplate template = null;
			if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
				List<ContributionTemplate> templates = rs.getTemplates();
				if (templates != null && !templates.isEmpty())
					template = rs.getTemplates().get(0);
			}
					
			newContribution.setContextUserId(author.getUserId());		
			Contribution c;
			try {
				c = createContribution(newContribution, author, type, template, rs);
			} catch (Exception e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error when creating Contribution: " + e.toString())));
			}
			if (c != null) {
				rs.addContribution(c);
				rs.update();
			}
			return ok(Json.toJson(c));
		}
	}

	/**
	 * POST      /api/assembly/:aid/campaign/:cid/component/:ciid/contribution
	 * @param aid
	 * @param cid
	 * @param ciid
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Campaign id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "ciid", value = "Component id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createCampaignComponentContribution(Long aid, Long cid, Long ciid) {
		// 1. obtaining the user of the requestor
		User author = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (newContributionForm.hasErrors()) {
			return contributionCreateError(newContributionForm);
		} else {
			Contribution newContribution = newContributionForm.get();
			ContributionTypes type = newContribution.getType();
			if (type == null) {
				type = ContributionTypes.COMMENT;
			}

			Component ci = Component.read(cid,ciid);
			ResourceSpace rs = ci.getResourceSpace();
			ContributionTemplate template = null;
			if(newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
				List<ContributionTemplate> templates = rs.getTemplates();
				if (templates!=null && !templates.isEmpty())
					template = rs.getTemplates().get(0);
			}
			newContribution.setContextUserId(author.getUserId());
			Contribution c;
			try {
				c = createContribution(newContribution, author, type, template, rs);
			} catch (Exception e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error when creating Contribution: " + e.toString())));
			}
			if (c != null) {
				rs.addContribution(c);
				rs.update();
			}
			return ok(Json.toJson(c));
		}
	}
	
	/**
	 * POST      /api/assembly/:aid/group/:gid/contribution
	 * @param aid
	 * @param gid
	 * @param space
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "gid", value = "Working group id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "space", value = "Resource space name within assembly", dataType = "String", paramType = "query", allowableValues = "resources,forum", defaultValue = ""),
			@ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createAssemblyGroupContribution(Long aid, Long gid, String space) {
		// 1. obtaining the user of the requestor
		User author = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (newContributionForm.hasErrors()) {
			return contributionCreateError(newContributionForm);
		} else {
			Contribution newContribution = newContributionForm.get();
			ContributionTypes type = newContribution.getType();
			if (type == null) {
				type = ContributionTypes.COMMENT;
			}

			WorkingGroup wg = WorkingGroup.read(gid);
			ResourceSpace rs = space != null && space.equals("forum") ? wg
					.getForum() : wg.getResources();
			ContributionTemplate template = null;
			if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
				List<ContributionTemplate> templates = rs.getTemplates();
				if (templates != null && !templates.isEmpty())
					template = rs.getTemplates().get(0);
			}
			Contribution c;
			try {
				c = createContribution(newContribution, author, type, template, rs);
			} catch (Exception e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error when creating Contribution: " + e.toString())));
			}
			if (c != null) {
				rs.addContribution(c);
				rs.update();
			}
			return ok(Json.toJson(c));
		}
	}
	
	/**
	 * POST      /api/assembly/:aid/contribution/:cid/comment
	 * @param aid
	 * @param cid
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createContributionComment(Long aid, Long cid) {
		// 1. obtaining the user of the requestor
		User author = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (newContributionForm.hasErrors()) {
			return contributionCreateError(newContributionForm);
		} else {
			Contribution newContribution = newContributionForm.get();
			ContributionTypes type = newContribution.getType();
			if (type == null) {
				type = ContributionTypes.COMMENT;
			}

			Contribution c = Contribution.read(cid);
			ResourceSpace rs = c.getResourceSpace();
			ContributionTemplate template = null;
			if(newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
				List<ContributionTemplate> templates = rs.getTemplates();
				if (templates!=null && !templates.isEmpty())
					template = rs.getTemplates().get(0);
			}
			Contribution cNew;
			try {
				cNew = createContribution(newContribution, author, type, template, rs);
			} catch (Exception e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error when creating Contribution: " + e.toString())));
			}
			if (cNew != null) {
				rs.addContribution(cNew);
				rs.update();
			}
			return ok(Json.toJson(cNew));
		}
	}
	
	/**
	 * POST      /api/assembly/:aid/forumpost
	 * @param aid
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createAssemblyForumPost(Long aid) {
		// 1. obtaining the user of the requestor
		User author = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (newContributionForm.hasErrors()) {
			return contributionCreateError(newContributionForm);
		} else {
			Contribution newContribution = newContributionForm.get();
			ContributionTypes type = newContribution.getType();
			if (type == null) {
				type = ContributionTypes.COMMENT;
			}

			Assembly a = Assembly.read(aid);
			ResourceSpace rs = a.getForum();
			ContributionTemplate template = null;
			if(newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
				List<ContributionTemplate> templates = rs.getTemplates();
				if (templates!=null && !templates.isEmpty())
					template = rs.getTemplates().get(0);
			}
			Contribution cNew;
			try {
				cNew = createContribution(newContribution, author, type, template, rs);
			} catch (Exception e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error when creating Contribution: " + e.toString())));
			}
			if (cNew != null) {
				rs.addContribution(cNew);
				rs.update();
			}
			return ok(Json.toJson(cNew));
		}
	}
	
	/**
	 * POST      /api/assembly/:aid/group/:gid/forumpost
	 * @param aid
	 * @param gid
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "gid", value = "working group id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createWorkingGroupForumPost(Long aid, Long gid) {
		// 1. obtaining the user of the requestor
		User author = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (newContributionForm.hasErrors()) {
			return contributionCreateError(newContributionForm);
		} else {
			Contribution newContribution = newContributionForm.get();
			ContributionTypes type = newContribution.getType();
			if (type == null) {
				type = ContributionTypes.COMMENT;
			}

			WorkingGroup wg = WorkingGroup.read(gid);
			ResourceSpace rs = wg.getForum();
			ContributionTemplate template = null;
			if(newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
				List<ContributionTemplate> templates = rs.getTemplates();
				if (templates!=null && !templates.isEmpty())
					template = rs.getTemplates().get(0);
			}
			Contribution c;
			try {
				c = createContribution(newContribution, author, type, template, rs);
			} catch (Exception e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error when creating Contribution: " + e.toString())));
			}
			if (c != null) {
				rs.addContribution(c);
				rs.update();
			}
			return ok(Json.toJson(c));
		}
	}

	/**
	 * POST      /api/assembly/:aid/contribution/:cid/attachment
	 * @param aid
	 * @param contributionId
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "attachment_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result addAttachmentContribution(Long aid, Long contributionId) {
		// 1. read the new contribution data from the body
				// another way of getting the body content => request().body().asJson()
				final Form<Resource> newAttachmentForm = ATTACHMENT_FORM
						.bindFromRequest();
				User author = User.findByAuthUserIdentity(PlayAuthenticate
						.getUser(session()));

				if (newAttachmentForm.hasErrors()) {
					TransferResponseStatus responseBody = new TransferResponseStatus();
					responseBody.setStatusMessage(Messages.get(
							GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,
							newAttachmentForm.errorsAsJson()));
					return badRequest(Json.toJson(responseBody));
				} else {
					Contribution c = Contribution.read(contributionId);
					ResourceSpace contributionRs = c.getResourceSpace();
					Resource newAttachment = newAttachmentForm.get();
					newAttachment.setCreator(author);
					contributionRs.addResource(newAttachment);
					contributionRs.update();
					return ok(Json.toJson(newAttachment));
				}
	}

	/* Update Endpoints */ 
	/**
	 * PUT       /api/assembly/:aid/contribution/:cid/feedback
	 * @param aid
	 * @param cid
	 * @param stid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result readContributionFeedback(Long aid, Long cid) {
		User user = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
		ContributionFeedback feedback = ContributionFeedback.findByContributionAndUserId(cid, user.getUserId());
		if(feedback!=null) {
			return ok(Json.toJson(feedback));
		} else {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage("User Feedback Does Not Exist Yet");
			return notFound(Json.toJson(responseBody));
		}
	}
	
	/**
	 * PUT       /api/assembly/:aid/contribution/:cid/feedback
	 * @param aid
	 * @param cid
	 * @param stid
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = ContributionStatistics.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "contribution_feedback_form", value = "Body of Contribution Statistics in JSON", required = true, dataType = "models.ContributionStatistics", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateContributionFeedback(Long aid, Long cid) {
		User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
		final Form<ContributionFeedback> updatedFeedbackForm = CONTRIBUTION_FEEDBACK_FORM.bindFromRequest();

		if (updatedFeedbackForm.hasErrors()) {
			return contributionFeedbackError(updatedFeedbackForm);
		} else {
			ContributionFeedback existingFeedback = ContributionFeedback.findByContributionAndUserId(cid, author.getUserId());
			ContributionFeedback feedback = updatedFeedbackForm.get();
			
			try {
				Ebean.beginTransaction();
				feedback.setContributionId(cid);
				feedback.setUserId(author.getUserId());

				if (existingFeedback != null) {
					feedback.setId(existingFeedback.getId());
					feedback.update();
				} else {
					ContributionFeedback.create(feedback);
				}
				Ebean.commitTransaction();
			} catch (Exception e) {
				Ebean.rollbackTransaction();
				return contributionFeedbackError(feedback, e.getLocalizedMessage());
			}
			
			ContributionStatistics updatedStats = new ContributionStatistics(cid);
			return ok(Json.toJson(updatedStats));
		}
	}
		
	/**
	 * PUT       /api/assembly/:aid/contribution/:cid
	 * @param aid
	 * @param contributionId
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateContribution(Long aid, Long contributionId) {
		// 1. read the new contribution data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Contribution> newContributionForm = CONTRIBUTION_FORM.bindFromRequest();
		User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));

		if (newContributionForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,
					newContributionForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Contribution newContribution = newContributionForm.get();
			newContribution.setContributionId(contributionId);
			newContribution.setContextUserId(author.getUserId());
			Contribution.update(newContribution);
			return ok(Json.toJson(newContribution));
		}
	}
	
	/**
	 * PUT       /api/assembly/:aid/contribution/:cid/softremoval
	 * TODO: create a dynamic handler to check if the contribution belongs to the user
	 * @param aid
	 * @param contributionId
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Logical removal of contribution in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	//@Dynamic(value = "ModeratorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result softDeleteContribution(Long aid, Long contributionId) {
		Contribution.softDelete(contributionId);
		return ok();
	}

	/**
	 * PUT       /api/assembly/:aid/contribution/:cid/recover
	 * TODO: create a dynamic handler to check if the contribution belongs to the user
	 * @param aid
	 * @param contributionId
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Logical recovery of contribution Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "ModeratorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result recoverContribution(Long aid, Long contributionId) {
		Contribution.softRecovery(contributionId);
		return ok();
	}
		
	/**
	 * DELETE    /api/assembly/:aid/contribution/:cid
	 * @param aid
	 * @param contributionId
	 * @return
	 */
	@ApiOperation(httpMethod = "DELETE", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Restrict({ @Group(GlobalData.ADMIN_ROLE) })
	public static Result forceDeleteContribution(Long aid, Long contributionId) {
		Contribution.delete(contributionId);
		return ok();
	}
	
	/*
	 * Non-exposed methods: creation methods
	 */
	/**
	 * This method is reused by all other contribution creation methods to centralize its logic
	 * 
	 * @param newContrib
	 * @param author
	 * @param type
	 * @param etherpadServerUrl
	 * @param etherpadApiKey
	 * @param t
	 * @param containerResourceSpace
	 * @return
	 * @throws MalformedURLException
	 * @throws MembershipCreationException 
	 */
	public static Contribution createContribution(Contribution newContrib,
			User author, ContributionTypes type, String etherpadServerUrl, String etherpadApiKey, 
			ContributionTemplate t, ResourceSpace containerResourceSpace) throws MalformedURLException, MembershipCreationException {
		newContrib.setType(type);		
		newContrib.addAuthor(author);
		if (newContrib.getLang() == null)
			newContrib.setLang(author.getLanguage());
		newContrib.setContextUserId(author.getUserId());
		
		if (etherpadServerUrl == null || etherpadServerUrl.isEmpty()) {
			// read etherpad server url from config file
			Logger.info("Etherpad URL was not configured");
			etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
		}

		if (etherpadApiKey == null || etherpadApiKey.isEmpty()) {
			// read etherpad server url from config file
			Logger.info("Etherpad API Key was not configured");
			etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);
		}
		
		Logger.info("Using Etherpad server at: "+etherpadServerUrl);
		Logger.debug("Using Etherpad API Key: "+etherpadApiKey);
		
		if(type!=null && (type.equals(ContributionTypes.PROPOSAL) || type.equals(ContributionTypes.NOTE))) {
			ContributionsDelegate.createAssociatedPad(etherpadServerUrl, etherpadApiKey, newContrib, containerResourceSpace.getResourceSpaceUuid());				
		}
		
		Logger.info("Creating new contribution");
		Logger.debug("=> " + newContrib.toString());
		
		// Get list of BRAINSTORMING contributions that inspire the new one
		List<Contribution> inspirations = newContrib.getTransientInspirations(); 
		
		// If contribution is a proposal and there is no working group associated as author, 
		// create one automatically with the creator as coordinator
		List<WorkingGroup> workingGroupAuthors = newContrib
				.getWorkingGroupAuthors();
		String newWorkingGroupName = "WG for '"+newContrib.getTitle()+"'";
		if (workingGroupAuthors!=null && !workingGroupAuthors.isEmpty()) {
			WorkingGroup wg = workingGroupAuthors.get(0);
			if (wg.getGroupId() == null) {
				newWorkingGroupName = wg.getName();
				workingGroupAuthors = null;
				newContrib.setWorkingGroupAuthors(null);
			}
		}
		
		WorkingGroup newWorkingGroup = new WorkingGroup();
		Boolean workingGroupIsNew = false;
		if (newContrib.getType().equals(ContributionTypes.PROPOSAL)
				&& (workingGroupAuthors == null || workingGroupAuthors
						.isEmpty())) {
			workingGroupIsNew = true;
			newWorkingGroup.setCreator(author);
			newWorkingGroup.setName(newWorkingGroupName);
			newWorkingGroup.setLang(author.getLanguage());
			newWorkingGroup.setExistingThemes(newContrib.getExistingThemes());
			newWorkingGroup.setListed(false);
			newWorkingGroup
					.setInvitationEmail("Hello! You have been invited to be a member of the "
							+ "Working Group \""+newWorkingGroup.getName()+"\" in AppCivist. "
									+ "If you are interested, follow the link attached to this "
									+ "invitation to accept it. If you are not interested, you "
									+ "can just ignore this message");
			newWorkingGroup.setMajorityThreshold("simple");
			newWorkingGroup.setBlockMajority(false);
			
			WorkingGroupProfile newWGProfile = new WorkingGroupProfile();
			newWGProfile.setIcon("https://s3-us-west-1.amazonaws.com/appcivist-files/icons/justicia-140.png");
			newWGProfile.setManagementType(ManagementTypes.COORDINATED_AND_MODERATED);
			newWGProfile.setSupportedMembership(SupportedMembershipRegistration.INVITATION_AND_REQUEST);

			newWorkingGroup.setProfile(newWGProfile);
			newWorkingGroup = WorkingGroup.create(newWorkingGroup);

			containerResourceSpace.addWorkingGroup(newWorkingGroup);
			
			// Find resource space of the assembly and add it also in there
			if (containerResourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
				Campaign c = containerResourceSpace.getCampaign();
				Assembly a = Assembly.read(c.getAssemblies().get(0));
				ResourceSpace aRs = a.getResources();
				aRs.addWorkingGroup(newWorkingGroup);
				aRs.update();
			}
			
			newContrib.getWorkingGroupAuthors().add(newWorkingGroup);
		}
		
		Contribution.create(newContrib);
		newContrib.refresh();
		
		if (newContrib.getType().equals(ContributionTypes.PROPOSAL) && inspirations != null) {
			ResourceSpace cSpace = ResourceSpace.read(newContrib.getResourceSpaceId());
			for (Contribution inspiration : inspirations) {
				Contribution c = Contribution.read(inspiration.getContributionId());
				cSpace.addContribution(c);
			}
			cSpace.update();
			cSpace.refresh();			

			// If the proposal is coming from brainstorming, invite the
			// commenters of the
			// each brainstorming contributions
			if (!inspirations.isEmpty() && workingGroupIsNew) {
				inviteCommentersInInspirationList(inspirations, newContrib,
						newWorkingGroup);
			}
		}

		// If contribution is a proposal and the resource space where it is added is a Campaign
		// create automatically a related candidate for the contribution in the bindingBallot 
		// and consultiveBallot associated to the campaign.
		if (containerResourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
			UUID binding = Campaign.queryBindingBallotByCampaignResourceSpaceId(containerResourceSpace
							.getResourceSpaceId());
			UUID consultive = Campaign.queryConsultiveBallotByCampaignResourceSpaceId(containerResourceSpace
							.getResourceSpaceId());

			// Add the candidates automatically only to the binding ballot marked in the campaign as such 
			// and to the "consultive" ballot marked in the campaign as such
			for (Ballot ballot : containerResourceSpace.getBallots()) {
				if ((ballot.getDecisionType().equals("BINDING") && (ballot.getUuid().equals(binding)))
						|| (ballot.getDecisionType().equals("CONSULTIVE") && ballot.getUuid().equals(consultive))) {
					BallotCandidate contributionAssociatedCandidate = new BallotCandidate();
					contributionAssociatedCandidate.setBallotId(ballot.getId());
					contributionAssociatedCandidate.setCandidateType(new Integer(1));
					contributionAssociatedCandidate.setContributionUuid(newContrib.getUuid());
					contributionAssociatedCandidate.save();
				}
			}
		}
		
		// If the contribution is a proposal, create an associated candidate in the ballot 
		// of the working group authors		
		for (WorkingGroup wg : newContrib.getWorkingGroupAuthors()) {
			UUID consensus = WorkingGroup.queryConsensusBallotByGroupResourceSpaceId(wg.getResourcesResourceSpaceId());
			Ballot b = Ballot.findByUUID(consensus);
			BallotCandidate contributionAssociatedCandidate = new BallotCandidate();
			contributionAssociatedCandidate.setBallotId(b.getId());
			contributionAssociatedCandidate.setCandidateType(new Integer(1));
			contributionAssociatedCandidate.setContributionUuid(newContrib.getUuid());
			contributionAssociatedCandidate.save();
		}
		
		return newContrib;
	}

	/**
	 * It looks into the list of comments of the brainstorming contributions that serve as inspiration
	 * for a new proposal
	 * @param inspirations
	 * @param newContrib
	 * @param newWorkingGroup
	 * @throws MembershipCreationException 
	 */
	private static void inviteCommentersInInspirationList(
			List<Contribution> inspirations, Contribution newContrib,
			WorkingGroup newWorkingGroup) throws MembershipCreationException {
		HashMap<String, Boolean> invitedEmails = new HashMap<String, Boolean>();
		for (Contribution inspirationObject : inspirations) {
			Contribution inspiration = Contribution.read(inspirationObject.getContributionId());
			List<Contribution> comments = inspiration.getComments();
			for (Contribution comment : comments) {
				User commentAuthor = comment.getAuthors().get(0);
				String authorEmail = commentAuthor.getEmail();
				String creatorEmail = newContrib.getAuthors().get(0).getEmail();
				Boolean invited = invitedEmails.get(authorEmail);
				if((invited==null || !invited) && !authorEmail.equals(creatorEmail)) {
					InvitationTransfer invitation = new InvitationTransfer();
					invitation.setEmail(authorEmail);
					invitation.setInvitationEmail(newWorkingGroup.getInvitationEmail());
					invitation.setTargetId(newWorkingGroup.getGroupId());
					invitation.setTargetType("GROUP");
					invitation.setCoordinator(false);
					invitation.setModerator(false);
					MembershipInvitation.create(invitation, newWorkingGroup.getCreator(), newWorkingGroup);
					invitedEmails.put(authorEmail,true);
				}
			}
		}
		
	}

	public static Result createContributionResult(Contribution newContrib,
			User author, ContributionTypes type, ContributionTemplate t, ResourceSpace containerResourceSpace) {
		try {
			return ok(Json.toJson(createContribution(newContrib, author, type, t, containerResourceSpace)));
		} catch (Exception e) {
			return internalServerError(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.SERVERERROR,
							"Error when creating Contribution: " + e.toString())));
		}
	}
		
	public static Contribution createContribution(Contribution newContrib, 
			User author, ContributionTypes type, ContributionTemplate t, 
			ResourceSpace containerResourceSpace) throws MalformedURLException, MembershipCreationException {
		// TODO: dynamically obtain etherpad server URL and Key from component configuration
		return createContribution(newContrib, author, type, null, null, t, containerResourceSpace);
	}
	
	public static Result createContributionInAssembly(
			Contribution newContrib, User author, Assembly a,
			ContributionTypes type) {
		ResourceSpace rs = a.getResources();
		
		ContributionTemplate template = null;
		if(newContrib.getType()!=null && newContrib.getType().equals(ContributionTypes.PROPOSAL)) {
			List<ContributionTemplate> templates = rs.getTemplates();
			if (templates!=null && !templates.isEmpty())
				template = rs.getTemplates().get(0);
		}
		
		Contribution c;
		try {
			c = createContribution(newContrib, author, type, template, rs);
		} catch (Exception e) {
			return internalServerError(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.SERVERERROR,
							"Error when creating contribution: " + e.toString())));
		}
		rs.addContribution(c);
		rs.update();
		return ok(Json.toJson(c));
	}
	
	public static Result updateContributionResult(Long sid, Long id,
			Contribution updatedContribution, ContributionTypes type) {
		updatedContribution.setContributionId(id);
		if (updatedContribution.getType().equals(type)) {
			Contribution.update(updatedContribution);
			Logger.info("Updating contribution");
			Logger.debug("=> " + updatedContribution.toString());
			return ok(Json.toJson(updatedContribution));
		} else {
			return contributionUpdateError(updatedContribution, "Update on "
					+ type + " for Contribution that is not of this type");
		}
	}

	/*
	 * Non-exposed methods: error Message Results
	 */
	private static Result contributionCreateError(
			Form<Contribution> newContributionForm) {
		return contributionOperationError(
				GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, newContributionForm
						.errorsAsJson().toString());
	}
	
	private static Result contributionFeedbackError(Form<ContributionFeedback> newStatsForm) {
		// TODO Auto-generated method stub
		return contributionOperationError(
				GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, newStatsForm
						.errorsAsJson().toString());
	}
	
	private static Result contributionFeedbackError(ContributionFeedback feedback, String errorMsg) {
		// TODO Auto-generated method stub
		return contributionOperationError(
				GlobalData.CONTRIBUTION_UPDATE_MSG_ERROR, "{ error : " + errorMsg + ", object : "
						+ Json.toJson(feedback));
	}

	public static Result contributionUpdateError(
			Form<Contribution> updatedContributionForm) {
		return contributionOperationError(
				GlobalData.CONTRIBUTION_UPDATE_MSG_ERROR,
				updatedContributionForm.errorsAsJson().toString());
	}

	public static Result contributionUpdateError(
			Contribution updatedContribution, String errorMsg) {
		return contributionOperationError(
				GlobalData.CONTRIBUTION_UPDATE_MSG_ERROR,
				"{ error : " + errorMsg + ", object : "
						+ Json.toJson(updatedContribution));
	}

	public static Result contributionCreateError(Contribution newContribution,
			String errorMsg) {
		return contributionOperationError(GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,"{ error : " + errorMsg + ", object : "
						+ Json.toJson(newContribution));
	}

	public static Result contributionOperationError(String msgi18nCode,
			String msg) {
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage(Messages.get(msgi18nCode) + ": " + msg);
		return badRequest(Json.toJson(responseBody));
	}
}
