package controllers;

import com.wordnik.swagger.annotations.Api;

import http.Headers;
import models.services.Service;
import models.services.ServiceAssembly;
import models.services.ServiceCampaign;
import models.services.ServiceCampaignCollection;
import models.services.ServiceCollection;
import models.services.ServiceIssue;
import models.services.ServiceIssueCollection;
import models.services.ServiceOperation;
import models.services.ServiceOperationCollection;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import be.objectify.deadbolt.java.actions.SubjectPresent;

@With(Headers.class)
public class ServiceAssemblies extends Controller {

		
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
	@SubjectPresent
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
	@SubjectPresent
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
	@SubjectPresent
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
	@SubjectPresent
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
	@SubjectPresent
	public static Result findServiceOperationById(Long aid, Long sid, Long oid) {
		ServiceOperation operation = ServiceOperation.readOperationOfService(
				aid, sid, oid);
		return ok(Json.toJson(operation));
	}
}
