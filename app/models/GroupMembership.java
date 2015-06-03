package models;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue("GROUP")
public class GroupMembership extends Membership {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3737906484702711675L;
	@JsonIgnore
	@ManyToOne
	private WorkingGroup workingGroup;

	public WorkingGroup getWorkingGroup() {
		return workingGroup;
	}

	public void setWorkingGroup(WorkingGroup WorkingGroup) {
		this.workingGroup = WorkingGroup;
	}

	/**
	 * Check if membership for this user to the group/assembly already exists
	 * 
	 * @param m
	 * @return
	 */
	public boolean checkIfExists() {
		GroupMembership gm = (GroupMembership) this;
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
