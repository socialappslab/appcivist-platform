package controllers;

import static play.data.Form.form;
import static play.libs.Json.toJson;
import http.Headers;

import java.util.List;

import models.TokenAction;
import models.TokenAction.Type;
import models.User;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.Session;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import providers.MyLoginUsernamePasswordAuthUser;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyIdentity;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;
import providers.MyUsernamePasswordAuthUser;

import com.feth.play.module.pa.PlayAuthenticate;

import enums.ResponseStatus;

@With(Headers.class)
public class Users extends Controller {

	/****************************************************************************************************
	 * Read-Only Endpoints
	 ***************************************************************************************************/
	@Security.Authenticated(Secured.class)
	public static Result getUsers() {
		List<User> users = User.findAll();

		if (request().accepts("application/xml")) {
			return ok("<errorMessage>Not Implemented Yet</errorMessage>");
		} else {
			return ok(Json.toJson(users));
		}
	}

	// TODO
	public static Result getLoginForm() {
		// return ok(views.html.login.render());
		return notFound("TODO: Not Implemented Yet");
	}

	@Security.Authenticated(Secured.class)
	public static Result getUser(Long id) {
		Logger.info("Obtaining user with id = "+id);
		User u = User.findByUserId(id);
		return ok(Json.toJson(u));
	}

	@Security.Authenticated(Secured.class)
	public static Result getCurrentUser(Long id) {
		final User localUser = getLocalUser(session());
		// UserBean bean = PlayDozerMapper.getInstance().map(localUser,
		// UserBean.class); // Example if we choose to use Dozer
		if (localUser.getUserId().equals(id)) {
			return ok(Json.toJson(localUser));
		} else {
			return badRequest("User " + id
					+ " is not the user registered in for this client");
		}
	}

