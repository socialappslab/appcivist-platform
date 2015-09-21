package controllers;

import static play.data.Form.form;

import java.util.List;

import models.Assembly;
import models.Contribution;
import models.User;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import security.SecurityModelConstants;
import be.objectify.deadbolt.java.actions.Dynamic;

import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;

import enums.ContributionTypes;

@Api(value = "/issue", description = "Citizen Contritubion Services: asking questions, reporting issues, proposing ideas, turning ideas into proposals within assemblies")
public class ContributionIssues extends Contributions {

	public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);
	public static final ContributionTypes cType = ContributionTypes.ISSUE;

	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findIssues(Long aid) {
		List<Contribution> contributions = Contribution
				.readListByAssemblyAndType(aid, cType);
		return ok(Json.toJson(contributions));
	}

	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findIssue(Long aid, Long id) {
		Contribution contribution = Contribution
				.readByIdAndType(aid, id, cType);
		return ok(Json.toJson(contribution));
	}

	// TODO: create a dynamic handler to check if the contribution belongs to
	// the user
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateIssue(Long aid, Long id) {
		// 1. read the new contribution data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Contribution> updatedContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (updatedContributionForm.hasErrors()) {
			return contributionUpdateError(updatedContributionForm);
		} else {
			Contribution updatedContribution = updatedContributionForm.get();
			return updateContribution(aid, id, updatedContribution, cType);
		}
	}

	// TODO: create a dynamic handler to check if the contribution belongs to
	// the user
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteIssue(Long aid, Long contributionId) {
		Contribution.deleteContributionByIdAndType(contributionId, cType);
		return ok();
	}

	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createIssue(Long aid) {
		User author = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (newContributionForm.hasErrors()) {
			return contributionCreateError(newContributionForm);
		} else {
			Contribution newContribution = newContributionForm.get();
			return createContribution(newContribution, author,
					Assembly.read(aid), cType);
		}
	}

	// implement here other question related endpoints
}
