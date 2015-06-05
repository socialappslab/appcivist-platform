package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;
import enums.PermissionTypes;

@Entity
public class Permission extends AppCivistBaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5209150394472354436L;

	@Id
	@GeneratedValue
	private Long permitId;
	private PermissionTypes permit;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private User creator;

	@ManyToOne(cascade=CascadeType.ALL)
	private List<Role> roles = new ArrayList<Role>();

	public Permission(User creator, PermissionTypes permit) {
		this.creator = creator;
		this.permit = permit;
	}

	public static Model.Finder<Long, Permission> find = new Model.Finder<Long, Permission>(
			Long.class, Permission.class);

	public static Permission read(Long permitId) {
		return find.ref(permitId);
	}

	public static List<Permission> findAll() {
		return find.all();
	}

	public static Permission create(Permission permission) {
		permission.save();
		permission.refresh();
		return permission;
	}

	public static Permission createObject(Permission permission) {
		permission.save();
		return permission;
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

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public Long getPermitId() {
		return permitId;
	}

	public void setPermitId(Long permitId) {
		this.permitId = permitId;
	}

	public PermissionTypes getPermit() {
		return permit;
	}

	public void setPermit(PermissionTypes permit) {
		this.permit = permit;
	}
}