	/****************************************************************************************************
	 * CREATE Endpoints
	 ***************************************************************************************************/
	public static Result doSignup() {
		
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			// return badRequest(signup.render(filledForm));
			TransferResponseStatus response = new TransferResponseStatus();
			response.setResponseStatus(ResponseStatus.BADREQUEST);
			response.setStatusMessage("play.authenticate.filledFromHasErrors:"
					+ filledForm.errorsAsJson());
			return badRequest(Json.toJson(response));
		} else {
			// Everything was filled correctly
			// Do something with your part of the form before handling the user
			// signup
			return MyUsernamePasswordAuthProvider.handleSignup(ctx());
		}
	}

	/****************************************************************************************************
	 * UPDATE Endpoints
	 ***************************************************************************************************/
	public static Result updateUser(Long uid) {
		return notFound("TODO: Not Implemented Yet");
	}

	/****************************************************************************************************
	 * DELETE Endpoints
	 ***************************************************************************************************/
	public static Result deleteUser(Long uid) {
		return notFound("TODO: Not Implemented Yet");
	}

	public static Result deleteUserForce(Long uid) {
		return notFound("TODO: Not Implemented Yet");
	}

	/****************************************************************************************************
	 * AUTHENTICATION Endpoints
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 ***************************************************************************************************/
	public static Result doLogin() throws InstantiationException, IllegalAccessException {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		Logger.info("REQUEST: Login => " + ctx().request());
		Logger.info("REQUEST: Login JSON => " + ctx().request().body().asJson());
		final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM.bindFromRequest();
		Logger.info("REQUEST: Login Form => "+filledForm.toString());
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			// return badRequest(login.render(filledForm));
			return badRequest("Errors in Data: "+filledForm.errorsAsJson());
		} else {
			// Everything was filled
			return MyUsernamePasswordAuthProvider.handleLogin(ctx());
		}
		
	}
	
	public static Result doLogout() {
		return notFound("TODO: Not Implemented Yet");
	}

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

	public static class PasswordChange {
		@MinLength(5)
		@Required
		public String password;

		@MinLength(5)
		@Required
		public String repeatPassword;

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getRepeatPassword() {
			return repeatPassword;
		}

		public void setRepeatPassword(String repeatPassword) {
			this.repeatPassword = repeatPassword;
		}

		public String validate() {
			if (password == null || !password.equals(repeatPassword)) {
				return Messages
						.get("playauthenticate.change_password.error.passwords_not_same");
			}
			return null;
		}
	}

	// private static final Form<Accept> ACCEPT_FORM = form(Accept.class);
	// private static final Form<Account.PasswordChange> PASSWORD_CHANGE_FORM =
	// form(Account.PasswordChange.class);
	//
	// @SubjectPresent
	// public static Result link() {
	// com.feth.play.module.pa.controllers.Authenticate.noCache(response());
	// return ok(link.render());
	// }
	//
	// @Restrict(@Group(MyRoles.MEMBER))
	// public static Result verifyEmail() {
	// com.feth.play.module.pa.controllers.Authenticate.noCache(response());
	// final User user = Application.getLocalUser(session());
	// if (user.emailValidated) {
	// // E-Mail has been validated already
	// flash(Application.FLASH_MESSAGE_KEY,
	// Messages.get("playauthenticate.verify_email.error.already_validated"));
	// } else if (user.email != null && !user.email.trim().isEmpty()) {
	// flash(Application.FLASH_MESSAGE_KEY, Messages.get(
	// "playauthenticate.verify_email.message.instructions_sent",
	// user.email));
	// MyUsernamePasswordAuthProvider.getProvider()
	// .sendVerifyEmailMailingAfterSignup(user, ctx());
	// } else {
	// flash(Application.FLASH_MESSAGE_KEY, Messages.get(
	// "playauthenticate.verify_email.error.set_email_first",
	// user.email));
	// }
	// return redirect(routes.Application.profile());
	// }
	//
	// @Restrict(@Group(Application.USER_ROLE))
	// public static Result changePassword() {
	// com.feth.play.module.pa.controllers.Authenticate.noCache(response());
	// final User u = Application.getLocalUser(session());
	//
	// if (!u.emailValidated) {
	// return ok(unverified.render());
	// } else {
	// return ok(password_change.render(PASSWORD_CHANGE_FORM));
	// }
	// }
	//
	// @Restrict(@Group(Application.USER_ROLE))
	// public static Result doChangePassword() {
	// com.feth.play.module.pa.controllers.Authenticate.noCache(response());
	// final Form<Account.PasswordChange> filledForm = PASSWORD_CHANGE_FORM
	// .bindFromRequest();
	// if (filledForm.hasErrors()) {
	// // User did not select whether to link or not link
	// return badRequest(password_change.render(filledForm));
	// } else {
	// final User user = Application.getLocalUser(session());
	// final String newPassword = filledForm.get().password;
	// user.changePassword(new MyUsernamePasswordAuthUser(newPassword),
	// true);
	// flash(Application.FLASH_MESSAGE_KEY,
	// Messages.get("playauthenticate.change_password.success"));
	// return redirect(routes.Application.profile());
	// }
	// }
	//
	// @SubjectPresent
	// public static Result askLink() {
	// com.feth.play.module.pa.controllers.Authenticate.noCache(response());
	// final AuthUser u = PlayAuthenticate.getLinkUser(session());
	// if (u == null) {
	// // account to link could not be found, silently redirect to login
	// return redirect(routes.Application.index());
	// }
	// return ok(ask_link.render(ACCEPT_FORM, u));
	// }
	//
	// @SubjectPresent
	// public static Result doLink() {
	// com.feth.play.module.pa.controllers.Authenticate.noCache(response());
	// final AuthUser u = PlayAuthenticate.getLinkUser(session());
	// if (u == null) {
	// // account to link could not be found, silently redirect to login
	// return redirect(routes.Application.index());
	// }
	//
	// final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
	// if (filledForm.hasErrors()) {
	// // User did not select whether to link or not link
	// return badRequest(ask_link.render(filledForm, u));
	// } else {
	// // User made a choice :)
	// final boolean link = filledForm.get().accept;
	// if (link) {
	// flash(Application.FLASH_MESSAGE_KEY,
	// Messages.get("playauthenticate.accounts.link.success"));
	// }
	// return PlayAuthenticate.link(ctx(), link);
	// }
	// }
	//
	// @SubjectPresent
	// public static Result askMerge() {
	// com.feth.play.module.pa.controllers.Authenticate.noCache(response());
	// // this is the currently logged in user
	// final AuthUser aUser = PlayAuthenticate.getUser(session());
	//
	// // this is the user that was selected for a login
	// final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
	// if (bUser == null) {
	// // user to merge with could not be found, silently redirect to login
	// return redirect(routes.Application.index());
	// }
	//
	// // You could also get the local user object here via
	// // User.findByAuthUserIdentity(newUser)
	// return ok(ask_merge.render(ACCEPT_FORM, aUser, bUser));
	// }
	//
	// @SubjectPresent
	// public static Result doMerge() {
	// com.feth.play.module.pa.controllers.Authenticate.noCache(response());
	// // this is the currently logged in user
	// final AuthUser aUser = PlayAuthenticate.getUser(session());
	//
	// // this is the user that was selected for a login
	// final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
	// if (bUser == null) {
	// // user to merge with could not be found, silently redirect to login
	// return redirect(routes.Application.index());
	// }
	//
	// final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
	// if (filledForm.hasErrors()) {
	// // User did not select whether to merge or not merge
	// return badRequest(ask_merge.render(filledForm, aUser, bUser));
	// } else {
	// // User made a choice :)
	// final boolean merge = filledForm.get().accept;
	// if (merge) {
	// flash(Application.FLASH_MESSAGE_KEY,
	// Messages.get("playauthenticate.accounts.merge.success"));
	// }
	// return PlayAuthenticate.merge(ctx(), merge);
	// }
	// }
	
	
	public static class PasswordReset extends Users.PasswordChange {

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

	private static final Form<PasswordReset> PASSWORD_RESET_FORM = form(PasswordReset.class);

	public static Result unverified() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
			
		// return the User
		return ok(toJson(Messages.get("playauthenticate.verify.email.cta"))); 
//TODO		return ok(unverified.render());
	}

	private static final Form<MyIdentity> FORGOT_PASSWORD_FORM = form(MyIdentity.class);

	public static Result forgotPassword(final String email) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		@SuppressWarnings("unused")
		Form<MyIdentity> form = FORGOT_PASSWORD_FORM;
		if (email != null && !email.trim().isEmpty()) {
			form = FORGOT_PASSWORD_FORM.fill(new MyIdentity(email));
		}
		return ok();
