package models;

import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;
import enums.AssemblyStatus;
import enums.ConfigTargets;
import enums.MembershipStatus;
import enums.ResourceSpaceTypes;
import exceptions.MembershipCreationException;
import io.swagger.annotations.ApiModel;
import models.location.Location;
import models.misc.Views;
import play.Logger;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import utils.GlobalData;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * An assembly represents the central repository of a group of people interested
 * in organizing themselves as a community for engaging in social activism 
 * 
 * @author cdparra
 */
@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="Assembly", description="An Assembly is an a space owned by a group of people to organize democratic actions")
public class Assembly extends AppCivistBaseModel {
	@JsonView(Views.Public.class)
	@Id @GeneratedValue @Column(name="assembly_id")
	private Long assemblyId;
	@Index
	@JsonView(Views.Public.class)
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;
	@MaxLength(value = 200) @Required
	@JsonView(Views.Public.class)
	private String name; 
	// Short, url friendly, version of the Name
	@MaxLength(value = 120)
	@JsonView(Views.Public.class)
	private String shortname; 
	@Required
	@Column(name="description", columnDefinition="text")
	@JsonView(Views.Public.class)
	private String description;
	@JsonView(Views.Public.class)
	private String url; 
	// If true, the 'profile' is public
	@JsonView(Views.Public.class)
	private Boolean listed = true;
	@Column(name="invitationEmail", columnDefinition="text")
	@JsonView(Views.Public.class)
	private String invitationEmail;
    @Column(name="principalAssembly", columnDefinition="text")
	@JsonView(Views.Public.class)
	private Boolean principalAssembly = false;

	@JsonView(Views.Public.class)
	@OneToOne(cascade=CascadeType.ALL)
	@JsonIgnoreProperties({ "assemblyProfileId", "assembly" })
	@JsonInclude(Include.NON_EMPTY)
	private AssemblyProfile profile = new AssemblyProfile();

	@JsonView(Views.Public.class)
	@ManyToOne(cascade=CascadeType.ALL)
	private Location location = new Location();

	/**
	 * The assembly resource set is where all the campaign, configurations,
	 * themes and general contributions are stored. Other resource spaces will
	 * be added if needed under proper names
	 */
	@OneToOne(cascade=CascadeType.ALL)
	@JsonInclude(Include.NON_EMPTY)
	@Where(clause="${ta}.removed=false")
	@JsonIgnore
	private ResourceSpace resources = new ResourceSpace(ResourceSpaceTypes.ASSEMBLY);

	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonInclude(Include.NON_EMPTY)
	@Where(clause="${ta}.removed=false")
	@JsonIgnore
	private ResourceSpace forum = new ResourceSpace(ResourceSpaceTypes.ASSEMBLY);

	/**
	 * The User who created the Assembly
	 */
	@JsonView(Views.Public.class)
	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonInclude(Include.NON_EMPTY)
	@Where(clause="${ta}.removed=false")
	private User creator; // user who has created the assembly?
	
