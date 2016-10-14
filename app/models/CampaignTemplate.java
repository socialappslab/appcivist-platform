package models;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.CampaignTemplatesEnum;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="CampaignTemplate", description="A template for a campaign is composed by specified component definitions that can be used to instantiate a campaign of a certain type")
public class CampaignTemplate extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long campaignTemplateId;
	@Enumerated(EnumType.STRING)
	private CampaignTemplatesEnum nameKey;
	private String name;
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="campaign_template_def_components")
	private List<ComponentDefinition> defComponents = new LinkedList<ComponentDefinition>();
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="campaign_template_req_configs")
	@JsonManagedReference
	@JsonInclude(content=Include.NON_EMPTY)
	private List<CampaignRequiredConfiguration> reqConfigs = new ArrayList<CampaignRequiredConfiguration>();
		
	@OneToMany(cascade=CascadeType.ALL)
	@JsonManagedReference
	@JsonInclude(content=Include.NON_EMPTY)
	@JsonIgnore
	private List<ComponentRequiredMilestone> reqMilestones = new ArrayList<ComponentRequiredMilestone>();
		
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, CampaignTemplate> find = new Finder<>(CampaignTemplate.class);

	public CampaignTemplate() {
		super();
	}

	public CampaignTemplate(CampaignTemplatesEnum name, List<ComponentDefinition> defaultPhases) {
		super();
		this.nameKey = name;
		this.defComponents = defaultPhases;
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

	public List<ComponentDefinition> getDefComponents() {
		HashMap<UUID, ComponentDefinition> componentsTable = new HashMap<>();
		for (ComponentDefinition c : defComponents) {
			componentsTable.put(c.getUuid(), c);
		}

		for (ComponentRequiredMilestone milestone : reqMilestones) {
			UUID targetUUID = milestone.getTargetComponentUuid();
			ComponentDefinition c = componentsTable.get(targetUUID);
			if (c!=null) {
				List<ComponentRequiredMilestone> rMile = c.getRequiredMilestones();
				if (rMile != null) {
					rMile.add(milestone);
				}
			}
		}
		return defComponents;
	}

	public void setDefComponents(List<ComponentDefinition> defaultComponents) {
		this.defComponents = defaultComponents;
	}
	
	/*
	 * Basic Data operations
	 */
	
	public List<CampaignRequiredConfiguration> getReqConfigs() {
		return reqConfigs;
	}

	public void setReqConfigs(
			List<CampaignRequiredConfiguration> requiredConfigurations) {
		this.reqConfigs = requiredConfigurations;
	}
	
	public List<ComponentRequiredMilestone> getReqMilestones() {
		return reqMilestones;
	}

	public void setReqMilestones(
			List<ComponentRequiredMilestone> requiredMilestones) {
		this.reqMilestones = requiredMilestones;
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
