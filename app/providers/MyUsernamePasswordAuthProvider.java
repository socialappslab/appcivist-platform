package providers;

import com.avaje.ebean.Ebean;
import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import controllers.Users;
import controllers.routes;
import models.*;
import models.TokenAction.Type;
import models.transfer.AssemblyTransfer;
import models.transfer.InvitationTransfer;
import play.Application;
import play.Logger;
import play.Play;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Lang;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Call;
import play.mvc.Http.Context;
import play.mvc.Result;
import service.PlayAuthenticateLocal;
import utils.GlobalData;
import utils.GlobalDataConfigKeys;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static play.data.Form.form;

public class MyUsernamePasswordAuthProvider
		extends
		UsernamePasswordAuthProvider<
			String, 
			MyLoginUsernamePasswordAuthUser, 
			MyUsernamePasswordAuthUser, 
			MyUsernamePasswordAuthProvider.MyLogin, 
			MyUsernamePasswordAuthProvider.MySignup> {

	private static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL + "." + "verificationLink.secure";
	private static final String SETTING_KEY_PASSWORD_RESET_LINK_SECURE = SETTING_KEY_MAIL + "." + "passwordResetLink.secure";
	private static final String SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET = "loginAfterPasswordReset";

	private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";
	
	@Override
	protected List<String> neededSettingKeys() {
		final List<String> needed = new ArrayList<String>(
				super.neededSettingKeys());
		needed.add(SETTING_KEY_VERIFICATION_LINK_SECURE);
		needed.add(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		needed.add(SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
		return needed;
	}

	public static MyUsernamePasswordAuthProvider getProvider() {
		return (MyUsernamePasswordAuthProvider) PlayAuthenticate
				.getProvider(UsernamePasswordAuthProvider.PROVIDER_KEY);
	}

	public static class MyIdentity {

		public MyIdentity() {
		}

		public MyIdentity(final String email) {
			this.email = email;
		}

		//@Required
		@Email
		public String email;

	}


	/**
	 * To add new fields for signup or login, modify the forms and the class MyUserNameAuthUser
	 * @author cdparra
	 *
	 */
	
	public static class MyLogin extends MyIdentity implements com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.UsernamePassword {

		//@Required
		@MinLength(5)
		public String password;

		@Override
		public String getEmail() {
			return email;
		}

		@Override
		public String getPassword() {
			return password;
		}
	}

	public static class MySignup extends MyLogin {

		@Required
		@MinLength(5)
		private String repeatPassword;
		public String name;		
		public String lang; 
		public AssemblyTransfer newAssembly;
		public AssemblyTransfer existingAssembly;
		private UUID invitationToken;
		
		public String validate() {
			String error = null;
			if (password == null) {
				error = Messages.get("playauthenticate.password.signup.error.password_null");

			} else if (getRepeatPassword()==null) {
				error = Messages.get("playauthenticate.password.signup.error.password_repeat_null");
			} else if (!password.equals(getRepeatPassword())) {
				error = Messages.get("playauthenticate.password.signup.error.passwords_not_same");
			} else if (lang == null) {
				error = Messages.get("playauthenticate.password.signup.error.missing_lang");
			} else if (invitationToken != null) {
				TokenAction ta = Users.tokenIsValid(invitationToken.toString(), Type.MEMBERSHIP_INVITATION);
				if (ta == null) {
					error = Messages.get("playauthenticate.token.error.message","Invitation token is not valid");
				}
			}
			if(error!=null) Logger.debug(error);
			return error;
		}

		public String getRepeatPassword() {
			return repeatPassword;
		}

		public void setRepeatPassword(String repeatPassword) {
			this.repeatPassword = repeatPassword;
		}
		
		public void setLang(String lang) {
			this.lang=lang;
		}
		
		public String getLang() {
			return this.lang;
		}
		
		public AssemblyTransfer getNewAssembly() {
			return this.newAssembly;
		}
		
		public void setNewAssembly(AssemblyTransfer a) {
			this.newAssembly = a;
		}
		
		public UUID getInvitationToken() {
			return this.invitationToken;
		}
		
		public void setInvitationToken(UUID t) {
			this.invitationToken = t;
		}

		public AssemblyTransfer getExistingAssembly() {
			return existingAssembly;
		}

		public void setExistingAssembly(AssemblyTransfer existingAssembly) {
			this.existingAssembly = existingAssembly;
		}
	}

	public static final Form<MySignup> SIGNUP_FORM = form(MySignup.class);
	public static final Form<MyLogin> LOGIN_FORM = form(MyLogin.class);

	@Inject
	public MyUsernamePasswordAuthProvider(Application app) {
		super(app);
	}

	protected Form<MySignup> getSignupForm() {
		return SIGNUP_FORM;
	}

	protected Form<MyLogin> getLoginForm() {
		return LOGIN_FORM;
	}

	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.SignupResult signupUser(
			final MyUsernamePasswordAuthUser user) {
		final User u = User.findByUsernamePasswordIdentity(user);
		if (u != null) {
			if (u.getEmailVerified() != null && u.getEmailVerified()) {
			   // This user exists, has its email validated and is active
			   return SignupResult.USER_EXISTS;
			} else {
				// this user exists, is active but has not yet validated its
				// email
				return SignupResult.USER_EXISTS_UNVERIFIED;
			}
		}

		User newUser;
		try {
			newUser = User.createFromAuthUser(user);
			user.setUserId(newUser.getUserId());
		} catch (Exception e) {
			if(Ebean.currentTransaction() != null) {
				Logger.debug("Ending transaction");
				Ebean.endTransaction();
			}
			e.printStackTrace();
		}

		// TODO verify that the email is correct and valid
		// Usually the email should be verified before allowing login, however
		// if you return
		// return SignupResult.USER_CREATED;
		// then the user gets logged in directly
		// return SignupResult.USER_CREATED_UNVERIFIED;

		return SignupResult.USER_CREATED;
	}

	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.LoginResult loginUser(
			final MyLoginUsernamePasswordAuthUser authUser) {
		final User u = User.findByUsernamePasswordIdentity(authUser);
		if (u == null) {
			return LoginResult.NOT_FOUND;
		} else {
			// TODO every certain time, resend verification email
			// if (!u.isEmailValidated()) {
			// return LoginResult.USER_UNVERIFIED;
			// }
			//
			// {
			for (final LinkedAccount acc : u.getLinkedAccounts()) {
				if (getKey().equals(acc.getProviderKey())) {
					if (authUser.checkPassword(acc.getProviderUserId(),
							authUser.getPassword())) {
						// Password was correct
						return LoginResult.USER_LOGGED_IN;
					} else {
						// if you don't return here,
						// you would allow the user to have
						// multiple passwords defined
						// usually we don't want this
						return LoginResult.WRONG_PASSWORD;
					}
				}
			}
			return LoginResult.WRONG_PASSWORD;
			// }
		}
	}

	@Override
	protected Call userExists(final UsernamePasswordAuthUser authUser) {
		// find out how to return just json
		// return null;
		return routes.Users.exists();
	}

	@Override
	protected Call userUnverified(final UsernamePasswordAuthUser authUser) {
		// find out how to return just json
		// return null;
		return routes.Users.unverified();
	}

	@Override
	protected MyUsernamePasswordAuthUser buildSignupAuthUser(
			final MySignup signup, final Context ctx) {
		return new MyUsernamePasswordAuthUser(signup);
	}

	@Override
	protected MyLoginUsernamePasswordAuthUser buildLoginAuthUser(
			final MyLogin login, final Context ctx) {
		return new MyLoginUsernamePasswordAuthUser(login.getPassword(),
				login.getEmail());
	}

	@Override
	protected MyLoginUsernamePasswordAuthUser transformAuthUser(
			final MyUsernamePasswordAuthUser authUser, final Context context) {
		context.changeLang(authUser.getLanguage());
		return new MyLoginUsernamePasswordAuthUser(authUser.getEmail());
	}

	@Override
	protected String getVerifyEmailMailingSubject(
			final MyUsernamePasswordAuthUser user, final Context ctx) {
		ctx.changeLang(user.getLanguage());
		return Messages.get("playauthenticate.password.verify_signup.subject");
	}

	@Override
	protected String onLoginUserNotFound(final Context context) {
		// context.flash()
		// .put(controllers.Application.FLASH_ERROR_KEY,
		// Messages.get("playauthenticate.password.login.unknown_user_or_pw"));
		// return controllers.routes.Application.onLoginUserNotFound().url();
		return UsernamePasswordAuthProvider.LoginResult.NOT_FOUND.toString();
		// return super.onLoginUserNotFound(context);
	}

	@Override
	protected Body getVerifyEmailMailingBody(final String token,
			final MyUsernamePasswordAuthUser user, final Context ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		// TODO find out how to return just json
		// final String url = "";
		final String url = routes.Users.verify(token).absoluteURL(
				ctx.request(), isSecure);

		final String userLangCode = user.getLanguage();
		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = userLangCode !=null ? userLangCode : lang.code();
		ctx.changeLang(lang);
		
		final String html = getEmailTemplate(
				"views.html.account.signup.email.verify_email", langCode, url,
				token, user.getName(), user.getEmail());
		final String text = getEmailTemplate(
				"views.txt.account.signup.email.verify_email", langCode, url,
				token, user.getName(), user.getEmail());

		return new Body(text, html);
	}

	private static String generateToken() {
		return UUID.randomUUID().toString();
	}

	@Override
	protected String generateVerificationRecord(
			final MyUsernamePasswordAuthUser user) {
		return generateVerificationRecord(User.findByAuthUserIdentity(user));
	}

	protected String generateVerificationRecord(final User user) {
		final String token = generateToken();
		// Do database actions, etc.
		TokenAction.create(Type.EMAIL_VERIFICATION, token, user);
		return token;
	}

	protected String generatePasswordResetRecord(final User u) {
		final String token = generateToken();
		TokenAction.create(Type.PASSWORD_RESET, token, u);
		return token;
	}
	
	protected String generateNewMembershipInvitation(final User u) {
		final String token = generateToken();
		TokenAction.create(Type.MEMBERSHIP_INVITATION, token, u);
		return token;
	}

	protected String generateNewInvitation(final String email) {
		final String token = generateToken();
		final User u = null;
		TokenAction.create(Type.MEMBERSHIP_INVITATION, token, u);
		return token;
	}


	protected String generateNewMembershipRequest(final User u) {
		final String token = generateToken();
		TokenAction.create(Type.MEMBERSHIP_INVITATION, token, u);
		return token;
	}

	protected String getPasswordResetMailingSubject(final User user,
			final Context ctx) {
		return Messages.get("playauthenticate.password.reset_email.subject");
	}

	protected Body getPasswordResetMailingBody(final String token,
			final User user, final Context ctx, String configUrl) {

		final boolean isSecure = getConfiguration().getBoolean(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		String url = Play.application().configuration().getString(GlobalData.CONFIG_FORGOT_PASSWORD_URL_BASE);
		if (configUrl != null && !configUrl.equals("")) {
			url= configUrl;
		}else{
			if (url == null) {
				url = Play.application().configuration().getString("application.baseUrl")+"/api"+GlobalData.FORGOT_PASSWORD_DEFAULT_URL_BASE;
			}
		}
		url += token;
		
		final String userLangCode = user.getLanguage();
		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = userLangCode !=null ? userLangCode : lang.code();
		ctx.changeLang(langCode);
		String locale = langCode;

		if (locale.equals("it_IT") || locale.equals("it-IT")
				|| locale.equals("it")) {
			locale = "it";
		} else if (locale.equals("es_ES") || locale.equals("es-ES")
				|| locale.equals("es")) {
			locale = "es";
		} else if (locale.equals("en_EN") || locale.equals("en-EN")
				|| locale.equals("en")) {
			locale = "en";
		} else if (locale.equals("de_DE") || locale.equals("de-DE")
				|| locale.equals("de")) {
			locale = "de";
		} else if (locale.equals("fr_FR") || locale.equals("fr-FR")
				|| locale.equals("fr")) {
			locale = "fr";
		}

		final String html = getEmailTemplate(
				"views.html.account.email.password_reset", locale, url,
				token, user.getName(), user.getEmail());
		final String text = getEmailTemplate(
				"views.txt.account.email.password_reset", locale, url, token,
				user.getName(), user.getEmail());
		return new Body(text, html);
	}

	public void sendPasswordResetMailing(final User user, final Context ctx, final String configUrl) {
		final String token = generatePasswordResetRecord(user);
		final String subject = getPasswordResetMailingSubject(user, ctx);
		final Body body = getPasswordResetMailingBody(token, user, ctx, configUrl);
		F.Promise.promise(() -> {
			try {
				mailer.sendMail(subject, body, getEmailName(user));
			} catch (Exception e) {
				Logger.debug("Forgot password email not sent.");
				Logger.debug(e.getMessage());
			}
			return Optional.ofNullable(null);
		});
	}

	public boolean isLoginAfterPasswordReset() {
		return getConfiguration().getBoolean(
				SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
	}

	protected String getVerifyEmailMailingSubjectAfterSignup(final User user,
			final Context ctx) {
		return Messages.get("playauthenticate.password.verify_email.subject");
	}
	
	protected String getMembershipInvitationEmail(String target) {
		return Messages.get("membership.invitation.email.subject",target);
	}

	protected String getEmailTemplate(final String template,
			final String langCode, final String url, final String token,
			final String name, final String email) {
		
		Class<?> cls = null;
		String ret = null;

		try {
			cls = Class.forName(template + "_" + langCode);
		} catch (ClassNotFoundException e) {
			Logger.warn("Template: '"
					+ template
					+ "_"
					+ langCode
					+ "' was not found! Trying to use English fallback template instead.");
			try {
				cls = Class.forName(template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE);
			} catch (ClassNotFoundException e2) {
				Logger.error("Fallback template: '" + template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE
						+ "' was not found either!");
				Logger.error(e2.getMessage());
			}
		}
		if (cls != null) {
			Method htmlRender = null;
			try {
				htmlRender = cls.getMethod("render", String.class,
						String.class, String.class, String.class);
				ret = htmlRender.invoke(null, url, token, name, email)
						.toString();
			} catch (NoSuchMethodException e) {
				Logger.debug(e.getMessage());
			} catch (IllegalAccessException e) {
                Logger.debug(e.getMessage());
			} catch (InvocationTargetException e) {
                Logger.debug(e.getMessage());
			}
		}
		return ret;
	}

	protected Body getVerifyEmailMailingBodyAfterSignup(final String token,
			final User user, final Context ctx, boolean isFromForgot) {

		// final boolean isSecure = getConfiguration().getBoolean(
		// SETTING_KEY_VERIFICATION_LINK_SECURE);
		// TODO find out how to return just json
		// final String url = "";
		// final String url = routes.Signup.verify(token).absoluteURL(
		// ctx.request(), isSecure);
		String baseURL = Play.application().configuration()
				.getString("application.baseUrl");
		final String url = baseURL + routes.Users.verify(token).url();

		final String userLangCode = user.getLanguage();
		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = userLangCode !=null ? userLangCode : lang.code();
		ctx.changeLang(lang);
		String locale = langCode;

		if (Pattern.compile("it", Pattern.CASE_INSENSITIVE).matcher(locale).matches()) {
			locale = "it";
		} else if (Pattern.compile("es", Pattern.CASE_INSENSITIVE).matcher(locale).matches()) {
			locale = "es";
		} else if (Pattern.compile("en", Pattern.CASE_INSENSITIVE).matcher(locale).matches()) {
			locale = "en";
		} else if (Pattern.compile("de", Pattern.CASE_INSENSITIVE).matcher(locale).matches()) {
			locale = "de";
		} else if (Pattern.compile("fr", Pattern.CASE_INSENSITIVE).matcher(locale).matches()) {
			locale = "fr";
		} else if (Pattern.compile("pt", Pattern.CASE_INSENSITIVE).matcher(locale).matches()) {
			locale = "pt";
		}

		String name = user.getName();
		String email = user.getEmail();
		String templateHtml = "views.html.account.email.verify_email";
		String templateTxt = "views.txt.account.email.verify_email";
		if(isFromForgot){
			templateHtml = "views.html.account.email.verify_email_forgot";
			templateTxt = "views.txt.account.email.verify_email_forgot";
		}
		Logger.debug("Email template used: "+templateHtml);
		final String html = getEmailTemplate(templateHtml, locale, url, token,
				name, email);
		final String text = getEmailTemplate(
				templateTxt, locale, url, token,
				name, email);

		return new Body(text, html);
	}

	public void sendVerifyEmailMailingAfterSignup(final User user,
			final Context ctx, boolean isFromForgot) {

		final String subject = getVerifyEmailMailingSubjectAfterSignup(user, ctx);
		final String token = generateVerificationRecord(user);
		final Body body = getVerifyEmailMailingBodyAfterSignup(token, user, ctx,isFromForgot);
        F.Promise.promise(() -> {
            try {
                mailer.sendMail(subject, body, getEmailName(user));
            } catch (Exception e) {
                Logger.debug("Verification email not sent.");
                Logger.debug(e.getMessage());
            }
            return Optional.ofNullable(null);
        });
	}

	protected Body getMembershipInvitationEmailBody(final String token,
			final Membership m) {

		// final boolean isSecure = getConfiguration().getBoolean(
		// SETTING_KEY_VERIFICATION_LINK_SECURE);
		// TODO find out how to return just json
		// final String url = "";
		// final String url = routes.Signup.verify(token).absoluteURL(
		// ctx.request(), isSecure);
		String baseURL = Play.application().configuration()
				.getString("application.baseUrl");
		final String url = baseURL + routes.Memberships.verifyMembership(m.getMembershipId(),token).url();

		// TODO: use requests
//		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
//		final String langCode = lang.code();

		User user = m.getUser();
		String locale = user.getLanguage();
		
		// Checking if the language is in ISO format e.g., it-IT, it_IT
		String[] isoLocale = locale.split("-");
		if(isoLocale!=null && isoLocale.length>0)
			locale = isoLocale[0]; // we will use only the short code for lang = "it", "es", "en" 
		else {
			isoLocale = locale.split("_");
			if(isoLocale != null && isoLocale.length>0)
				locale = isoLocale[0];
		}
		
		// TODO: what we do with Users that don't have emails? 
		String name = user.getName();
		String email = user.getEmail();
		final String html = getEmailTemplate(
				"views.html.account.membership.invitation_email", locale, url, token,
				name, email);
		final String text = getEmailTemplate(
				"views.html.account.membership.invitation_email", locale, url, token,
				name, email);

		return new Body(text, html);
	}	
	
	protected Body getInvitationEmailBody(final String token, final Long id, final InvitationTransfer invitation, final String targetCollection) {
		String baseURL = Play.application().configuration().getString("application.uiUrl");
		//final String url = baseURL + routes.Memberships.verifyMembership(m.getMembershipId(),token).url();
		final String url = baseURL + "/invitation/"+token+"/"
				+targetCollection.toLowerCase()
				+"/"+id+"?moderator="+invitation.getModerator()
				+"&coordinator="+invitation.getCoordinator();

		String locale = "en";
		
		// Checking if the language is in ISO format e.g., it-IT, it_IT
		String[] isoLocale = locale.split("-");
		if(isoLocale!=null && isoLocale.length>0)
			locale = isoLocale[0]; // we will use only the short code for lang = "it", "es", "en" 
		else {
			isoLocale = locale.split("_");
			if(isoLocale != null && isoLocale.length>0)
				locale = isoLocale[0];
		}
		
		// TODO: what we do with Users that don't have emails? 
		String name = invitation.getEmail();
		String email = invitation.getEmail();
		final String html = getEmailTemplate(
				"views.html.account.membership.invitation_email", locale, url, token,
				name, email);
		final String text = getEmailTemplate(
				"views.html.account.membership.invitation_email", locale, url, token,
				name, email);

		return new Body(text, html);
	}	
	
	public void sendMembershipInvitationEmail(final Membership m, String targetCollection) {
		final String subject = getMembershipInvitationEmail(targetCollection+"("+m.getTargetAssembly()+")");
		final String token = generateNewMembershipInvitation(m.getUser());
		final Body body = getMembershipInvitationEmailBody(token, m);
        F.Promise.promise(() -> {
            try {
                mailer.sendMail(subject, body, getEmailName(m.getUser()));
            } catch (Exception e) {
                Logger.debug("Membership invitation email not sent.");
                Logger.debug(e.getMessage());
            }
            return Optional.ofNullable(null);
        });
    }

	public static void sendNewsletterEmail(String subject, String mail, String template) {
		final Body body = new Body(null, template);

		Mailer mailer = Mailer.getCustomMailer(PlayAuthenticate.getConfiguration().getConfig("password").getConfig(
				"mail"));
		Logger.info("Sending " + body.getHtml());
        F.Promise.promise(() -> {
            try {
                mailer.sendMail(subject, body, mail);
            } catch (Exception e) {
                Logger.debug("Newsletter email not sent.");
                Logger.debug(e.getMessage());
            }
            return Optional.ofNullable(null);
        });
	}
	
	public void sendInvitationByEmail(final InvitationTransfer invitation, String targetCollection, Long id) {
		final String subject = getMembershipInvitationEmail(targetCollection+"("+invitation.getTargetId()+")");
		final String token = generateNewInvitation(invitation.getEmail());
		final Body body = getInvitationEmailBody(token, id, invitation, targetCollection);
		mailer.sendMail(subject, body, invitation.getEmail());
        F.Promise.promise(() -> {
            try {
            } catch (Exception e) {
            }
            return Optional.ofNullable(null);
        });
    }
	
	public void sendInvitationByEmail(final MembershipInvitation invitation, String invitationEmail, String invitationSubject) {
		final String subject = invitationSubject;
		final Body body = new Body(invitationEmail,invitationEmail);
		mailer.sendMail(subject, body, invitation.getEmail());
	}

	public void sendLdapFakeMailEmail(String url, String username, Assembly assembly) {
		String mail = null;
		for(Config config: assembly.getConfigs()) {
			if(config.getKey().equals(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_ADMIN_MAIL)) {
				mail = config.getValue();
			}
		}
		if(mail != null) {
			final String subject = "[APPCIVIST] New ldap user created without email";
			final Body body = new Body("The user " + username + " was created without having an email. The email " + url + " was used");
			mailer.sendMail(subject, body, mail);
		} else {
			Logger.warn("None admin ldap mail configured");
		}
	}
	
	public void sendInvitationByEmail(final MembershipInvitation invitation, String invitationEmailTxt,String invitationEmailHTML, String invitationSubject) {
		final String subject = invitationSubject;
		final Body body = new Body(invitationEmailTxt,invitationEmailHTML);
		mailer.sendMail(subject, body, invitation.getEmail());
	}

	public void sendZipContributionFile(String url, String email) {
        Logger.debug("Sending zip URL ("+url+") by email");
        final Body body = new Body("Your contribution files is ready, you can download here: "+ url);
		mailer.sendMail("Contribution File", body, email);
	}

	private String getEmailName(final User user) {
		return getEmailName(user.getEmail(), user.getName());
	}

	public static Result handleLogin(final Context ctx) {
		return PlayAuthenticateLocal.handleAuthentication(PROVIDER_KEY, ctx, getEnum("LOGIN"));
	}

	public static Result handleSignup(final Context ctx) {
		return PlayAuthenticateLocal.handleAuthentication(PROVIDER_KEY, ctx, getEnum("SIGNUP"));
	}

	private static Object getEnum(String enumName) {
		Object kase = null;
		try {
			Class<?> clazz = UsernamePasswordAuthProvider.class
					.getDeclaredClasses()[3];
			Field field = clazz.getDeclaredField(enumName);
			field.setAccessible(true);
			kase = field.get(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return kase;
	}
	

}
