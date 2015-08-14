package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.avaje.ebean.ExpressionList;

@Entity
public class PhaseDefinition extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long phaseDefinitionId;
	
	private String name; 
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<RequiredCampaignPhaseMilestone> requiredMilestones = new ArrayList<RequiredCampaignPhaseMilestone>();
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<RequiredPhaseConfiguration> requiredConfigurations = new ArrayList<RequiredPhaseConfiguration>();
	
	
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, PhaseDefinition> find = new Finder<>(PhaseDefinition.class);

	public PhaseDefinition() {
		super();
	}
	
	public PhaseDefinition(String name) {
		super();
		this.name = name;
	}

	public Long getPhaseDefinitionId() {
		return phaseDefinitionId;
	}

	public void setPhaseDefinitionId(Long phaseDefinitionId) {
		this.phaseDefinitionId = phaseDefinitionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<RequiredCampaignPhaseMilestone> getRequiredMilestones() {
		return requiredMilestones;
	}

	public void setRequiredMilestones(
			List<RequiredCampaignPhaseMilestone> requiredMilestones) {
		this.requiredMilestones = requiredMilestones;
	}

	public List<RequiredPhaseConfiguration> getRequiredConfigurations() {
		return requiredConfigurations;
	}

	public void setRequiredConfigurations(
			List<RequiredPhaseConfiguration> requiredConfigurations) {
		this.requiredConfigurations = requiredConfigurations;
	}
	
	/*
	 * Basic Data operations
	 */
	
	public static PhaseDefinition read(Long id) {
        return find.ref(id);
    }

	public static PhaseDefinition readByName(String definitionName) {
		ExpressionList<PhaseDefinition> phaseDefinition = find.where().eq("name",definitionName);
		return phaseDefinition.findUnique();
	}

    public static List<PhaseDefinition> findAll() {
        return find.all();
    }

    public static PhaseDefinition create(PhaseDefinition object) {
        object.save();
        object.refresh();
        return object;
    }

    public static PhaseDefinition createObject(PhaseDefinition object) {
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
