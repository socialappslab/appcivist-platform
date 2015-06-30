package controllers;

import static play.data.Form.form;
import http.Headers;
import models.Assembly;
import models.AssemblyCollection;
import models.User;
import models.services.Service;
import models.services.ServiceAssembly;
import models.services.ServiceCampaign;
import models.services.ServiceCampaignCollection;
import models.services.ServiceCollection;
import models.services.ServiceIssue;
import models.services.ServiceIssueCollection;
import models.services.ServiceOperation;
import models.services.ServiceOperationCollection;
import models.transfer.TransferMembership;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import security.SecurityModelConstants;
import utils.GlobalData;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;

import enums.ResponseStatus;

@With(Headers.class)
public class Assemblies extends Controller {

	public static final Form<Assembly> ASSEMBLY_FORM = form(Assembly.class);
	public static final Form<TransferMembership> MEMBERSHIP_FORM = form(TransferMembership.class);

	/**
	 * Return the full list of assemblies
	 * 
	 * @return models.AssemblyCollection
	 */
	// @Security.Authenticated(Secured.class)
	@SubjectPresent
	public static Result findAssemblies() {
		AssemblyCollection assemblies = Assembly.findAll();
		return ok(Json.toJson(assemblies));
	}

	/**
	 * Return queries based on a query
	 * 
	 * @return models.AssemblyCollection
	 */
	@SubjectPresent
	public static Result searchAssemblies(String query) {
		// check the user who is accepting the invitation is
		// TODO
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage("Not implemented yet");
		return notFound(Json.toJson(responseBody));
	}

	@SubjectPresent
	public static Result createAssembly() {
		// 1. obtaining the user of the requestor
		User groupCreator = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Assembly> newAssemblyForm = ASSEMBLY_FORM.bindFromRequest();

		if (newAssemblyForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
					newAssemblyForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Assembly newAssembly = newAssemblyForm.get();

			// setting default values (TODO: maybe we should create a dedicated
			// method for this in each model)
			newAssembly.setCreator(groupCreator);
			if (newAssembly.getLang() == null)
				newAssembly.setLang(groupCreator.getLanguage());

			// TODO: check if assembly with same title exists
			// if not add it

			newAssembly.save();

			// TODO: return URL of the new group
			Logger.info("Creating working group");
			Logger.debug("=> " + newAssemblyForm.toString());

			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setNewResourceId(newAssembly.getAssemblyId());
			responseBody.setStatusMessage(Messages.get(
					GlobalData.ASSEMBLY_CREATE_MSG_SUCCESS,
					newAssembly.getName(), groupCreator.getIdentifier()));
			responseBody.setNewResourceURL(GlobalData.ASSEMBLY_BASE_PATH + "/"
					+ newAssembly.getAssemblyId());
			return ok(Json.toJson(responseBody));
		}
	}

	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findAssembly(Long id) {
		Assembly a = Assembly.read(id);
		return a != null ? ok(Json.toJson(a)) : notFound(Json
				.toJson(new TransferResponseStatus(ResponseStatus.NODATA,
						"No assembly with ID = " + id)));
	}

	@SubjectPresent
	public static Result deleteAssembly(Long id) {
		Assembly.delete(id);
		return ok();
	}

	@SubjectPresent
	public static Result updateAssembly(Long id) {
		// 1. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Assembly> newAssemblyForm = ASSEMBLY_FORM.bindFromRequest();

		if (newAssemblyForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
					newAssemblyForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Assembly newAssembly = newAssemblyForm.get();

			TransferResponseStatus responseBody = new TransferResponseStatus();
			newAssembly.setAssemblyId(id);
			newAssembly.update();

			// TODO: return URL of the new group
			Logger.info("Updating assembly");
			Logger.debug("=> " + newAssemblyForm.toString());

			responseBody.setNewResourceId(newAssembly.getAssemblyId());
			responseBody.setStatusMessage(Messages.get(
					GlobalData.ASSEMBLY_CREATE_MSG_SUCCESS,
					newAssembly.getName()));
			responseBody.setNewResourceURL(GlobalData.ASSEMBLY_BASE_PATH + "/"
					+ newAssembly.getAssemblyId());

			return ok(Json.toJson(responseBody));
		}
	}

