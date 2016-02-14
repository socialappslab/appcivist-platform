package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class ComponentRequiredConfiguration extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long componentRequiredConfigurationId; 
	
	@ManyToOne
	@JsonBackReference
	private ComponentDefinition componentDef;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private ConfigDefinition configDef;

	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, ComponentRequiredConfiguration> find = new Finder<>(ComponentRequiredConfiguration.class);

	
	public ComponentRequiredConfiguration(Long componentRequiredConfigurationId,
			ComponentDefinition definition, ConfigDefinition configuration) {
		super();
		this.componentRequiredConfigurationId = componentRequiredConfigurationId;
		this.componentDef = definition;
		this.configDef= configuration;
	}

	public Long getComponentRequiredConfigurationId() {
		return componentRequiredConfigurationId;
	}

	public void setComponentRequiredConfigurationId(Long componentRequiredConfigurationId) {
		this.componentRequiredConfigurationId = componentRequiredConfigurationId;
	}

	public ComponentDefinition getComponentDef() {
		return componentDef;
	}

	public void setComponentDef(ComponentDefinition phaseDefinition) {
		this.componentDef = phaseDefinition;
	}

	public ConfigDefinition getConfigDef() {
		return configDef;
	}

	public void setConfigDef(ConfigDefinition configuration) {
		this.configDef = configuration;
	}	
	
	/*
	 * Basic Data operations
	 */
	
	public static ComponentRequiredConfiguration read(Long id) {
        return find.ref(id);
    }

    public static List<ComponentRequiredConfiguration> findAll() {
        return find.all();
    }

    public static ComponentRequiredConfiguration create(ComponentRequiredConfiguration object) {
        object.save();
        object.refresh();
        return object;
    }

    public static ComponentRequiredConfiguration createObject(ComponentRequiredConfiguration object) {
        object.save();
        return object;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }
}
