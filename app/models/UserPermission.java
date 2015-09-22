package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import be.objectify.deadbolt.core.models.Permission;
import com.avaje.ebean.Model;

@Entity
public class UserPermission extends Model implements Permission {

	@Id
	@GeneratedValue
	private Long permissionId;
	
    @Column(name = "permission_value")
    public String value;
	

	public UserPermission(String value) {
		this.value = value;
	}

	public static Finder<Long, UserPermission> find = new Finder<Long, UserPermission>(
			UserPermission.class);

	public static UserPermission read(Long permitId) {
		return find.ref(permitId);
	}

	public static List<UserPermission> findAll() {
		return find.all();
	}

	public static UserPermission create(UserPermission permission) {
		permission.save();
		permission.refresh();
		return permission;
	}

	public static UserPermission createObject(UserPermission permission) {
		permission.save();
		return permission;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	public Long getPermissionId() {
		return permissionId;
	}

	public void setPermissionId(Long permitId) {
		this.permissionId = permitId;
	}

	@Override
	public String getValue() {
		return this.value;
	}
	
	public void setValue(String value) {
		this.value=value;
	}
	

    public static UserPermission findByValue(String value)
    {
        return find.where()
                   .eq("value",
                       value)
                   .findUnique();
    }
}
