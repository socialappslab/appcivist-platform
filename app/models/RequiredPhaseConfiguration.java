package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class RequiredPhaseConfiguration extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long requiredPhaseConfigurationId; 
	
	@ManyToOne
	@JsonBackReference
	private PhaseDefinition phaseDefinition;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private ConfigDefinition configDefinition;

	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, RequiredPhaseConfiguration> find = new Finder<>(RequiredPhaseConfiguration.class);

	
	public RequiredPhaseConfiguration(Long requiredPhaseConfigurationId,
			PhaseDefinition phaseDefinition, ConfigDefinition configuration) {
		super();
		this.requiredPhaseConfigurationId = requiredPhaseConfigurationId;
		this.phaseDefinition = phaseDefinition;
		this.configDefinition= configuration;
	}

	public Long getRequiredPhaseConfigurationId() {
		return requiredPhaseConfigurationId;
	}

	public void setRequiredPhaseConfigurationId(Long requiredPhaseConfigurationId) {
		this.requiredPhaseConfigurationId = requiredPhaseConfigurationId;
	}

	public PhaseDefinition getPhaseDefinition() {
		return phaseDefinition;
	}

	public void setPhaseDefinition(PhaseDefinition phaseDefinition) {
		this.phaseDefinition = phaseDefinition;
	}

	public ConfigDefinition getConfigDefinition() {
		return configDefinition;
	}

	public void setConfigDefinition(ConfigDefinition configuration) {
		this.configDefinition = configuration;
	}	
	
	/*
	 * Basic Data operations
	 */
	
	public static RequiredPhaseConfiguration read(Long id) {
        return find.ref(id);
    }

    public static List<RequiredPhaseConfiguration> findAll() {
        return find.all();
    }

    public static RequiredPhaseConfiguration create(RequiredPhaseConfiguration object) {
        object.save();
        object.refresh();
        return object;
    }

    public static RequiredPhaseConfiguration createObject(RequiredPhaseConfiguration object) {
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
