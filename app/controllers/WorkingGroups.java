package controllers;

import static play.data.Form.form;
import http.Headers;

import java.util.List;

import models.User;
import models.WorkingGroup;
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
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import enums.ResponseStatus;

@Api(value = "/group", description = "Group Management endpoints in the Assembly Making service")
@With(Headers.class)
public class WorkingGroups extends Controller {

	public static final Form<WorkingGroup> WORKING_GROUP_FORM = form(WorkingGroup.class);
	public static final Form<TransferMembership> MEMBERSHIP_FORM = form(TransferMembership.class);

	/**
	 * Return the full list of assemblies
	 * 
	 * @return WorkingGroup list
	 */
	@ApiOperation(httpMethod = "GET", response = TransferMembership.class, produces = "application/json", value = "List groups of an assembly")
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findWorkingGroups(Long aid) {
		List<WorkingGroup> workingGroups = WorkingGroup.findByAssembly(aid);
		return ok(Json.toJson(workingGroups));
	}

	// TODO make a read working group for non members
	// TODO only members of the group can read the whole group
	@SubjectPresent
	public static Result findWorkingGroup(Long aid, Long wGroupId) {
		WorkingGroup workingGroup = WorkingGroup.read(wGroupId);
		return workingGroup != null ? ok(Json.toJson(workingGroup))
				: notFound(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.NODATA, "No group with ID = "
										+ wGroupId)));
	}

	@SubjectPresent
//	@Restrict({@Group("COORDINATOR")})
	public static Result deleteWorkingGroup(Long aid, Long wGroupId) {
		WorkingGroup.delete(wGroupId);
		return ok();
	}

	// TODO ASSEMMBLY MEMBER can create if he/she is COORDINATOR or if the assembly is open to groups
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
			} else {
				if (newWorkingGroup.getCreator() == null) {
					newWorkingGroup.setCreator(groupCreator);
				}

				WorkingGroup.create(newWorkingGroup);

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

	@Security.Authenticated(Secured.class)
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

	@SubjectPresent
	public static Result createGroupMembership(Long aid, Long id, String type) {
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

	@SubjectPresent
	public static Result listMembershipsWithStatus(Long aid, Long id, String status) {
		// check the user who is accepting the invitation is
		// TODO
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage("Not implemented yet");
		return notFound(Json.toJson(responseBody));
	}

}
