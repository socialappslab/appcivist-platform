package controllers;

import static play.data.Form.form;
import http.Headers;

import java.util.List;

import models.Assembly;
import models.ComponentInstance;
import models.Contribution;
import models.ResourceSpace;
import models.User;
import models.WorkingGroup;
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

import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import delegates.ContributionsDelegate;
import enums.ContributionTypes;

@Api(value = "/contribution", description = "Contribution Making Service: contributions by citizens to different spaces of civic engagement")
@With(Headers.class)
public class Contributions extends Controller {

	public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);

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
			@ApiImplicitParam(name = "space", value = "Resource space name within assembly", dataType = "String", paramType = "query", allowableValues = "resources,forum", defaultValue = ""),
			@ApiImplicitParam(name = "type", value = "Type of contributions", dataType = "String", paramType = "query", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = ""),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findCampaignComponentContributions(Long aid, Long cid,
			Long ciid, String space, String type) {
		ComponentInstance c = ComponentInstance.read(cid, ciid);
		ResourceSpace rs = null;
		if (c != null) {
			// TODO: add multiple spaces to components
			// if (space != null && space.equals("forum"))
			// rs = c.getForum();
			// else
			// rs = c.getResources();
			rs = c.getResources();
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
	
	@ApiOperation(httpMethod = "POST", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "sid", value = "Resource Space id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "contribution_form", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
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
			Contribution c = createContribution(newContribution, author, type);
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
			Contribution c = createContribution(newContribution, author, type);
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
			@ApiImplicitParam(name = "space", value = "Resource space name within assembly", dataType = "String", paramType = "query", allowableValues = "resources,forum", defaultValue = ""),
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

			ComponentInstance ci = ComponentInstance.read(cid,ciid);
			ResourceSpace rs = ci.getResources();
			Contribution c = createContribution(newContribution, author, type);
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
			Contribution c = createContribution(newContribution, author, type);
			if (c != null) {
				rs.addContribution(c);
				rs.update();
			}
			return ok(Json.toJson(c));
		}
	}

	// TODO: create a dynamic handler to check if the contribution belongs to
	// the user
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
		final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (newContributionForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,
					newContributionForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Contribution newContribution = newContributionForm.get();
			TransferResponseStatus responseBody = new TransferResponseStatus();
			newContribution.setContributionId(contributionId);
			Contribution.update(newContribution);
			Logger.info("Creating new contribution");
			Logger.debug("=> " + newContributionForm.toString());

			responseBody.setNewResourceId(newContribution.getContributionId());
			responseBody.setStatusMessage(Messages.get(
					GlobalData.CONTRIBUTION_CREATE_MSG_SUCCESS,
					newContribution.getTitle()/*
											 * , roleCreator.getIdentifier ()
											 */));
			responseBody.setNewResourceURL(GlobalData.CONTRIBUTION_BASE_PATH
					+ "/" + newContribution.getContributionId());

			return ok(Json.toJson(responseBody));
		}
	}

	// TODO: create a dynamic handler to check if the contribution belongs to
	// the user
	@ApiOperation(httpMethod = "DELETE", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Contribution id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteContribution(Long aid, Long contributionId) {
		Contribution.delete(contributionId);
		return ok();
	}

	public static Result createContributionResult(Contribution newContrib,
			User author, ContributionTypes type) {
		return ok(Json.toJson(createContribution(newContrib, author, type)));
	}

	public static Contribution createContribution(Contribution newContrib,
			User author, ContributionTypes type) {
		newContrib.setType(type);
		newContrib.addAuthor(author);
		if (newContrib.getLang() == null)
			newContrib.setLang(author.getLanguage());
		Contribution.create(newContrib);
		newContrib.refresh();
		Logger.info("Creating new contribution");
		Logger.debug("=> " + newContrib.toString());
		return newContrib;
	}

	public static Result createContributionInAssembly(
			Contribution newContrib, User author, Assembly a,
			ContributionTypes type) {
		Contribution c = createContribution(newContrib, author, type);
		ResourceSpace rs = a.getResources();
		rs.addContribution(c);
		rs.update();
		// TODO Auto-generated method stub
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
