package models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.CampaignTypesEnum;

@Entity
public class CampaignType extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long campaignTypeId;
	@Enumerated(EnumType.STRING)
	private CampaignTypesEnum nameKey;
	private String name;
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="campaign_type_default_phases")
	private List<PhaseDefinition> defaultPhases = new LinkedList<PhaseDefinition>();
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="campaign_type_required_configs")
	@JsonManagedReference
	@JsonInclude(content=Include.NON_EMPTY)
	private List<RequiredCampaignConfiguration> requiredConfigurations = new ArrayList<RequiredCampaignConfiguration>();
		
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, CampaignType> find = new Finder<>(CampaignType.class);

	public CampaignType() {
		super();
	}

	public CampaignType(CampaignTypesEnum name, List<PhaseDefinition> defaultPhases) {
		super();
		this.nameKey = name;
		this.defaultPhases = defaultPhases;
	}

	public Long getCampaignTypeId() {
		return campaignTypeId;
	}

	public void setCampaignTypeId(Long campaignTypeId) {
		this.campaignTypeId = campaignTypeId;
	}

	public CampaignTypesEnum getNameKey() {
		return nameKey;
	}

	public void setNameKey(CampaignTypesEnum name) {
		this.nameKey = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String niceName) {
		this.name = niceName;
	}

	public List<PhaseDefinition> getDefaultPhases() {
		return defaultPhases;
	}

	public void setDefaultPhases(List<PhaseDefinition> defaultPhases) {
		this.defaultPhases = defaultPhases;
	}
	
	/*
	 * Basic Data operations
	 */
	
	public List<RequiredCampaignConfiguration> getRequiredConfigurations() {
		return requiredConfigurations;
	}

	public void setRequiredConfigurations(
			List<RequiredCampaignConfiguration> requiredConfigurations) {
		this.requiredConfigurations = requiredConfigurations;
	}

	public static CampaignType read(Long id) {
        return find.ref(id);
    }

    public static List<CampaignType> findAll() {
        return find.all();
    }

    public static CampaignType create(CampaignType object) {
        object.save();
        object.refresh();
        return object;
    }

    public static CampaignType createObject(CampaignType object) {
        object.save();
        return object;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }
    
    public static CampaignType findByName(CampaignTypesEnum nameKey) {
    	return find.where().eq("nameKey",nameKey).findUnique();
    }
}
