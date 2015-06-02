package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.pa.PlayAuthenticate;
import enums.MembershipStatus;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Http;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthUser;

import javax.persistence.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="MEMBERSHIP_TYPE")
public abstract class Membership extends AppCivistBaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4939869903730586228L;
    private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";
	
    @Id
    @GeneratedValue
    private Long membershipId;
    private Date expiration;
    private MembershipStatus status;
    private User creator;

    @JsonIgnore
    @ManyToOne
    private User user;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.ALL)
    private List<Role> roles = new ArrayList<Role>();

    public static Model.Finder<Long, Membership> find = new Model.Finder<Long, Membership>(
            Long.class, Membership.class);

    public static Membership read(Long membershipId) {
        return find.ref(membershipId);
    }

    public static List<Membership> findAll() {
        return find.all();
    }

    public static Membership create(Membership membership) {
        membership.save();
        membership.refresh();
        return membership;
    }

    public static Membership createObject(Membership membership) {
        membership.save();
        return membership;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }

/*******************Creating and sending token*********************************/

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public static String generateVerificationRecord(final User user) {
        final String token = generateToken();
        // Do database actions, etc.
        TokenAction.create(TokenAction.Type.EMAIL_VERIFICATION, token, user);
        return token;
    }
/*
    protected Mailer.Mail.Body getVerifyEmailMailingBody(final String token, final User user) {

        // final boolean isSecure = getConfiguration().getBoolean(
        // SETTING_KEY_VERIFICATION_LINK_SECURE);
        // TODO find out how to return just json
        // final String url = "";
        // final String url = routes.Signup.verify(token).absoluteURL(

        String baseURL = Play.application().configuration()
                .getString("application.baseUrl");
        final String url = baseURL + routes.Users.verify(token).url();

        final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
        final String langCode = lang.code();

        String locale = langCode;

        if (user.getLocale() != null) {
            locale = user.getLocale();
        }

        if (locale.equals("it_IT") || locale.equals("it-IT")
                || locale.equals("it")) {
            locale = "it";
        } else if (locale.equals("es_ES") || locale.equals("es-ES")
                || locale.equals("es")) {
            locale = "es";
        } else if (locale.equals("en_EN") || locale.equals("en-EN")
                || locale.equals("en")) {
            locale = "en";
        }

        String name = user.getName() + " " + user.getName();
        String email = user.getEmail();
        final String html = getEmailTemplate(
                "views.html.account.membership.invitation_email", "en", "", token,
                name, email);
        final String text = getEmailTemplate(
                "views.txt.account.membership.invitation_email", "en", "", token,
                name, email);

        return new Mailer.Mail.Body(text, html);
    }

    protected String getEmailTemplate(final String template,
                                      final String langCode, final String url, final String token,
                                      final String name, final String email) {
        Class<?> cls = null;
        String ret = null;
        String locale = "";

        if (langCode.equals("it_IT") || langCode.equals("it-IT")) {
            locale = "it";
        } else if (langCode.equals("es_ES") || langCode.equals("es-ES")) {
            locale = "es";
        } else {
            locale = langCode;
        }

        try {
            cls = Class.forName(template + "_" + locale);
        } catch (ClassNotFoundException e) {
            Logger.warn("Template: '"
                    + template
                    + "_"
                    + locale
                    + "' was not found! Trying to use English fallback template instead.");
        }
        if (cls == null) {
            try {
                cls = Class.forName(template + "_"
                        + EMAIL_TEMPLATE_FALLBACK_LANGUAGE);
            } catch (ClassNotFoundException e) {
                Logger.error("Fallback template: '" + template + "_"
                        + EMAIL_TEMPLATE_FALLBACK_LANGUAGE
                        + "' was not found either!");
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
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public void sendVerifyEmailMailing(final User user) {

        final String subject = Messages.get("membership.invitation.email.message");
        final String token = generateVerificationRecord(user);
        final Mailer.Mail.Body body = getVerifyEmailMailingBody(token, user);
        MyUsernamePasswordAuthProvider.getMailer().sendMail(subject, body, getEmailName(user));
    }*/

    private String getEmailName(final User user) {
        return getEmailName(user.getEmail(), user.getName());
    }

    protected String getEmailName(final String email, final String name) {
        return Mailer.getEmailName(email, name);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User User) {
        this.user = User;
    }
    
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public void setStatus(MembershipStatus status) {
        this.status = status;
    }

    public List<Role> getRole() {
        return roles;
    }

    public void setRole(List<Role> roles) {
        this.roles = roles;
    }
}
