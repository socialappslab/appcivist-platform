package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class Component extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long componentId;
	@Index
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;
	private String name; 
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<ComponentRequiredConfiguration> requiredConfigurations = new ArrayList<ComponentRequiredConfiguration>();
	
	@Transient
	private List<ComponentRequiredMilestone> requiredMilestones = new ArrayList<>();
	
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, Component> find = new Finder<>(Component.class);

	public Component() {
		super();
	}
	
	public Component(String name) {
		super();
		this.name = name;
	}

	public Long getComponentId() {
		return componentId;
	}

	public void setComponentId(Long phaseDefinitionId) {
		this.componentId = phaseDefinitionId;
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

	public static Component read(Long id) {
        return find.ref(id);
    }

	public static Component readByName(String definitionName) {
		ExpressionList<Component> phaseDefinition = find.where().eq("name",definitionName);
		return phaseDefinition.findUnique();
	}

    public static List<Component> findAll() {
        return find.all();
    }

    public static Component create(Component object) {
        object.save();
        object.refresh();
        return object;
    }

    public static Component createObject(Component object) {
        object.save();
        return object;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }

	public static Component findByUUID(UUID targetUUID) {
		return find.where().eq("uuid", targetUUID).findUnique();
	}
}
