package models;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import models.misc.Views;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;


@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="CustomFieldValue", description="Model reprensenting Custom field value")
public class CustomFieldValue extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long customFieldValueId;
	@JsonView(Views.Public.class)
	private UUID uuid = UUID.randomUUID();
	@Column(name="entity_target_type")
	@JsonView(Views.Public.class)
	private String entityTargetType;
	@JsonView(Views.Public.class)
	@Column(name="entity_target_uuid")
	private UUID entityTargetUuid;
	@JsonView(Views.Public.class)
	private String value;

	@JsonView(Views.Public.class)
	@JoinColumn(name = "custom_field_definition_id")
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.REFRESH)
	private CustomFieldDefinition customFieldDefinition;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "customFieldValues")
	private List<ResourceSpace> containingSpaces;
	
	public CustomFieldValue() {
		super();
		this.uuid = UUID.randomUUID();
	}

	public static Finder<Long, CustomFieldValue> find = new Finder<>(CustomFieldValue.class);

	public static List<CustomFieldValue> findAll() {
		return find.all();
	}

	public static CustomFieldValue read(Long id) {
		return find.ref(id);
	}

	public static CustomFieldValue read(UUID targetUuid, Long id) {
		return find.where().eq("entityTargetUuid", targetUuid).eq("customFieldDefinition.idCustomFieldDefinition",id).findUnique();
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

	public UUID getEntityTargetUuid() {
		return entityTargetUuid;
	}

	public void setEntityTargetUuid(UUID entityTargetUuid) {
		this.entityTargetUuid = entityTargetUuid;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Long getCustomFieldValueId() {
		return customFieldValueId;
	}

	public void setCustomFieldValueId(Long customFieldValueId) {
		this.customFieldValueId = customFieldValueId;
	}

	public CustomFieldDefinition getCustomFieldDefinition() {
		return customFieldDefinition;
	}

	public void setCustomFieldDefinition(CustomFieldDefinition customFieldDefinition) {
		this.customFieldDefinition = customFieldDefinition;
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

	public static void delete(Long id) {
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

	public static List<CustomFieldValue> updateList(List<CustomFieldValue> list) {
		List<CustomFieldValue> customFieldValues = new ArrayList<>();
		for (CustomFieldValue c : list) {
			CustomFieldValue customFieldValue = read(c.getEntityTargetUuid(),c.getCustomFieldDefinition().getCustomFieldDefinitionId());
			customFieldValue.setValue(c.getValue());
			customFieldValue.update();
			customFieldValue.refresh();
			customFieldValues.add(customFieldValue);
		}
		return customFieldValues;
	}
}