package models;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import models.TokenAction.Type;

import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ManagementTypes;
import enums.MembershipStatus;
import enums.MembershipTypes;
import exceptions.MembershipCreationException;
import models.misc.Views;
import play.Logger;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "MEMBERSHIP_TYPE")
@JsonInclude(Include.NON_EMPTY)
@Where(clause="removed=false")
@ApiModel(value="Membership", description="Model representing membership of users in Assemblies or Working Groups")
public class Membership extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	private Long membershipId;
	@JsonView(Views.Public.class)
	private Long expiration;
	@JsonView(Views.Public.class)
	@Enumerated(EnumType.STRING)
	private MembershipStatus status;

	@ManyToOne
	@JsonIgnore
	private User creator;

	@ManyToOne
	private User user;

	public static abstract class AuthorsVisibleMixin {
		@JsonView(Views.Public.class)
		@JsonIgnore(false)
		private User creator;
		@JsonView(Views.Public.class)
		@JsonIgnore(false)
		private User user;

	}

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable( name = "membership_role", 
			joinColumns = { @JoinColumn(name = "membership_membership_id", referencedColumnName = "membership_id", insertable=true, updatable=true) }, 
			inverseJoinColumns = { @JoinColumn(name = "role_role_id", referencedColumnName = "role_id", insertable=true, updatable=true)})
	private List<SecurityRole> roles = new ArrayList<SecurityRole>();

	@JsonView(Views.Public.class)
	@Column(name = "MEMBERSHIP_TYPE", insertable = false, updatable = false)
	private String membershipType;

	@ManyToOne
	@JoinColumn(name = "assembly_assembly_id")
	//@Column(name = "ASSEMBLY_ASSEMBLY_ID", insertable = false, updatable = false)
	@JsonView(Views.Public.class)
	private Assembly targetAssembly;

	//@Column(name = "WORKING_GROUP_GROUP_ID", insertable = false, updatable = false)
	@ManyToOne
	@JoinColumn(name = "working_group_group_id")
	@JsonView(Views.Public.class)
	private WorkingGroup targetGroup;

	@Transient
	private Long targetGroupId;

	@Transient
	private List<Long> targetGroupAssemblies;
	
	@Transient
	private Long targetAssemblyId;

	
	private UUID targetUuid;
	
	@Transient 
	private String invitationToken; 
	
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

	public Long getTargetGroupId() {
		return targetGroup != null ? targetGroup.getGroupId() : null ;
	}

	public void setTargetGroupId(Long targetGroupId) {
		this.targetGroupId = targetGroupId;
	}

	public Long getTargetAssemblyId() {
		return targetAssembly != null ? targetAssembly.getAssemblyId() : null ;
	}

	public UUID getTargetUuid() {
		return targetUuid;
	}

	public void setTargetUuid(UUID targetUuid) {
		this.targetUuid = targetUuid;
	}

	public String getInvitationToken() {
		return invitationToken;
	}

	public void setInvitationToken(String invitationToken) {
		this.invitationToken = invitationToken;
	}

	public static Membership read(Long membershipId) {
		return find.ref(membershipId);
	}

	public static List<Membership> findAll() {
		return find.all();
	}

	public static Membership create(Membership membership) throws MembershipCreationException {
		if (!membership.alreadyExists()) {
			membership.save();
			membership.refresh();
			return membership;
		} else {
			throw new MembershipCreationException("Membership already exists");
		}
	}

	private boolean alreadyExists() {
		if (this.targetAssembly!=null) {
			return find.where().eq("targetAssembly", this.targetAssembly)
					.eq("user", this.user).findList().size() > 0;	
		} else if (this.targetGroup!=null){
			return find.where().eq("targetGroup", this.targetGroup)
						.eq("user", this.user).findList().size() > 0;
		} else {
			return false;
		}
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
		Membership m1 = null;
		if (gm.getTargetAssembly()!=null) {
			m1 = MembershipAssembly.findByUserAndAssembly(gm.getUser(), gm.getTargetAssembly());
		}
		Membership m2 = null;
		if (gm.getTargetGroup()!=null) {
			m2 = MembershipGroup.findByUserAndGroup(gm.getUser(), gm.getTargetGroup());;
		}
		
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

	public static List<Membership> findByUserAndAssembly(User u, Integer assemblyId) {
		Query<Membership> q = find.where().eq("user",u).eq("targetAssembly.assemblyId", assemblyId).query();
		List<Membership> membs = q.findList();
		return membs;
	}

	public static List<Membership> findByUserAndTargetUuid(User u, UUID targetUuid) {
		return find.where().eq("user",u).eq("targetUuid", targetUuid).findList();
	}

	public List<SecurityRole> getRolesFilteredByName(String roleName) {
		return this.roles.stream()
				.filter(p-> p.getName().equals(roleName))
				.collect(Collectors.toList());
	}
	
	public List<SecurityRole> filterByRoleName(String name) {
		return this.roles.stream().filter(p -> p.getName().toString().equals(name)).collect(Collectors.toList());
	}

	public static Boolean checkIfExistsByEmailAndId(String email, Long targetId, MembershipTypes targetType) {
		User u = User.findByEmail(email);
		if (u==null) {
			return false;
		} 
		Assembly a = null;
		WorkingGroup wg = null;
		if (targetType!=null && targetType.equals(MembershipTypes.ASSEMBLY)) {
			a = Assembly.read(targetId);
			if (a==null) {
				return false;
			}
		} else if (targetType!=null && targetType.equals(MembershipTypes.GROUP)) {
			wg = WorkingGroup.read(targetId);
			if (wg==null) {
				return false;
			}
		}
		Membership tempMem = new Membership();
		tempMem.setUser(u);
		if (a!=null) {
			tempMem.setTargetAssembly(a);
		} else if (wg!=null) {
			tempMem.setTargetGroup(wg);
		}
		
		return Membership.checkIfExists(tempMem);
	}

	public List<Membership> getGroupsMemberships(){

		List<Membership> membershipsInResourceSpace = new ArrayList<>();
		if (this.membershipType.equals("ASSEMBLY")) {
			membershipsInResourceSpace = find.where().
					eq("targetGroup.containingSpaces.resourceSpaceId",
							((MembershipAssembly) this).getAssembly().getResourcesResourceSpaceId()).eq("user", this.user).findList();

		}

		return membershipsInResourceSpace;

	}
}