	// Back reference relationships to ignore when parsing to json but to keep to allow cascade deletion
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="assembly")
	@JsonIgnore
	private List<MembershipAssembly> memberships;
	
	// Shortcuts to resources in the Assembly Resource Space ('resources')
	@JsonView(Views.Public.class)
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Component> components = new ArrayList<>();
	@JsonView(Views.Public.class)
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Config> configs = new ArrayList<>();
	@JsonView(Views.Public.class)
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Theme> themes = new ArrayList<>();
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Organization> organizations = new ArrayList<>();
	@JsonView(Views.Public.class)
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	@JsonIgnoreProperties({ "configs", "forumPosts", "proposals", "brainstormingContributions"})
	private List<WorkingGroup> workingGroups = new ArrayList<>();
	@JsonView(Views.Public.class)
	@Transient
	@JsonInclude(Include.NON_EMPTY)	
	@JsonIgnoreProperties({ "configs", "workingGroups"})
	private List<Campaign> campaigns = new ArrayList<>();
	@JsonView(Views.Public.class)
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Contribution> forumPosts = new ArrayList<>();
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private Long forumResourceSpaceId;
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private Long resourcesResourceSpaceId;
	@Transient
	@JsonView(Views.Public.class)
	@JsonInclude(Include.NON_EMPTY)
	private UUID forumResourceSpaceUUID;
	@Transient
	@JsonView(Views.Public.class)
	@JsonInclude(Include.NON_EMPTY)
	private UUID resourcesResourceSpaceUUID;
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	@JsonIgnoreProperties({ "configs", "campaigns", "forumPosts", "workingGroups", "components", "followedAssemblies", "followingAssemblies"})
	private List<Assembly> followedAssemblies = new ArrayList<>();
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	@JsonIgnoreProperties({ "configs", "campaigns", "forumPosts", "workingGroups", "components", "followedAssemblies", "followingAssemblies"})
	private List<Assembly> followingAssemblies = new ArrayList<>();
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Component> existingComponents = new ArrayList<>();
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Config> existingConfigs = new ArrayList<>();
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Theme> existingThemes = new ArrayList<>();
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<WorkingGroup> existingWorkingGroups = new ArrayList<>();
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Campaign> existingCampaigns = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	private AssemblyStatus status;
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, Assembly> find = new Finder<>(Assembly.class);

	/**
	 * Empty constructor
	 */
	public Assembly() {
		super();
		this.uuid = UUID.randomUUID();
		this.setDefaultValues();
	}

	/**
	 * Basic assembly constructor (with the most basic elements of an assembly)
	 * 
	 * @param
	 * @param assemblyDescription
	 * @param assemblyCity
	 */
	public Assembly(String assemblyName, String assemblyDescription,
			String assemblyCity) {
		super();
		this.uuid = UUID.randomUUID();
		this.name = assemblyName;
		this.description = assemblyDescription;
		this.location = new Location(null, assemblyCity, null, null, null);
		this.setDefaultValues();
	}

	public Assembly(User creator, String name, String description, String city,
			String state, String country, AssemblyProfile profile) {
		super();
		this.uuid = UUID.randomUUID();
		this.creator = creator;
		this.name = name;
		this.description = description;
		this.location = new Location(null, city, state, null, country);
		this.profile = profile;
		this.setDefaultValues();
	}

	public Assembly(User creator, String name, String shortname,
			String description, Location location, UUID uuid,
			AssemblyProfile profile, ResourceSpace resources) {
		super();
		this.uuid = uuid;
		this.creator = creator;
		this.name = name;
		this.shortname = shortname;
		this.description = description;
		this.location = location;
		this.profile = profile;
		this.resources = resources;
	}
	
	public Assembly(User creator, String name, String shortname,
			String description, Location location, String uuid,
			AssemblyProfile profile, ResourceSpace resources) {
		super();
		this.uuidAsString = uuid;
		this.uuid = UUID.fromString(this.uuidAsString);
		this.creator = creator;
		this.name = name;
		this.shortname = shortname;
		this.description = description;
		this.location = location;
		this.profile = profile;
		this.resources = resources;
	}

	
	/**
	 * Tasks to perform the first time an assembly is created/persisted
	 */
	@PrePersist
	public void onPrePersist() {
		// 1. Check configuration values targetUUID points to the assembly
		if(this.resources!=null && this.resources.getConfigs()!=null) {
			for (Config c : this.resources.getConfigs()) {
				if(c.getTargetUuid()==null) {
					c.setTargetUuid(this.uuid);
					c.setConfigTarget(ConfigTargets.ASSEMBLY);
					String key = c.getKey();
					ConfigDefinition cd = ConfigDefinition.findByKey(key);
					c.setDefinition(cd);
				}
			}
		}
	}
	
	/*
	 * Getters and Setters
	 */

	public Long getAssemblyId() {
		return assemblyId;
	}

	public void setAssemblyId(Long assemblyId) {
		this.assemblyId = assemblyId;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public AssemblyProfile getProfile() {
		return this.profile;
	}

	public void setProfile(AssemblyProfile p) {
		this.profile = p;
	}

	/*
	 * Basic Data Queries
	 */

	public ResourceSpace getResources() {
		return resources;
	}

	public void setResources(ResourceSpace resources) {
		this.resources = resources;
	}

	public ResourceSpace getForum() {
		return forum;
	}

	public void setForum(ResourceSpace forum) {
		this.forum = forum;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getUuidAsString() {
		return uuid!=null ? uuid.toString() : null;
	}

	public void setUuidAsString(String uuidAsString) {
		this.uuidAsString = uuidAsString;
		this.uuid = UUID.fromString(uuidAsString);
	}

	public Boolean getListed() {
		return listed;
	}

	public void setListed(Boolean listed) {
		this.listed = listed;
	}

	public String getInvitationEmail() {
		return invitationEmail;
	}

	public void setInvitationEmail(String invitationEmail) {
		this.invitationEmail = invitationEmail;
	}

	public List<MembershipAssembly> getMemberships() {
		return memberships;
	}

	public void setMemberships(List<MembershipAssembly> memberships) {
		this.memberships = memberships;
	}

	public List<Theme> getThemes() {
		return resources != null ? resources.getThemes() : null;
	}

	public void setThemes(List<Theme> themes) {
		this.themes = themes;
		this.resources.setThemes(themes);
	}

	public List<Component> getComponents() {
		return resources != null ? resources.getComponents() : null;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
		this.resources.setComponents(components);
	}

	public List<Config> getConfigs() {
		return resources != null ? resources.getConfigs() : null;
	}

	public void setConfigs(List<Config> configs) {
		this.configs = configs;
		this.resources.setConfigs(configs);
	}

	public List<WorkingGroup> getWorkingGroups() {
		return resources != null ? resources.getWorkingGroups() : null;
	}

	public void setWorkingGroups(List<WorkingGroup> workingGroups) {
		this.workingGroups = workingGroups;
		this.resources.setWorkingGroups(workingGroups);
	}
	
	public List<Campaign> getCampaigns() {
		return resources != null ? resources.getCampaigns() : null;
	}
	public void setCampaigns(List<Campaign> campaigns) {
		this.campaigns = campaigns;
		this.resources.setCampaigns(campaigns);
	}

	public List<Organization> getOrganizations() {
		return  resources != null ? resources.getOrganizations() : null;
	}

	public void setOrganizations(List<Organization> organizations) {
		this.organizations = organizations;
		this.resources.setOrganizations(organizations);
	}

	public List<Contribution> getForumPosts() {
		return forum.getContributions();
	}

	public void setForumPosts(List<Contribution> forumPosts) {
		this.forum.setContributions(forumPosts);
	}

	public void addForumPosts(Contribution forumPost) {
		this.forum.getContributions().add(forumPost);
	}

	public Long getForumResourceSpaceId() {
		return forum !=null ? forum.getResourceSpaceId() : null;
	}
	
	public void setForumResourceSpaceId(Long id) {
		if(this.forum!=null && this.forum.getResourceSpaceId() == null)
			this.forum.setResourceSpaceId(id);
	}

	public Long getResourcesResourceSpaceId() {
		return resources !=null ? resources.getResourceSpaceId() : null;
	}
	
	public void setResourcesResourceSpaceId(Long id) {
		if(this.resources!=null && this.resources.getResourceSpaceId() == null)
			this.resources.setResourceSpaceId(id);
	}

	public UUID getForumResourceSpaceUUID() {
		return forum !=null ? forum.getResourceSpaceUuid(): null;
	}

	public void setForumResourceSpaceUUID(UUID forumResourceSpaceUUID) {
		if(this.forum!=null && this.forum.getResourceSpaceUuid() == null)
			this.resources.setResourceSpaceUuid(forumResourceSpaceUUID);	}

	public UUID getResourcesResourceSpaceUUID() {
		return resources !=null ? resources.getResourceSpaceUuid() : null;
	}

	public void setResourcesResourceSpaceUUID(UUID resourcesResourceSpaceUUID) {
		if(this.resources!=null && this.resources.getResourceSpaceUuid() == null)
			this.resources.setResourceSpaceUuid(resourcesResourceSpaceUUID);	}

	public List<Assembly> getFollowedAssemblies() {
		return this.resources.getAssemblies();
	}

	public void setFollowedAssemblies(List<Assembly> followedAssemblies) {
		this.resources.setAssemblies(followedAssemblies);
	}

	public List<Assembly> getFollowingAssemblies() {
		return followingAssemblies;
	}

	public void setFollowingAssemblies(List<Assembly> followingAssemblies) {
		this.followingAssemblies = followingAssemblies;
	}

	public List<Component> getExistingComponents() {
		return existingComponents;
	}

	public void setExistingComponents(List<Component> existingComponents) {
		this.existingComponents = existingComponents;
	}

	public List<Config> getExistingConfigs() {
		return existingConfigs;
	}

	public void setExistingConfigs(List<Config> existingConfigs) {
		this.existingConfigs = existingConfigs;
	}

	public List<Theme> getExistingThemes() {
		return existingThemes;
	}

	public void setExistingThemes(List<Theme> existingThemes) {
		this.existingThemes = existingThemes;
	}

	public List<WorkingGroup> getExistingWorkingGroups() {
		return existingWorkingGroups;
	}

	public void setExistingWorkingGroups(List<WorkingGroup> existingWorkingGroups) {
		this.existingWorkingGroups = existingWorkingGroups;
	}
	public List<Campaign> getExistingCampaigns() {
		return existingCampaigns;
	}
	public void setExistingCampaigns(List<Campaign> existingCampaigns) {
		this.existingCampaigns = existingCampaigns;
	}

	/**
	 * Returns all the assemblies in our system
	 * 
	 * @return
	 */
	public static List<Assembly> findAll(Boolean onlyListed) {
		if(onlyListed) return find.where().eq("listed",true).findList();
		else return find.all();
	}

	public static void createMembership(Assembly a) throws MembershipCreationException {
        // 5. Add the creator as a members with roles MODERATOR, COORDINATOR and MEMBER
        MembershipAssembly ma = new MembershipAssembly();
        ma.setAssembly(a);
        ma.setCreator(a.getCreator());
        ma.setUser(a.getCreator());
        ma.setStatus(MembershipStatus.ACCEPTED);
        ma.setLang(a.getLang());

        List<SecurityRole> roles = new ArrayList<SecurityRole>();
        roles.add(SecurityRole.findByName("MEMBER"));
        roles.add(SecurityRole.findByName("COORDINATOR"));
        roles.add(SecurityRole.findByName("MODERATOR"));
        ma.setRoles(roles);

        MembershipAssembly.create(ma);

        if (a.getUrl() == null || a.getUrl() == "") {
            a.setUrl(GlobalData.APPCIVIST_ASSEMBLY_BASE_URL + "/"
                    + a.getAssemblyId());
        }

    }

	public static void createResources(Assembly a) {
        // 1. Check first for existing entities in ManyToMany relationships.
        // Save them for later update

        List<Theme> existingThemes = a.getExistingThemes();



        // 3. Add existing entities in relationships to the manytomany resources
        // then update
		Assembly assembly = Assembly.read(a.getAssemblyId());
        ResourceSpace assemblyResources = assembly.getResources();

        if (existingThemes != null && !existingThemes.isEmpty())
            assemblyResources.getThemes().addAll(existingThemes);
		assemblyResources.update();
        a.refresh();


    }

	public static void create(Assembly a) throws MembershipCreationException {
		if (a.getAssemblyId() != null
				&& (a.getUrl() == null || a.getUrl() == "")) {
			a.setUrl(GlobalData.APPCIVIST_ASSEMBLY_BASE_URL + "/"
					+ a.getAssemblyId());
		}
        // 1. Check first for existing entities in ManyToMany relationships.
        // Save them for later update
        List<Component> existingComponents = a.getExistingComponents();
        List<WorkingGroup> existingWorkingGroups = a.getExistingWorkingGroups();
        List<Campaign> existingCampaigns = a.getExistingCampaigns();
        List<Assembly> followedAssemblies = a.getFollowedAssemblies();

        List<Organization> organizations = new ArrayList<Organization>();
        for (Organization organization : a.getOrganizations()) {
            if (organization.getOrganizationId() != null) {
                Organization existingOrganization = Organization.read(organization.getOrganizationId());
                organizations.add(existingOrganization);
            } else {
                organizations.add(organization);
            }
        }
        a.setOrganizations(organizations);
        // 2. Create the new assembly
        a.save();

        // 3. Add existing entities in relationships to the manytomany resources
        // then update
        ResourceSpace assemblyResources = a.getResources();

        if (existingComponents != null && !existingComponents.isEmpty())
            assemblyResources.getComponents().addAll(existingComponents);
        if (organizations != null)
            assemblyResources.setOrganizations(organizations);
        if (existingWorkingGroups != null && !existingWorkingGroups.isEmpty())
            assemblyResources.getWorkingGroups().addAll(existingWorkingGroups);
        if (existingCampaigns != null && !existingCampaigns.isEmpty())
            assemblyResources.getCampaigns().addAll(existingCampaigns);
        if (followedAssemblies != null && !followedAssemblies.isEmpty())
            assemblyResources.getAssemblies().addAll(followedAssemblies);

        assemblyResources.update();

        // 4. Refresh the new campaign to get the newest version
        a.refresh();
		
		if (a.getUrl() == null || a.getUrl() == "") {
			a.setUrl(GlobalData.APPCIVIST_ASSEMBLY_BASE_URL + "/"
					+ a.getAssemblyId());
		}
	}

	public static Assembly read(Long assemblyId) {
		return find.ref(assemblyId);
	}

	public static Assembly createObject(Assembly assembly) {
		assembly.save();
		return assembly;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static Assembly update(Assembly a) {
		List<Theme> themes = new ArrayList<>();
		for (Theme theme : a.getThemes()) {
			if (theme.getThemeId() == null) {
				themes.add(theme);
			} else {
				theme.update();
				theme.refresh();
			}
		}
		
		if (!themes.isEmpty()) {
			a.setThemes(themes);
		} else {
			a.setThemes(null);
		}
		
		a.update();
		a.refresh();
		return a;
	}

	public void setDefaultValues() {
		// 1. Set default shortname if not given
		if (this.shortname == null) {
			if (this.name != null) {
				this.shortname = name.replaceAll("[^\\w]", "")
						.replaceAll("[\\s]", "-").toLowerCase();
			}
		}
		if (this.resources==null) {
			this.resources = new ResourceSpace(ResourceSpaceTypes.ASSEMBLY);
		}
		this.forum = new ResourceSpace(ResourceSpaceTypes.ASSEMBLY);
	}

	public static List<Assembly> findBySimilarName(String query, Boolean onlyListed) {
		Query<Assembly> q = find.where().ilike("name", "%" + query + "%").query();
		if(onlyListed) q = q.where().eq("listed",true).query();
		return q.findList();
		
	}

	public static List<Assembly> findFeaturedAssemblies(String query, Boolean onlyListed) {
		Query<Assembly> q = find.setMaxRows(6).orderBy("creation");
		q = addQueryCriteria("name",query, q);
		if(onlyListed) q = q.where().eq("listed",true).query();
		return q.findList();
	}

	public static List<Assembly> findRandomAssemblies(String query, Boolean onlyListed) {
		Query<Assembly> q = find.setMaxRows(6).orderBy("random()");
		q = addQueryCriteria("name",query, q);
		if(onlyListed) q = q.where().eq("listed",true).query();
		return q.findList();
	}

	private static Query<Assembly> addQueryCriteria(String property,  String query, Query<Assembly> q) {
		if(query!=null && !query.isEmpty()) {
			q = q.where().ilike(property, "%"+query+"%").query();
		}
		return q;
	}

	public static Assembly readByUUID(UUID assemblyUUID) {
		return find.where().eq("uuid", assemblyUUID).findUnique();
	}

	public List<Theme> extractExistingThemes() {
		List<Theme> existing = new ArrayList<Theme>();
		if (this.resources!=null) {
			List<Theme> themes = this.resources.getThemes();
			if(themes!=null) {
				for (Theme theme : themes) {
					Long id = theme.getThemeId();
					if (id!=null) {
						this.removeTheme(theme);
						existing.add(theme);
					}
				}
			}
		}
		return existing;
	}

	public void removeTheme(Theme theme) {
		this.resources.removeTheme(theme);
	}
	
	public void addTheme(Theme theme) {
		this.resources.addTheme(theme);
	}
	
	public void addThemes(List<Theme> themes) {
		this.resources.getThemes().addAll(themes);
	}
	
	public void updateResources() {
		this.resources.update();
	}

	public AssemblyStatus getStatus() {
		return status;
	}

	public void setStatus(AssemblyStatus status) {
		this.status = status;
	}

	public static Boolean isAssemblyListed(Long id) {
		return find.where().eq("assemblyId",id).eq("listed",true).findUnique()!=null;
	}
	
	public static Boolean isAssemblyListed(UUID uuid) {
		return find.where().eq("assemblyUuid",uuid).eq("listed",true).findUnique()!=null;
	}

	public static int findCampaignWithTitle(Long aid, String title) {
		return find.where().eq("assemblyId",aid).eq("resources.campaigns.title",title).findList().size();
	}

	public static List<Assembly> findAssemblyFromCampaign(Long cid) {
		return find.where().eq("resources.campaigns.campaignId",cid).findList();
	}

	public static List<Assembly> findAssemblyFromCampaign(UUID uuid) {
		return find.where().eq("resources.campaigns.uuid",uuid).findList();
	}

	public static List<Assembly> findAssemblyFromComponent(Long cid) {
		return find.where().eq("resources.components.componentId",cid).findList();
	}

	public static List<Campaign> findCampaigns(Long aid) throws Exception {
		return find.where().eq("assemblyId",aid)
				.findUnique()
				.getResources()
				.getCampaignsFilteredByStatus("all");
	}

	public static Assembly findByName(String n) {
		List<Assembly> as = find.where().eq("name", n).findList();
		return as!=null && as.size()>0 ? as.get(0) : null;	
	}

    public static Assembly findByShortName(String n) {
        List<Assembly> as = find.where().eq("shortname", n).findList();
        return as!=null && as.size()>0 ? as.get(0) : null;
    }

	public static Assembly findById(Long aid) {
		Assembly a = find.byId(aid);
		return a;
	}
	public Boolean getPrincipalAssembly() {
		return principalAssembly;
	}

	public void setPrincipalAssembly(Boolean principalAssembly) {
		this.principalAssembly = principalAssembly;
	}

	public static Assembly readAndUpdate(Assembly newAssembly, Long id) {
		Assembly oldAssembly = Assembly.findById(id);
		newAssembly.setAssemblyId(id);
		newAssembly.setForumPosts(oldAssembly.getForumPosts());
		newAssembly.setFollowedAssemblies(oldAssembly.getFollowedAssemblies());
		newAssembly.setFollowingAssemblies(oldAssembly.getFollowingAssemblies());
		List<Theme> themes = newAssembly.getThemes();
		List<Theme> themesLoaded = new ArrayList<Theme>();
		for (Theme theme: themes) {
			Theme t = Theme.read(theme.getThemeId());
			themesLoaded.add(t);
		}
		newAssembly.setThemes(themesLoaded);
		List<Config> configs = newAssembly.getConfigs();
		List<Config> configsLoaded = new ArrayList<Config>();
		for (Config conf: configs) {
			Config c = Config.read(conf.getUuid());
			if (c==null) {
				conf.setConfigTarget(ConfigTargets.ASSEMBLY);
				conf.setTargetUuid(newAssembly.getUuid());
				Config.create(conf);
			}
			configsLoaded.add(conf);
		}
		newAssembly.setConfigs(configsLoaded);
		List<Campaign> campaigns = newAssembly.getCampaigns();
		List<Campaign> campaignsLoaded = new ArrayList<Campaign>();
		for (Campaign camp: campaigns) {
			Campaign c = Campaign.read(camp.getCampaignId());
			campaignsLoaded.add(c);
		}
		newAssembly.setCampaigns(campaignsLoaded);
		List<WorkingGroup> wg = newAssembly.getWorkingGroups();
		List<WorkingGroup> wgLoaded = new ArrayList<WorkingGroup>();
		for (WorkingGroup wgroup: wg
				) {
			WorkingGroup workingGroup = WorkingGroup.read(wgroup.getGroupId());
			wgLoaded.add(workingGroup);
		}
		newAssembly.setWorkingGroups(wgLoaded);
		return newAssembly;
	}
}