//TODO		return ok(password_forgot.render(form));
	}

	public static Result doForgotPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MyIdentity> filledForm = FORGOT_PASSWORD_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill in his/her email
			return badRequest();
//TODO			return badRequest(password_forgot.render(filledForm));
		} else {
			// The email address given *BY AN UNKNWON PERSON* to the form - we
			// should find out if we actually have a user with this email
			// address and whether password login is enabled for him/her. Also
			// only send if the email address of the user has been verified.
			final String email = filledForm.get().email;

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
					provider.sendPasswordResetMailing(user, ctx());
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
					provider.sendVerifyEmailMailingAfterSignup(user, ctx());
				}
			}

			return redirect(routes.Application.index());
		}
	}

	/**
	 * Returns a token object if valid, null if not
	 * 
	 * @param token
	 * @param type
	 * @return
	 */
	protected static TokenAction tokenIsValid(final String token, final Type type) {
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
//TODO			return badRequest(no_token_or_invalid.render());
		}
			return ok();
//TODO		return ok(password_reset.render(PASSWORD_RESET_FORM
//				.fill(new PasswordReset(token))));
	}

	public static Result doResetPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest();
//TODO			return badRequest(password_reset.render(filledForm));
		} else {
			final String token = filledForm.get().token;
			final String newPassword = filledForm.get().password;

			final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
			if (ta == null) {
				return badRequest();
//TODO				return badRequest(no_token_or_invalid.render());
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
//TODO			return redirect(routes.Application.login());
		}
	}

	public static Result exists() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(toJson(Messages.get("playauthenticate.user.exists.message")));
//		return ok();
//TODO		return ok(exists.render());
	}

	public static Result verify(final String token) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final TokenAction ta = tokenIsValid(token, Type.EMAIL_VERIFICATION);
		if (ta == null) {
			return badRequest(toJson(Messages.get("playauthenticate.token.error.message")));
//TODO			return badRequest(no_token_or_invalid.render());
		}
		final String email = ta.targetUser.getEmail();
		User.verify(ta.targetUser);;
//		flash(Application.FLASH_MESSAGE_KEY,
//				Messages.get("playauthenticate.verify_email.success", email));
//		if (Application.getLocalUser(session()) != null) {
//			return redirect(routes.Application.index());
//		} else {
//			return ok();
			return ok(toJson(Messages.get("playauthenticate.verify_email.success", email)));
//TODO			return redirect(routes.Application.login());
//		}
	}

	
	
	

	/****************************************************************************************************
	 * Auxiliary Operations
	 ***************************************************************************************************/
	public static User getLocalUser(final Session session) {
		final User localUser = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session));
		return localUser;
	}

	public static Result getUserByEmail(String e) {
		User u = User.findByEmail(e);

		if (request().accepts("application/xml")) {
			return ok("<errorMessage>Not Implemented Yet</errorMessage>");
		} else {
			return ok(Json.toJson(u));
		}
	}

	public static Result getByEmail(String email) {
		return getUserByEmail(email);
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

}
