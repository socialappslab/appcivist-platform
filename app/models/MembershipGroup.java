package models;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Query;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.MembershipStatus;

@Entity
@DiscriminatorValue("GROUP")
@JsonInclude(Include.NON_EMPTY)
public class MembershipGroup extends Membership {
	
	@ManyToOne
	@JsonIgnoreProperties({"creator", "members"})
	private WorkingGroup workingGroup;

	public MembershipGroup() {
		super();
	}
	public MembershipGroup(Long expiration, MembershipStatus status,
			User creator, User user, List<SecurityRole> roles, String membershipType) {
		super(expiration, status, creator, user, roles, membershipType);
	}
	
	public MembershipGroup(Long expiration, MembershipStatus status,
			User creator, User user, List<SecurityRole> roles, String membershipType, 
			WorkingGroup workingGroup) {
		super(expiration, status, creator, user, roles, membershipType);
		this.workingGroup = workingGroup;
	}
	
	/*
	 * Getters and Setters
	 */
	
	public WorkingGroup getWorkingGroup() {
		return workingGroup;
	}

	public void setWorkingGroup(WorkingGroup workingGroup) {
		this.workingGroup = workingGroup;
	}

	/**
	 * Check if membership for this user to the group/assembly already exists
	 * 
	 * @param m
	 * @return
	 */
	public static boolean checkIfExists(Membership m) {
		MembershipGroup gm = (MembershipGroup) m;
		return find.where().eq("creator", gm.getCreator())
				.eq("user", gm.getUser())
				.eq("workingGroup", gm.getWorkingGroup()).findUnique() != null;
	}

	/**
	 * Find a membership of the user in the target collection (group or
	 * assembly)
	 * 
	 * @param user
	 * @param target
	 * @return
	 */
	public static Membership findByUserAndGroup(User user, WorkingGroup target) {
		return find.where().eq("user", user).eq("workingGroup", target)
				.findUnique();
	}
	/**
	 * Find a membership record for userId and groupId
	 * 
	 * @param userId
	 * @param groupId
	 * @return
	 */
	public static Membership findByUserAndGroupId(Long userId, Long groupId) {
		return find.where().eq("user.userId", userId).eq("workingGroup.groupId", groupId)
				.findUnique();		
	}

	public static Boolean isUserMemberOfGroup(Long userId, Long groupId) {
		Membership m = find.where().eq("user.userId", userId)
				.eq("workingGroup.groupId", groupId)
				.eq("status",MembershipStatus.ACCEPTED)
				.findUnique();		
		return m==null ? false : true;
	}
	
	public static Boolean hasUserRequestedMembershipToAssembly(Long userId, Long assemblyId) {
		Membership m = find.where().eq("user.userId", userId)
				.eq("workingGroup.groupId", assemblyId)
				.eq("status",MembershipStatus.REQUESTED)
				.findUnique();
				
		return m != null;		
	}

	public static List<Membership> findByAssemblyIdGroupIdAndStatus(Long aid,
			Long gid, String status) {
		Query<Membership> q = find.where().eq("workingGroup.groupId", gid).query();
		if (status != null && !status.isEmpty()
				&& !status.toUpperCase().equals("ALL"))
			q = q.where().eq("status", status.toUpperCase()).query();
		return q.findList();		
	}
}
