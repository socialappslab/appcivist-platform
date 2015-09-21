package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

import com.avaje.ebean.Query;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import models.TokenAction.Type;
import enums.ManagementTypes;
import enums.MembershipStatus;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "MEMBERSHIP_TYPE")
@JsonInclude(Include.NON_EMPTY)
public class Membership extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	private Long membershipId;
	private Long expiration;
	private MembershipStatus status;

	@ManyToOne
	private User creator;

	@ManyToOne
	private User user;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "MEMBERSHIP_ROLE", joinColumns = { @JoinColumn(name = "membership_membership_id", referencedColumnName = "membership_id", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "role_role_id", referencedColumnName = "role_id", nullable = false) })
	private List<SecurityRole> roles = new ArrayList<SecurityRole>();

	@Column(name = "MEMBERSHIP_TYPE", insertable = false, updatable = false)
	private String membershipType;

	@Column(name = "ASSEMBLY_ASSEMBLY_ID", insertable = false, updatable = false)
	private Assembly targetAssembly;

	@Column(name = "WORKING_GROUP_GROUP_ID", insertable = false, updatable = false)
	private WorkingGroup targetGroup;

	private UUID targetUuid;
	
	public static Finder<Long, Membership> find = new Finder<>(Membership.class);

	public Membership(Long expiration, MembershipStatus status, User creator,
			User user, List<SecurityRole> roles, String membershipType) {
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

	public List<SecurityRole> getRoles() {
		return roles;
	}

	public void setRoles(List<SecurityRole> roles) {
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

	public Assembly getTargetAssembly() {
		return targetAssembly;
	}

	public void setTargetAssembly(Assembly targetAssembly) {
		this.targetAssembly = targetAssembly;
	}

	public WorkingGroup getTargetGroup() {
		return targetGroup;
	}

	public void setTargetGroup(WorkingGroup targetGroup) {
		this.targetGroup = targetGroup;
	}

	public UUID getTargetUuid() {
		return targetUuid;
	}

	public void setTargetUuid(UUID targetUuid) {
		this.targetUuid = targetUuid;
	}

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
	 * TODO: CHECK this method to fix the bug of null results
	 * 
	 * @param m
	 * @return
	 */
	public static boolean checkIfExists(Membership gm) {
		Membership m1 = find.where().eq("creator", gm.getCreator())
				.eq("user", gm.getUser())
				.eq("targetAssembly", gm.getTargetAssembly()).findUnique();

		Membership m2 = find.where().eq("creator", gm.getCreator())
				.eq("user", gm.getUser())
				.eq("targetGroup", gm.getTargetGroup()).findUnique();

		return m1 != null || m2 != null;
	}

	public static Boolean userCanInvite(User user, WorkingGroup workingGroup) {
		Boolean userCanInvite = false;

		ManagementTypes roleForInvitations = workingGroup.getManagementType();
		Membership m = MembershipGroup.findByUserAndGroup(user, workingGroup);

		if (roleForInvitations != null && m != null) {
			for (SecurityRole userRole : m.getRoles()) {
				userCanInvite = roleForInvitations.toString().toUpperCase()
						.equals(userRole.getName().toUpperCase());
				if (userCanInvite)
					return userCanInvite;
			}
		}
		return userCanInvite;
	}

	// TODO: rethink the whole role rules using http://deadbolt.ws/#/java-docs
	public static Boolean userCanInvite(User user, Assembly assembly) {
		Boolean userCanInvite = false;

		ManagementTypes roleForInvitations = assembly.getProfile().getManagementType();
		Membership m = MembershipAssembly.findByUserAndAssembly(user, assembly);

		if (roleForInvitations != null && m != null) {
			for (SecurityRole userRole : m.getRoles()) {
				userCanInvite = roleForInvitations.toString().toUpperCase()
						.equals(userRole.getName().toUpperCase());
				if(userCanInvite)
					return userCanInvite;
			}
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

	public static List<Membership> findByUser(User u, String membershipType) {
		Query<Membership> q = find.where().eq("user",u).query();
		if (!membershipType.isEmpty()) 
			q = q.where().eq("membershipType", membershipType).query();
		List<Membership> membs = q.findList();
		return membs;
	}

	public static List<Membership> findByUserAndTargetUuid(User u, UUID targetUuid) {
		return find.where().eq("user",u).eq("targetUuid", targetUuid).findList();
	}
}
