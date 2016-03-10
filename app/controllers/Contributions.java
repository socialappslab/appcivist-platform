package controllers;

import static play.data.Form.form;
import http.Headers;

import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;

import models.Assembly;
import models.Campaign;
import models.Component;
import models.Contribution;
import models.ContributionStatistics;
import models.ContributionTemplate;
import models.Resource;
import models.ResourceSpace;
import models.User;
import models.WorkingGroup;
import models.transfer.PadTransfer;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.SecurityModelConstants;
import utils.GlobalData;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import delegates.ContributionsDelegate;
import enums.ContributionTypes;
import enums.ResponseStatus;

@Api(value = "/contribution", description = "Contribution Making Service: contributions by citizens to different spaces of civic engagement")
@With(Headers.class)
public class Contributions extends Controller {

	public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);
	public static final Form<ContributionStatistics> CONTRIBUTION_STATS_FORM = form(ContributionStatistics.class);
	public static final Form<Resource> ATTACHMENT_FORM = form(Resource.class);

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

	@ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in a Resource Space")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "sid", value = "Resource Space id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "type", value = "Type of contributions", dataType = "String", paramType = "query", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = ""),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findResourceSpaceContributions(Long sid, String type) {
		ResourceSpace rs = ResourceSpace.read(sid);
		List<Contribution> contributions = ContributionsDelegate
				.findContributionsInResourceSpace(rs, type, null);
		return contributions != null ? ok(Json.toJson(contributions))
				: notFound(Json.toJson(new TransferResponseStatus(
						"No contributions for {resource space}: " + sid+", type="+type)));
	}
	
	/* CREATE ENDPOINTS */
	
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
				c = createContribution(newContribution, author, type, template, rs.getUuid());
			} catch (MalformedURLException e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error in etherpad server URL: " + e.toString())));
			}
			if (c != null) {
				rs.addContribution(c);
				rs.update();
			}
			return ok(Json.toJson(c));
		}
	}

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
				c = createContribution(newContribution, author, type, template, rs.getUuid());
			} catch (MalformedURLException e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error in etherpad server URL: " + e.toString())));
			}
			if (c != null) {
				rs.addContribution(c);
				rs.update();
			}
			return ok(Json.toJson(c));
		}
	}

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
				c = createContribution(newContribution, author, type, template, rs.getUuid());
			} catch (MalformedURLException e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error in etherpad server URL: " + e.toString())));
			}
			if (c != null) {
				rs.addContribution(c);
				rs.update();
			}
			return ok(Json.toJson(c));
		}
	}
	
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
				c = createContribution(newContribution, author, type, template, rs.getUuid());
			} catch (MalformedURLException e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error in etherpad server URL: " + e.toString())));
			}
			if (c != null) {
				rs.addContribution(c);
				rs.update();
			}
			return ok(Json.toJson(c));
		}
	}
	
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
				cNew = createContribution(newContribution, author, type, template, rs.getUuid());
			} catch (MalformedURLException e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error in etherpad server URL: " + e.toString())));
			}
			if (cNew != null) {
				rs.addContribution(cNew);
				rs.update();
			}
			return ok(Json.toJson(cNew));
		}
	}
	
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
				cNew = createContribution(newContribution, author, type, template, rs.getUuid());
			} catch (MalformedURLException e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error in etherpad server URL: " + e.toString())));
			}
			if (cNew != null) {
				rs.addContribution(cNew);
				rs.update();
			}
			return ok(Json.toJson(cNew));
		}
	}
	
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
				c = createContribution(newContribution, author, type, template, rs.getUuid());
			} catch (MalformedURLException e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error in etherpad server URL: " + e.toString())));
			}
			if (c != null) {
				rs.addContribution(c);
				rs.update();
			}
			return ok(Json.toJson(c));
		}
	}

	@ApiOperation(httpMethod = "PUT", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "stid", value = "Contribution Stats id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "contribution_stats_form", value = "Body of Contribution Statistics in JSON", required = true, dataType = "models.ContributionStatistics", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateContributionStats(Long aid, Long cid, Long stid) {
		// 1. obtaining the user of the requestor
//		User author = User.findByAuthUserIdentity(PlayAuthenticate
//				.getUser(session()));
// TODO: ASSOCIATE likes of users
		
		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<ContributionStatistics> newStatsForm = CONTRIBUTION_STATS_FORM
				.bindFromRequest();

		if (newStatsForm.hasErrors()) {
			return contributionStatsCreateError(newStatsForm);
		} else {
			ContributionStatistics newStats = newStatsForm.get();
			newStats.setContributionStatisticsId(stid);
			newStats.update();
			return ok(Json.toJson(newStats));
		}
	}
	
	@ApiOperation(httpMethod = "PUT", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "stid", value = "Contribution Stats id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "contribution_stats_form", value = "Body of Contribution Statistics in JSON", required = true, dataType = "models.ContributionStatistics", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateContributionStatsByStatsId(Long stid) {
		// 1. obtaining the user of the requestor
//		User author = User.findByAuthUserIdentity(PlayAuthenticate
//				.getUser(session()));
// TODO: ASSOCIATE likes of users
		
		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<ContributionStatistics> newStatsForm = CONTRIBUTION_STATS_FORM
				.bindFromRequest();

		if (newStatsForm.hasErrors()) {
			return contributionStatsCreateError(newStatsForm);
		} else {
			ContributionStatistics newStats = newStatsForm.get();
			newStats.setContributionStatisticsId(stid);
			newStats.update();
			return ok(Json.toJson(newStats));
		}
	}
	
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

	// TODO: create a dynamic handler to check if the contribution belongs to
	// the user
	@ApiOperation(httpMethod = "PUT", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "ModeratorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteContribution(Long aid, Long contributionId) {
		Contribution.softDelete(contributionId);
		return ok();
	}

	// TODO: create a dynamic handler to check if the contribution belongs to
	// the user
	@ApiOperation(httpMethod = "PUT", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
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
	
	@ApiOperation(httpMethod = "GET", response = String.class, produces = "application/json", value = "Get the padId of a Contribution")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "AuthorOfContribution", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
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
	
	public static Result createContributionResult(Contribution newContrib,
			User author, ContributionTypes type, ContributionTemplate t, UUID resourceSpaceUUID) {
		try {
			return ok(Json.toJson(createContribution(newContrib, author, type, t, resourceSpaceUUID)));
		} catch (MalformedURLException e) {
			return internalServerError(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.SERVERERROR,
							"Error in etherpad server URL: " + e.toString())));
		}
	}

	public static Contribution createContribution(Contribution newContrib,
			User author, ContributionTypes type, String etherpadServerUrl, String etherpadApiKey, 
			ContributionTemplate t, UUID resourceSpaceConfigsUUID) throws MalformedURLException {
		newContrib.setType(type);		
		newContrib.addAuthor(author);
		if (newContrib.getLang() == null)
			newContrib.setLang(author.getLanguage());
		newContrib.setContextUserId(author.getUserId());
		
		if (etherpadServerUrl == null || etherpadServerUrl.isEmpty()) {
			// read etherpad server url from config file
			Logger.info("Etherpad URL was not configured");
			etherpadServerUrl = Play.application().configuration().getString("appcivist.services.etherpad.default.serverBaseUrl");
		}

		if (etherpadApiKey == null || etherpadApiKey.isEmpty()) {
			// read etherpad server url from config file
			Logger.info("Etherpad API Key was not configured");
			etherpadApiKey = Play.application().configuration().getString("appcivist.services.etherpad.default.apiKey");
		}
		
		Logger.info("Using Etherpad server at: "+etherpadServerUrl);
		Logger.debug("Using Etherpad API Key: "+etherpadApiKey);
		
		if(type!=null && type.equals(ContributionTypes.PROPOSAL)) {
			ContributionsDelegate.createAssociatedPad(etherpadServerUrl, etherpadApiKey, newContrib, resourceSpaceConfigsUUID);				
		}
		
		Logger.info("Creating new contribution");
		Logger.debug("=> " + newContrib.toString());
		Contribution.create(newContrib);
		newContrib.refresh();
		return newContrib;
	}
		
	public static Contribution createContribution(Contribution newContrib, User author, ContributionTypes type, ContributionTemplate t, UUID resourceSpaceConfigsUUID) throws MalformedURLException {
		// TODO: dynamically obtain etherpad server URL and Key from component configuration
		return createContribution(newContrib, author, type, null, null, t, resourceSpaceConfigsUUID);
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
			c = createContribution(newContrib, author, type, template, rs.getUuid());
		} catch (MalformedURLException e) {
			return internalServerError(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.SERVERERROR,
							"Error in etherpad server URL: " + e.toString())));
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

	public static Result contributionCreateError(
			Form<Contribution> newContributionForm) {
		return contributionOperationError(
				GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, newContributionForm
						.errorsAsJson().toString());
	}
	
	private static Result contributionStatsCreateError(
			Form<ContributionStatistics> newStatsForm) {
		// TODO Auto-generated method stub
		return contributionOperationError(
				GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, newStatsForm
						.errorsAsJson().toString());
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
		return contributionOperationError(
				GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,
				"{ error : " + errorMsg + ", object : "
						+ Json.toJson(newContribution));
	}

	public static Result contributionOperationError(String msgi18nCode,
			String msg) {
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage(Messages.get(msgi18nCode) + ": " + msg);
		return badRequest(Json.toJson(responseBody));
	}
}
