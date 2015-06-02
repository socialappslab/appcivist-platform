package controllers;

import static play.data.Form.form;

import java.util.List;

import com.feth.play.module.pa.PlayAuthenticate;

import models.User;
import models.WorkingGroup;
import play.Logger;
import play.mvc.*;
import play.i18n.Messages;
import play.data.Form;
import play.libs.Json;
import utils.GlobalData;
import utils.ResponseStatusBean;
import http.Headers;

@With(Headers.class)
public class WorkingGroups extends Controller {

	public static final Form<WorkingGroup> WORKING_GROUP_FORM = form(WorkingGroup.class);

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

	//@Security.Authenticated(Secured.class)
	public static Result findWorkingGroup(Long wGroupId){
		WorkingGroup workingGroup = WorkingGroup.read(wGroupId);
		return ok(Json.toJson(workingGroup));
	}

	@Security.Authenticated(Secured.class)
	public static Result deleteWorkingGroup(Long wGroupId){
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
			ResponseStatusBean responseBody = new ResponseStatusBean();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.GROUP_CREATE_MSG_ERROR,newWorkingGroupForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			WorkingGroup newWorkingGroup = newWorkingGroupForm.get();
			
			if(newWorkingGroup.getLang() == null) 
				newWorkingGroup.setLang(groupCreator.getLocale());

			ResponseStatusBean responseBody = new ResponseStatusBean();

			if(WorkingGroup.readByTitle(newWorkingGroup.getName()) > 0){
				Logger.info("Working Group already exists");
			}
			else{
				if (newWorkingGroup.getCreator() == null){
					newWorkingGroup.setCreator(groupCreator);
				}

				WorkingGroup.create(newWorkingGroup);

				// TODO: return URL of the new group
				Logger.info("Creating working group");
				Logger.debug("=> " + newWorkingGroupForm.toString());


				responseBody.setNewResourceId(newWorkingGroup.getGroupId());
				responseBody.setStatusMessage(Messages.get(
						GlobalData.GROUP_CREATE_MSG_SUCCESS,
						newWorkingGroup.getName()/*, groupCreator.getIdentifier()*/));
				responseBody.setNewResourceURL(GlobalData.GROUP_BASE_PATH+"/"+newWorkingGroup.getGroupId());
			}

			return ok(Json.toJson(responseBody));
		}
	}

	//@Security.Authenticated(Secured.class)
	public static Result updateWorkingGroup(Long groupId) {
		// 1. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<WorkingGroup> newWorkingGroupForm = WORKING_GROUP_FORM
				.bindFromRequest();

		if (newWorkingGroupForm.hasErrors()) {
			ResponseStatusBean responseBody = new ResponseStatusBean();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.GROUP_CREATE_MSG_ERROR,newWorkingGroupForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			WorkingGroup newWorkingGroup = newWorkingGroupForm.get();

			ResponseStatusBean responseBody = new ResponseStatusBean();

			if(WorkingGroup.readByTitle(newWorkingGroup.getName()) > 0){
				Logger.info("Working group already exists");
			}
			else{
				newWorkingGroup.setGroupId(groupId);
				newWorkingGroup.update();

				// TODO: return URL of the new group
				Logger.info("Creating working group");
				Logger.debug("=> " + newWorkingGroupForm.toString());


				responseBody.setNewResourceId(newWorkingGroup.getGroupId());
				responseBody.setStatusMessage(Messages.get(
						GlobalData.GROUP_CREATE_MSG_SUCCESS,
						newWorkingGroup.getName()/*, groupCreator.getIdentifier()*/));
				responseBody.setNewResourceURL(GlobalData.GROUP_BASE_PATH+"/"+newWorkingGroup.getGroupId());
			}

			return ok(Json.toJson(responseBody));
		}
	}

}
