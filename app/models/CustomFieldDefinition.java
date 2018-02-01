package models;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import enums.EntityTypes;
import enums.LimitTypes;
import io.swagger.annotations.ApiModel;
import models.misc.Views;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="CustomFieldDefinition", description="Model reprensenting Custom field definition")
public class CustomFieldDefinition extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	private Long customFieldDefinitionId;
	@JsonView(Views.Public.class)
	private UUID uuid = UUID.randomUUID();
	@JsonView(Views.Public.class)
	private String name;
	@JsonView(Views.Public.class)
	@Column(name="description", columnDefinition="text")
	private String description;

	@JsonView(Views.Public.class)
	@Enumerated(EnumType.STRING)
	@Column(name="entity_type")
	private EntityTypes entityType;
	
	@JsonView(Views.Public.class)
	@Column(name="entity_filter_attribute_name", columnDefinition="text")
	private String entityFilterAttributeName;

	@JsonView(Views.Public.class)
	@Column(name="entity_filter", columnDefinition="text")
	private String entityFilter;

	@JsonView(Views.Public.class)
	@Column(name="field_type")
	private String fieldType;
	
	@JsonView(Views.Public.class)
	@Column(name="field_position")
	private Integer position;

	@JsonView(Views.Public.class)
	@Column(name="field_limit", columnDefinition="text")
	private String limit;

	@JsonView(Views.Public.class)
	@Column(name="limit_type")
	@Enumerated(EnumType.STRING)
	private LimitTypes limitType;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "customFieldDefinitions")
	private List<ResourceSpace> containingSpaces;

	@JsonIgnore
	@OneToMany(fetch=FetchType.LAZY, mappedBy = "customFieldDefinition", cascade = CascadeType.ALL)
	private List<CustomFieldValue> customFieldValues;

	@JsonView(Views.Public.class)
	@OneToMany(fetch=FetchType.LAZY, mappedBy = "customFieldDefinition", cascade = CascadeType.ALL)
	private List<CustomFieldValueOption> customFieldValueOptions;


	public static Finder<Long, CustomFieldDefinition> find = new Finder<>(CustomFieldDefinition.class);

	public CustomFieldDefinition() {
		super();
		this.uuid = UUID.randomUUID();
	}

	public static CustomFieldDefinition read(Long customFieldDefinitionId) {
		return find.ref(customFieldDefinitionId);
	}


	public static List<CustomFieldDefinition> findAll() {
		return find.all();
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getCustomFieldDefinitionId() {
		return customFieldDefinitionId;
	}

	public void setCustomFieldDefinitionId(Long customFieldDefinitionId) {
		this.customFieldDefinitionId = customFieldDefinitionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEntityFilterAttributeName() {
		return entityFilterAttributeName;
	}

	public void setEntityFilterAttributeName(String entityFilterAttributeName) {
		this.entityFilterAttributeName = entityFilterAttributeName;
	}

	public String getEntityFilter() {
		return entityFilter;
	}

	public void setEntityFilter(String entityFilter) {
		this.entityFilter = entityFilter;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	
	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public EntityTypes getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityTypes entityType) {
		this.entityType = entityType;
	}

	public LimitTypes getLimitType() {
		return limitType;
	}

	public void setLimitType(LimitTypes limitType) {
		this.limitType = limitType;
	}

	public List<ResourceSpace> getContainingSpaces() {
		return containingSpaces;
	}

	public void setContainingSpaces(List<ResourceSpace> containingSpaces) {
		this.containingSpaces = containingSpaces;
	}

	/*
     * Basic Data operations
     */

	public static CustomFieldDefinition create(CustomFieldDefinition object) {
		object.save();
		object.refresh();
		if(object.getCustomFieldValueOptions()!=null && object.getCustomFieldValueOptions().size()!=0){
			for (CustomFieldValueOption customFieldValueOption:object.getCustomFieldValueOptions()) {
				customFieldValueOption.setCustomFieldDefinition(object);
				customFieldValueOption.save();
			}
		}
		return object;
	}

	public static CustomFieldDefinition createObject(CustomFieldDefinition object) {
		object.save();
		return object;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	public List<CustomFieldValue> getCustomFieldValues() {
		return customFieldValues;
	}

	public void setCustomFieldValues(List<CustomFieldValue> customFieldValue) {
		this.customFieldValues = customFieldValue;
	}

	public List<CustomFieldValueOption> getCustomFieldValueOptions() {
		return customFieldValueOptions;
	}

	public void setCustomFieldValueOptions(List<CustomFieldValueOption> customFieldValueOptions) {
		this.customFieldValueOptions = customFieldValueOptions;
	}
}
