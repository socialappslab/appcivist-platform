package controllers;

import static play.data.Form.form;

import java.util.List;

import com.feth.play.module.pa.PlayAuthenticate;

import models.User;
import models.WorkingGroup;
import models.transfer.TransferMembership;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.mvc.*;
import play.i18n.Messages;
import play.data.Form;
import play.libs.Json;
import utils.GlobalData;
import http.Headers;

@With(Headers.class)
public class WorkingGroups extends Controller {

	public static final Form<WorkingGroup> WORKING_GROUP_FORM = form(WorkingGroup.class);
	public static final Form<TransferMembership> MEMBERSHIP_FORM = form(TransferMembership.class);

	/**
	 * Return the full list of assemblies
	 * 
	 * @return WorkingGroup list
	 */
	@Security.Authenticated(Secured.class)
	public static Result findWorkingGroups() {
		List<WorkingGroup> workingGroups = WorkingGroup.findAll();
		return ok(Json.toJson(workingGroups));
	}

	// @Security.Authenticated(Secured.class)
	public static Result findWorkingGroup(Long wGroupId) {
		WorkingGroup workingGroup = WorkingGroup.read(wGroupId);
		return ok(Json.toJson(workingGroup));
	}

	@Security.Authenticated(Secured.class)
	public static Result deleteWorkingGroup(Long wGroupId) {
		WorkingGroup.delete(wGroupId);
		return ok();
	}

	@Security.Authenticated(Secured.class)
	public static Result createWorkingGroup() {
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
				newWorkingGroup.setLang(groupCreator.getLocale());

			TransferResponseStatus responseBody = new TransferResponseStatus();

			if (WorkingGroup.readByTitle(newWorkingGroup.getName()) > 0) {
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

	// @Security.Authenticated(Secured.class)
	public static Result updateWorkingGroup() {
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

			if (WorkingGroup.readByTitle(newWorkingGroup.getName()) > 0) {
				Logger.info("Working group already exists");
			} else {
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

	// POST /api/group/:id/membership/:type
	// controllers.WorkingGroups.createGroupMembership(id: Long, type: String)
	@Security.Authenticated(Secured.class)
	public static Result createGroupMembership(Long id, String type) {
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
					newMembership.getUserId(), newMembership.getEmail());
		}
	}
	
	// GET /api/group/:id/membership
	// controllers.WorkingGroups.listMemberships(id: Long)
	@Security.Authenticated(Secured.class)
	public static Result listMemberships(Long id) {
		// check the user who is accepting the invitation is
		// TODO
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage("Not implemented yet");
		return notFound(Json.toJson(responseBody));
	}
	
	// GET /api/group/:id/membership/:status
	// controllers.WorkingGroups.listMembershipsWithStatus(id: Long, status:
	// String)
	@Security.Authenticated(Secured.class)
	public static Result listMembershipsWithStatus(Long id, String status) {
		// check the user who is accepting the invitation is
		// TODO
		TransferResponseStatus responseBody = new TransferResponseStatus();
		responseBody.setStatusMessage("Not implemented yet");
		return notFound(Json.toJson(responseBody));
	}
	

}
