package controllers;

import static play.data.Form.form;
import static play.libs.Json.toJson;

import com.feth.play.module.pa.user.AuthUserIdentity;
import enums.ResourceTypes;
import http.Headers;

import java.util.List;
import java.util.Date;
import java.util.Calendar;

import io.swagger.annotations.*;
import models.Resource;
import models.TokenAction;
import models.LinkedAccount;
import models.TokenAction.Type;
import models.User;
import models.UserProfile;
import models.misc.S3File;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.data.DynamicForm;
import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Security;
import play.mvc.Http.Session;
import play.mvc.Result;
import play.mvc.With;
import providers.MyLoginUsernamePasswordAuthUser;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyIdentity;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;
import providers.MyUsernamePasswordAuthUser;
import security.SecurityModelConstants;
import utils.GlobalData;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import views.html.*;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import enums.ResponseStatus;
/**
 * User Management operations, including authentication of users, 
 * merging/linking of accounts and reading information about user profiles
 * @author cdparra
 *
 */
@Api(value = "00 user: User Management", description = "User Management operations")
@With(Headers.class)
public class Users extends Controller {
	public static final Form<User> USER_FORM = form(User.class);
	public static final Form<UserProfile> USER_PROFILE_FORM = form(UserProfile.class);
	private static final Form<Accept> ACCEPT_FORM = form(Accept.class);
	private static final Form<PasswordChange> PASSWORD_CHANGE_FORM = form(PasswordChange.class);
	private static final Form<PasswordReset> PASSWORD_RESET_FORM = form(PasswordReset.class);
	private static final Form<MyIdentity> FORGOT_PASSWORD_FORM = form(MyIdentity.class);
	private static final Form<ResetConfig> FORGOT_CONFIG_PASSWORD_FORM = form(ResetConfig.class);
	private static final Form<PasswordForgotChange> PASSWORD_CHANGE_FORGOT_FORM = form(PasswordForgotChange.class);

	/**
	 * Authentication Auxiliary Classes
	 */
	public static class Accept {
		@Required
		@NonEmpty
		public Boolean accept;

		public Boolean getAccept() {
			return accept;
		}

		public void setAccept(Boolean accept) {
			this.accept = accept;
		}
	}

	public static class PasswordReset extends PasswordChange {

		public PasswordReset() {
		}

		public PasswordReset(final String token) {
			this.token = token;
		}

		public String token;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
	}

	/****************************************************************************************************
	 * Read-Only Endpoints
	 ***************************************************************************************************/

