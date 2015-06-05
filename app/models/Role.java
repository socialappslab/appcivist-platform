package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import play.db.ebean.Model;

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

	public Role(String name, List<Membership> memberships, List<User> users) {
		this.name = name;
	}

	public static Model.Finder<Long, Role> find = new Model.Finder<Long, Role>(
			Long.class, Role.class);

	public static Role read(Long roleId) {
		return find.ref(roleId);
	}

	public static Role readByTitle(String name) {
		return find.where().eq("name", name).findUnique();
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
}
