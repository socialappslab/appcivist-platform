package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import models.misc.Views;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;


@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="CustomFieldValue", description="Model reprensenting Custom field value")
public class CustomFieldValue extends AppCivistBaseModel {

	@Id
	@JsonView(Views.Public.class)
	private UUID uuid = UUID.randomUUID();
	@Column(name="entity_target_type")
	@JsonView(Views.Public.class)
	private String entityTargetType;
	@JsonView(Views.Public.class)
	@Column(name="entity_target_uuid")
	private String entityTargetUuid;
	@JsonView(Views.Public.class)
	private String value;

	public CustomFieldValue() {
		super();
		this.uuid = UUID.randomUUID();
	}

	public static Finder<UUID, CustomFieldValue> find = new Finder<>(CustomFieldValue.class);

	public static List<CustomFieldValue> findAll() {
		return find.all();
	}

	public static CustomFieldValue read(UUID uuid) {
		return find.ref(uuid);
	}

	public static CustomFieldValue read(UUID targetUuid, UUID uuid) {
		return find.where().eq("entityTargetUuid", targetUuid).eq("uuid",uuid).findUnique();
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getEntityTargetType() {
		return entityTargetType;
	}

	public void setEntityTargetType(String entityTargetType) {
		this.entityTargetType = entityTargetType;
	}

	public String getEntityTargetUuid() {
		return entityTargetUuid;
	}

	public void setEntityTargetUuid(String entityTargetUuid) {
		this.entityTargetUuid = entityTargetUuid;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/*
     * Basic Data operations
     */

	public static CustomFieldValue create(CustomFieldValue object) {
		object.save();
		object.refresh();
		return object;
	}

	public static CustomFieldValue createObject(CustomFieldValue object) {
		object.save();
		return object;
	}

	public static void delete(UUID id) {
		find.ref(id).delete();
	}

	public static void delete(UUID targetUuid, UUID uuid) {
		find.where().eq("entityTargetUuid", targetUuid).eq("uuid", uuid).findUnique().delete();
	}

	public static CustomFieldValue update(CustomFieldValue object) {
		object.update();
		object.refresh();
		return object;
	}
}
