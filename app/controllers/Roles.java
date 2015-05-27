package controllers;

import static play.data.Form.form;

import java.util.List;

import com.feth.play.module.pa.PlayAuthenticate;

import models.User;
import models.Role;
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
public class Roles extends Controller {

	public static final Form<Role> ROLE_FORM = form(Role.class);
	
	/**
	 * Return the full list of roles
	 * 
	 * @return Roles list
	 */
	@Security.Authenticated(Secured.class)
	public static Result findRoles() {
		List<Role> roles = Role.findAll();
		return ok(Json.toJson(roles));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result findRole(Long roleId) {
		Role role = Role.read(roleId);
		return ok(Json.toJson(role));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result findRoleToDelete(Long roleId) {
		Role.delete(roleId);
		return ok();
	}
	
	@Security.Authenticated(Secured.class)
	public static Result createRole() {
		// 1. obtaining the user of the requestor
		User roleCreator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));

		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Role> newRoleForm = ROLE_FORM.bindFromRequest();

		if (newRoleForm.hasErrors()) {
			ResponseStatusBean responseBody = new ResponseStatusBean();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.GROUP_CREATE_MSG_ERROR,newRoleForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Role newRole = newRoleForm.get();
			
			// setting default values (TODO: maybe we should create a dedicated method for this in each model)
			newRole.setCreator(roleCreator);
			
			if(newRole.getLang() == null) 
				newRole.setLang(roleCreator.getLocale());

			if( Role.readByTitle(newRole.getName()) != null ){
				Logger.info("Role already exists");
			}
			else{
				Role.create(newRole);
			}

			Logger.info("Creating new role");
			Logger.debug("=> " + newRoleForm.toString());

			ResponseStatusBean responseBody = new ResponseStatusBean();
			responseBody.setNewResourceId(newRole.getRoleId());
			responseBody.setStatusMessage(Messages.get(
					GlobalData.GROUP_CREATE_MSG_SUCCESS,
					newRole.getName(), roleCreator.getIdentifier()));
			responseBody.setNewResourceURL(GlobalData.GROUP_BASE_PATH+"/"+newRole.getRoleId());
			return ok(Json.toJson(responseBody));
		}
	}

}
