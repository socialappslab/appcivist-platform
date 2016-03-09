package controllers;

import static play.data.Form.form;
import http.Headers;

import java.util.List;

import models.Assembly;
import models.Campaign;
import models.Contribution;
import models.Membership;
import models.MembershipGroup;
import models.MembershipInvitation;
import models.ResourceSpace;
import models.User;
import models.WorkingGroup;
import models.transfer.AssemblySummaryTransfer;
import models.transfer.InvitationTransfer;
import models.transfer.MembershipTransfer;
import models.transfer.TransferResponseStatus;
import models.transfer.WorkingGroupSummaryTransfer;
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

import delegates.WorkingGroupsDelegate;
import enums.ResponseStatus;

@Api(value = "/group", description = "Group Management endpoints in the Assembly Making service")
@With(Headers.class)
public class WorkingGroups extends Controller {

	public static final Form<WorkingGroup> WORKING_GROUP_FORM = form(WorkingGroup.class);
	public static final Form<MembershipTransfer> MEMBERSHIP_FORM = form(MembershipTransfer.class);

	/**
	 * Return the full list of working groups in an assembly
	 * 
	 * @return WorkingGroup list
	 */
	@ApiOperation(httpMethod = "GET", response = MembershipTransfer.class, produces = "application/json", value = "List groups of an assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly owner numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result findWorkingGroups(Long aid) {
		List<WorkingGroup> workingGroups = WorkingGroup.findByAssembly(aid);
		return ok(Json.toJson(workingGroups));
	}
	
