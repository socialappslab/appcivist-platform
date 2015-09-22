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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.CampaignTemplatesEnum;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class CampaignTemplate extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long campaignTemplateId;
	@Enumerated(EnumType.STRING)
	private CampaignTemplatesEnum nameKey;
	private String name;
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="campaign_template_default_components")
	private List<Component> defaultComponents = new LinkedList<Component>();
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="campaign_template_required_configs")
	@JsonManagedReference
	@JsonInclude(content=Include.NON_EMPTY)
	private List<CampaignRequiredConfiguration> requiredConfigurations = new ArrayList<CampaignRequiredConfiguration>();
		
	@OneToMany(cascade=CascadeType.ALL)
	@JsonManagedReference
	@JsonInclude(content=Include.NON_EMPTY)
	private List<ComponentRequiredMilestone> requiredMilestones = new ArrayList<ComponentRequiredMilestone>();
		
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, CampaignTemplate> find = new Finder<>(CampaignTemplate.class);

	public CampaignTemplate() {
		super();
	}

	public CampaignTemplate(CampaignTemplatesEnum name, List<Component> defaultPhases) {
		super();
		this.nameKey = name;
		this.defaultComponents = defaultPhases;
	}

	public Long getCampaignTemplateId() {
		return campaignTemplateId;
	}

	public void setCampaignTemplateId(Long campaignTemplateId) {
		this.campaignTemplateId = campaignTemplateId;
	}

	public CampaignTemplatesEnum getNameKey() {
		return nameKey;
	}

	public void setNameKey(CampaignTemplatesEnum name) {
		this.nameKey = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String niceName) {
		this.name = niceName;
	}

	public List<Component> getDefaultComponents() {
		return defaultComponents;
	}

	public void setDefaultComponents(List<Component> defaultComponents) {
		this.defaultComponents = defaultComponents;
	}
	
	/*
	 * Basic Data operations
	 */
	
	public List<CampaignRequiredConfiguration> getRequiredConfigurations() {
		return requiredConfigurations;
	}

	public void setRequiredConfigurations(
			List<CampaignRequiredConfiguration> requiredConfigurations) {
		this.requiredConfigurations = requiredConfigurations;
	}
	
	public List<ComponentRequiredMilestone> getRequiredMilestones() {
		return requiredMilestones;
	}

	public void setRequiredMilestones(
			List<ComponentRequiredMilestone> requiredMilestones) {
		this.requiredMilestones = requiredMilestones;
	}

	public static CampaignTemplate read(Long id) {
        return find.ref(id);
    }

    public static List<CampaignTemplate> findAll() {
        return find.all();
    }

    public static CampaignTemplate create(CampaignTemplate object) {
        object.save();
        object.refresh();
        return object;
    }

    public static CampaignTemplate createObject(CampaignTemplate object) {
        object.save();
        return object;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }
    
    public static CampaignTemplate findByName(CampaignTemplatesEnum nameKey) {
    	return find.where().eq("nameKey",nameKey).findUnique();
    }
}
