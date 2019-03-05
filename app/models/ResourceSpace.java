package models;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.*;

import models.misc.Views;

import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.BallotStatus;
import enums.CampaignTemplatesEnum;
import enums.ContributionTypes;
import enums.ManagementTypes;
import enums.MembershipTypes;
import enums.MyRoles;
import enums.ResourceSpaceTypes;
import enums.ResourceTypes;
import play.Logger;

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
	 * configs 			=> new
	 * themes 			=> existing and new
	 * organizations 	=> existing and new
	 * campaigns        => existing
	 * components 		=> existing and new
	 * milestones 		=> new
	 * workingGroups    => existing
	 * contributions    => existing and new
	 * assemblies       => existing
	 * resources        => new
	 * hashtags         => existing and new
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

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_organization")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Organization> organizations;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_custom_field_definition")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<CustomFieldDefinition> customFieldDefinitions;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_custom_field_value")
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<CustomFieldValue> customFieldValues;

	@JsonIgnore
	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_ballot_history")
	@Where(clause="${ta}.removed=false")
	private List<Ballot> ballotHistories;
	
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch=FetchType.EAGER)
	@JoinTable(name = "resource_space_theme")
	@JsonIgnoreProperties({ "categoryId" })
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Theme> themes;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch=FetchType.EAGER)
	@JoinTable(name = "resource_space_campaign")
	@JsonBackReference
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Campaign> campaigns;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "resource_space_campaign_components")
    @JsonBackReference
    @Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Component> components;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_campaign_milestones")
    @JsonBackReference
    @Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<ComponentMilestone> milestones;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "resource_space_working_groups")
    @JsonBackReference
	@Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<WorkingGroup> workingGroups;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "resource_space_contributions")
    @JsonBackReference
    @Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Contribution> contributions;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_contribution_histories")
    @JsonBackReference
    @Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<ContributionHistory> contributionHistories;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_assemblies")
    @JsonBackReference
    @Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Assembly> assemblies;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_resource")
    @JsonBackReference
    @Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Resource> resources;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_hashtag")
    @JsonBackReference
    @Where(clause="${ta}.removed=false")
	private List<Hashtag> hashtags;
	
	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_ballots")
    @JsonBackReference
    @Where(clause="${ta}.removed=false")
	@JsonView(Views.Public.class)
	private List<Ballot> ballots;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "resource_space_templates")
    @JsonBackReference
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
    @JsonBackReference
	private Assembly assemblyResources;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "resourceSpace")
    @JsonIgnore
    @JsonBackReference
	private ResourceSpaceAssociationHistory associationHistoryResources;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "forum")
	@JsonIgnore
    @JsonBackReference
	private Assembly assemblyForum;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "resources")
	@JsonIgnore
    @JsonBackReference
	private WorkingGroup workingGroupResources;
		
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "forum")
	@JsonIgnore
    @JsonBackReference
	private WorkingGroup workingGroupForum;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "resourceSpace")
	@JsonIgnore
    @JsonBackReference
	private Contribution contribution;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "forum")
	@JsonIgnore
    @JsonBackReference
	private Contribution forumContribution;


	@OneToOne(fetch = FetchType.LAZY, mappedBy = "forum")
	@JsonIgnore
    @JsonBackReference
	private Campaign campaignForum;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "resources")
	@JsonIgnore
    @JsonBackReference
	private Campaign campaign;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "resourceSpace")
	@JsonIgnore
    @JsonBackReference
	private Component component;

	@JsonView(Views.Public.class)
	private UUID consensusBallot;
	@Transient
	private String consensusBallotAsString;

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

	public UUID getConsensusBallot() {
		return consensusBallot;
	}

	public void setConsensusBallot(UUID consensusBallot) {
		this.consensusBallot = consensusBallot;
	}

	public String getConsensusBallotAsString() {
		return consensusBallot!=null ? consensusBallot.toString() : null;
	}

	public void setConsensusBallotAsString(String consensusBallotAsString) {
		this.consensusBallotAsString = consensusBallotAsString;
		this.consensusBallot = UUID.fromString(consensusBallotAsString);
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

	public List<Organization> getOrganizations() {
		return organizations!=null ? organizations: new ArrayList<>();
	}

	public void setOrganizations(List<Organization> organizations) {
		this.organizations = organizations;
	}

	public List<CustomFieldDefinition> getCustomFieldDefinitions() {
		return customFieldDefinitions;
	}

	public void setCustomFieldDefinitions(List<CustomFieldDefinition> customFieldDefinitions) {
		this.customFieldDefinitions = customFieldDefinitions;
	}

	public List<CustomFieldValue> getCustomFieldValues() {
		return customFieldValues;
	}

	public void setCustomFieldValues(List<CustomFieldValue> customFieldValues) {
		this.customFieldValues = customFieldValues;
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

	public void addOrganization(Organization organization) {
		if(this.organizations==null)
			this.organizations = new ArrayList<>();
		if (!this.organizations.contains(organization))
			this.organizations.add(organization);
	}

	public void removeOrganization(Organization organization) {
		if(this.organizations==null)
			this.organizations = new ArrayList<>();
		this.organizations.remove(organization);
	}

	public void copyOrganizationsFrom(ResourceSpace target) {
		List<Organization> organizations = target.getOrganizations();
		for (Organization organization : organizations) {
			this.addOrganization(organization);
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

	public Boolean isContributionInSpace(Long id) {
		List<Contribution> result = this.contributions.stream().filter(c -> c.getContributionId() == id).collect(Collectors.toList());
        return result !=null ? result.size()>0 : false;
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

	public ResourceSpaceAssociationHistory getAssociationHistoryResources() {
		return associationHistoryResources;
	}

	public void setAssociationHistoryResources(ResourceSpaceAssociationHistory associationHistoryResources) {
		this.associationHistoryResources = associationHistoryResources;
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

	public Contribution getForumContribution() {
		return forumContribution;
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

	public static ResourceSpace findByContribution(Long resourceSpaceId, Long contributionId) {
		return find.where().eq("resourceSpaceId", resourceSpaceId).eq("contributions.contributionId",contributionId).findUnique();
	}

	public static ResourceSpace findByContributionUuid(UUID resourceSpaceUuid, UUID contributionUuid) {
		return find.where().eq("uuid", resourceSpaceUuid).eq("contributions.uuid",contributionUuid).findUnique();
	}

	public static ResourceSpace findByAssembly(Long resourceSpaceId, Long assemblyId) {
		return find.where().eq("resourceSpaceId", resourceSpaceId).eq("assemblies.assemblyId",assemblyId).findUnique();
	}

	public static ResourceSpace findByConfig(Long resourceSpaceId, UUID configUuid) {
		return find.where().eq("resourceSpaceId", resourceSpaceId).eq("configs.uuid",configUuid).findUnique();
	}

	public static ResourceSpace findByBallot(Long resourceSpaceId, Long ballotId) {
		return find.where().eq("resourceSpaceId", resourceSpaceId).eq("ballots.id",ballotId).findUnique();
	}

	public static ResourceSpace findByCampaign(Long resourceSpaceId, Long campaignId) {
		return find.where().eq("resourceSpaceId", resourceSpaceId).eq("campaigns.campaignId",campaignId).findUnique();
	}

	public static ResourceSpace findByComponent(Long resourceSpaceId, Long componentId) {
		return find.where().eq("resourceSpaceId", resourceSpaceId).eq("components.componentId",componentId).findUnique();
	}

	public static ResourceSpace findByTheme(Long resourceSpaceId, Long themeId) {
		return find.where().eq("resourceSpaceId", resourceSpaceId).eq("themes.themeId",themeId).findUnique();
	}

	public static ResourceSpace findByResource(Long resourceSpaceId, Long resourceId) {
		return find.where().eq("resourceSpaceId", resourceSpaceId).eq("resources.resourceId",resourceId).findUnique();
	}

	public static ResourceSpace findByGroup(Long resourceSpaceId, Long groupId) {
		return find.where().eq("resourceSpaceId", resourceSpaceId).eq("workingGroups.groupId",groupId).findUnique();
	}

	public static ResourceSpace findByMilestone(Long resourceSpaceId, Long milestoneId) {
		return find.where().eq("resourceSpaceId", resourceSpaceId).eq("milestones.componentMilestoneId",milestoneId).findUnique();
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
		List<Contribution> cPerType = new ArrayList<>();
		for (Contribution c : this.contributions) {
			if (c.getType().equals(type)) {
				cPerType.add(c);
			}
		}
		return cPerType;
	}
	
	
	public static ResourceSpace setResourceSpaceItems(ResourceSpace rs,
			ResourceSpace rsNew) {
		if(ResourceSpaceTypes.ASSEMBLY.equals(rsNew.getType())){
			rs.getAssemblies().add(rsNew.getAssemblyResources());
		} else if(ResourceSpaceTypes.CAMPAIGN.equals(rsNew.getType())){
			rs.getCampaigns().add(rsNew.getCampaign());
		} else if(ResourceSpaceTypes.COMPONENT.equals(rsNew.getType())){
			rs.getComponents().add(rsNew.getComponent());
		} else if(ResourceSpaceTypes.CONTRIBUTION.equals(rsNew.getType())){
			rs.getContributions().add(rsNew.getContribution());
		} else if(ResourceSpaceTypes.WORKING_GROUP.equals(rsNew.getType())){
			rs.getWorkingGroups().add(rsNew.getWorkingGroupResources());
		}
		return rs;
	}

	public static Boolean isCoordinatorResourceSpace(User u, ResourceSpace rs) {
		Boolean allowed = false;
		if(ResourceSpaceTypes.ASSEMBLY.equals(rs.getType())){
			AssemblyProfile ap = null;
			Membership m = null;
			Assembly a = rs.getAssemblyResources();
			if (a!=null) {
				m = MembershipAssembly.findByUserAndAssemblyIds(u.getUserId(), a.getAssemblyId());
				ap = a.getProfile();
				Boolean assemblyNotOpen = true;
				if (ap!=null) {
					assemblyNotOpen = ap.getManagementType().equals(ManagementTypes.OPEN);

				}
				if (m!=null && assemblyNotOpen) {
					List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.COORDINATOR.getName());
					allowed = membershipRoles != null && !membershipRoles.isEmpty();
				}
			}
		} else if(ResourceSpaceTypes.CONTRIBUTION.equals(rs.getType())){
			Contribution contribution = rs.getContribution();
			if(contribution!=null){
				allowed = Contribution.isUserAuthor(u,contribution.getContributionId());
			}
		} else if(ResourceSpaceTypes.WORKING_GROUP.equals(rs.getType())){
			WorkingGroup group =  rs.getWorkingGroupResources();
			if(group!=null){
				Membership m = MembershipGroup.findByUserAndGroupId(u.getUserId(), group.getGroupId());
				WorkingGroup wg = WorkingGroup.read(group.getGroupId());
				Boolean groupNotOpen = !wg.getProfile().getManagementType().equals(ManagementTypes.OPEN);
				if(wg.getIsTopic()){
					groupNotOpen = false;
				}
				if (m!=null && groupNotOpen) {
					List<SecurityRole> membershipRoles = m.filterByRoleName(MyRoles.COORDINATOR.getName());
					allowed = membershipRoles != null && !membershipRoles.isEmpty();
				}
			}
		}else{
			allowed=true;
		}
		return allowed;
	}

	public static Boolean isMemberResourceSpace(User u, ResourceSpace rs, Contribution c) {
		Boolean allowed = false;
		if(ResourceSpaceTypes.ASSEMBLY.equals(rs.getType())){
			AssemblyProfile ap = null;
			Membership m = null;
			Assembly a = rs.getAssemblyResources();
			if (a!=null) {
				m = MembershipAssembly.findByUserAndAssemblyIds(u.getUserId(), a.getAssemblyId());
				ap = a.getProfile();
				Boolean assemblyNotOpen = true;
				if (ap!=null) {
					assemblyNotOpen = ap.getManagementType().equals(ManagementTypes.OPEN);

				}
				allowed = m!=null;
				if(!allowed) {
					// Check if the user has been invited. In which case, it will be considered a member
					MembershipInvitation mi = MembershipInvitation.findByUserIdTargetIdAndType(u.getUserId(), a.getAssemblyId(), MembershipTypes.ASSEMBLY);
					allowed =  mi!=null;
				}
			}
		} else if(!(ContributionTypes.COMMENT.equals(c.getType()) || ContributionTypes.DISCUSSION.equals(c.getType())) && ResourceSpaceTypes.WORKING_GROUP.equals(rs.getType())){
			WorkingGroup group =  rs.getWorkingGroupResources();
			if(group!=null){
				Membership m = MembershipGroup.findByUserAndGroupId(u.getUserId(),group.getGroupId());
				WorkingGroup wg = WorkingGroup.read(group.getGroupId());
				Boolean groupNotOpen = !wg.getProfile().getManagementType().equals(ManagementTypes.OPEN);
				if(wg.getIsTopic()){
					groupNotOpen = false;
				}
				allowed = m!=null;
				if(!allowed) {
					// Check if the user has been invited. In which case, it will be considered a member
					MembershipInvitation mi = MembershipInvitation.findByUserIdTargetIdAndType(u.getUserId(), wg.getGroupId(), MembershipTypes.GROUP);
					allowed =  mi!=null;
				}
			}
		}
		return allowed;
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
	public List<Campaign> getCampaignsFilteredByStatus(String status) throws Exception {
		try {
		if (status!=null) {
			switch (status) {
			case "ongoing":
				return this.campaigns
						.stream()
						.filter(p -> p.getActive())
                        .sorted(Comparator.comparing(Campaign::getStartDate).reversed())
						.collect(Collectors.toList());
			case "past":
				return this.campaigns.stream()
                        .filter(p -> p.getPast())
                        .sorted(Comparator.comparing(Campaign::getStartDate).reversed())
                        .collect(Collectors.toList());
			case "upcoming":
				return this.campaigns.
						stream()
						.filter(p -> p.getUpcoming())
                        .sorted(Comparator.comparing(Campaign::getStartDate))
						.collect(Collectors.toList());
			default:
				return this.campaigns
						.stream()
						.sorted(Comparator.comparing(Campaign::getStartDate).reversed())
						.collect(Collectors.toList());
			}
		}
		return this.campaigns;
		} catch (NullPointerException exp) {
			throw new Exception("Some campaigns have not components (startDate)");
		}

	}

	public List<Ballot> getBallotsFilteredByStatusDate(BallotStatus status, Date startsAt, Date endsAt) {
		List<Ballot> filtered = this.ballots;
		if (status!=null) {
			filtered = filtered.stream().filter(p -> p.getStatus() == status).collect(Collectors.toList());
		}
		if (startsAt!=null) {
			filtered = filtered.stream().filter(p -> p.getStartsAt().after(startsAt)).collect(Collectors.toList());
		}
		if (endsAt!=null) {
			filtered = filtered.stream().filter(p -> p.getEndsAt().before(endsAt)).collect(Collectors.toList());
		}
		return filtered;
	}

	public List<WorkingGroup> getGroupsFilteredByTopic() {
		return this.workingGroups.stream().filter(p -> p.getIsTopic()).collect(Collectors.toList());
	}

	public Config getConfigByKey(String key) {
		if (configs!=null) {
			List<Config> matchingConfigs = this.configs.stream().filter(p -> p.getKey().equals(key)).collect(Collectors.toList());
			if (matchingConfigs != null && !matchingConfigs.isEmpty()) {
				return matchingConfigs.get(0);
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Ballot> getBallotHistories() {
		return ballotHistories;
	}

	public void setBallotHistories(List<Ballot> ballotHistories) {
		this.ballotHistories = ballotHistories;
	}
	
	// Analytics
	public Map<String,Map<String,Map<String,Integer>>> contributionCountPerType(String includeThemes, String includeUserInsights, Long userId) {
		Map<String,Map<String,Integer>> contributionCountMap = new HashMap<>();
		Map<String,Map<String,Integer>> themeContribCountMap = new HashMap<>();
		Map<String,Map<String,Map<String,Integer>>> result = new HashMap<>();

		for (Contribution c : this.contributions) {
			Map<String,Integer> contributionTypeMap = contributionCountMap.get(c.getType().toString());
			if (contributionTypeMap == null) {
				contributionTypeMap = new HashMap<>();
				contributionTypeMap.put("TOTAL", 1);
				contributionTypeMap.put(c.getStatus().toString(),1);
				
				Integer authorCount = c.getAuthors().size();
				Integer nonMemberAuthorCount = c.getNonMemberAuthors().size(); 
				contributionTypeMap.put("MEMBER_AUTHORS",authorCount);
				contributionTypeMap.put("NON_MEMBER_AUTHORS",nonMemberAuthorCount);
				contributionCountMap.put(c.getType().toString(),contributionTypeMap);
            } else {
				Integer currentTotal = contributionTypeMap.get("TOTAL");
				Integer currentForStatus = contributionTypeMap.get(c.getStatus().toString());
				contributionTypeMap.put("TOTAL", currentTotal+1);
				contributionTypeMap.put(c.getStatus().toString(),currentForStatus!=null ? currentForStatus+1 : 1);
				
				Integer authorCount = c.getAuthors().size();
				Integer nonMemberAuthorCount = c.getNonMemberAuthors().size(); 
				Integer currentAuthorTotal = contributionTypeMap.get("MEMBER_AUTHORS");
				Integer currentNonMemberAuthorTotal = contributionTypeMap.get("NON_MEMBER_AUTHORS");
				contributionTypeMap.put("MEMBER_AUTHORS",currentAuthorTotal!=null ? currentAuthorTotal+authorCount:authorCount);
				contributionTypeMap.put("NON_MEMBER_AUTHORS",currentNonMemberAuthorTotal!=null? currentNonMemberAuthorTotal+nonMemberAuthorCount:nonMemberAuthorCount);
				contributionCountMap.put(c.getType().toString(),contributionTypeMap);
			}

            if (includeUserInsights!= "" && includeUserInsights.equals("true")) {
				Boolean contributionHasCreator = c!=null && c.getCreator() !=null;
				Boolean userIsCreator = contributionHasCreator && c.getCreator().getUserId().equals(userId);

                if (userIsCreator) {
                    Integer curretMinetotal = contributionTypeMap.get("MINE");
                    contributionTypeMap.put("MINE",curretMinetotal!=null?curretMinetotal+1:1);
                } else {
                    for (User u : c.getAuthors()) {
                        Long sharedUserId = u.getUserId();
                        if (sharedUserId.equals(userId)) {
                            Integer currentTotal= contributionTypeMap.get("SHARED_WITH");
                            contributionTypeMap.put("SHARED_WITH",currentTotal!=null?currentTotal+1:1);
                        }
                    }
                }
            }
			Integer currentAuthorTotal = contributionTypeMap.get("MEMBER_AUTHORS");
			Integer currentNonMemberAuthorTotal = contributionTypeMap.get("NON_MEMBER_AUTHORS");
			contributionTypeMap.put("AUTHORS",currentAuthorTotal+currentNonMemberAuthorTotal);

			if (c.getType().equals(ContributionTypes.DISCUSSION) || c.getType().equals(ContributionTypes.COMMENT) ) {
				contributionTypeMap = contributionCountMap.get("DISCUSSION_COMMENT");
				if (contributionTypeMap == null) {
					contributionTypeMap = new HashMap<>();
					contributionTypeMap.put("TOTAL", 1);
				} else {
					Integer currentTotal = contributionTypeMap.get("TOTAL");
					contributionTypeMap.put("TOTAL", currentTotal+1);
				}
			}

			if (includeThemes != "" && includeThemes.equals("true")) {
				for (Theme t : c.getThemes()) {
					Map<String,Integer> themeTypeMap = themeContribCountMap.get(t.getType().toString());
					if (themeTypeMap==null) {
						themeTypeMap = new HashMap<>();
						themeTypeMap.put(t.getTitle(),1);
						themeContribCountMap.put(t.getType().toString(),themeTypeMap);
					} else {
						Integer currentTotal = themeTypeMap.get(t.getTitle());
						themeTypeMap.put(t.getTitle(),currentTotal!=null ? currentTotal+1 : 1);
					}
				}
			}

		}


        result.put("contributions_per_type",contributionCountMap);
		result.put("contributions_per_theme",themeContribCountMap);
		return result;
	}
	
	public Map<String,Integer> resourceCountPerType() {
		Map<String,Integer> countMap = new HashMap<String, Integer>();
		for (Resource r : this.resources) {
			Integer currentValue = countMap.get(r.getResourceType().toString());
			countMap.put(r.getResourceType().toString(),currentValue !=null ? currentValue+1 : 1);
		}
		return countMap;
	}
	
	public Map<String,Integer> themeCountPerType() {
		Map<String,Integer> countMap = new HashMap<String, Integer>();
		for (Theme t : this.themes) {
			Integer currentValue = countMap.get(t.getType().toString());
			countMap.put(t.getType().toString(),currentValue !=null ? currentValue+1 : 1);	
		}
		return countMap;
	}
	
	public Map<String,Integer> getCampaignCountByStatus() {
		Map<String,Integer> campaignCount = new HashMap<>();
		for (Campaign c : campaigns) {
			if (c.getActive()) {
				Integer currentValue = campaignCount.get("ONGOING");
				campaignCount.put("ONGOING",currentValue !=null ? currentValue+1 : 1);
			} else if (c.getPast()) {
				Integer currentValue = campaignCount.get("PAST");
				campaignCount.put("PAST",currentValue !=null ? currentValue+1 : 1);
			} else if (c.getUpcoming()) {
				Integer currentValue = campaignCount.get("UPCOMING");
				campaignCount.put("UPCOMING",currentValue !=null ? currentValue+1 : 1);
			}
		}
		return campaignCount;
	}
}
