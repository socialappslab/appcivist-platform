package models;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class RequiredCampaignConfiguration extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	private Long requiredCampaignConfigurationId; 
	private UUID uuid = UUID.randomUUID();
	
	@ManyToOne
	@JsonBackReference
	private CampaignType campaignType;
	
	@ManyToOne
	private ConfigDefinition configDefinition;

	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, RequiredCampaignConfiguration> find = new Finder<>(RequiredCampaignConfiguration.class);

	
	public RequiredCampaignConfiguration(Long requiredPhaseConfigurationId,
			CampaignType campaignType, ConfigDefinition configDefinition) {
		super();
		this.requiredCampaignConfigurationId = requiredPhaseConfigurationId;
		this.campaignType = campaignType;
		this.configDefinition = configDefinition;
	}

	public Long getRequiredPhaseConfigurationId() {
		return requiredCampaignConfigurationId;
	}

	public void setRequiredPhaseConfigurationId(Long requiredPhaseConfigurationId) {
		this.requiredCampaignConfigurationId = requiredPhaseConfigurationId;
	}

	public CampaignType getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(CampaignType campaignType) {
		this.campaignType = campaignType;
	}

	public ConfigDefinition getConfigDefinition() {
		return configDefinition;
	}

	public void setConfigDefinition(ConfigDefinition configDefinition) {
		this.configDefinition = configDefinition;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}	
}
