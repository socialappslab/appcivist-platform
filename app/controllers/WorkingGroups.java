package controllers;

import static play.data.Form.form;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import delegates.CampaignDelegate;
import enums.BallotStatus;
import enums.ContributionStatus;
import enums.ContributionTypes;
import http.Headers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import models.*;
import models.misc.Views;
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
import play.mvc.Results;
import play.mvc.With;
import play.twirl.api.Content;
import security.SecurityModelConstants;
import utils.GlobalData;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.feth.play.module.pa.PlayAuthenticate;

import delegates.WorkingGroupsDelegate;
import enums.ResponseStatus;
import exceptions.MembershipCreationException;

@Api(value = "02 group: Working Group Management", description = "Group Management endpoints in the Assembly Making service")
@With(Headers.class)
public class WorkingGroups extends Controller {

	public static final Form<WorkingGroup> WORKING_GROUP_FORM = form(WorkingGroup.class);
	public static final Form<MembershipTransfer> MEMBERSHIP_FORM = form(MembershipTransfer.class);

	/**
	 * Return the full list of working groups in an assembly
	 * 
	 * @return WorkingGroup list
	 */
	@ApiOperation(httpMethod = "GET", response = WorkingGroup.class, responseContainer="List", produces = "application/json", value = "List groups of an assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result findWorkingGroups(@ApiParam(name = "aid", value = "Assembly ID") Long aid) {
		List<WorkingGroup> workingGroups = WorkingGroup.findByAssembly(aid);
		return ok(Json.toJson(workingGroups));
	}
	
