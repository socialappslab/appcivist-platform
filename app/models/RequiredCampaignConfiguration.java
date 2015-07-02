package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class RequiredCampaignConfiguration extends AppCivistBaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6546753271739021232L;
	
	
	@Id
	@GeneratedValue
	private Long requiredPhaseConfigurationId; 
	
	@ManyToOne
	private PhaseDefinition phaseDefinition;
	
	@ManyToOne
	private Config configuration;

	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, RequiredCampaignConfiguration> find = new Finder<Long, RequiredCampaignConfiguration>(
			Long.class, RequiredCampaignConfiguration.class);

	
	public RequiredCampaignConfiguration(Long requiredPhaseConfigurationId,
			PhaseDefinition phaseDefinition, Config configuration) {
		super();
		this.requiredPhaseConfigurationId = requiredPhaseConfigurationId;
		this.phaseDefinition = phaseDefinition;
		this.configuration = configuration;
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

	public Config getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Config configuration) {
		this.configuration = configuration;
	}	
}
