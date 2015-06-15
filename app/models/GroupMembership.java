package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import enums.MembershipStatus;

@Entity
@DiscriminatorValue("GROUP")
public class GroupMembership extends Membership {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3737906484702711675L;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private WorkingGroup workingGroup;

	public GroupMembership() {
		super();
	}
	public GroupMembership(Long expiration, MembershipStatus status,
			User creator, User user, List<SecurityRole> roles, String membershipType) {
		super(expiration, status, creator, user, roles, membershipType);
	}
	
	public GroupMembership(Long expiration, MembershipStatus status,
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
		GroupMembership gm = (GroupMembership) m;
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
}
