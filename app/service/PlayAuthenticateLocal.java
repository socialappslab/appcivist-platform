package service;

import static play.libs.Json.toJson;

import java.io.UnsupportedEncodingException;

import models.LinkedAccount;
import models.User;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.api.libs.Crypto;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Http.Session;
import play.mvc.Result;
import providers.LdapAuthProvider;
import providers.MyUsernamePasswordAuthProvider;

import com.avaje.ebean.Ebean;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.user.AuthUser;

import controllers.routes;
import enums.ResponseStatus;

public class PlayAuthenticateLocal extends PlayAuthenticate {

	private static final String SETTING_KEY_ACCOUNT_AUTO_LINK = "accountAutoLink";
	private static final String SETTING_KEY_ACCOUNT_AUTO_MERGE = "accountAutoMerge";

	public static final String USER_KEY = "pa.u.id";
	public static final String PROVIDER_KEY = "pa.p.id";
	public static final String EXPIRES_KEY = "pa.u.exp";
	public static final String SESSION_KEY_STRING = "SESSION_KEY";
	/**
	 * Deprecated by public static Result loginAndRedirect(final Context
	 * context, final AuthUser loginUser, Object payload)
	 * 
	 * Kept to allow reverse compatibility
	 * 
	 * @param context
	 * @param loginUser
	 * @return
	 */
	public static Result loginAndRedirect(final Context context,
			final AuthUser loginUser) {

		return loginAndRedirect(context, loginUser, null);
	}

	/**
	 * Login and redirect signs in the user and sends him back the user
	 * information along with the sessionKey. 
	 * 
	 * Payload is an object containing information about what was the original call 
	 * (signup or login) in order to allow signup with automatic login afterwards
	 * 
	 * @param context
	 * @param loginUser
	 * @param payload
	 * @return
	 */
	public static Result loginAndRedirect(final Context context,
			final AuthUser loginUser, Object payload) {

		String encoded = "";
		String signed = "";

		try {
			encoded = getEncodedUserKey(loginUser.expires(),loginUser.getProvider(),loginUser.getId());
			signed = getSignedUserKey(encoded);
		} catch (UnsupportedEncodingException e) {
		    TransferResponseStatus message = new TransferResponseStatus();
		    message.setResponseStatus(ResponseStatus.SERVERERROR);
		    message.setStatusMessage("Problem while creating the session token.");
		    message.setErrorTrace(e.getMessage());
			Logger.error(e.getMessage());
			return Controller.internalServerError(Json.toJson(message));
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("session generated: " + PlayAuthenticateLocal.SESSION_KEY_STRING+"=" + signed + "-"
					+ encoded);
		}
		if(loginUser.getProvider().equals("password")) {
			models.User user = models.User.findByEmail(loginUser.getId());

			if (payload != null && payload.toString().equals("SIGNUP")) {
				// send verification email
				// added to support login after signup
				Logger.debug("signing up a new user:");
				Logger.debug("--> id: " + loginUser.getId());
				Logger.debug("--> provider: " + loginUser.getProvider());
				Logger.debug("sending verification email:");
				Logger.debug("--> provider: " + loginUser.getProvider());
				MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider
						.getProvider();
				provider.sendVerifyEmailMailingAfterSignup(user, context,false);

			}
			user.setSessionKey(signed + "-" + encoded);
			return Controller.ok(toJson(user));
		}else{
			storeUser(context.session(), loginUser);
			models.User user = models.User.findByProviderAndKey(loginUser.getProvider(),loginUser.getId());
			user.setSessionKey(signed + "-" + encoded);
			return Controller.ok(toJson(user));
		}
	}

	public static String getEncodedUserKey(long expiration, String provider, String id) throws UnsupportedEncodingException{
		StringBuffer sb = new StringBuffer();

		if (expiration != AuthUser.NO_EXPIRATION) {
			sb.append(java.net.URLEncoder.encode(PlayAuthenticateLocal.EXPIRES_KEY, "UTF-8"));
			sb.append("=");
			sb.append(java.net.URLEncoder.encode(expiration+"", "UTF-8"));
			sb.append("&");
		}
		sb.append(java.net.URLEncoder.encode(PlayAuthenticateLocal.PROVIDER_KEY, "UTF-8"));
		sb.append("=");
		sb.append(java.net.URLEncoder.encode(provider, "UTF-8"));
		sb.append("&");
		sb.append(java.net.URLEncoder.encode(PlayAuthenticateLocal.USER_KEY, "UTF-8"));
		sb.append("=");
		sb.append(java.net.URLEncoder.encode(id, "UTF-8"));

		return sb.toString();
	}

