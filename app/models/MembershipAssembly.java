package models;

import com.avaje.ebean.Query;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import enums.MembershipStatus;
import enums.MyRoles;
import exceptions.MembershipCreationException;
import io.swagger.annotations.ApiModel;
import play.Logger;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.UUID;

@Entity
@DiscriminatorValue("ASSEMBLY")
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="MembershipAssembly", description="Membership in an assembly")
public class MembershipAssembly extends Membership {

	//@JsonIgnore
	@ManyToOne
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
	
	public static MembershipAssembly create(MembershipAssembly membership) throws MembershipCreationException {
		if (!membership.alreadyExists()) {
			membership.save();
			membership.refresh();
			return membership;
		} else {
			throw new MembershipCreationException("Membership already exists");
		}
	}
	
	/*
	 * Basic Queries
	 */


	/**
	 * Find a membership of the user in the target collection (group or
	 * assembly)
	 * 
	 * @param user
	 * @param target
	 * @return
	 */
	public static Membership findByUserAndAssembly(User user, Assembly target) {
		List<Membership> memberships =  find.where().eq("user", user).eq("assembly", target)
				.findList();
		if(memberships != null && memberships.size() > 0) {
			return memberships.get(0);
		} else {
			return null;
		}
	}

	public static boolean hasRole(User user, Assembly target, MyRoles role) {
		Logger.debug("Checking if user " + user.getName() + " is "+ role.getName() +" in assembly "+ target.getAssemblyId());
		List<Membership> memberships =  find.where().eq("user", user).eq("assembly", target)
				.findList();
		for(Membership membership: memberships) {
			List<SecurityRole> membershipRoles = membership.filterByRoleName(role.getName());
			if(!membershipRoles.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find a membership record for userId and assemblyId
	 * 
	 * @param userId
	 * @param assemblyId
	 * @return
	 */
	public static Membership findByUserAndAssemblyIds(Long userId, Long assemblyId) {
		List<Membership> memberships = find.where().eq("user.userId", userId).eq("assembly.assemblyId", assemblyId).findList();
		if (memberships!=null) {
			if (memberships.size()>0) {
				return (memberships.get(0));
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static Boolean isUserMemberOfAssembly(Long userId, Long assemblyId) {
		Membership m = find.where().eq("user.userId", userId)
				.eq("assembly.assemblyId", assemblyId)
				.eq("status",MembershipStatus.ACCEPTED)
				.findUnique();
				
		return m != null;		
	}
	
	public static Boolean hasUserRequestedMembershipToAssembly(Long userId, Long assemblyId) {
		Membership m = find.where().eq("user.userId", userId)
				.eq("assembly.assemblyId", assemblyId)
				.eq("status",MembershipStatus.REQUESTED)
				.findUnique();
				
		return m != null;		
	}
	
	public static List<Membership> findByAssemblyIdAndStatus(Long id,
			String status) {
		Query<Membership> q = find.where().eq("assembly.assemblyId", id)
				.query();
		if (status != null && !status.isEmpty()
				&& !status.toUpperCase().equals("ALL"))
			q = q.where().eq("status", status.toUpperCase()).query();
		q = q.where().eq("user.removed",false).query();
		return q.findList();
	}

	public static List<Membership> findByAssemblyIdStatusAndNameQuery(Long id,
															 String status,
															 String nameQuery) {
		Query<Membership> q = find.where().eq("assembly.assemblyId", id).query();
		if (status != null && !status.isEmpty()
				&& !status.toUpperCase().equals("ALL"))
			q = q.where().eq("status", status.toUpperCase()).query();
		if (nameQuery != null && !nameQuery.isEmpty())
			q = q.where().ilike("user.name", "%"+nameQuery.toLowerCase()+"%").query();
		q = q.where().eq("user.removed",false).query();
		return q.findList();
	}
	
	public static Membership findByUserAndAssemblyUuid(Long userId,
			UUID assemblyUuid) {
		return find.where().eq("user.userId", userId).eq("assembly.uuid", assemblyUuid)
				.findUnique();		
	}
	
	public boolean alreadyExists() {
		return this.assembly != null && find.where().eq("assembly", this.assembly).eq("user", this.getUser()).findList().size() > 0;
	}
	public static Integer membershipCountByAssemblyId(Long assemblyId) {
		return find.where().eq("assembly.assemblyId",assemblyId).findRowCount();
	}


}
