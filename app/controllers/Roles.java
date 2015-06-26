package controllers;

import static play.data.Form.form;

import java.util.List;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import models.User;
import models.SecurityRole;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.mvc.*;
import play.i18n.Messages;
import play.data.Form;
import play.libs.Json;
import utils.GlobalData;
import http.Headers;

@With(Headers.class)
public class Roles extends Controller {

	public static final Form<SecurityRole> ROLE_FORM = form(SecurityRole.class);
	
	/**
	 * Return the full list of roles
	 * 
	 * @return Roles list
	 */
	@Restrict({ @Group("ADMIN") })
	public static Result findRoles() {
		List<SecurityRole> roles = SecurityRole.findAll();
		return ok(Json.toJson(roles));
	}
	
	@Restrict({ @Group("ADMIN") })
	public static Result findRole(Long roleId) {
		SecurityRole role = SecurityRole.read(roleId);
		return ok(Json.toJson(role));
	}
	
	@Restrict({ @Group("ADMIN") })
	public static Result deleteRole(Long roleId) {
		SecurityRole.delete(roleId);
		return ok();
	}
	
	@Restrict({ @Group("ADMIN") })
	public static Result createRole() {
		// 2. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<SecurityRole> newRoleForm = ROLE_FORM.bindFromRequest();
		
		if (newRoleForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.ROLE_CREATE_MSG_ERROR,newRoleForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			
			SecurityRole newRole = newRoleForm.get();
			
			TransferResponseStatus responseBody = new TransferResponseStatus();

			if( SecurityRole.findByName(newRole.getName()) != null ){
				Logger.info("Role already exists");
			}
			else{

				SecurityRole.create(newRole);
				Logger.info("Creating new role");
				Logger.debug("=> " + newRoleForm.toString());

				responseBody.setNewResourceId(newRole.getRoleId());
				responseBody.setStatusMessage(Messages.get(
						GlobalData.ROLE_CREATE_MSG_SUCCESS,
						newRole.getName()/*, roleCreator.getIdentifier()*/));
				responseBody.setNewResourceURL(GlobalData.ROLE_BASE_PATH+"/"+newRole.getRoleId());
			}

			return ok(Json.toJson(responseBody));
		}
	}

	@Restrict({ @Group("ADMIN") })
	public static Result updateRole(Long roleId) {
		// 1. read the new role data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<SecurityRole> newRoleForm = ROLE_FORM.bindFromRequest();

		if (newRoleForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.ROLE_CREATE_MSG_ERROR,newRoleForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {

			SecurityRole newRole = newRoleForm.get();

			TransferResponseStatus responseBody = new TransferResponseStatus();

			if( SecurityRole.findByName(newRole.getName()) != null ){
				Logger.info("Role already exists");
			}
			else {
				newRole.setRoleId(roleId);
				newRole.update();
				Logger.info("Creating new role");
				Logger.debug("=> " + newRoleForm.toString());

				responseBody.setNewResourceId(newRole.getRoleId());
				responseBody.setStatusMessage(Messages.get(
						GlobalData.ROLE_CREATE_MSG_SUCCESS,
						newRole.getName()/*, roleCreator.getIdentifier()*/));
				responseBody.setNewResourceURL(GlobalData.ROLE_BASE_PATH + "/" + newRole.getRoleId());
			}

			return ok(Json.toJson(responseBody));
		}
	}

	
	/**
	 * 
	 */
	public static Boolean userHasRole(User user, String role) {
		// TODO check if user has the role
		return true;
	}
	
	public static Boolean userHasRoleInMembership(User user, String role) {
		// TODO check if user has the role in the membership
		return true;
	}
	
}
