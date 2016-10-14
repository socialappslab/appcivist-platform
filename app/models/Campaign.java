package models;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import utils.GlobalData;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.ResourceSpaceTypes;
import enums.VotingSystemTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="Campaign", description="Campaigns are actions or processes organized by an Assembly")
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
	private UUID consultiveBallot; 
	@Transient
	private String consultiveBallotAsString; 
	private UUID bindingBallot; 
	@Transient
	private String bindingBallotAsString; 
	// Relationships	
	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JsonIgnoreProperties({"uuid"})
	@JsonInclude(Include.NON_EMPTY)
	@JsonIgnore
	private ResourceSpace resources;
	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="campaign")
	@JsonInclude(Include.NON_EMPTY)
	@JsonManagedReference
	@JsonIgnoreProperties({ "fromComponent", "toComponent" })
	@OrderBy("start DESC")
	private List<CampaignTimelineEdge> timelineEdges = new ArrayList<>();
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private Long resourceSpaceId;
	@Transient
	private List<Component> components = new ArrayList<>();
	@Transient
	private List<Config> configs = new ArrayList<>();
	@Transient
	private List<Theme> themes = new ArrayList<>();
	@Transient
	private List<WorkingGroup> workingGroups = new ArrayList<>();
	@Transient
	private List<Long> assemblies = new ArrayList<>();
	@Transient
	private List<Contribution> contributions = new ArrayList<>();
	@Transient
	private List<Ballot> ballots = new ArrayList<>();
	@Transient
	private List<Component> existingComponents = new ArrayList<>();
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
		if (template != null && template.getDefComponents() != null) {
			List<ComponentDefinition> defaultPhases = template.getDefComponents();

			for (ComponentDefinition phaseDefinition : defaultPhases) {
				Component phase = new Component(this, phaseDefinition);
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
		if (template != null && template.getDefComponents() != null) this.populateDefaultComponents(template.getDefComponents());
	}

	public Campaign(String title, String shortname, Boolean listed, CampaignTemplate template,
String uuidAsString, List<Component> phases) {
		super();
		this.title = title;
		this.shortname = shortname;
		this.listed = listed;
		this.template = template;
		this.uuidAsString = uuidAsString;
		this.uuid =  UUID.fromString(uuidAsString);
		this.resources = new ResourceSpace(ResourceSpaceTypes.CAMPAIGN);

		// automatically populate the phases based on the campaign template
		if (template != null && template.getDefComponents() != null) this.populateDefaultComponents(template.getDefComponents());
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
		Date start = this.getStartDate();
		if (start!=null) {
			Boolean campaignStarted = start.before(Calendar.getInstance().getTime());
			Boolean campaignStartsToday = start.equals(Calendar.getInstance().getTime());
			return campaignStarted || campaignStartsToday;
		}
		return false;
	}
	
	public Boolean getPast() {
		Date end = this.getEndDate();
		if (end!=null) {
			Boolean campaignFinished = end.before(Calendar.getInstance().getTime());
			return campaignFinished;
		}
		return false;
	}

	public Boolean getUpcoming() {
		Date start = this.getStartDate();
		if (start!=null) {
			Boolean campaignUpcoming = start.after(Calendar.getInstance().getTime());
			return campaignUpcoming ;
		}
		return false;
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
		return uuid!=null ? uuid.toString() : null;
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

	public List<CampaignTimelineEdge> getTimelineEdges() {
		return timelineEdges;
	}

	public void setTimelineEdges(List<CampaignTimelineEdge> timelineEdges) {
		this.timelineEdges = timelineEdges;
	}
	
	public Long getResourceSpaceId() {
		return resources != null ? resources.getResourceSpaceId() : null;
	}
	
	public void setResourceSpaceId(Long id) {
		if (this.resources !=null && this.resources.getResourceSpaceId() == null) 
			this.resources .setResourceSpaceId(id);
	}

	public List<Component> getComponents() {
		List<Component> components = new ArrayList<>();
		Map<Long, Component> edges = new HashMap<>();

		for (CampaignTimelineEdge edge : this.timelineEdges) {
			edges.put(edge.getFromComponentId(), edge.getToComponent());
		}

		if (this.timelineEdges != null && !this.timelineEdges.isEmpty()) {
			CampaignTimelineEdge firstEdge = this.timelineEdges.get(0);
			if (firstEdge != null) {
				components.add(firstEdge.getFromComponent());
				Component nextComponent = edges.get(firstEdge
						.getFromComponent().getComponentId());
				while (nextComponent != null) {
					components.add(nextComponent);
					nextComponent = edges.get(nextComponent.getComponentId());
				}
			}
		}
		return components;

	}

	public void setComponents(List<Component> components) {
		this.components = components;
		this.resources.setComponents(components);
	}

	public void addComponent(Component componentIsntance) {
		this.components.add(componentIsntance);
		this.resources.getComponents().add(componentIsntance);
	}

	private List<Component> getTransientComponents() {
		return this.components;
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

	public List<Contribution> getContributions() {
		return this.resources.getContributions();
	}

	public void setContributions(List<Contribution> contributions) {
		this.contributions = contributions;
		this.resources.setContributions(contributions);
	}

	public List<Ballot> getBallots() {
		this.ballots = this.resources.getBallots();
		return this.ballots;
	}
	
	public void setBallots(List<Ballot> ballots) {
		this.ballots = ballots;
		this.resources.setBallots(ballots);
	}
	
	public List<Component> getExistingComponents() {
		return existingComponents;
	}

	public void setExistingComponents(List<Component> newComponents) {
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
		List<Component> components = this.resources.getComponents(); 
		if (components != null && !components.isEmpty()) {
			Collections.sort(components,new Component());
			Component firstPhase = components.get(0);
			return firstPhase.getStartDate();
		}
		return null;
	}

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	public Date getEndDate() {
		List<Component> components = this.resources.getComponents(); 
		if (components != null && !components.isEmpty()) {
			Collections.sort(components,new Component());
			Component lastPhase = components.get(components.size()-1);
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

	public UUID getConsultiveBallot() {
		return consultiveBallot;
	}

	
	public void setConsultiveBallot(UUID upsDownBallot) {
		this.consultiveBallot = upsDownBallot;
	}

	
	public void setConsultiveBallotAsString(String upsDownBallotAsString) {
		this.consultiveBallotAsString = upsDownBallotAsString;
		this.consultiveBallot = UUID.fromString(upsDownBallotAsString);
	}
	
	public UUID getBindingBallot() {
		return bindingBallot;
	}

	public void setBindingBallot(UUID bindingBallot) {
		this.bindingBallot = bindingBallot;
	}

	public void setBindingBallotAsString(String bindingBallotAsString) {
		this.bindingBallotAsString = bindingBallotAsString;
		this.bindingBallot = UUID.fromString(bindingBallotAsString);
	}

	/*
	 * Basic Data Operations
	 */
	public static void create(Campaign campaign) {
		// 1. Check first for existing entities in ManyToMany relationships. 
		//    Save them for later update
		List<Theme> existingThemes = campaign.getExistingThemes();
		List<WorkingGroup> existingWorkingGroups = campaign.getExistingWorkingGroups();

		// Save components to create them independently 
		List<Component> componentList = campaign.getTransientComponents();
		campaign.setComponents(new ArrayList<>());

		// By default, if no goal is stated, then the goal is the same as the title
		if (campaign.getGoal()==null) {
			campaign.setGoal(campaign.getTitle());
		}

		// 2. Create the new campaign
		campaign.save();
		
		// 3. Add existing entities in relationships to the ManyToMany resource space then update
		ResourceSpace campaignResources = campaign.getResources();
		
		// 4. Save components and create the edges 
		//    Timeline edges are used to keep trakc of the Component Graph
		//    and know how components must be connected
		int edges = 0;
		List<CampaignTimelineEdge> edgeList = new ArrayList<>();
		for (Component component : componentList) {
			if (!component.getLinked()) {
				component = Component.createObject(component);
				component.refresh();
			} else {
				component = Component.read(component.getComponentId());
			}
			
			campaignResources.addComponent(component);

			// If this is a voting component, create a ballot associated with it 
			// and add the ballot to the campaign
			String componentKey = component.getKey();
			if (!component.getLinked() && componentKey != null && componentKey.toLowerCase().equals("voting") ) {
				// 6. Create a decision ballot associated with this component and add it to the campaign
				Ballot ballot = new Ballot();
				Date startBallot = component.getStartDate();
				Date endBallot = component.getEndDate();
				
				// if component has no start date, use now
				startBallot = startBallot != null ? startBallot : Calendar.getInstance().getTime();
				
				// if component has no end date, use 30 days after startDate
				Calendar c = Calendar.getInstance();
				c.setTime(startBallot);
				c.add(Calendar.DATE, 30);
				endBallot = endBallot != null ? endBallot : c.getTime();

				ballot.setStartsAt(startBallot);
				ballot.setEndsAt(endBallot);
				ballot.setPassword(campaign.getUuidAsString());
				
				Config votingSystemConfig = component.getResourceSpace()
						.getConfigByKey(
								GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM);
				VotingSystemTypes vtype = votingSystemConfig != null ? VotingSystemTypes
						.valueOf(votingSystemConfig.getValue())
						: VotingSystemTypes.PLURALITY;
				ballot.setVotingSystemType(vtype);
				// TODO: include registration and signature configuration in configs of component
				ballot.setRequireRegistration(false);
				ballot.setUserUuidAsSignature(true);
				ballot.setDecisionType("BINDING");
				ballot.setComponent(component);
				ballot.save();
				ballot.refresh();
				campaign.setBindingBallotAsString(ballot.getUuid().toString());
				campaignResources.addBallot(ballot);
				// Add Ballot configurations
				for (Config config : component.getConfigs()) {
					BallotConfiguration ballotConfig = new BallotConfiguration();
					ballotConfig.setBallotId(ballot.getId());
					ballotConfig.setKey(config.getKey());
					ballotConfig.setValue(config.getValue());
					ballotConfig.save();
					if (config.getKey().equals("component.voting.ballot.password")) {
						BallotRegistrationField brf = new BallotRegistrationField();
						brf.setBallotId(ballot.getId());
						brf.setDescription("The password used by non-users to vote on proposals through the voting ballot");
						brf.setExpectedValue(config.getValue());
						brf.setName("Ballot Password");
						brf.setPosition(0);
						brf.save();
						ballot.setPassword(config.getValue());
						ballot.update();
					}
				}
				
				// TODO: add ballot registration fields
			}
			
			// Add connection to the Timeline of Edges
			CampaignTimelineEdge edge = new CampaignTimelineEdge();
			edge.setCampaign(campaign);
			if (edges == 0) {
				edge.setFromComponent(component);
				edge.setStart(true);
				edgeList.add(edge);
				edges++;
			} else {
				if (edges < componentList.size() - 1) {
					edge.setFromComponent(component);
					edgeList.add(edge);
				}
				CampaignTimelineEdge prevEdge = edgeList.get(edges - 1);
				prevEdge.setToComponent(component);
				edges++;
			}
		}
		campaign.setTimelineEdges(edgeList);
		
		// 5. Add existing themes to the resource space
		if (existingThemes != null && !existingThemes.isEmpty())
			campaignResources.getThemes().addAll(existingThemes);
		if (existingWorkingGroups != null && !existingWorkingGroups.isEmpty())
			campaignResources.getWorkingGroups().addAll(existingWorkingGroups);
		
		// 7. Create the default up/down votes ballot
		Ballot consultiveBallot = new Ballot();
		Date startUDBallot = campaign.getStartDate();
		Date endUDBallot = campaign.getEndDate();
		startUDBallot = startUDBallot != null ? startUDBallot : Calendar.getInstance().getTime();
		// if component has no end date, use 30 days after startDate
		Calendar c = Calendar.getInstance();
		c.setTime(startUDBallot);
		c.add(Calendar.DATE, 30);
		endUDBallot = endUDBallot  != null ? endUDBallot : c.getTime();
		consultiveBallot.setStartsAt(startUDBallot);
		consultiveBallot.setEndsAt(endUDBallot);
		consultiveBallot.setPassword(campaign.getUuidAsString());
		consultiveBallot.setVotingSystemType(VotingSystemTypes.PLURALITY);
		consultiveBallot.setRequireRegistration(false);
		consultiveBallot.setUserUuidAsSignature(true);
		consultiveBallot.setDecisionType("CONSULTIVE");
		consultiveBallot.save();
		consultiveBallot.refresh();
		
		campaign.setConsultiveBallotAsString(consultiveBallot.getUuid().toString());
		campaignResources.addBallot(consultiveBallot);
		
		// Add Ballot configurations
		
		// VOTING SYSTEM
		BallotConfiguration ballotConfig = new BallotConfiguration();
		ballotConfig.setBallotId(consultiveBallot.getId());
		ballotConfig.setKey(GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM);
		ballotConfig.setValue("PLURALITY");
		ballotConfig.save();
		
		// VOTING SYSTEM BLOCK THRESHOLD
		ballotConfig = new BallotConfiguration();
		ballotConfig.setBallotId(consultiveBallot.getId());
		ballotConfig.setKey(GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM_PLURALITY_TYPE);
		ballotConfig.setValue("YES/NO/ABSTAIN");
		ballotConfig.save();
		
		campaign.update();
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
	
	private void populateDefaultComponents(List<ComponentDefinition> defaultPhaseDefinitions) {
		List<ComponentDefinition> defaultPhases = this.template.getDefComponents();
		for (ComponentDefinition phaseDefinition : defaultPhases) {
			Component phase = new Component(this, phaseDefinition);
			this.addComponent(phase);
		}
	}

	public static List<Campaign> getOngoingCampaignsFromAssembly(Assembly a) {
		List<Campaign> ongoingCampaigns = new ArrayList<Campaign>();
		ResourceSpace resources = a.getResources();
		List<Campaign> campaigns = null;
		if (resources != null)
			campaigns = resources.getCampaignsFilteredByStatus("ongoing");
		if (campaigns != null && !campaigns.isEmpty()) {
//			for (Campaign c : campaigns) {
//				Calendar today = Calendar.getInstance();
//				if (c.getStartDate()!=null && c.getEndDate()!=null && c.getStartDate().before(today.getTime())
//						&& c.getEndDate().after(today.getTime())) {
//					ongoingCampaigns.add(c);
//				}
//			}
			ongoingCampaigns.addAll(campaigns);
		}
		return ongoingCampaigns;
	}	
	
	public static List<Campaign> getOngoingCampaignsFromAssembly(Long assemblyId) {
		List<Campaign> campaigns = find.where().eq("containingSpaces.assemblyResources.assemblyId", assemblyId).findList();
		if (campaigns!=null && !campaigns.isEmpty())
			return campaigns.stream().filter(p -> p.getActive()).collect(Collectors.toList());
		else 
			return null;
	}	

	public static UUID queryBindingBallotByCampaignResourceSpaceId(Long rsId) {
		String sql = "select binding_ballot from campaign where resources_resource_space_id = :rsId";
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		sqlQuery.setParameter("rsId", rsId);
		SqlRow result = sqlQuery.findUnique();
		return result.getUUID("binding_ballot");
	}
	
	public static UUID queryConsultiveBallotByCampaignResourceSpaceId(Long rsId) {
		String sql = "select consultive_ballot from campaign where resources_resource_space_id = :rsId";
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		sqlQuery.setParameter("rsId", rsId);
		SqlRow result = sqlQuery.findUnique();
		return result.getUUID("consultive_ballot");	
	}

	public static Campaign readByUUID(UUID uuid) {
		return find.where().eq("uuid", uuid).findUnique();
	}

	public static List<Campaign> findByBindingBallotUUID(UUID uuid) {
		return find.where().eq("bindingBallot",uuid).findList();
	}
}
