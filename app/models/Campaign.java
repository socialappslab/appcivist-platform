package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import scala.Array;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ResourceSpaceTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class Campaign extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	@Column(name="campaign_id")
	private Long campaignId;
	private String title; // e.g., "PB for Vallejo 2015"
	private String shortname;
	@Column(name="goal", columnDefinition="text")
	private String goal;	
	private String url;
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;
	// If the campaign is listed, its basic profile is reading accessible by all 
	private Boolean listed = true;

	// Relationships	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonIgnoreProperties({"uuid"})
	@JsonInclude(Include.NON_EMPTY)
	@JsonIgnore
	private ResourceSpace resources;
	
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private Long resourceSpaceId;
	
	
	@Transient
	private List<ComponentInstance> components = new ArrayList<>();
	@Transient
	private List<Config> configs = new ArrayList<>();
	@Transient
	private List<Theme> themes = new ArrayList<>();
	@Transient
	private List<WorkingGroup> workingGroups = new ArrayList<>();
	@Transient
	private List<Long> assemblies = new ArrayList<>();

	@Transient
	private List<ComponentInstance> existingComponents = new ArrayList<>();
	@Transient
	private List<Config> existingConfigs = new ArrayList<>();
	@Transient
	private List<Theme> existingThemes = new ArrayList<>();
	@Transient
	private List<WorkingGroup> existingWorkingGroups = new ArrayList<>();
	
	// TODO: check if it works
	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "campaigns")
	private List<ResourceSpace> containingSpaces;

	@ManyToOne
	private CampaignTemplate template;
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, Campaign> find = new Finder<>(Campaign.class);

	public Campaign() {
		super();
		this.uuid =  UUID.randomUUID(); 
		this.resources = new ResourceSpace(ResourceSpaceTypes.CAMPAIGN);
	}

	public Campaign(String title, Date startDate, Date endDate, Boolean active,
			String url, CampaignTemplate template) {
		super();
		this.title = title;
		this.url = url;
		this.template = template;
		this.uuid =  UUID.randomUUID(); 
		this.resources = new ResourceSpace(ResourceSpaceTypes.CAMPAIGN);

		// automatically populate the phases based on the campaign template
		if (template != null && template.getDefaultComponents() != null) {
			List<Component> defaultPhases = template.getDefaultComponents();

			for (Component phaseDefinition : defaultPhases) {
				ComponentInstance phase = new ComponentInstance(this, phaseDefinition);
				this.addComponent(phase);
			}
		}
	}

	public Campaign(String title, Date startDate, Date endDate, Boolean active,
			String url, CampaignTemplate template,
			List<Config> configs) {
		super();
		this.title = title;
		this.url = url;
		this.template = template;

		this.uuid =  UUID.randomUUID(); 
		this.resources = new ResourceSpace(ResourceSpaceTypes.CAMPAIGN);

		// automatically populate the phases based on the campaign template
		if (template != null && template.getDefaultComponents() != null) this.populateDefaultComponents(template.getDefaultComponents());
	}

	public Campaign(String title, String shortname, Boolean listed, CampaignTemplate template,
String uuidAsString, List<ComponentInstance> phases) {
		super();
		this.title = title;
		this.shortname = shortname;
		this.listed = listed;
		this.template = template;
		this.uuidAsString = uuidAsString;
		this.uuid =  UUID.fromString(uuidAsString);
		this.resources = new ResourceSpace(ResourceSpaceTypes.CAMPAIGN);

		// automatically populate the phases based on the campaign template
		if (template != null && template.getDefaultComponents() != null) this.populateDefaultComponents(template.getDefaultComponents());
	}
	

	/*
	 * Getters and Setters
	 */
	public Long getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getGoal() {
		return goal;
	}

	public void setGoal(String goal) {
		this.goal = goal;
	}

	public Boolean getActive() {
		return this.getStartDate()!=null && (this.getStartDate().before(Calendar.getInstance().getTime())
				|| this.getStartDate().equals(Calendar.getInstance().getTime()));
	}
	
	public Boolean getPast() {
		return this.getEndDate()!=null && (this.getEndDate().before(Calendar.getInstance().getTime()));
	}

	public Boolean getUpcoming() {
		return this.getStartDate()!=null && (this.getStartDate().after(Calendar.getInstance().getTime()));
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public ResourceSpace getResources() {
		return resources;
	}

	public void setResources(ResourceSpace resources) {
		this.resources = resources;
	}

	public Long getResourceSpaceId() {
		return resources != null ? resources.getResourceSpaceId() : null;
	}

	public List<ComponentInstance> getComponents() {
		return this.resources.getComponents();
	}

	public void setComponents(List<ComponentInstance> components) {
		this.components = components;
		this.resources.setComponents(components);
	}

	public void addComponent(ComponentInstance componentIsntance) {
		this.components.add(componentIsntance);
		this.resources.getComponents().add(componentIsntance);
	}


	public List<Config> getConfigs() {
		return this.resources.getConfigs();
	}

	public void setConfigs(List<Config> configs) {
		this.configs = configs;
		this.resources.setConfigs(configs);
	}

	public List<Theme> getThemes() {
		return this.resources.getThemes();
	}

	public void setThemes(List<Theme> themes) {
		this.themes = themes;
		this.resources.setThemes(themes);
	}

	public void addTheme(Theme t) {
		this.themes.add(t);
		this.resources.addTheme(t);
	}
	
	public List<WorkingGroup> getWorkingGroups() {
		return this.resources.getWorkingGroups();
	}

	public void setWorkingGroups(List<WorkingGroup> workingGroups) {
		this.workingGroups = workingGroups;
		this.resources.setWorkingGroups(workingGroups);
	}
	
	public void addWorkingGroup(WorkingGroup wg) {
		this.workingGroups.add(wg);
		this.resources.addWorkingGroup(wg);
	}
	
	public List<Long> getAssemblies() {
		List <Long> assemblyIds = new ArrayList<>();
		List<ResourceSpace> spaces = this.containingSpaces.stream().filter(p -> p.getType() == ResourceSpaceTypes.ASSEMBLY)
		.collect(Collectors.toList());
		
		for (ResourceSpace resourceSpace : spaces) {
			Assembly a = resourceSpace.getAssemblyResources();
			if(a!=null) {
				assemblyIds.add(a.getAssemblyId());
			}
		}
		return assemblyIds;
	}

	public List<ComponentInstance> getExistingComponents() {
		return existingComponents;
	}

	public void setExistingComponents(List<ComponentInstance> newComponents) {
		this.existingComponents = newComponents;
	}

	public List<Config> getExistingConfigs() {
		return existingConfigs;
	}

	public void setExistingConfigs(List<Config> newConfigs) {
		this.existingConfigs = newConfigs;
	}

	public List<Theme> getExistingThemes() {
		return existingThemes;
	}

	public void setExistingThemes(List<Theme> newThemes) {
		this.existingThemes = newThemes;
	}

	public List<WorkingGroup> getExistingWorkingGroups() {
		return existingWorkingGroups;
	}

	public void setExistingWorkingGroups(List<WorkingGroup> newWorkingGroups) {
		this.existingWorkingGroups = newWorkingGroups;
	}

	public CampaignTemplate getTemplate() {
		return template;
	}

	public void setTemplate(CampaignTemplate template) {
		this.template = template;
	}

	public static List<Campaign> findAll() {
		List<Campaign> campaigns = find.all();
		return campaigns;
	}
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	public Date getStartDate() {
		List<ComponentInstance> components = this.resources.getComponents(); 
		if (components != null && !components.isEmpty()) {
			Collections.sort(components,new ComponentInstance());
			ComponentInstance firstPhase = components.get(0);
			return firstPhase.getStartDate();
		}
		return null;
	}

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	public Date getEndDate() {
		List<ComponentInstance> components = this.resources.getComponents(); 
		if (components != null && !components.isEmpty()) {
			Collections.sort(components,new ComponentInstance());
			ComponentInstance lastPhase = components.get(components.size()-1);
			return lastPhase.getEndDate();
		}
		return null;
	}
		
	public Boolean getListed() {
		return listed;
	}

	public void setListed(Boolean listed) {
		this.listed = listed;
	}

	/*
	 * Basic Data Operations
	 */
	public static void create(Campaign campaign) {
		// 1. Check first for existing entities in ManyToMany relationships. 
		//    Save them for later update
		List<ComponentInstance> existingComponents = campaign.getExistingComponents();
		List<Theme> existingThemes = campaign.getExistingThemes();
		List<WorkingGroup> existingWorkingGroups = campaign.getExistingWorkingGroups();
		
		// 2. Create the new campaign
		campaign.save();
		
		// 3. Add existing entities in relationships to the manytomany resources
		//    then update
		ResourceSpace campaignResources = campaign.getResources();
		
		if (existingComponents!=null && !existingComponents.isEmpty())
			campaignResources.getComponents().addAll(existingComponents);
		if (existingThemes!=null && !existingThemes.isEmpty())
			campaignResources.getThemes().addAll(existingThemes);
		if (existingWorkingGroups!=null && !existingWorkingGroups.isEmpty())
			campaignResources.getWorkingGroups().addAll(existingWorkingGroups);
		campaignResources.update();
		
		// 4. Refresh the new campaign to get the newest version
		campaign.refresh();
	}

	public static Campaign read(Long campaignId) {
		return find.ref(campaignId);
	}

	public static Integer readByTitle(String campaignTitle) {
		ExpressionList<Campaign> campaigns = find.where().eq("title",campaignTitle);
		return campaigns.findList().size();
	}

	public static Campaign createObject(Campaign campaign) {
		campaign.save();
		return campaign;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static Campaign update(Campaign c) {
		c.update();
		c.refresh();
		return c;
	}
	
	
	private void populateDefaultComponents(List<Component> defaultPhaseDefinitions) {
		List<Component> defaultPhases = this.template.getDefaultComponents();
		for (Component phaseDefinition : defaultPhases) {
			ComponentInstance phase = new ComponentInstance(this, phaseDefinition);
			this.addComponent(phase);
		}
	}

	public static List<Campaign> getOngoingCampaignsFromAssembly(Assembly a) {
		List<Campaign> ongoingCampaigns = new ArrayList<Campaign>();
		ResourceSpace resources = a.getResources();
		List<Campaign> campaigns = null;
		if (resources != null)
			campaigns = resources.getCampaigns();
		if (campaigns != null && !campaigns.isEmpty()) {
			for (Campaign c : campaigns) {
				Calendar today = Calendar.getInstance();
				if (c.getStartDate()!=null && c.getEndDate()!=null && c.getStartDate().before(today.getTime())
						&& c.getEndDate().after(today.getTime())) {
					ongoingCampaigns.add(c);
				}
			}
		}
		return ongoingCampaigns;
	}	
}
