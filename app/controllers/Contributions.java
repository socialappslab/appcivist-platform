package controllers;

import static play.data.Form.form;

import java.util.List;

import models.Assembly;
import models.Contribution;
import models.User;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import security.SecurityModelConstants;
import utils.GlobalData;
import be.objectify.deadbolt.java.actions.Dynamic;

import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;

import enums.ContributionTypes;

@Api(value = "/contribution", description = "Citizen Contritubion Services: asking questions, reporting issues, proposing ideas, turning ideas into proposals within assemblies")
public class Contributions extends Controller {

	public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);

	/**
	 * Return the full list of contributions
	 *
	 * @return Contribution list
	 */
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findContributions(Long aid) {
		List<Contribution> contributions = Contribution.findAllByAssembly(aid);
		return ok(Json.toJson(contributions));
	}

	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findContribution(Long aid, Long contributionId) {
		Contribution contribution = Contribution.read(contributionId);
		return ok(Json.toJson(contribution));
	}

	// TODO: create a dynamic handler to check if the contribution belongs to
	// the user
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteContribution(Long aid, Long contributionId) {
		Contribution.delete(contributionId);
		return ok();
	}

	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createContribution(Long aid) {
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
			return createContribution(newContribution, author,
					Assembly.read(aid), type);
		}
	}

	// TODO: create a dynamic handler to check if the contribution belongs to
	// the user
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

	public static Result createContribution(Contribution newContrib,
			User author, Assembly assembly, ContributionTypes type) {
		newContrib.setType(type);
		newContrib.setAuthor(author);
		newContrib.setAssembly(assembly);
		if (newContrib.getLang() == null)
			newContrib.setLang(author.getLanguage());
		Contribution.create(newContrib);
		newContrib.refresh();
		Logger.info("Creating new contribution");
		Logger.debug("=> " + newContrib.toString());
		return ok(Json.toJson(newContrib));
	}

	public static Result updateContribution(Long aid, Long id,
			Contribution updatedContribution, ContributionTypes type) {
		updatedContribution.setContributionId(id);
		updatedContribution.setAssembly(Assembly.read(aid));
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