	@ApiOperation(httpMethod = "GET", response = WorkingGroup.class, responseContainer="List", produces = "application/json", value = "List of groups created in a campaign")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findWorkingGroupsInCampaign(@ApiParam(name = "aid", value = "Assembly ID") Long aid, @ApiParam(name = "cid", value = "Campaign ID") Long cid) {
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

	@ApiOperation(httpMethod = "GET", response = WorkingGroup.class, produces = "application/json", value = "Get working group by ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result findWorkingGroup(@ApiParam(name = "aid", value = "Assembly ID") Long aid, @ApiParam(name = "gid", value = "Working Group ID") Long wGroupId) {
		WorkingGroup workingGroup = WorkingGroup.read(wGroupId);
		return workingGroup != null ? ok(Json.toJson(workingGroup))
				: notFound(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.NODATA, "No group with ID = "
										+ wGroupId)));
	}

	@ApiOperation(httpMethod = "DELETE", response = WorkingGroup.class, produces = "application/json", value = "Delete group by ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result deleteWorkingGroup(@ApiParam(name = "aid", value = "Assembly ID") Long aid, @ApiParam(name = "gid", value = "Working Group ID") Long wGroupId) {
		WorkingGroup.delete(wGroupId);
		return ok();
	}

	@ApiOperation(httpMethod = "POST", response = WorkingGroup.class, produces = "application/json", 
			value = "Create a new working group",
			notes="Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = BAD_REQUEST, message = "group create error", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Working Group Object", value = "New Working Group in json", dataType = "models.WorkingGroup", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	@SubjectPresent
	public static Result createWorkingGroup(@ApiParam(name = "aid", value = "Assembly ID") Long aid) {
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

			if (WorkingGroup.numberByNameInAssembly(newWorkingGroup.getName(),aid) > 0) {
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
			value = "Create a Working Group in the Campaign identified by ID", 
			notes="This will also add the Working Gorup to the Assembly organizing this campaign. Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = BAD_REQUEST, message = "Group create error", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Working Group Object", value = "New Working Group in json", dataType = "models.WorkingGroup", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createWorkingGroupInCampaign(@ApiParam(name = "aid", value = "Assembly ID") Long aid, @ApiParam(name = "cid", value = "Campaign ID") Long cid) {
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
			Assembly a = Assembly.read(aid);
			Long containingSpace = a != null ? a.getResourcesResourceSpaceId() : null;
			
			if (WorkingGroup.numberByNameInAssembly(newWorkingGroup.getName(),containingSpace) > 0) {
				Logger.info("Working Group already exists");
				responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
				responseBody.setStatusMessage(Messages.get("appcivist.api.error.groups.group_with_same_name_already_exists"));
				return internalServerError(Json.toJson(responseBody));
			} else {
				Ebean.beginTransaction();
				try {
					if (newWorkingGroup.getCreator() == null) {
						newWorkingGroup.setCreator(groupCreator);
					}
					List<InvitationTransfer> invitations = newWorkingGroup.getInvitations();
					newWorkingGroup = WorkingGroup.create(newWorkingGroup);
					newWorkingGroup.refresh();
					// Add the working group to the assembly
					ResourceSpace rs = ResourceSpace.read(a.getResourcesResourceSpaceId());
					rs.addWorkingGroup(newWorkingGroup);
					newWorkingGroup.getContainingSpaces().add(rs);
					rs.update();
					
					// Add the working group to the campaign
					Campaign c = Campaign.read(cid);
					ResourceSpace rsC = ResourceSpace.read(c.getResourceSpaceId());
					rsC.addWorkingGroup(newWorkingGroup);
					newWorkingGroup.getContainingSpaces().add(rsC);
					rsC.update();
					
					newWorkingGroup.refresh();
					
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

	@ApiOperation(httpMethod = "PUT", response = WorkingGroup.class, produces = "application/json", value = "Update Working Group by ID", notes="Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = BAD_REQUEST, message = "Group create error", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Working Group Object", value = "New Working Group in json", dataType = "models.WorkingGroup", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateWorkingGroup(@ApiParam(name = "aid", value = "Assembly ID") Long aid, @ApiParam(name = "aid", value = "Working Group ID") Long groupId) {
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

			if (WorkingGroup.numberByNameInAssembly(newWorkingGroup.getName(),aid) > 0) {
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

	@ApiOperation(httpMethod = "POST", response = MembershipTransfer.class, produces = "application/json", value = "Add membership to the working group", 
			notes = "Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = BAD_REQUEST, message = "mebership create error", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Membership simplified object", value = "membership's form in body", dataType = "models.transfer.MembershipTransfer", paramType = "body", required = true),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result createGroupMembership(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid, 
			@ApiParam(name = "gid", value = "Working Group ID") Long id, 
			@ApiParam(name = "Type", value = "Type of Membership") String type) {
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
			try {
				Ebean.beginTransaction();
				Result r = Memberships.createMembership(requestor, "group", id, type,
						newMembership.getUserId(), newMembership.getEmail(),
						newMembership.getDefaultRoleId(),
						newMembership.getDefaultRoleName());
				return r;
			} catch (MembershipCreationException e) {
				Ebean.rollbackTransaction();
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
						"Error: "+e.getMessage()));
				return internalServerError(Json.toJson(responseBody));
			}
		}
	}
	
	@ApiOperation(httpMethod = "GET", response = Membership.class, responseContainer = "List", produces = "application/json", value = "Get Working Group Memberships by ID and status")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No membership in this group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result listMembershipsWithStatus(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid, 
			@ApiParam(name = "gid", value = "Working Group ID") Long gid, 
			@ApiParam(name = "status", value = "Status of membership invitation or request", allowableValues = "REQUESTED, INVITED, FOLLOWING, ALL", required = true) String status) {
		List<Membership> m = MembershipGroup.findByAssemblyIdGroupIdAndStatus(aid, gid, status);
		if (m != null && !m.isEmpty())
			return ok(Json.toJson(m));
		return notFound(Json.toJson(new TransferResponseStatus(
				"No memberships with status '" + status + "' in Working Group '"
						+ gid + "'")));
	}

	@ApiOperation(httpMethod = "GET", response = TransferResponseStatus.class, produces = "application/json", value = "Get Working Group Memberships by user ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User is not Member of Group", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	//@SubjectPresent
	public static Result isUserMemberOfGroup(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid, 
			@ApiParam(name = "gid", value = "Working Group ID")Long gid, 
			@ApiParam(name = "uid", value = "User ID") Long userId) {
		Boolean result = MembershipGroup.isUserMemberOfGroup(userId, gid);
		if (result) return ok(Json.toJson(new TransferResponseStatus(ResponseStatus.OK, 
					"User '" + userId + "' is a member of Working Group '"+ gid + "'")));
		else return ok(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, 
				"User '" + userId + "' is not a member of Working Group '"+ gid + "'")));
	}
	
	/**
	 * 
	 * @return models.AssemblyCollection
	 */
	@ApiOperation(httpMethod = "GET", response = WorkingGroupSummaryTransfer.class, produces = "application/json", value = "Get working group profile if it is listed")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") 
	})
	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result getListedWorkingGroupProfile(@ApiParam(name = "aid", value = "Assembly ID") Long aid, @ApiParam(name = "gid", value = "Working Group ID") Long gid) {
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
		WorkingGroupSummaryTransfer group = WorkingGroupsDelegate.readListedWorkingGroup(gid, requestor);
		if (group != null) {
			return ok(Json.toJson(group));		
		} else {
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage("Group with id = '"+gid+"' is not available for this user", "")));
		}
	}

	@ApiOperation(httpMethod = "GET", response = WorkingGroupSummaryTransfer.class, produces = "application/json", value = "Get working group PROPOSALS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") 
	})
	@Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result listWorkingGroupProposals(@ApiParam(name = "aid", value = "Assembly ID") Long aid, @ApiParam(name = "gid", value = "Working Group ID") Long gid){
		List<Contribution> proposals = WorkingGroup.listWorkingGroupProposals(gid);
		return ok(Json.toJson(proposals));
	}
	
	@ApiOperation(httpMethod = "GET", response = WorkingGroupSummaryTransfer.class, produces = "application/json", value = "Get working group CONTRIBUTIONS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") 
	})
	@Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
	public static Result listWorkingGroupContributions(@ApiParam(name = "aid", value = "Assembly ID") Long aid, @ApiParam(name = "gid", value = "Working Group ID") Long gid) {
		List<Contribution> contributions = WorkingGroup.listWorkingGroupContributions(gid);
		return ok(Json.toJson(contributions));
	}
	
	//TODO
	@ApiOperation(value="List memberships in group", hidden=true)
	@SubjectPresent
	public static Result listMemberships(Long aid, Long id) {
		// check the user who is accepting the invitation is
		// TODO
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage("Not implemented yet");
		return notFound(Json.toJson(responseBody));
	}

	@ApiOperation(httpMethod = "POST", response = Ballot.class, produces = "application/json", value = "Creates new Consensus Ballot from selected/remaining proposals in working group")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result nextBallotForWorkingGroup(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid,
			@ApiParam(name = "gid", value = "Working Group ID") Long gid) {

		List<String> selectedProposalsPayload = null;
		List<UUID> selectedProposals = null;
		if(request().body().asJson().isArray()){
			selectedProposalsPayload = new ObjectMapper().convertValue(request().body().asJson(), ArrayList.class);
			selectedProposals = selectedProposalsPayload.stream().map(p -> UUID.fromString(p)).collect(Collectors.toList());
		}
		//First, we have to find the working group and the current Ballot for the working group
		WorkingGroup workingGroup = WorkingGroup.read(gid);
		UUID consensus = WorkingGroup.queryConsensusBallotByGroupResourceSpaceId(workingGroup.getResourcesResourceSpaceId());
		Ballot currentBallot = Ballot.findByUUID(consensus);
		List<BallotCandidate> newCandidates = new ArrayList<>();

		//Creates new candidates based on selected proposals (if any), or based on current non excluded proposals
		List<BallotCandidate> currentCandidates = currentBallot.getBallotCandidates();
		if(selectedProposals == null || selectedProposals.isEmpty()){
			//Select only candidates with non-excluded proposals
			if(currentCandidates != null){
				newCandidates = currentCandidates.stream().
						filter(candidate -> !ContributionStatus.EXCLUDED.equals(candidate.getContribution().getStatus())).collect(Collectors.toList());
			}
		}else{
			newCandidates = selectedProposals.stream().map(c -> {
				BallotCandidate filtered = null;
				for(BallotCandidate bc : currentCandidates){
					if(bc.getContributionUuid().equals(c)){
						filtered =  bc;
						break;
					}
				}
				return filtered;
			}).collect(Collectors.toList());
		}
		Ballot newBallot = Ballot.createConsensusBallotForWorkingGroup(workingGroup);

		for(BallotCandidate candidate : newCandidates){
			BallotCandidate contributionAssociatedCandidate = new BallotCandidate();
			contributionAssociatedCandidate.setBallotId(newBallot.getId());
			contributionAssociatedCandidate.setCandidateType(new Integer(1));
			contributionAssociatedCandidate.setContributionUuid(candidate.getContribution().getUuid());
			contributionAssociatedCandidate.save();
		}
		//Send the current ballot to a historic
		workingGroup.getBallotHistories().add(currentBallot);
		workingGroup.save();

		//Archive previous ballot
		currentBallot.setStatus(BallotStatus.ARCHIVED);

		return ok(Json.toJson(newBallot));
	}

	@ApiOperation(httpMethod = "PUT", response = Ballot.class, produces = "application/json", value = "Creates new Consensus Ballot from selected/remaining proposals in working group")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result archiveWorkingGroupsBallot(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid,
			@ApiParam(name = "gid", value = "Working Group ID") Long gid) {

		WorkingGroup workingGroup = WorkingGroup.read(gid);
		UUID consensus = WorkingGroup.queryConsensusBallotByGroupResourceSpaceId(workingGroup.getResourcesResourceSpaceId());
		Ballot ballot = Ballot.findByUUID(consensus);
		ballot.setStatus(BallotStatus.ARCHIVED);
		ballot.update();
		return ok(Json.toJson(ballot));
	}


	@ApiOperation(httpMethod = "GET", response = WorkingGroup.class, produces = "application/json", value = "Read working group by Universal ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No working group found", response = TransferResponseStatus.class) })
	public static Result findWorkingGroupByUUID(@ApiParam(name = "uuid", value = "Working Group Universal ID (UUID)") UUID uuid) {
		try{

			WorkingGroup wgroup = WorkingGroup.readByUUID(uuid);
			if(wgroup == null){
				return ok(Json
						.toJson(new TransferResponseStatus("No working group found")));
			}

			/*ObjectMapper mapper = new ObjectMapper();
			mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			mapper.addMixIn(Campaign.class, Campaign.AssembliesVisibleMixin.class);
			String result = mapper.writerWithView(Views.Public.class)
					.writeValueAsString(summary);

			Content ret = new Content() {
				@Override public String body() { return result; }
				@Override public String contentType() { return "application/json"; }
			};*/

			return Results.ok(Json.toJson(wgroup));
		}catch(Exception e){
			return badRequest(Json.toJson(Json
					.toJson(new TransferResponseStatus("Error processing request"))));
		}

	}
}
