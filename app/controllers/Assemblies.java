package controllers;

import static play.data.Form.form;

import com.feth.play.module.pa.PlayAuthenticate;

import models.Assembly;
import models.AssemblyCollection;
import models.Campaign;
import models.CampaignCollection;
import models.Issue;
import models.IssueCollection;
import models.User;
import models.WorkingGroup;
import models.services.Service;
import models.services.ServiceCollection;
import models.services.ServiceOperation;
import models.services.ServiceOperationCollection;
import play.Logger;
import play.mvc.*;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import utils.GlobalData;
import utils.ResponseStatusBean;
import http.Headers;

@With(Headers.class)
public class Assemblies extends Controller {

	public static final Form<Assembly> ASSEMBLY_FORM = form(Assembly.class);
	
	/**
	 * Return the full list of assemblies
	 * 
	 * @return models.AssemblyCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findAssemblies() {
		AssemblyCollection assemblies = Assembly.findAll();
		return ok(Json.toJson(assemblies));
	}

	/**
	 * Return the full list of issues for one assembly
	 * 
	 * @return models.IssueCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findIssues(Long aid) {
		Assembly assembly = Assembly.read(aid);
		IssueCollection issues = new IssueCollection();
		issues.setIssues(assembly.getIssues());
		return ok(Json.toJson(issues));
	}

	@Security.Authenticated(Secured.class)
	public static Result createAssembly() {
		// 1. obtaining the user of the requestor
		User groupCreator = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Assembly> newAssemblyForm = ASSEMBLY_FORM
				.bindFromRequest();

		if (newAssemblyForm.hasErrors()) {
			ResponseStatusBean responseBody = new ResponseStatusBean();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.ASSEMBLY_CREATE_MSG_ERROR,newAssemblyForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Assembly newAssembly = newAssemblyForm.get();

			// setting default values (TODO: maybe we should create a dedicated method for this in each model)
			newAssembly.setCreator(groupCreator);
			if (newAssembly.getLang()==null)
				newAssembly.setLang(groupCreator.getLocale());
			
			// TODO: check if assembly with same title exists
			// if not add it

			newAssembly.save();

			// TODO: return URL of the new group
			Logger.info("Creating working group");
			Logger.debug("=> " + newAssemblyForm.toString());

			ResponseStatusBean responseBody = new ResponseStatusBean();
			responseBody.setNewResourceId(newAssembly.getAssemblyId());
			responseBody.setStatusMessage(Messages.get(
					GlobalData.ASSEMBLY_CREATE_MSG_SUCCESS,
					newAssembly.getName(), groupCreator.getIdentifier()));
			responseBody.setNewResourceURL(GlobalData.ASSEMBLY_BASE_PATH+"/"+newAssembly.getAssemblyId());
			return ok(Json.toJson(responseBody));
		}
	}

	/*******************************************************
	 * OLD ENDPOINTS - TO BE DEPRECATED SHORTLY
	 */

	/**
	 * Return the full list of campaigns for one issue
	 * 
	 * @return models.IssueCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findIssueCampaigns(Long aid, Long iid) {
		Issue issue = Issue.readIssueOfAssembly(aid, iid);
		CampaignCollection campaigns = new CampaignCollection();
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
		Campaign campaign = Campaign.readCampaignOfIssue(aid, iid, cid);
		return ok(Json.toJson(campaign));
	}

	/**
	 * Return the full list of services for one assembly
	 * 
	 * @return models.IssueCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findServices(Long aid) {
		Assembly assembly = Assembly.read(aid);
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
