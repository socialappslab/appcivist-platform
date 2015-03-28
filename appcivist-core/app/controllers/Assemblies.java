package controllers;


import models.Assembly;
import models.AssemblyCollection;
import models.Campaign;
import models.CampaignCollection;
import models.Issue;
import models.IssueCollection;
import models.services.Service;
import models.services.ServiceCollection;
import models.services.ServiceOperation;
import models.services.ServiceOperationCollection;
import play.mvc.*;
import play.libs.Json;

public class Assemblies extends Controller {

	/**
	 * Return the full list of assemblies
	 * @return models.AssemblyCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findAssemblies() {
		AssemblyCollection assemblies = Assembly.findAll();
		return ok(Json.toJson(assemblies));
	}

	/**
	 * Return the full list of issues for one assembly
	 * @return models.IssueCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findIssues(Long aid) {
		Assembly assembly = Assembly.read(aid);
		IssueCollection issues = new IssueCollection();
		issues.setIssues(assembly.getIssues());
		return ok(Json.toJson(issues));
	}
	
	/**
	 * Return the full list of campaigns for one issue
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
	 * @return models.IssueCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findIssueCampaignById(Long aid, Long iid, Long cid) {
		Campaign campaign = Campaign.readCampaignOfIssue(aid, iid, cid);
		return ok(Json.toJson(campaign));
	}
	
	/**
	 * Return the full list of services for one assembly
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
	 * @return models.IssueCollection
	 */
	@Security.Authenticated(Secured.class)
	public static Result findServiceOperationById(Long aid, Long sid, Long oid) {
		ServiceOperation operation = ServiceOperation.readOperationOfService(aid, sid, oid);
		return ok(Json.toJson(operation));
	}
}
