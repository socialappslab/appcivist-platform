package models;

import io.swagger.annotations.ApiModel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.data.format.Formats;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name="Token_Action")
@ApiModel(value="TokenAction", description="Generated tokens for verificatoin and other security actions")
public class TokenAction extends Model {

	public enum Type {
		@EnumValue("EV")
		EMAIL_VERIFICATION,

		@EnumValue("PR")
		PASSWORD_RESET, 
		
		@EnumValue("MI")
		MEMBERSHIP_INVITATION, 

		@EnumValue("MR")
		MEMBERSHIP_REQUEST
	}

	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds Defaults to one week
	 */
	private final static long VERIFICATION_TIME = 7 * 24 * 3600;

	@Id
	@GeneratedValue
	@Column(name="token_id")
	public Long id;

	@Column(unique = true)
	public String token;

	@ManyToOne
	@JoinColumn(name="user_id")
	public User targetUser;

	@OneToOne
	@JoinColumn(name="membership_invitation_id")
	@JsonBackReference
	public MembershipInvitation targetInvitation;
	
	public Type type;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date created;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date expires;

	public static final Finder<Long, TokenAction> find = new Finder<Long, TokenAction>(TokenAction.class);

	public static TokenAction findByToken(final String token, final Type type) {
		return find.where().eq("token", token).eq("type", type).findUnique();
	}

	public static void deleteByUser(final User u, final Type type) {
		Ebean.delete(find.where().eq("targetUser.userId", u.getUserId())
				.eq("type", type).findIterate());
	}

	public boolean isValid() {
		return this.expires.after(new Date());
	}

	public static TokenAction create(final Type type, final String token,
			final User targetUser) {
		final TokenAction ua = new TokenAction();
		ua.targetUser = targetUser;
		ua.token = token;
		ua.type = type;
		final Date created = new Date();
		ua.created = created;
		ua.expires = new Date(created.getTime() + VERIFICATION_TIME * 1000);
		ua.save();
		return ua;
	}

	public static TokenAction create(final Type type, final String token,
			final MembershipInvitation targetInvitation) {
		final TokenAction ua = new TokenAction();
		ua.targetInvitation = targetInvitation;
		ua.token = token;
		ua.type = type;
		final Date created = new Date();
		ua.created = created;
		ua.expires = new Date(created.getTime() + VERIFICATION_TIME * 1000);
		ua.save();
		return ua;
	}

	public User getTargetUser() {
		return targetUser;
	}

	public void setTargetUser(User targetUser) {
		this.targetUser = targetUser;
	}
}
