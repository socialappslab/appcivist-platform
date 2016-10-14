package models;

import io.swagger.annotations.ApiModel;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="CampaignRequiredConfiguration", description="Required configuration value for a particular campaign")
public class CampaignRequiredConfiguration extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	private Long campaignRequiredConfigurationId; 
	private UUID uuid = UUID.randomUUID();
	
	@ManyToOne
	@JsonBackReference
	private CampaignTemplate campaignTemplate;
	
	@ManyToOne
	private ConfigDefinition configDefinition;

	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, CampaignRequiredConfiguration> find = new Finder<>(CampaignRequiredConfiguration.class);

	
	public CampaignRequiredConfiguration(Long requiredPhaseConfigurationId,
			CampaignTemplate campaignType, ConfigDefinition configDefinition) {
		super();
		this.campaignRequiredConfigurationId = requiredPhaseConfigurationId;
		this.campaignTemplate = campaignType;
		this.configDefinition = configDefinition;
		this.uuid = UUID.randomUUID();
	}

	public Long getCampaignRequiredConfigurationId() {
		return campaignRequiredConfigurationId;
	}

	public void setCampaignRequiredConfigurationId(Long requiredPhaseConfigurationId) {
		this.campaignRequiredConfigurationId = requiredPhaseConfigurationId;
	}

	public CampaignTemplate getCampaignTemplate() {
		return campaignTemplate;
	}

	public void setCampaignTemplate(CampaignTemplate campaignTemplate) {
		this.campaignTemplate = campaignTemplate;
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
