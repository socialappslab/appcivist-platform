package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.CampaignTypesEnum;
import enums.ResourceSpaceTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class ResourceSpace extends AppCivistBaseModel {
	@Id
	private UUID uuid;
	@Enumerated(EnumType.STRING)
	private ResourceSpaceTypes type = ResourceSpaceTypes.ASSEMBLY;
	private UUID parent;
	
	/**
	 * Assembly configuration parameters, e.g., modules and functionalities of modules that are enabled, 
	 * things specific to one assembly, future plugins or extended services configurations
	 */
//	@OneToMany(mappedBy = "assembly", cascade = CascadeType.ALL)
//	@JsonManagedReference
//	@OneToMany(cascade = CascadeType.ALL)
//	@JoinColumn(name = "target_uuid", referencedColumnName="uuid")
//    @JoinTable(name="Config", 
//            joinColumns=@JoinColumn(name="uuid", referencedColumnName="targetUuid"),
//    		inverseJoinColumns=@JoinColumn(name="uuid"))
//	@Formula(select="select c from config c where c.targetUuid=${ta}.uuid")
	@ManyToMany(cascade = {CascadeType.ALL})
	@JoinTable(name="resource_space_config")
	private List<Config> configs = new ArrayList<Config>();
	
	@ManyToMany(cascade = {CascadeType.ALL})
	@JoinTable(name="resource_space_theme")
	@JsonIgnoreProperties({"categoryId"})
	private List<Theme> themes = new ArrayList<Theme>();
	
	@ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="resource_space_campaign")
	@JsonManagedReference
	private List<Campaign> campaigns = new ArrayList<Campaign>();

	@ManyToMany(cascade = {CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinTable(name="resource_space_campaign_phases")
	private List<CampaignPhase> phases = new ArrayList<CampaignPhase>();
	
	@ManyToMany(cascade = {CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinTable(name="resource_space_working_groups")
	private List<WorkingGroup> workingGroups = new ArrayList<WorkingGroup>();

	@ManyToMany(cascade = {CascadeType.ALL}, fetch=FetchType.LAZY)
	@Size(max=20)
	@JoinTable(name="resource_space_contributions")
	private List<Contribution> contributions = new ArrayList<Contribution>();
	
	@ManyToMany(cascade = {CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinTable(name="resource_space_assemblies")
	private List<Assembly> assemblies = new ArrayList<Assembly>();
	
	@ManyToMany(cascade = {CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinTable(name="resource_space_resource")
	private List<Resource> resources = new ArrayList<Resource>();

	// TODO: add assembly members list here
	
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<UUID, ResourceSpace> find = new Finder<>(ResourceSpace.class);

	/**
	 * Empty constructor
	 */
	public ResourceSpace() {
		super();
		this.uuid = UUID.randomUUID();
	}
	
	public ResourceSpace(ResourceSpaceTypes type) {
		super();
		this.type = type;
		this.uuid = UUID.randomUUID();
	}
	
	public ResourceSpace(ResourceSpaceTypes type, UUID parent) {
		super();
		this.type = type;
		this.parent = parent;
		this.uuid = UUID.randomUUID();
	}

	public ResourceSpace(ResourceSpaceTypes type, List<Theme> themes,
			List<Campaign> campaigns, List<Config> configs) {
		super();
		this.type = type;
		this.themes = themes;
		this.campaigns = campaigns;
		this.configs = configs;
	}
	
	public UUID getResourceSpaceUuid() {
		return uuid;
	}

	public void setResourceSpaceUuid(UUID assemblyResourceSetId) {
		this.uuid = assemblyResourceSetId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public ResourceSpaceTypes getType() {
		return type;
	}

	public void setType(ResourceSpaceTypes type) {
		this.type = type;
	}

	public UUID getParent() {
		return parent;
	}

	public void setParent(UUID parent) {
		this.parent = parent;
	}

	public List<Theme> getThemes() {
		return themes;
	}

	public void setThemes(List<Theme> interests) {
		this.themes = interests;
	}
	
	public void addTheme(Theme category) {
        this.themes.add(category);
    }
 
    public void removeTheme(Theme category) {
        this.themes.remove(category);
    }
    
    public void copyThemesFrom(ResourceSpace target) {
    	List<Theme> themes = target.getThemes();
    	for (Theme theme : themes) {
			this.addTheme(theme);
		}
    	this.save();
    	this.refresh();
    }

	public List<Campaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(List<Campaign> campaigns) {
		this.campaigns = campaigns;
	}

	public List<Config> getConfigs() {
		return configs;
	}

	public void setConfigs(List<Config> configs) {
		this.configs = configs;
	}

	public List<CampaignPhase> getPhases() {
		return phases;
	}

	public void setPhases(List<CampaignPhase> phases) {
		this.phases = phases;
	}

	public List<WorkingGroup> getWorkingGroups() {
		return workingGroups;
	}

	public void setWorkingGroups(List<WorkingGroup> workingGroups) {
		this.workingGroups = workingGroups;
	}

	public List<Contribution> getContributions() {
		return contributions;
	}

	public void setContributions(List<Contribution> contributions) {
		this.contributions = contributions;
	}

	public List<Assembly> getAssemblies() {
		return assemblies;
	}

	public void setAssemblies(List<Assembly> assemblies) {
		this.assemblies = assemblies;
	}

	/*
	 * Basic Data Queries
	 */
	public static void create(ResourceSpace assemblyResourceSet) {
		assemblyResourceSet.save();
		assemblyResourceSet.refresh();
	}

	public static ResourceSpace read(UUID resourceSetId) {
		return find.ref(resourceSetId);
	}

	public static ResourceSpace createObject(ResourceSpace resourceSet) {
		resourceSet.save();
		return resourceSet;
	}

	public static void delete(UUID id) {
		find.ref(id).delete();
	}

	public static ResourceSpace update(ResourceSpace resourceSet) {
		resourceSet.update();
		resourceSet.refresh();
		return resourceSet;
	}

	public void setDefaultValues() {
		Campaign defaultCampaign = new Campaign();
		defaultCampaign.setTitle("Default Campaign");
		defaultCampaign.setActive(true);
		CampaignType ct = CampaignType.findByName(CampaignTypesEnum.PARTICIPATORY_BUDGETING);
		defaultCampaign.setType(ct);
		
		List<PhaseDefinition> phases = ct.getDefaultPhases();
		
		for (PhaseDefinition phaseDefinition : phases) {
			CampaignPhase phase = new CampaignPhase(defaultCampaign, phaseDefinition);
			defaultCampaign.getPhases().add(phase);
		}
		this.getCampaigns().add(defaultCampaign);
	}

	public static List<ResourceSpace> findByCategory(String category) {
		return find.where().like("assemblyThemes.title","%"+category+"%").findList();
	}
}
