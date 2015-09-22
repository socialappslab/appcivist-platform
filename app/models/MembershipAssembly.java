package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Query;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import enums.MembershipStatus;

@Entity
@DiscriminatorValue("ASSEMBLY")
public class MembershipAssembly extends Membership {
	
	@ManyToOne(cascade=CascadeType.ALL)
	@JsonIgnoreProperties({"creator", "membershipRole", "campaigns", "assemblyConfigs"})
	private Assembly assembly;
	
	public MembershipAssembly() {
		super();
	}
	public MembershipAssembly(Long expiration, MembershipStatus status,
			User creator, User user, List<SecurityRole> roles, String membershipType) {
		super(expiration, status, creator, user, roles, membershipType);
	}
	
	public MembershipAssembly(Long expiration, MembershipStatus status,
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
		MembershipAssembly gm = (MembershipAssembly) m;
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
	/**
	 * Find a membership record for userId and assemblyId
	 * 
	 * @param userId
	 * @param assemblyId
	 * @return
	 */
	public static Membership findByUserAndAssemblyIds(Long userId, Long assemblyId) {
		return find.where().eq("user.userId", userId).eq("assembly.assemblyId", assemblyId)
				.findUnique();		
	}

	public static List<Membership> findByAssemblyIdAndStatus(Long id,
			String status) {
		Query<Membership> q = find.where().eq("assembly.assemblyId", id)
				.query();
		if (status != null && !status.isEmpty()
				&& !status.toUpperCase().equals("ALL"))
			q = q.where().eq("status", status.toUpperCase()).query();
		return q.findList();
	}
	
	public static Membership findByUserAndAssemblyUuid(Long userId,
			UUID assemblyUuid) {
		return find.where().eq("user.userId", userId).eq("assembly.uuid", assemblyUuid)
				.findUnique();		
	}
}
