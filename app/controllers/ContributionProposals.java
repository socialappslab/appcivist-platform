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

import enums.ContributionTypes;

//@Api(value = "/proposal", description = "Citizen Proposal Making services: turning ideas into proposals within assemblies")
public class ContributionProposals extends Contributions {

	// TODO: ensure that proposals have one ore more connected ideas upon which they built
	// TODO: add etherpads to the model of proposals
	public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);
	public static final ContributionTypes cType = ContributionTypes.PROPOSAL;

	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findProposals(Long aid) {
		List<Contribution> contributions = Contribution
				.readListByTargetSpaceAndType(Assembly.read(aid).getResources().getResourceSpaceId(),cType);
		return ok(Json.toJson(contributions));
	}

	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findProposal(Long aid, Long id) {
		Contribution contribution = Contribution
				.readByIdAndType(aid, id, cType);
		return ok(Json.toJson(contribution));
	}

	// TODO: create a dynamic handler to check if the contribution belongs to
	// the user
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateProposal(Long aid, Long id) {
		// 1. read the new contribution data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Contribution> updatedContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (updatedContributionForm.hasErrors()) {
			return contributionUpdateError(updatedContributionForm);
		} else {
			Contribution updatedContribution = updatedContributionForm.get();
			return updateContributionResult(aid, id, updatedContribution, cType);
		}
	}

	// TODO: create a dynamic handler to check if the contribution belongs to
	// the user
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteProposal(Long aid, Long contributionId) {
		Contribution.deleteContributionByIdAndType(contributionId, cType);
		return ok();
	}

	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createProposal(Long aid) {
		User author = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
				.bindFromRequest();

		if (newContributionForm.hasErrors()) {
			return contributionCreateError(newContributionForm);
		} else {
			Contribution newContribution = newContributionForm.get();
			return createContributionInAssembly(newContribution, author,
					Assembly.read(aid), cType);
		}
	}

	// implement here other question related endpoints}
}