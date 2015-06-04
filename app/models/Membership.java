package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.pa.PlayAuthenticate;

import enums.MembershipRoles;
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

import models.TokenAction.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "MEMBERSHIP_TYPE")
public abstract class Membership extends AppCivistBaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4939869903730586228L;
	private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";

	@Id
	@GeneratedValue
	private Long membershipId;
	private Long expiration;
	private MembershipStatus status;
	private User creator;

	@JsonIgnore
	@ManyToOne
	private User user;

	@JsonIgnore
	@ManyToMany(cascade = CascadeType.ALL)
	private List<Role> roles = new ArrayList<Role>();

	@Column(name = "MEMBERSHIP_TYPE", insertable = false, updatable = false)
	private String membershipType;

	public static Model.Finder<Long, Membership> find = new Model.Finder<Long, Membership>(
			Long.class, Membership.class);

	/*
	 * Getters and Setters
	 */

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

	public Long getExpiration() {
		return expiration;
	}

	public void setExpiration(Long expiration) {
		this.expiration = expiration;
	}

	public MembershipStatus getStatus() {
		return status;
	}

	public void setStatus(MembershipStatus status) {
		this.status = status;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public String getMembershipType() {
		return membershipType;
	}

	public void setMembershipType(String membershipType) {
		this.membershipType = membershipType;
	}

	/*
	 * Basic Queries
	 */

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

	/*
	 * Other queries
	 */

	/**
	 * Check if membership for this user to the group/assembly already exists
	 * 
	 * @param m
	 * @return
	 */
	abstract public boolean checkIfExists();

	public static Boolean userCanInvite(User user, WorkingGroup workingGroup) {
		Boolean userCanInvite = false;

		MembershipRoles roleForInvitations = workingGroup.getMembershipRole();
		Membership m = GroupMembership.findByUserAndGroup(user, workingGroup);

		if (roleForInvitations != null) {
			for (Role userRole : m.getRoles()) {
				userCanInvite = roleForInvitations.toString().toUpperCase() == userRole
						.getName().toUpperCase();
			}
		} else {
			userCanInvite = m != null;
		}
		return userCanInvite;
	}

	// TODO: rethink the whole role rules using http://deadbolt.ws/#/java-docs
	public static Boolean userCanInvite(User user, Assembly assembly) {
		Boolean userCanInvite = false;

		MembershipRoles roleForInvitations = assembly.getMembershipRole();
		Membership m = AssemblyMembership.findByUserAndAssembly(user, assembly);

		if (roleForInvitations != null) {
			for (Role userRole : m.getRoles()) {
				userCanInvite = roleForInvitations.toString().toUpperCase() == userRole
						.getName().toUpperCase();
			}
		} else {
			userCanInvite = m != null;
		}
		return userCanInvite;
	}

	public static void verify(Long id, User targetUser) {
		Membership m = Membership.read(id);
		m.setStatus(MembershipStatus.ACCEPTED);
		m.update();
		m.refresh();
		// user.update(unverified.getId());
		TokenAction.deleteByUser(targetUser, Type.MEMBERSHIP_INVITATION);
	}

}
