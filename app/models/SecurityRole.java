package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import be.objectify.deadbolt.core.models.Role;
import com.avaje.ebean.Model;
@Entity
public class SecurityRole extends Model implements Role {

	@Id
	@GeneratedValue
	private Long roleId;
	private String name;

	public SecurityRole(String name) {
		this.name = name;
	}

	public static Finder<Long, SecurityRole> find = new Finder<Long, SecurityRole>(SecurityRole.class);

	public static SecurityRole read(Long roleId) {
		return find.ref(roleId);
	}

	public static SecurityRole findByName(String name) {
		return find.where().eq("name", name).findUnique();
	}

	public static List<SecurityRole> findAll() {
		return find.all();
	}

	public static SecurityRole create(SecurityRole role) {
		role.save();
		role.refresh();
		return role;
	}

	public static SecurityRole createObject(SecurityRole role) {
		role.save();
		return role;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static SecurityRole update(SecurityRole role) {
		role.update();
		role.refresh();
		return role;
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
