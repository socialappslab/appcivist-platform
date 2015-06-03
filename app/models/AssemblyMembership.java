package models;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue("ASSEMBLY")
public class AssemblyMembership extends Membership {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6654162992798204503L;
	@JsonIgnore
	@ManyToOne
	private Assembly assembly;

	public Assembly getAssembly() {
		return assembly;
	}

	public void setAssembly(Assembly organization) {
		this.assembly = organization;
	}

	/**
	 * Check if membership for this user to the group/assembly already exists
	 * 
	 * @param m
	 * @return
	 */
	public boolean checkIfExists() {
		AssemblyMembership gm = (AssemblyMembership) this;
		return find.where().eq("creator", gm.getCreator())
				.eq("user", gm.getUser()).eq("assembly", gm.getAssembly())
				.findUnique() != null;
	}

	/**
	 * Find a membership of the user in the target collection (group or
	 * assembly)
	 * 
	 * @param user
	 * @param target
	 * @return
	 */
	public static Membership findByUserAndAssembly(User user, Assembly target) {
		return find.where().eq("user", user).eq("assembly", target)
				.findUnique();
	}
}
