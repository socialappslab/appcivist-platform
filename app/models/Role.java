package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Role extends AppCivistBaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1734493746753409942L;

	@Id
	@GeneratedValue
	private Long roleId;
	private String name;
	private User creator;

    @JsonIgnore
    @ManyToMany(mappedBy="roles")
    private List<Membership> memberships  = new ArrayList<Membership>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "roles")
	private Permission permits;

    @JsonIgnore
    @ManyToMany(mappedBy="roles")
    private List<User> users = new ArrayList<User>();
	/*
	 * @OneToMany(cascade = CascadeType.ALL, mappedBy="role") private
	 * List<Membership> memberships = new ArrayList<Membership>();
	 */

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "role")
	private List<WorkingGroup> workingGroups = new ArrayList<WorkingGroup>();

	public Role(User creator, String name, Permission permits, /*
																 * List<Membership
																 * >
																 * memberships,
																 */
			List<WorkingGroup> workingGroups, List<User> users) {
		this.creator = creator;
		this.name = name;
		this.permits = permits;
		// this.memberships = memberships;
		this.workingGroups = workingGroups;
		this.users = users;
	}

	public static Model.Finder<Long, Role> find = new Model.Finder<Long, Role>(
			Long.class, Role.class);

    public static Role read(Long roleId) {
        return find.ref(roleId);
    }
    
    public static Integer readByTitle(String name){
    	ExpressionList<Role> roles = find.where().eq("name", name);
    	return roles.findList().size();
    }

	public static List<Role> findAll() {
		return find.all();
	}

	public static Role create(Role role) {
		role.save();
		role.refresh();
		return role;
	}

	public static Role createObject(Role role) {
		role.save();
		return role;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Permission getPermits() {
		return permits;
	}

	public void setPermits(Permission permits) {
		this.permits = permits;
	}

	/*
	 * public List<Membership> getMemberships() { return memberships; }
	 * 
	 * public void setMemberships(List<Membership> memberships) {
	 * this.memberships = memberships; }
	 */
	public List<WorkingGroup> getWorkingGroups() {
		return workingGroups;
	}

	public void setWorkingGroups(List<WorkingGroup> workingGroups) {
		this.workingGroups = workingGroups;
	}

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<Membership> memberships) {
        this.memberships = memberships;
    }
}