	/**
	 * GET       /api/user
	 * Get list of users
	 *
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = User.class, responseContainer = "List", produces = "application/json", value = "Get list of users", notes = "Get the full list of users. Only available to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Request has errors", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ @ApiImplicitParam(name = "SESSION_KEY", value = "User's auth key", dataType = "String", paramType = "header") })
	@Restrict({ @Group(GlobalData.ADMIN_ROLE) })
	public static Result getUsers() {
		List<User> users = User.findAll();
		return ok(Json.toJson(users));
	}

	/**
	 * GET       /api/user/:uid
	 * Get user with uid
	 *
	 * @param id
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = User.class, produces = "application/json", value = "Get a user by id", notes = "Get a user by id. Only available to ADMINS")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's auth key", dataType = "String", paramType = "header") })
	@Dynamic(value = "OnlyMeAndAdmin", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result getUser(Long id) {
		Logger.info("Obtaining user with id = " + id);
		User u = User.findByUserId(id);
		return ok(Json.toJson(u));
	}

	/**
	 * GET       /api/user/:uid/loggedin
	 * Get session user
	 *
	 * @param uid
	 * @return
	 */
	@ApiOperation(nickname = "loggedin", httpMethod = "GET", response = User.class, produces = "application/json", value = "Get session user", notes = "Get session user currently loggedin, as available in HTTP session")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's auth key", dataType = "String", paramType = "header") })
	@Dynamic(value = "OnlyMeAndAdmin", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result getCurrentUser(Long uid) {
		final User localUser = getLocalUser(session());
		if (localUser != null) {
			Logger.debug("Loggedin user: " + Json.toJson(localUser));
			return ok(Json.toJson(localUser));
		} else
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage(
					"No user logged in with session in this client", "")));
	}

	/**
	 * GET       /api/user/:uid/profile
	 * Get session user's profile
	 *
	 * @param uid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = UserProfile.class, produces = "application/json, text/html", value = "Get session user's profile", notes = "Get session user currently loggedin, as available in HTTP session")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's auth key", dataType = "String", paramType = "header") })
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result profile(Long uid) {
		final UserProfile userProfile = UserProfile.readByUserId(uid);
		if (userProfile != null) {
			if (request().getHeader("Content-Type") != null
					&& request().getHeader("Content-Type").equals("text/html"))
				return ok(profile.render(userProfile));
			else
				return ok(Json.toJson(userProfile));
		} else {
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage(
					"No user profile for user '" + uid + "'", "")));
		}
	}


	/**
	 * GET 	  /api/user/:uid/fbtoken/
	 * che
	 *
	 * @param uid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = LinkedAccount.class, produces = "application/json", value = "check if user has a Facebook access token", notes = " user with id :id")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Facebook Token not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's auth key", dataType = "String", paramType = "header") })
	@SubjectPresent
	public static Result checkFbToken(Long uid) {
		User user = User.findByUserId(uid);
		LinkedAccount linkedAccount = LinkedAccount.findByProviderKey(user, "social_ideation_facebook");
		if (linkedAccount != null) {
			return ok();
		} else {
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage(
					"No linkedAccount for user: " + uid, "")));
		}
	}
    


	/****************************************************************************************************
	 * CREATE Endpoints
	 ***************************************************************************************************/
	/****************************************************************************************************
	 * AUTHENTICATION Endpoints
	 ***************************************************************************************************/

	/**
	 * POST      /api/user/signup
	 *
	 * @return
	 */
	@ApiOperation(nickname = "signup", httpMethod = "POST", response = User.class, produces = "application/json", value = "Creates a new unverified user with an email and a password. Sends a verification email.")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Request has errors", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ @ApiImplicitParam(name = "signup_form", value = "User's signup form", dataType = "providers.MySignup", paramType = "body") })
	public static Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			// TODO: HTML rendered response return
			// badRequest(signup.render(filledForm));
			return badRequest(Json.toJson(TransferResponseStatus.badMessage(
					Messages.get("playauthenticate.signup.form-has-errors"),
					filledForm.errorsAsJson().toString())));
		} else{
			// Everything was filled correctly
			return MyUsernamePasswordAuthProvider.handleSignup(ctx());
		}
	}

	/**
	 * POST      /api/user/login
	 *
	 * @return
	 */
	@ApiOperation(nickname = "login", httpMethod = "POST", response = User.class, produces = "application/json", value = "Creates a new session key for the requesting user, if the system authenticates him/her.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class),
			@ApiResponse(code = 400, message = "Request has errors", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ @ApiImplicitParam(name = "login_form", value = "User's Login Credentials", dataType = "providers.MyLogin", paramType = "body") })
	public static Result doLogin() throws InstantiationException,
			IllegalAccessException {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		Logger.info("REQUEST: Login => " + ctx().request());
		final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
				.bindFromRequest();
		Logger.info("REQUEST: Login Form => " + filledForm.toString());
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			// TODO: return badRequest(login.render(filledForm));
			return badRequest(Json.toJson(TransferResponseStatus.badMessage(
					Messages.get("playauthenticate.login.form-has-errors"),
					filledForm.errorsAsJson().toString())));
		} else {
			// Everything was filled
			Result r = MyUsernamePasswordAuthProvider.handleLogin(ctx());
			return r;
		}

	}

	/**
	 * POST      /api/user/logout
	 *
	 * @return
	 */
	@ApiOperation(nickname = "logout", httpMethod = "POST", response = User.class, consumes = "application/json", value = "Expires the session key of the requesting user")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
			@ApiImplicitParam(name = "user_body", value = "Logout body must be empty JSON", dataType = "String", paramType = "body") })
	@Restrict(@Group(GlobalData.USER_ROLE))
	public static Result doLogout() {
		// TODO: modify to return HTML or JSON instead of redirect
		return com.feth.play.module.pa.controllers.Authenticate.logout();
	}

	/**
	 * POST	  /api/user/:uid/account/
	 * Stores a new facebook access token for user
	 *
	 * @param uid
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = LinkedAccount.class, produces = "application/json", value = "Stores a new facebook access token for user", notes = " user with id :id")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Request has errors", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's auth key", dataType = "String", paramType = "header"),
			@ApiImplicitParam(name = "userId", value = "Facebook user's id", dataType = "String", paramType = "body"),
			@ApiImplicitParam(name = "accessToken", value = "New access token from facebook", dataType = "String", paramType = "body"),
			@ApiImplicitParam(name = "expiration", value = "Expiration (in seconds) of the token", dataType = "String", paramType = "body") })
	@SubjectPresent
	public static Result createFbToken(Long uid) {
		User user = User.findByUserId(uid);
		LinkedAccount linkedAccount = new LinkedAccount();
		linkedAccount.setUser(user);
		DynamicForm form = Form.form().bindFromRequest();
		linkedAccount.setProviderUserId(form.get("userId"));
		linkedAccount.setProviderKey("social_ideation_facebook");
		linkedAccount.save();
		TokenAction tokenAction = TokenAction.create(TokenAction.Type.FACEBOOK_TOKEN, form.get("accessToken"), user);

		Calendar cal = Calendar.getInstance();
        Date created = cal.getTime();
        tokenAction.setCreated(created);
        cal.add(Calendar.SECOND, Integer.parseInt(form.get("expiration")));
        Date expires = cal.getTime();
        tokenAction.setExpires(expires);
		
		tokenAction.save();

		if (linkedAccount != null) {
			return ok();
		} else {
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage(
					"No linkedAccount for user: " + uid, "")));
		}
	}

	/****************************************************************************************************
	 * UPDATE Endpoints
	 ***************************************************************************************************/

	/**
	 * PUT       /api/user/:uid
	 * Update user information
	 *
	 * @param uid
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = TransferResponseStatus.class, produces = "application/json", value = "Update user information", notes = "Updates user information")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
			@ApiImplicitParam(name = "user", value = "User's updated information", dataType = "models.User", paramType = "body") })
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result updateUser(Long uid) {
		final Form<User> updatedUserForm = USER_FORM.bindFromRequest();
		if (updatedUserForm.hasErrors())
			return badRequest(Json.toJson(TransferResponseStatus
					.badMessage("Errors in form", updatedUserForm
							.errorsAsJson().toString())));
		else {
			User updatedUser = updatedUserForm.get();
			updatedUser.setUserId(uid);
			updatedUser.update();
			Logger.info("Updating User");
			Logger.debug("=> " + updatedUserForm.toString());

			Http.MultipartFormData body = request().body().asMultipartFormData();
			Http.MultipartFormData.FilePart uploadFilePart = null;
			if(body != null){
				uploadFilePart = body.getFile("profile_pic");
			}
			if (uploadFilePart != null) {

				try{
					Logger.info("Processing profile pic");
					S3File s3File = new S3File();
					s3File.name = uploadFilePart.getFilename();
					s3File.file = uploadFilePart.getFile();
					s3File.save();
					Resource resource = new Resource();
					resource.setUrl(s3File.getUrl());
					resource.setResourceType(ResourceTypes.PICTURE);
					resource.save();
					updatedUser.setProfilePic(resource);
					updatedUser.update();
				}catch(Exception e){
					return internalServerError(Json.toJson(new TransferResponseStatus(ResponseStatus.BADREQUEST,
							"Error updating user's picture")));
				}

			}

			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setNewResourceId(updatedUser.getUserId());
			responseBody.setStatusMessage("User " + updatedUser.getUserId()
					+ "updated successfully");
			responseBody.setNewResourceURL(routes.Users + "/"
					+ updatedUser.getUserId());
			return ok(Json.toJson(responseBody));
		}
	}

	/**
	 * PUT       /api/user/:uid/profile
	 * Update user's profile information
	 *
	 * @param uid
	 * @return
	 */
	@ApiOperation(nickname = "profile", httpMethod = "PUT", response = TransferResponseStatus.class, produces = "application/json", value = "Update user's profile information", notes = "Updates user's profile information")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
			@ApiImplicitParam(name = "user", value = "User's updated profile information", dataType = "models.UserProfile", paramType = "body") })
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result updateUserProfile(Long uid) {
		final Form<UserProfile> updatedUserForm = USER_PROFILE_FORM
				.bindFromRequest();
		if (updatedUserForm.hasErrors())
			return badRequest(Json.toJson(TransferResponseStatus
					.badMessage("Errors in form", updatedUserForm
							.errorsAsJson().toString())));
		else {
			UserProfile updatedUser = updatedUserForm.get();
			User user = User.read(uid);
			updatedUser.setUser(user);
			updatedUser.update();
			Logger.info("Updating User Profile");
			Logger.debug("=> " + updatedUserForm.toString());

			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setNewResourceId(updatedUser.getUser().getUserId());
			responseBody.setStatusMessage("User "
					+ updatedUser.getUser().getUserId()
					+ "updated successfully");
			responseBody.setNewResourceURL(routes.Users + "/"
					+ updatedUser.getUser().getUserId());
			return ok(Json.toJson(responseBody));
		}
	}

	/**
	 * POST      /api/user/:uid/profile
	 * Create user's profile
	 *
	 * @param uid
	 * @return
	 */
	@ApiOperation(nickname = "profile", httpMethod = "POST", response = UserProfile.class, produces = "application/json", value = "Create user's profile")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
			@ApiImplicitParam(name = "user", value = "User's new profile information", dataType = "models.UserProfile", paramType = "body") })
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result createUserProfile(Long uid) {
		final Form<UserProfile> newUserForm = USER_PROFILE_FORM
				.bindFromRequest();
		if (newUserForm.hasErrors())
			return badRequest(Json.toJson(TransferResponseStatus
					.badMessage("Errors in form", newUserForm
							.errorsAsJson().toString())));
		else {
			UserProfile newUserProfile = newUserForm.get();
			User user = User.read(uid);
			newUserProfile.setUser(user);
			newUserProfile.save();
			newUserProfile.refresh();
			Logger.info("Creating User Profile for User: "+user.getUserId());
			Logger.debug("=> " + newUserForm.toString());
			return ok(Json.toJson(newUserProfile));
		}
	}
	
	/**
	 * POST	  /api/user/:uid/fbtoken/
	 * Update user's facebook access token
	 *
	 * @param uid
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = LinkedAccount.class, produces = "application/json", value = "Update user's facebook access token", notes = " user with id :id")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Linked Account not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's auth key", dataType = "String", paramType = "header"),
			@ApiImplicitParam(name = "userId", value = "Facebook user's id", dataType = "String", paramType = "body"),
			@ApiImplicitParam(name = "accessToken", value = "New access token from facebook", dataType = "String", paramType = "body"),
			@ApiImplicitParam(name = "expiration", value = "Expiration (in seconds) of the token", dataType = "String", paramType = "body") })
	@SubjectPresent
	public static Result updateFbToken(Long uid) {
		User user = User.findByUserId(uid);
		LinkedAccount linkedAccount = LinkedAccount.findByProviderKey(user, "social_ideation_facebook");
		DynamicForm form = Form.form().bindFromRequest();
		linkedAccount.setProviderUserId(form.get("userId"));
		linkedAccount.update();
		TokenAction tokenAction = TokenAction.findByUserAndType(user, TokenAction.Type.FACEBOOK_TOKEN);
		tokenAction.setToken(form.get("accessToken"));

		Calendar cal = Calendar.getInstance();
        Date created = cal.getTime();
        tokenAction.setCreated(created);
        cal.add(Calendar.SECOND, Integer.parseInt(form.get("expiration")));
        Date expires = cal.getTime();
        tokenAction.setExpires(expires);

		tokenAction.update();

		if (linkedAccount != null) {
			return ok();
		} else {
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage(
					"No linkedAccount for user: " + uid, "")));
		}
	}

	/****************************************************************************************************
	 * DELETE Endpoints
	 ***************************************************************************************************/

	/**
	 * DELETE    /api/user/:uid
	 * Soft delete user
	 *
	 * @param id
	 * @return
	 */
	@ApiOperation(httpMethod = "DELETE", response = TransferResponseStatus.class, produces = "application/json", value = "Soft delete of an user", notes = "Soft delete of an user by simply deactivating it")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "OnlyMeAndAdmin", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result deleteUser(Long id) {
		Logger.info("Obtaining user with id = " + id);
		User u = User.findByUserId(id);
		TransferResponseStatus responseBody = new TransferResponseStatus();
		if (u != null) {
			u.setActive(false);
			u.update();
			responseBody.setStatusMessage("User " + u.getUserId()
					+ " deactivated with success");
			responseBody.setNewResourceURL(routes.Users + "/" + u.getUserId());
			return ok(Json.toJson(responseBody));
		} else {
			responseBody.setStatusMessage("User " + id + " not found");
			return notFound(Json.toJson(responseBody));
		}
	}

	/**
	 * DELETE    /api/user/:uid/force
	 * Force delete user
	 *
	 * @param id
	 * @return
	 */
	@ApiOperation(httpMethod = "DELETE", response = TransferResponseStatus.class, produces = "application/json", value = "Delete a user", notes = "Delete a user, but not his/her contributions")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uid", value = "User's ID", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "OnlyMeAndAdmin", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result deleteUserForce(Long id) {
		Logger.info("Obtaining user with id = " + id);
		User u = User.findByUserId(id);

		TransferResponseStatus responseBody = new TransferResponseStatus();

		if (u != null) {
			u.delete();
			responseBody.setStatusMessage("User " + id
					+ " deleted with success");
			return ok(Json.toJson(responseBody));
		} else {
			responseBody.setStatusMessage("User " + id + " not found");
			return notFound(Json.toJson(responseBody));
		}
	}

	/****************************************************************************************************
	 * USER Accounts management endpoints
	 ***************************************************************************************************/

	/**
	 * 	GET       /api/user/verify/:token
	 * Verify invitation token
	 *
	 * @param token
	 * @return
	 */
	@ApiOperation(nickname = "verify", httpMethod = "GET", response = TransferResponseStatus.class, produces = "application/json", value = "Verify invitation token")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Error in Request", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ @ApiImplicitParam(name = "token", value = "Verification token", dataType = "String", paramType = "path") })
	public static Result verify(final String token) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final TokenAction ta = tokenIsValid(token, Type.EMAIL_VERIFICATION);
		if (ta == null) {
			return badRequest(toJson(TransferResponseStatus.badMessage(
					Messages.get("playauthenticate.token.error.message"),
					"Token is null")));
			// TODO return badRequest(no_token_or_invalid.render());
		}
		final String email = ta.targetUser.getEmail();
		User.verify(ta.targetUser);
		flash(Application.FLASH_MESSAGE_KEY,
				Messages.get("playauthenticate.verify_email.success", email));
		// if (Application.getLocalUser(session()) != null) {
		// return redirect(routes.Application.index());
		// } else {
		// return ok();
		// }
		TokenAction.deleteByUser(ta.targetUser, Type.EMAIL_VERIFICATION);
		return ok(toJson(TransferResponseStatus.okMessage(
				Messages.get("playauthenticate.verify_email.success", email),
				"")));
	}

	/**
	 * 	GET       /api/user/link
	 *
	 * @return
	 */
	@ApiOperation(nickname = "link", httpMethod = "GET", produces = "text/html", value = "Returns a form to link external auth provider accounts")
	@ApiImplicitParams({ @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"), })
	public static Result link() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(link.render());
	}

	/**
	 * 	GET       /api/user/verify
	 *
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", value = "Gets page for email verification")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"), })
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result verifyEmail() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final User user = Users.getLocalUser(session());
		if (user.isEmailVerified()) {
			// E-Mail has been validated already
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.verify_email.error.already_validated"));
		} else if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
			flash(Application.FLASH_MESSAGE_KEY, Messages.get(
					"playauthenticate.verify_email.message.instructions_sent",
					user.getEmail()));
			MyUsernamePasswordAuthProvider.getProvider()
					.sendVerifyEmailMailingAfterSignup(user, ctx(),false);
		} else {
			flash(Application.FLASH_MESSAGE_KEY, Messages.get(
					"playauthenticate.verify_email.error.set_email_first",
					user.getEmail()));
		}
		return redirect(routes.Users.profile(user.getUserId()));
	}

	@ApiOperation(httpMethod = "POST", produces = "application/html", value = "Changes user's password")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
			@ApiImplicitParam(name = "Password Change Object", value = "User's updated password form", dataType = "controllers.PasswordChange", paramType = "body") })
	@Restrict(@Group(GlobalData.USER_ROLE))
	public static Result doChangePassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<PasswordChange> filledForm = PASSWORD_CHANGE_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(Json.toJson(Json
					.toJson(new TransferResponseStatus("Error processing request"))));
		} else {
			final User user = Users.getLocalUser(session());
			final String newPassword = filledForm.get().password;
			final String oldPassword = filledForm.get().oldPassword;
			MyUsernamePasswordAuthUser passwordOldUser = new MyUsernamePasswordAuthUser(oldPassword);
 			if (passwordOldUser.checkPassword(user.getAccountByProvider(MyUsernamePasswordAuthProvider.PROVIDER_KEY).getProviderUserId(),oldPassword)){
				user.changePassword(new MyUsernamePasswordAuthUser(newPassword),true);
				return ok(Json.toJson("ok"));
			}else{
				// User old password incorrect
				return badRequest(Json.toJson(Json
						.toJson(new TransferResponseStatus("Password provided incorrect"))));
			}
		}
	}

	@ApiOperation(httpMethod = "POST", produces = "application/html", value = "Changes user's password")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Password Forgot Change Object", value = "User's updated password form", dataType = "controllers.PasswordForgotChange", paramType = "body") })
	public static Result doChangeForgotPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<PasswordForgotChange> filledForm = PASSWORD_CHANGE_FORGOT_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(Json.toJson(Json
					.toJson(new TransferResponseStatus("Error processing request"))));
		} else {
			final TokenAction ta = tokenIsValid(filledForm.get().token, Type.PASSWORD_RESET);
			if (ta == null) {
				return badRequest(Json.toJson(Json
						.toJson(new TransferResponseStatus("Invalid token"))));
			}
			final User user = ta.getTargetUser();
			final String newPassword = filledForm.get().password;
			user.changePassword(new MyUsernamePasswordAuthUser(newPassword),true);
			TokenAction.deleteByUser(user, Type.PASSWORD_RESET);
			return ok(Json.toJson("ok"));
		}
	}

	@SubjectPresent
	public static Result askLink() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}
		return ok(ask_link.render(ACCEPT_FORM, u));
	}

	@SubjectPresent
	public static Result doLink() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}

		final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(ask_link.render(filledForm, u));
		} else {
			// User made a choice :)
			final boolean link = filledForm.get().accept;
			if (link) {
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.accounts.link.success"));
			}
			return PlayAuthenticate.link(ctx(), link);
		}
	}

	@SubjectPresent
	public static Result askMerge() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}

		// You could also get the local user object here via
		// User.findByAuthUserIdentity(newUser)
		return ok(ask_merge.render(ACCEPT_FORM, aUser, bUser));
	}

	@SubjectPresent
	public static Result doMerge() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to login
			return redirect(routes.Application.index());
		}

		final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to merge or not merge
			return badRequest(ask_merge.render(filledForm, aUser, bUser));
		} else {
			// User made a choice :)
			final boolean merge = filledForm.get().accept;
			if (merge) {
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.accounts.merge.success"));
			}
			return PlayAuthenticate.merge(ctx(), merge);
		}
	}

	public static Result unverified() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());

		// return the User
		return ok(toJson(Messages.get("playauthenticate.verify.email.cta")));
		// TODO return ok(unverified.render());
	}

	@ApiOperation(httpMethod = "POST", value = "Request a token to change forgot password")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found") })
	public static Result doForgotPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<ResetConfig> filledForm = FORGOT_CONFIG_PASSWORD_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill in his/her email
			return badRequest(Json.toJson(new TransferResponseStatus("Form has errors: " + filledForm.errorsAsJson())));
			// TODO return badRequest(password_forgot.render(filledForm));
		} else {
			// The email address given *BY AN UNKNWON PERSON* to the form - we
			// should find out if we actually have a user with this email
			// address and whether password login is enabled for him/her. Also
			// only send if the email address of the user has been verified.
			final String email = filledForm.get().email;
			final String configUrl = filledForm.get().configUrl;

			// We don't want to expose whether a given email address is signed
			// up, so just say an email has been sent, even though it might not
			// be true - that's protecting our user privacy.
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get(
							"playauthenticate.reset_password.message.instructions_sent",
							email));

			final User user = User.findByEmail(email);
			if (user != null) {
				// yep, we have a user with this email that is active - we do
				// not know if the user owning that account has requested this
				// reset, though.
				final MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider
						.getProvider();
				// User exists
				if (user.isEmailVerified()) {
					provider.sendPasswordResetMailing(user, ctx(),configUrl);
					// In case you actually want to let (the unknown person)
					// know whether a user was found/an email was sent, use,
					// change the flash message
				} else {
					// We need to change the message here, otherwise the user
					// does not understand whats going on - we should not verify
					// with the password reset, as a "bad" user could then sign
					// up with a fake email via OAuth and get it verified by an
					// a unsuspecting user that clicks the link.
					flash(Application.FLASH_MESSAGE_KEY,
							Messages.get("playauthenticate.reset_password.message.email_not_verified"));

					// You might want to re-send the verification email here...
					provider.sendVerifyEmailMailingAfterSignup(user, ctx(),true);
				}
				return ok(Json.toJson("ok"));
			}

			TransferResponseStatus response = new TransferResponseStatus();
			response.setResponseStatus(ResponseStatus.NODATA);
			response.setStatusMessage("User not found with email: "+email);
			return notFound(Json.toJson(response));
		}
	}

	/**
	 * Returns a token object if valid, null if not
	 * 
	 * @param token
	 * @param type
	 * @return
	 */
	public static TokenAction tokenIsValid(final String token,
			final Type type) {
		TokenAction ret = null;
		if (token != null && !token.trim().isEmpty()) {
			final TokenAction ta = TokenAction.findByToken(token, type);
			if (ta != null && ta.isValid()) {
				ret = ta;
			}
		}

		return ret;
	}

	public static Result resetPassword(final String token) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
		if (ta == null) {
			return badRequest();
			//return badRequest(no_token_or_invalid.render());
		}
		return ok();
		//return ok(password_reset.render(PASSWORD_RESET_FORM.fill(new PasswordReset(token))));
	}

	public static Result doResetPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest();
			//return badRequest(password_reset.render(filledForm));
		} else {
			final String token = filledForm.get().token;
			final String newPassword = filledForm.get().password;

			final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
			if (ta == null) {
				return badRequest();
				//return badRequest(no_token_or_invalid.render());
			}
			final User u = ta.targetUser;
			try {
				// Pass true for the second parameter if you want to
				// automatically create a password and the exception never to
				// happen
				u.resetPassword(new MyUsernamePasswordAuthUser(newPassword),
						false);
			} catch (final RuntimeException re) {
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.no_password_account"));
			}
			final boolean login = MyUsernamePasswordAuthProvider.getProvider()
					.isLoginAfterPasswordReset();
			if (login) {
				// automatically log in
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.success.auto_login"));

				return PlayAuthenticate.loginAndRedirect(ctx(),
						new MyLoginUsernamePasswordAuthUser(u.getEmail()));
			} else {
				// send the user to the login page
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.success.manual_login"));
			}
			return redirect(routes.Application.index());
			// TODO return redirect(routes.Application.login());
		}
	}

	public static Result exists() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(toJson(Messages.get("playauthenticate.user.exists.message")));
		// return ok();
		// TODO return ok(exists.render());
	}

	/****************************************************************************************************
	 * Auxiliary Operations
	 ***************************************************************************************************/
	public static User getLocalUser(final Session session) {
		final User localUser = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session));
		return localUser;
	}

	public static Result onLoginUserNotFound() {
		TransferResponseStatus response = new TransferResponseStatus();
		response.setResponseStatus(ResponseStatus.NODATA);
		response.setStatusMessage(Messages
				.get("playauthenticate.password.login.unknown_user_or_pw"));
		return notFound(Json.toJson(response));
		// return notFound(Messages
		// .get("playauthenticate.password.login.unknown_user_or_pw"));
	}

	public static Result oAuthDenied(final String providerKey) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		flash(Application.FLASH_ERROR_KEY,
				"You need to accept the OAuth connection in order to use this website!");
		return redirect(routes.Application.index());
	}

	public static Result redirectAfterLogout() {
		return ok("You have been logout!. If you were trying to login, there was a session already from your browser for another user.");
	}

}