	@ApiOperation(httpMethod = "GET", response = MembershipTransfer.class, produces = "application/json", value = "List of groups created in a campaign")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Campaien numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result findWorkingGroupsInCampaign(Long aid, Long cid) {
		Campaign c = Campaign.read(cid);
		if (c!=null) {
			List<WorkingGroup> workingGroups = c.getWorkingGroups();
			return ok(Json.toJson(workingGroups));
		} else {
			TransferResponseStatus response = new TransferResponseStatus();
			response.setResponseStatus(ResponseStatus.NODATA);
			response.setStatusMessage("Campaign "+cid+" does not exist");
			return notFound(Json.toJson(response));
		}
	}

	
	@ApiOperation(httpMethod = "GET", response = Campaign.class, produces = "application/json", value = "Get working group by ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "gid", value = "Working Group numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result findWorkingGroup(Long aid, Long wGroupId) {
		WorkingGroup workingGroup = WorkingGroup.read(wGroupId);
		return workingGroup != null ? ok(Json.toJson(workingGroup))
				: notFound(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.NODATA, "No group with ID = "
										+ wGroupId)));
	}

	@ApiOperation(httpMethod = "GET", response = Campaign.class, produces = "application/json", value = "Delete group by ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "gid", value = "Working Group numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result deleteWorkingGroup(Long aid, Long wGroupId) {
		WorkingGroup.delete(wGroupId);
		return ok();
	}

	@ApiOperation(httpMethod = "POST", response = Campaign.class, produces = "application/json", value = "Create a new working group")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly owner numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "new group form", value = "New Working Group in json", dataType = "models.WorkingGroup", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	//@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	@SubjectPresent
	public static Result createWorkingGroup(Long aid) {
		// 1. obtaining the user of the requestor
		User groupCreator = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<WorkingGroup> newWorkingGroupForm = WORKING_GROUP_FORM
				.bindFromRequest();

		if (newWorkingGroupForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.GROUP_CREATE_MSG_ERROR,
					newWorkingGroupForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			WorkingGroup newWorkingGroup = newWorkingGroupForm.get();

			if (newWorkingGroup.getLang() == null)
				newWorkingGroup.setLang(groupCreator.getLanguage());

			TransferResponseStatus responseBody = new TransferResponseStatus();

			if (WorkingGroup.numberByName(newWorkingGroup.getName()) > 0) {
				Logger.info("Working Group already exists");
				responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
				responseBody.setStatusMessage("Working Group already exists");
				return internalServerError(Json.toJson(responseBody));
			} else {
				Ebean.beginTransaction();
				try {
					if (newWorkingGroup.getCreator() == null) {
						newWorkingGroup.setCreator(groupCreator);
					}
					List<InvitationTransfer> invitations = newWorkingGroup.getInvitations();
					newWorkingGroup = WorkingGroup.create(newWorkingGroup);

					// Add the working group to the assembly
					ResourceSpace rs = Assembly.read(aid).getResources();
					rs.addWorkingGroup(newWorkingGroup);
					rs.update();
					
					// Create and send invitations
					// Create and send invitations
					
					if (invitations != null) {
						for (InvitationTransfer invitation : invitations) {
							MembershipInvitation.create(invitation, groupCreator, newWorkingGroup);
						}
					}
					
					
				} catch (Exception e) {
					Ebean.rollbackTransaction();
					e.printStackTrace();
					Logger.info("Error creating Working Group: "+e.getMessage());
					responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
					responseBody.setStatusMessage("Error creating Working Group: "+e.getMessage());
					return internalServerError(Json.toJson(responseBody));
				}
				Ebean.commitTransaction();
			}
			return ok(Json.toJson(newWorkingGroup));
		}
	}

	@ApiOperation(httpMethod = "POST", response = Campaign.class, produces = "application/json", 
			value = "Create a Working Group in the Campaign identified by ID. This will also add the Working Gorup to the Assembly organizing this campaign.")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly owner numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "cid", value = "Campaign in numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "new group form", value = "New Working Group in json", dataType = "models.WorkingGroup", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createWorkingGroupInCampaign(Long aid, Long cid) {
		// 1. obtaining the user of the requestor
		User groupCreator = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<WorkingGroup> newWorkingGroupForm = WORKING_GROUP_FORM
				.bindFromRequest();

		if (newWorkingGroupForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.GROUP_CREATE_MSG_ERROR,
					newWorkingGroupForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			WorkingGroup newWorkingGroup = newWorkingGroupForm.get();

			if (newWorkingGroup.getLang() == null)
				newWorkingGroup.setLang(groupCreator.getLanguage());

			TransferResponseStatus responseBody = new TransferResponseStatus();

			if (WorkingGroup.numberByName(newWorkingGroup.getName()) > 0) {
				Logger.info("Working Group already exists");
				responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
				responseBody.setStatusMessage("Working Group already exists");
				return internalServerError(Json.toJson(responseBody));
			} else {
				Ebean.beginTransaction();
				try {
					if (newWorkingGroup.getCreator() == null) {
						newWorkingGroup.setCreator(groupCreator);
					}
					List<InvitationTransfer> invitations = newWorkingGroup.getInvitations();
					newWorkingGroup = WorkingGroup.create(newWorkingGroup);

					// Add the working group to the assembly
					ResourceSpace rs = Assembly.read(aid).getResources();
					rs.addWorkingGroup(newWorkingGroup);
					rs.update();
					
					// Add the working group to the campaign
					ResourceSpace rsC = Campaign.read(cid).getResources();
					rsC.addWorkingGroup(newWorkingGroup);
					rsC.update();
					
					// Create and send invitations							
					if (invitations != null) {
						for (InvitationTransfer invitation : invitations) {
							MembershipInvitation.create(invitation, groupCreator, newWorkingGroup);
						}
					}
				} catch (Exception e) {
					Ebean.rollbackTransaction();
					e.printStackTrace();
					Logger.info("Error creating Working Group: "+e.getMessage());
					responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
					responseBody.setStatusMessage("Error creating Working Group: "+e.getMessage());
					return internalServerError(Json.toJson(responseBody));
				}
				Ebean.commitTransaction();
			}
			return ok(Json.toJson(newWorkingGroup));
		}
	}

	@ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Get campaign by ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly owner numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "gid", value = "Working Group numerical id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "new group form", value = "New Working Group in json", dataType = "models.WorkingGroup", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateWorkingGroup(Long aid, Long groupId) {
		// 1. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<WorkingGroup> newWorkingGroupForm = WORKING_GROUP_FORM
				.bindFromRequest();

		if (newWorkingGroupForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.GROUP_CREATE_MSG_ERROR,
					newWorkingGroupForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			WorkingGroup newWorkingGroup = newWorkingGroupForm.get();

			TransferResponseStatus responseBody = new TransferResponseStatus();

			if (WorkingGroup.numberByName(newWorkingGroup.getName()) > 0) {
				String status_message = "Working group already exists with the same name already exists";
				Logger.info(status_message);
				responseBody.setResponseStatus(ResponseStatus.UNAUTHORIZED);
				responseBody.setStatusMessage(status_message);
			} else {
				newWorkingGroup.setGroupId(groupId);
				newWorkingGroup.update();

				// TODO: return URL of the new group
				Logger.info("Creating working group");
				Logger.debug("=> " + newWorkingGroupForm.toString());

				responseBody.setNewResourceId(newWorkingGroup.getGroupId());
				responseBody.setStatusMessage(Messages.get(
						GlobalData.GROUP_CREATE_MSG_SUCCESS,
						newWorkingGroup.getName()/*
												 * ,
												 * groupCreator.getIdentifier()
												 */));
				responseBody.setNewResourceURL(GlobalData.GROUP_BASE_PATH + "/"
						+ newWorkingGroup.getGroupId());
			}

			return ok(Json.toJson(responseBody));
		}
	}

	@ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", value = "Add membership to the assembly", notes = "Get the full list of assemblies. Only availabe to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "gid", value = "Group id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "type", value = "Type of membership", allowableValues = "INVITATION, REQUEST, SUBSCRIPTION", required = true, paramType = "path"),
			@ApiImplicitParam(name = "membership_form", value = "membership's form in body", dataType = "models.transfer.MembershipTransfer", paramType = "body", required = true),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfGroup", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createGroupMembership(Long aid, Long id, String type) {
		// 1. obtaining the user of the requestor
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<MembershipTransfer> newMembershipForm = MEMBERSHIP_FORM
				.bindFromRequest();

		if (newMembershipForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					newMembershipForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			MembershipTransfer newMembership = newMembershipForm.get();
			return Memberships.createMembership(requestor, "group", id, type,
					newMembership.getUserId(), newMembership.getEmail(),
					newMembership.getDefaultRoleId(),
					newMembership.getDefaultRoleName());
		}
	}

	@SubjectPresent
	public static Result listMemberships(Long aid, Long id) {
		// check the user who is accepting the invitation is
		// TODO
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage("Not implemented yet");
		return notFound(Json.toJson(responseBody));
	}
	
	@ApiOperation(httpMethod = "GET", response = Membership.class, responseContainer = "List", produces = "application/json", value = "Get Assembly Memberships by ID and status", notes = "Get the full list of assemblies. Only availabe to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No membership in this group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "gid", value = "Group id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "status", value = "Status of membership invitation or request", allowableValues = "REQUESTED, INVITED, FOLLOWING, ALL", required = true, paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result listMembershipsWithStatus(Long aid, Long gid, String status) {
		List<Membership> m = MembershipGroup.findByAssemblyIdGroupIdAndStatus(aid, gid, status);
		if (m != null && !m.isEmpty())
			return ok(Json.toJson(m));
		return notFound(Json.toJson(new TransferResponseStatus(
				"No memberships with status '" + status + "' in Working Group '"
						+ gid + "'")));
	}

	@ApiOperation(httpMethod = "GET", response = TransferResponseStatus.class, produces = "application/json", value = "Get Assembly Memberships by ID and status", notes = "Get the full list of assemblies. Only availabe to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User is not Member of Group", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aid", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "gid", value = "Group id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "uid", value = "User id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	//@SubjectPresent
	public static Result isUserMemberOfGroup(Long aid, Long gid, Long userId) {
		Boolean result = MembershipGroup.isUserMemberOfGroup(userId, gid);
		if (result) return ok(Json.toJson(new TransferResponseStatus(ResponseStatus.OK, 
					"User '" + userId + "' is a member of Working Group '"+ gid + "'")));
		else return ok(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, 
				"User '" + userId + "' is not a member of Working Group '"+ gid + "'")));
	}
	
	public static Result listWorkingGroupProposals(Long aid, Long gid) {
		WorkingGroup wg = WorkingGroup.read(gid);
		List<Contribution> proposals = wg.getProposals();
		return ok(Json.toJson(proposals));
	}
	
	/**
	 * 
	 * @return models.AssemblyCollection
	 */
	@ApiOperation(httpMethod = "GET", response = AssemblySummaryTransfer.class, produces = "application/json", value = "Get working group profile if it is listed")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"), 
			@ApiImplicitParam(name = "gid", value = "Groups ID", dataType="Long", paramType="path")
	})
	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result getListedWorkingGroupProfile(Long aid, Long gid) {
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
		WorkingGroupSummaryTransfer group = WorkingGroupsDelegate.readListedWorkingGroup(gid, requestor);
		if (group != null) {
			return ok(Json.toJson(group));		
		} else {
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage("Group with id = '"+gid+"' is not available for this user", "")));
		}
	}
	
}
