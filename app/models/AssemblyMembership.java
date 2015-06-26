package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import enums.MembershipStatus;

@Entity
@DiscriminatorValue("ASSEMBLY")
public class AssemblyMembership extends Membership {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6654162992798204503L;
	
	@ManyToOne(cascade=CascadeType.ALL)
	@JsonIgnoreProperties({"creator", "membershipRole", "campaigns", "assemblyConfigs"})
	private Assembly assembly;
	
	public AssemblyMembership() {
		super();
	}
	public AssemblyMembership(Long expiration, MembershipStatus status,
			User creator, User user, List<SecurityRole> roles, String membershipType) {
		super(expiration, status, creator, user, roles, membershipType);
	}
	
	public AssemblyMembership(Long expiration, MembershipStatus status,
			User creator, User user, List<SecurityRole> roles, String membershipType, 
			Assembly assembly) {
		super(expiration, status, creator, user, roles, membershipType);
		this.assembly = assembly;
	}
	
	/*
	 * Getters and Setters
	 */
	public Assembly getAssembly() {
		return assembly;
	}

	public void setAssembly(Assembly assembly) {
		this.assembly = assembly;
	}
	
	/*
	 * Basic Queries
	 */
	
	/**
	 * Check if membership for this user to the group/assembly already exists
	 * 
	 * @param m
	 * @return
	 */
	public static boolean checkIfExists(Membership m) {
		AssemblyMembership gm = (AssemblyMembership) m;
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
