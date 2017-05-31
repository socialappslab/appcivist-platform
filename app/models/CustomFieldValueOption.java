package models;

import com.avaje.ebean.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import models.misc.Views;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="CustomFieldValueOption", description="Model reprensenting Custom field value option")
public class CustomFieldValueOption extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long customFieldValueOptionId;
	@JsonView(Views.Public.class)
	private UUID uuid = UUID.randomUUID();
	@Column(name="value_type")
	@JsonView(Views.Public.class)
	private String valueType;
	@JsonView(Views.Public.class)
	private String value;
	@JsonView(Views.Public.class)
	private String name;

	@JsonIgnore
	@JoinColumn(name = "custom_field_definition_id")
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.REFRESH)
	private CustomFieldDefinition customFieldDefinition;

	public CustomFieldValueOption() {
		super();
		this.uuid = UUID.randomUUID();
	}

	public static Finder<Long, CustomFieldValueOption> find = new Finder<>(CustomFieldValueOption.class);

	public static List<CustomFieldValueOption> findAll() {
		return find.all();
	}

	public static CustomFieldValueOption read(Long id) {
		return find.ref(id);
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public Long getCustomFieldValueOptionId() {
		return customFieldValueOptionId;
	}

	public void setCustomFieldValueOptionId(Long customFieldValueOptionId) {
		this.customFieldValueOptionId = customFieldValueOptionId;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public static CustomFieldValueOption create(CustomFieldValueOption object) {
		object.save();
		object.refresh();
		return object;
	}

	public static CustomFieldValueOption createObject(CustomFieldValueOption object) {
		object.save();
		return object;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static CustomFieldValueOption update(CustomFieldValueOption object) {
		object.update();
		object.refresh();
		return object;
	}
}
