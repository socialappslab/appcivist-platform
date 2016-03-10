package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(Include.NON_EMPTY)
@SequenceGenerator(name="componentDefSeq", initialValue=5, allocationSize=50)
public class ComponentDefinition extends AppCivistBaseModel {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="componentDefSeq")
	private Long componentDefId;
	@Index
	private UUID uuid = UUID.randomUUID();
	private String name;
	@Column(name = "description", columnDefinition = "text")
	private String description;

	@Transient
	private String uuidAsString;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="componentDef")
	private List<ComponentRequiredConfiguration> requiredConfigurations = new ArrayList<ComponentRequiredConfiguration>();

	@Transient
	private List<ComponentRequiredMilestone> requiredMilestones = new ArrayList<>();

	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, ComponentDefinition> find = new Finder<>(ComponentDefinition.class);

	public ComponentDefinition() {
		super();
	}

	public ComponentDefinition(String name) {
		super();
		this.name = name;
	}

	public Long getComponentDefId() {
		return componentDefId;
	}

	public void setComponentDefId(Long phaseDefinitionId) {
		this.componentDefId = phaseDefinitionId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getUuidAsString() {
		this.uuidAsString = this.uuid.toString();
		return this.uuidAsString;
	}

	public void setUuidAsString(String uuidAsString) {
		this.uuidAsString = uuidAsString;
		this.uuid = UUID.fromString(uuidAsString);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<ComponentRequiredConfiguration> getRequiredConfigurations() {
		return requiredConfigurations;
	}

	public void setRequiredConfigurations(
			List<ComponentRequiredConfiguration> requiredConfigurations) {
		this.requiredConfigurations = requiredConfigurations;
	}

	/*
	 * Basic Data operations
	 */

	public List<ComponentRequiredMilestone> getRequiredMilestones() {
		return requiredMilestones;
	}

	public void setRequiredMilestones(
			List<ComponentRequiredMilestone> requiredMilestones) {
		this.requiredMilestones = requiredMilestones;
	}

	public static ComponentDefinition read(Long id) {
		return find.ref(id);
	}

	public static ComponentDefinition readByName(String definitionName) {
		ExpressionList<ComponentDefinition> phaseDefinition = find.where().eq("name",
				definitionName);
		return phaseDefinition.findUnique();
	}

	public static List<ComponentDefinition> findAll() {
		return find.all();
	}

	public static ComponentDefinition create(ComponentDefinition object) {
		object.save();
		object.refresh();
		return object;
	}

	public static ComponentDefinition createObject(ComponentDefinition object) {
		object.save();
		return object;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	public static ComponentDefinition findByUUID(UUID targetUUID) {
		return find.where().eq("uuid", targetUUID).findUnique();
	}

	public static ComponentDefinition findByName(String name) {
		return find.where().eq("name",name).findUnique();
	}
}
