package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.CampaignTemplatesEnum;
import enums.ResourceSpaceTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class ResourceSpace extends AppCivistBaseModel {
	
	@Id
	@GeneratedValue
	private Long resourceSpaceId; 
	@Index
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;
	@Enumerated(EnumType.STRING)
	private ResourceSpaceTypes type = ResourceSpaceTypes.ASSEMBLY;
	private UUID parent;
	
	/**
	 * Configuration parameters, e.g., modules and functionalities of modules that are enabled, 
	 * things specific to one assembly, future plugins or extended services configurations
	 */
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
	@JoinTable(name="resource_space_campaign_components")
	private List<ComponentInstance> components = new ArrayList<ComponentInstance>();
	
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
	
	@ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="resource_space_hashtag")
	private List<Hashtag> hashtags = new ArrayList<Hashtag>();
	
	// TODO: add assembly members list here
	
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, ResourceSpace> find = new Finder<>(ResourceSpace.class);

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

	public String getUuidAsString() {
		return uuid.toString();
	}

	public void setUuidAsString(String uuidAsString) {
		this.uuidAsString = uuidAsString;
		this.uuid = UUID.fromString(uuidAsString);
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
	
	public void addTheme(Theme theme) {
        this.themes.add(theme);
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

	public void addCampaign(Campaign c) {
        this.campaigns.add(c);
    }
	
	public List<Config> getConfigs() {
		return configs;
	}

	public void setConfigs(List<Config> configs) {
		this.configs = configs;
	}
	
	public void addConfig(Config c) {
        this.configs.add(c);
    }
	
	public List<ComponentInstance> getComponents() {
		return components;
	}

	public void setComponents(List<ComponentInstance> components) {
		this.components = components;
	}

	public void addComponent(ComponentInstance c) {
        this.components.add(c);
    }
	
	public List<WorkingGroup> getWorkingGroups() {
		return workingGroups;
	}

	public void setWorkingGroups(List<WorkingGroup> workingGroups) {
		this.workingGroups = workingGroups;
	}

	public void addWorkingGroup(WorkingGroup wg) {
        this.workingGroups.add(wg);
    }
	
	public List<Contribution> getContributions() {
		return contributions;
	}

	public void setContributions(List<Contribution> contributions) {
		this.contributions = contributions;
	}

	public void addContribution(Contribution c) {
		this.contributions.add(c);	
	}

	public List<Assembly> getAssemblies() {
		return assemblies;
	}

	public void setAssemblies(List<Assembly> assemblies) {
		this.assemblies = assemblies;
	}

	public void addAssembly(Assembly a) {
        this.assemblies.add(a);
    }
	
	public Long getResourceSpaceId() {
		return resourceSpaceId;
	}

	public void setResourceSpaceId(Long resourceSpaceId) {
		this.resourceSpaceId = resourceSpaceId;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	public void addResource(Resource resource) {
		this.resources.add(resource);
	}
	public List<Hashtag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(List<Hashtag> hashtags) {
		this.hashtags = hashtags;
	}

	public void addHashtag(Hashtag h) {
		this.hashtags.add(h);	
	}
	
	/*
	 * Basic Data Queries
	 */
	public static void create(ResourceSpace resourceSet) {
		resourceSet.save();
		resourceSet.refresh();
	}

	public static ResourceSpace read(Long resourceSetId) {
		return find.ref(resourceSetId);
	}

	public static ResourceSpace readByUUID(UUID resourceSetUuid) {
		return find.where().eq("uuid",resourceSetUuid).findUnique();
	}

	public static ResourceSpace createObject(ResourceSpace resourceSet) {
		resourceSet.save();
		return resourceSet;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void deleteByUUID(UUID resourceSetUuid) {
		find.where().eq("uuid",resourceSetUuid).findUnique().delete();
	}
	
	public static ResourceSpace update(ResourceSpace resourceSet) {
		resourceSet.update();
		resourceSet.refresh();
		return resourceSet;
	}

	public void setDefaultValues() {
		Campaign defaultCampaign = new Campaign();
		defaultCampaign.setTitle("Default Campaign");
		CampaignTemplate ct = CampaignTemplate.findByName(CampaignTemplatesEnum.PARTICIPATORY_BUDGETING);
		defaultCampaign.setType(ct);
		
		List<Component> components = ct.getDefaultComponents();
		
		for (Component component : components) {
			ComponentInstance componentInstance = new ComponentInstance(defaultCampaign, component);
			defaultCampaign.getResources().getComponents().add(componentInstance);
		}
		this.getCampaigns().add(defaultCampaign);
	}

	public static List<ResourceSpace> findByTheme(String theme) {
		return find.where().like("themes.title","%"+theme+"%").findList();
	}
}
