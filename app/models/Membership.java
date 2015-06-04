package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import models.TokenAction.Type;
import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import enums.MembershipRoles;
import enums.MembershipStatus;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "MEMBERSHIP_TYPE")
public abstract class Membership extends AppCivistBaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4939869903730586228L;
	@Id
	@GeneratedValue
	private Long membershipId;
	private Long expiration;
	private MembershipStatus status;
	
	@ManyToOne
	private User creator;

	@JsonIgnore
	@ManyToOne
	private User user;

	@ManyToMany(cascade = CascadeType.ALL)
	private List<Role> roles = new ArrayList<Role>();

	@Column(name = "MEMBERSHIP_TYPE", insertable = false, updatable = false)
	private String membershipType;

	public static Model.Finder<Long, Membership> find = new Model.Finder<Long, Membership>(
			Long.class, Membership.class);

	public Membership(Long expiration, MembershipStatus status, User creator,
			User user, List<Role> roles, String membershipType) {
		super();
		this.expiration = expiration;
		this.status = status;
		this.creator = creator;
		this.user = user;
		this.roles = roles;
		this.membershipType = membershipType;
	}

	public Membership() {
		super();
	}

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
		membership.saveManyToManyAssociations("roles");
		membership.refresh();
		return membership;
	}

	public static Membership createObject(Membership membership) {
		membership.save();
		membership.saveManyToManyAssociations("roles");
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
