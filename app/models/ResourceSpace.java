package models;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.*;

import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import enums.CampaignTemplatesEnum;
import enums.ContributionTypes;
import enums.ResourceSpaceTypes;
import enums.ResourceTypes;
import models.transfer.NotificationSignalTransfer;
import models.misc.Views;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="ResourceSpace", description="A Resource Space connects entities in the model to other entities")
public class ResourceSpace extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long resourceSpaceId;

	@JsonView(Views.Public.class)
	@Index
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;
	@Enumerated(EnumType.STRING)
	@JsonView(Views.Public.class)
	private ResourceSpaceTypes type = ResourceSpaceTypes.ASSEMBLY;
	private UUID parent;

	@Transient
	@JsonView(Views.Public.class)
	private String name;

	/**
	 * 
	 * When creating entities with an associated resourceSpace, bare in mind 
	 * the following rules about what can be contained in the lists below
	 * configs 		=> new 
	 * themes 			=> existing and new
	 * campaigns       => existing  
	 * components 		=> existing and new
	 * milestones 		=> new
	 * workingGroups   => existing
	 * contributions   => existing and new
	 * assemblies      => existing 
	 * resources       => new
	 * hashtags        => existing and new
	 * 
	 * The ones marked "existing" means that they will have an special array to
	 * refer to the existing related entities and therefore issue an upudate on 
	 * the resource space rather than a save
	 */
	
	/**
	 * Configuration parameters, e.g., modules and functionalities of modules
	 * that are enabled, things specific to one assembly, future plugins or
	 * extended services configurations.
	 * 
	 */
	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_config")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Config> configs;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch=FetchType.EAGER)
	@JoinTable(name = "resource_space_theme")
	@JsonIgnoreProperties({ "categoryId" })
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Theme> themes;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch=FetchType.EAGER)
	@JoinTable(name = "resource_space_campaign")
	@JsonManagedReference
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Campaign> campaigns;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "resource_space_campaign_components")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Component> components;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_campaign_milestones")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<ComponentMilestone> milestones;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "resource_space_working_groups")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<WorkingGroup> workingGroups;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "resource_space_contributions")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Contribution> contributions;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_contribution_histories")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<ContributionHistory> contributionHistories;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_assemblies")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Assembly> assemblies;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_resource")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Resource> resources;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_hashtag")
	@Where(clause="${ta}.removed=false")
	private List<Hashtag> hashtags;
	
	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_ballots")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Ballot> ballots;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_templates")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<ContributionTemplate> templates;

	
	/*
	 * OneToOne relationships with
	 * - Assembly {resources, forum}
	 * - Working Groups {resources, forum}
	 * - Campaigns {resources}
	 * - Contributions {resourceSpace}
	 * - Component {resourceSpace}
	 */
		
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "resources")
	@JsonIgnore
	private Assembly assemblyResources;
		
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "forum")
	@JsonIgnore
	private Assembly assemblyForum;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "resources")
	@JsonIgnore
	private WorkingGroup workingGroupResources;
		
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "forum")
	@JsonIgnore
	private WorkingGroup workingGroupForum;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "resourceSpace")
	@JsonIgnore
	private Contribution contribution;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "forum")
	@JsonIgnore
	private Campaign campaignForum;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "resources")
	@JsonIgnore
	private Campaign campaign;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "resourceSpace")
	@JsonIgnore
	private Component component;
	
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, ResourceSpace> find = new Finder<>(
			ResourceSpace.class);

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
		return themes !=null ? themes : new ArrayList<>();
	}

	public void setThemes(List<Theme> interests) {
		this.themes = interests;
	}

	public void addTheme(Theme theme) {
		if(this.themes==null)
			this.themes = new ArrayList<>();
		if (!this.themes.contains(theme)) 
			this.themes.add(theme);
	}

	public void removeTheme(Theme category) {
		if(this.themes==null)
			this.themes = new ArrayList<>();
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
		return campaigns != null ? this.campaigns : new ArrayList<>();
	}

	public void setCampaigns(List<Campaign> campaigns) {
		this.campaigns = campaigns;
	}

	public void addCampaign(Campaign c) {
		if(!this.campaigns.contains(c))
			this.campaigns.add(c);
	}

	public Campaign getCampaignForum() {
		return campaignForum;
	}

	public void setCampaignForum(Campaign campaignForum) {
		this.campaignForum = campaignForum;
	}

	public List<Config> getConfigs() {
		return configs!=null ? this.configs : new ArrayList<>();
	}

	public void setConfigs(List<Config> configs) {
		if(this.configs==null)
			this.configs = new ArrayList<>();
		this.configs = configs;
	}

	public void addConfig(Config c) {
		if(this.configs==null)
			this.configs = new ArrayList<>();
		if(!this.configs.contains(c))
			this.configs.add(c);
	}

	public List<Component> getComponents() {
		if(this.components==null)
			this.components = new ArrayList<>();
		return components;
	}

	public void setComponents(List<Component> components) {
		if(this.components==null)
			this.components = new ArrayList<>();
		this.components = components;
	}

	public void addComponent(Component c) {
		if(this.components==null)
			this.components = new ArrayList<>();
		if(!this.components.contains(c))
			this.components.add(c);
	}

	public List<ComponentMilestone> getMilestones() {
		if(this.milestones==null)
			this.milestones = new ArrayList<>();
		return milestones;
	}

	public void setMilestones(List<ComponentMilestone> milestones) {
		if(this.milestones==null)
			this.milestones = new ArrayList<>();
		this.milestones = milestones;
	}

	public List<WorkingGroup> getWorkingGroups() {
		if(this.workingGroups==null)
			this.workingGroups = new ArrayList<>();
		return workingGroups;
	}

	public void setWorkingGroups(List<WorkingGroup> workingGroups) {
		if(this.workingGroups==null)
			this.workingGroups = new ArrayList<>();
		this.workingGroups = workingGroups;
	}

	public void addWorkingGroup(WorkingGroup wg) {
		if(this.workingGroups==null)
			this.workingGroups = new ArrayList<>();
		if(!this.workingGroups.contains(wg))
			this.workingGroups.add(wg);
	}

	public List<Contribution> getContributions() {
		if(this.contributions==null)
			this.contributions = new ArrayList<>();
		return contributions;
	}

	public void setContributions(List<Contribution> contributions) {
		if(this.contributions==null)
			this.contributions= new ArrayList<>();
		this.contributions = contributions;
	}


	public List<ContributionHistory> getContributionHistories() {
		if(this.contributionHistories==null)
			this.contributionHistories = new ArrayList<>();
		return contributionHistories;
	}

	public void setContributionHistories(List<ContributionHistory> contributionHistories) {
		this.contributionHistories = contributionHistories;
	}

	public void addContribution(Contribution c) {
		if(this.contributions==null)
			this.contributions= new ArrayList<>();
		this.contributions.add(c);
	}

	public List<Assembly> getAssemblies() {
		if(this.assemblies==null)
			this.assemblies= new ArrayList<>();
		return assemblies;
	}

	public void setAssemblies(List<Assembly> assemblies) {
		if(this.assemblies==null)
			this.assemblies= new ArrayList<>();
		this.assemblies = assemblies;
	}

	public void addAssembly(Assembly a) {
		if(this.assemblies==null)
			this.assemblies= new ArrayList<>();
		if (!this.assemblies.contains(a)) {
			this.assemblies.add(a);
		}
	}

	public Long getResourceSpaceId() {
		return resourceSpaceId;
	}

	public void setResourceSpaceId(Long resourceSpaceId) {
		this.resourceSpaceId = resourceSpaceId;
	}

	public List<Resource> getResources() {
		if(this.resources==null)
			this.resources= new ArrayList<>();
		return resources;
	}

	public void setResources(List<Resource> resources) {
		if(this.resources==null)
			this.resources= new ArrayList<>();
		this.resources = resources;
	}

	public void addResource(Resource resource) {
		if(this.resources==null)
			this.resources= new ArrayList<>();
		this.resources.add(resource);
	}

	public List<Hashtag> getHashtags() {
		if(this.hashtags==null)
			this.hashtags= new ArrayList<>();
		return hashtags;
	}

	public void setHashtags(List<Hashtag> hashtags) {
		if(this.hashtags==null)
			this.hashtags= new ArrayList<>();
		this.hashtags = hashtags;
	}

	public void addHashtag(Hashtag h) {
		if(this.hashtags==null)
			this.hashtags= new ArrayList<>();
		this.hashtags.add(h);
	}

	public List<Ballot> getBallots() {
		if(this.ballots == null)
			this.ballots = new ArrayList<>();
		return ballots;
	}

	public void setBallots(List<Ballot> ballots) {
		if(this.ballots == null)
			this.ballots= new ArrayList<>();
		this.ballots = ballots;
	}

	public void addBallot(Ballot b) {
		if(this.ballots==null)
			this.ballots= new ArrayList<>();
		this.ballots.add(b);
	}

	public List<ContributionTemplate> getTemplates() {
		if(this.templates==null)
			this.templates= new ArrayList<>();
		return templates;
	}

	public void setTemplates(List<ContributionTemplate> templates) {
		if(this.templates==null)
			this.templates= new ArrayList<>();
		this.templates = templates;
	}

	public void addTemplates(ContributionTemplate ct) {
		if(this.templates==null)
			this.templates= new ArrayList<>();
		this.templates.add(ct);
	}
	
	public Assembly getAssemblyResources() {
		return assemblyResources;
	}

	public void setAssemblyResources(Assembly assemblyResources) {
		this.assemblyResources = assemblyResources;
	}

	public Assembly getAssemblyForum() {
		return assemblyForum;
	}

	public void setAssemblyForum(Assembly assemblyForum) {
		this.assemblyForum = assemblyForum;
	}

	public WorkingGroup getWorkingGroupResources() {
		return workingGroupResources;
	}

	public void setWorkingGroupResources(WorkingGroup workingGroupResources) {
		this.workingGroupResources = workingGroupResources;
	}

	public WorkingGroup getWorkingGroupForum() {
		return workingGroupForum;
	}

	public void setWorkingGroupForum(WorkingGroup workingGroupForum) {
		this.workingGroupForum = workingGroupForum;
	}

	public Contribution getContribution() {
		return contribution;
	}

	public void setContribution(Contribution contribution) {
		this.contribution = contribution;
	}

	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
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
		return find.where().eq("uuid", resourceSetUuid).findUnique();
	}

	public static ResourceSpace createObject(ResourceSpace resourceSet) {
		resourceSet.save();
		return resourceSet;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void deleteByUUID(UUID resourceSetUuid) {
		find.where().eq("uuid", resourceSetUuid).findUnique().delete();
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
		defaultCampaign.setTemplate(ct);

		List<ComponentDefinition> components = ct.getDefComponents();

		for (ComponentDefinition definition : components) {
			Component component = new Component(defaultCampaign, definition);
			defaultCampaign.getResources().getComponents().add(component);
		}
		this.getCampaigns().add(defaultCampaign);
	}

	public static List<ResourceSpace> findByTheme(String theme) {
		return find.where().like("themes.title", "%" + theme + "%").findList();
	}

	public List<Contribution> getContributionsFilteredByType(
			ContributionTypes type) {
		List<Contribution> comments = new ArrayList<>();
		for (Contribution c : this.contributions) {
			if (c.getType().equals(type)) {
				comments.add(c);
			}
		}
		return comments;
	}
	public static ResourceSpace setResourceSpaceItems(ResourceSpace rs,
			ResourceSpace rsNew) {
		if(ResourceSpaceTypes.ASSEMBLY.equals(rsNew.getType())){
			List<Assembly> assemblyList = rsNew.getAssemblies();
			rs.getAssemblies().addAll(assemblyList);
		} else if(ResourceSpaceTypes.CAMPAIGN.equals(rsNew.getType())){
			List<Campaign> campaignList = rsNew.getCampaigns();
			rs.getCampaigns().addAll(campaignList);
		} else if(ResourceSpaceTypes.COMPONENT.equals(rsNew.getType())){
			List<Component> componentsList = rsNew.getComponents();
			rs.getComponents().addAll(componentsList);
		} else if(ResourceSpaceTypes.CONTRIBUTION.equals(rsNew.getType())){
			List<Contribution> contributionList = rsNew.getContributions();
			rs.getContributions().addAll(contributionList);
		} else if(ResourceSpaceTypes.VOTING_BALLOT.equals(rsNew.getType())){
			List<Ballot>  ballotList = rsNew.getBallots();
			rs.getBallots().addAll(ballotList);
		} else if(ResourceSpaceTypes.WORKING_GROUP.equals(rsNew.getType())){
			List<WorkingGroup> workingGroupList = rsNew.getWorkingGroups();
			rs.getWorkingGroups().addAll(workingGroupList);
		}
		return rs;
	}
	
	public void setContributionsFilteredByType(List<Contribution> contributions, ContributionTypes type) {
		// 1. Filter the contributions of "type" from the contribution list
		List<Contribution> filteredContributions = this.contributions.stream()
				.filter(p -> p.getType() != type)
				.collect(Collectors.toList());
		
		// 2. Add the new list of contributions of "type"
		filteredContributions.addAll(contributions);

		// 3. Update the list of contributions
		setContributions(filteredContributions);
	}

	public List<Resource> getResourcesFilteredByType(ResourceTypes type) {
		return this.resources.stream()
				.filter(r -> r.getResourceType() == type )
				.collect(Collectors.toList());
	}
	
	/**
	 * Filter campaign by status where status can be "ongoing", "past" or "upcoming"
	 * @param status
	 * @return
	 */
	public List<Campaign> getCampaignsFilteredByStatus(String status) {
		if (status!=null) {
			switch (status) {
			case "ongoing":
				return this.campaigns.stream().filter(p -> p.getActive()).collect(Collectors.toList());
			case "past":
				return this.campaigns.stream().filter(p -> p.getPast()).collect(Collectors.toList());
			case "upcoming":
				return this.campaigns.stream().filter(p -> p.getUpcoming()).collect(Collectors.toList());
			default:
				return this.campaigns;
			}
		}
		return this.campaigns;
	}
	
	public Config getConfigByKey(String key) {
		List<Config> matchingConfigs = this.configs.stream().filter(p -> p.getKey().equals(key)).collect(Collectors.toList());
		if (matchingConfigs != null && !matchingConfigs.isEmpty()) {
			return matchingConfigs.get(0);
		} 
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