	@SubjectPresent
	public static Result createAssemblyMembership(Long id, String type) {
		// 1. obtaining the user of the requestor
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<TransferMembership> newMembershipForm = MEMBERSHIP_FORM
				.bindFromRequest();

		if (newMembershipForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					newMembershipForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			TransferMembership newMembership = newMembershipForm.get();
			return Memberships.createMembership(requestor, "assembly", id,
					type, newMembership.getUserId(), newMembership.getEmail(),
					newMembership.getDefaultRoleId(),
					newMembership.getDefaultRoleName());
		}
	}

	// TODO GET /api/assembly/:id/membership/:status
	// controllers.Assemblies.listMembershipsWithStatus(id: Long, status:
	// String)
	@Security.Authenticated(Secured.class)
	public static Result listMemberships(Long id) {
		// check the user who is accepting the invitation is
		// TODO
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage("Not implemented yet");
		return notFound(Json.toJson(responseBody));
	}

	@Security.Authenticated(Secured.class)
	public static Result listMembershipsWithStatus(Long id, String status) {
		// check the user who is accepting the invitation is
		// TODO
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage("Not implemented yet");
		return notFound(Json.toJson(responseBody));
	}

	/*******************************************************
	 * OLD ENDPOINTS - TO BE DEPRECATED SHORTLY
	 */

	/**
	 * Return the full list of issues for one assembly
	 * 
	 * @return models.IssueCollection
	 */
	@SubjectPresent
	public static Result findIssues(Long aid) {
		ServiceAssembly assembly = ServiceAssembly.read(aid);
		ServiceIssueCollection issues = new ServiceIssueCollection();
		issues.setIssues(assembly.getServiceIssues());
		return ok(Json.toJson(issues));
	}

	/**
	 * Return the full list of campaigns for one issue
	 * 
	 * @return models.IssueCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findIssueCampaigns(Long aid, Long iid) {
		ServiceIssue issue = ServiceIssue.readIssueOfServiceAssembly(aid, iid);
		ServiceCampaignCollection campaigns = new ServiceCampaignCollection();
		campaigns.setCampaigns(issue.getDecisionWorkflow());
		return ok(Json.toJson(campaigns));
	}

	/**
	 * Return a campaign of an specific issue
	 * 
	 * @return models.IssueCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findIssueCampaignById(Long aid, Long iid, Long cid) {
		ServiceCampaign campaign = ServiceCampaign.readCampaignOfIssue(aid,
				iid, cid);
		return ok(Json.toJson(campaign));
	}

	/**
	 * Return the full list of services for one assembly
	 * 
	 * @return models.IssueCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findServices(Long aid) {
		ServiceAssembly assembly = ServiceAssembly.read(aid);
		ServiceCollection services = new ServiceCollection();
		services.setServices(assembly.getConnectedServices());
		return ok(Json.toJson(services));
	}

	/**
	 * Return the full list of operations for one service
	 * 
	 * @return models.IssueCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findServiceOperations(Long aid, Long sid) {
		Service service = Service.readServiceOfAssembly(aid, sid);
		ServiceOperationCollection operations = new ServiceOperationCollection();
		operations.setServiceOperations(service.getOperations());
		return ok(Json.toJson(operations));
	}

	/**
	 * Return an operation of an specific service
	 * 
	 * @return models.IssueCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findServiceOperationById(Long aid, Long sid, Long oid) {
		ServiceOperation operation = ServiceOperation.readOperationOfService(
				aid, sid, oid);
		return ok(Json.toJson(operation));
	}
}