	public static String getSignedUserKey(String encoded) {
		Crypto cryptoObject = play.Play.application().injector().instanceOf(Crypto.class);
		return cryptoObject.sign(encoded);
	}

	public static Result handleAuthentication(final String provider, final Context context, final Object payload) {
		final AuthProvider ap = getProvider(provider);

		if (ap == null) {
			// Provider wasn't found and/or user was fooling with our stuff -
			// tell him off:
			TransferResponseStatus response = new TransferResponseStatus(ResponseStatus.NOTAVAILABLE,Messages.get(
					"playauthenticate.core.exception.provider_not_found",
					provider));
			return Controller.notFound(toJson(response));
		}

		try {
			// Authenticate the Session Key of the request
			final Object o = ap.authenticate(context, payload);
			if (o instanceof String) {
				if ("NOT_FOUND".equals(o)) {
					TransferResponseStatus response = new TransferResponseStatus();
					response.setResponseStatus(ResponseStatus.NOTAVAILABLE);
					response.setStatusMessage(Messages.get("playauthenticate.password.login.unknown_user_or_pw"));
					return Controller.notFound(toJson(response));
					// In case redirection is needed again, uncomment the following and comment current return
					// return Controller.notFound(Messages.get("playauthenticate.password.login.unknown_user_or_pw"));
				} else if (routes.Users.unverified().url().equals(o)) {
					TransferResponseStatus response = new TransferResponseStatus();
					response.setResponseStatus(ResponseStatus.BADREQUEST);
					response.setStatusMessage(
					        Messages.get("playauthenticate.user.exists.message")+" "
                                    + Messages.get("playauthenticate.verify.email.cta"));
					return Controller.badRequest(toJson(response));
				} else if (routes.Users.exists().url().equals(o)) {
					TransferResponseStatus response = new TransferResponseStatus();
					response.setResponseStatus(ResponseStatus.BADREQUEST);
					response.setStatusMessage(Messages
							.get("playauthenticate.user.exists.message"));
					return Controller.badRequest(toJson(response));
				} else {
					return Controller.redirect((String) o);
				}
			} else if (o instanceof Result) {
				return (Result) o;
			} else if (o instanceof AuthUser) {
				Logger.info("Instance of AuthUser");
				final AuthUser newUser = (AuthUser) o;
				final Session session = context.session();

				/*
				 * We might want to do merging here:
				 *
				 * Adapted from:
				 * http://stackoverflow.com/questions/6666267/architecture
				 * -for-merging-multiple-user-accounts-together 1. The account
				 * is linked to a local account and no session cookie is present
				 * --> Login 2. The account is linked to a local account and a
				 * session cookie is present --> Merge 3. The account is not
				 * linked to a local account and no session cookie is present
				 * --> Signup 4. The account is not linked to a local account
				 * and a session cookie is present --> Linking Additional
				 * account
				 */

				// Get the user with which we are logged in - is null if we are
				// not logged in (does NOT check expiration)
				AuthUser oldUser = getUser(session);

				// checks if the user is logged in (also checks the expiration!)
				boolean isLoggedIn = isLoggedIn(session);

				Object oldIdentity = null;

				// check if local user still exists - it might have been
				// deactivated/deleted,
				// so this is a signup, not a link
				if (isLoggedIn) {
					Logger.info("Is logged in");
					oldIdentity = getUserService().getLocalIdentity(oldUser);
					isLoggedIn = oldIdentity != null;
					if (!isLoggedIn) {
						// if isLoggedIn is false here, then the local user has
						// been deleted/deactivated
						// so kill the session
						// TODO: REDIRECT TO THE ORIGIN URL
						logout(session);
						oldUser = null;
					}
				}

				//See if there is already an appcivist user with the linked account provider id that matches
				// the ldap username.
				Object loginIdentity = getUserService().getLocalIdentity(newUser);

				if(newUser instanceof LdapAuthProvider.LdapAuthUser && loginIdentity == null) {
					Logger.info("Sign in from a ldap user, linked account with ldap username not found");
					LdapAuthProvider.LdapAuthUser identityToSearch = new LdapAuthProvider.LdapAuthUser();
					LdapAuthProvider.LdapAuthUser ldapUser = (LdapAuthProvider.LdapAuthUser) newUser;
					identityToSearch.setId(ldapUser.getMail());
					loginIdentity = User.findByAuthUserIdentity(identityToSearch);
					//If (2) is success, let's update the provider id to be the ldap username (which is what
					// we actually use to sign in). This is in order to progressively cleanup the linked account
					// records of those who signed up already.
					if(loginIdentity != null) {
						Logger.info("Ldap linkend account with mail as id found, updating");
						LinkedAccount linkedAccount = LinkedAccount.findByProviderKey((User) loginIdentity, "ldap");
						linkedAccount.setProviderUserId(ldapUser.getId());
						linkedAccount.update();
						linkedAccount.refresh();
					}
				}

				final boolean isLinked = loginIdentity != null;

				final AuthUser loginUser;
				if (isLinked && !isLoggedIn) {
					// 1. -> Login
					loginUser = newUser;
					Logger.info("Is linked and not logged in");
				} else if (isLinked) {
					Logger.info("Is linked and logged in");
					// 2. -> Merge

					// merge the two identities and return the AuthUser we want
					// to use for the log in
					if (isAccountMergeEnabled()
							&& !loginIdentity.equals(oldIdentity)) {
						// account merge is enabled
						// and
						// The currently logged in user and the one to log in
						// are not the same, so shall we merge?

						if (isAccountAutoMerge()) {
							// Account auto merging is enabled
							loginUser = getUserService()
									.merge(newUser, oldUser);
						} else {
							// Account auto merging is disabled - forward user
							// to merge request page
							final Call c = getResolver().askMerge();
							if (c == null) {
								throw new RuntimeException(
										Messages.get(
												"playauthenticate.core.exception.merge.controller_undefined",
												SETTING_KEY_ACCOUNT_AUTO_MERGE));
							}
							storeMergeUser(newUser, session);
							// TODO solve this avoiding redirect
							return Controller.redirect(c);
						}
					} else {
						// the currently logged in user and the new login belong
						// to the same local user,
						// or Account merge is disabled, so just change the log
						// in to the new user
						loginUser = newUser;
					}

				} else if (!isLoggedIn) {
					Logger.info("Is not linked and not logged in");
					// 3. -> Signup
						Ebean.beginTransaction();
						loginUser = signupUser(newUser);
						Ebean.commitTransaction();
				} else {
					// !isLinked && isLoggedIn:

					// 4. -> Link additional
					if (isAccountAutoLink()) {
						// Account auto linking is enabled

						loginUser = getUserService().link(oldUser, newUser);
					} else {
						// Account auto linking is disabled - forward user to
						// link suggestion page
						final Call c = getResolver().askLink();
						if (c == null) {
							throw new RuntimeException(
									Messages.get(
											"playauthenticate.core.exception.link.controller_undefined",
											SETTING_KEY_ACCOUNT_AUTO_LINK));
						}
						storeLinkUser(newUser, session);
						// TODO change this avoiding redirect
						return Controller.redirect(c);
					}

				}

				return loginAndRedirect(context, loginUser, payload);
			} else {
				TransferResponseStatus response = new TransferResponseStatus();
				response.setResponseStatus(ResponseStatus.SERVERERROR);
				response.setStatusMessage("playauthenticate.core.exception.general");
				return Controller.internalServerError(toJson(response));
			}
		} catch (final AuthException e) {
			if(Ebean.currentTransaction() != null) {
				Ebean.endTransaction();
			}
			final Call c = getResolver().onException(e);
			if (c != null) {
				// TODO Solve avoiding redirects
				return Controller.redirect(c);
			} else {
				final String message = e.getMessage();
				if (message != null) {
					TransferResponseStatus response = new TransferResponseStatus();
					response.setResponseStatus(ResponseStatus.SERVERERROR);
					response.setStatusMessage(message);
					return Controller.internalServerError(toJson(response));
					// return Controller.internalServerError(message);
				} else {
					TransferResponseStatus response = new TransferResponseStatus();
					response.setResponseStatus(ResponseStatus.SERVERERROR);
					response.setStatusMessage("Internal server error");
					return Controller.internalServerError(toJson(response));
					// return Controller.internalServerError();
				}
			}
		} finally {
			if(Ebean.currentTransaction() != null) {
				Ebean.endTransaction();
			}
		}
	}
	
//	public static Result logout(final Session session) {
//		session.remove(USER_KEY);
//		session.remove(PROVIDER_KEY);
//		session.remove(EXPIRES_KEY);
//
//		
//		Call c = getResolver().afterLogout();
//		String fallback = SETTING_KEY_AFTER_LOGOUT_FALLBACK;
//		
//		return Controller.redirect(getUrl(c,fallback));
//	}
	
	private static AuthUser signupUser(final AuthUser u) throws AuthException {
		final AuthUser loginUser;
		final Object id = getUserService().save(u);
		if (id == null) {
			throw new AuthException(
					Messages.get("playauthenticate.core.exception.singupuser_failed"));
		}

		loginUser = u;
		return loginUser;
	}
	
}
