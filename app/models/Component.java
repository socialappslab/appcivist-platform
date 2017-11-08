package models;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;

import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import models.misc.Views;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;

import enums.ComponentTypes;
import enums.ResourceSpaceTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
@SequenceGenerator(name="componentSeq", initialValue=9, allocationSize=50)
@ApiModel(value="Component", description="A component represents a space of action within a campaign. It can be the stage in a process or the space of deliberation when a campaign des not have a process")
public class Component extends AppCivistBaseModel implements Comparator<Component> {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="componentSeq")
	@Column(name = "component_id")
	private Long componentId;
	@JsonView(Views.Public.class)
	private String title;
	@Enumerated(EnumType.STRING)
	@JsonView(Views.Public.class)
	private ComponentTypes type = ComponentTypes.IDEAS;
	@JsonView(Views.Public.class)
	private String key;
	@JsonView(Views.Public.class)
	@Column(name = "description", columnDefinition = "text")
	private String description;
	@JsonView(Views.Public.class)
	private Date startDate;
	@JsonView(Views.Public.class)
	private Date endDate;
	@JsonView(Views.Public.class)
	private UUID uuid = UUID.randomUUID();
	@JsonView(Views.Public.class)
	private int position;
	@JsonView(Views.Public.class)
	private int timeline;
	@Transient 
	private Boolean linked;

	@OneToMany(cascade=CascadeType.ALL, mappedBy="fromComponent", fetch=FetchType.LAZY)
	@JsonBackReference
	private List<CampaignTimelineEdge> fromEdges = new ArrayList<>();

	@OneToMany(cascade=CascadeType.ALL, mappedBy="toComponent", fetch=FetchType.LAZY)
	@JsonBackReference
	private List<CampaignTimelineEdge> toEdges = new ArrayList<>();
	
	@ManyToOne
	private ComponentDefinition definition;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonIgnoreProperties({ "uuid" })
	@JsonInclude(Include.NON_EMPTY)
	@JsonIgnore
	private ResourceSpace resourceSpace = new ResourceSpace(ResourceSpaceTypes.COMPONENT);

	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private Long resourceSpaceId;
	

	@Transient
	@JsonInclude(Include.NON_EMPTY)
	@JsonView(Views.Public.class)
	private Long resourceSpaceUUID;

	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Config> configs;
	
	// TODO: check if it works
	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "components")
	private List<ResourceSpace> containingSpaces;

	@Transient
	@JsonView(Views.Public.class)
	private List<ComponentMilestone> milestones = new ArrayList<ComponentMilestone>();

	@Transient
	private List<Contribution> contributions = new ArrayList<>();

	@Transient
	private List<ContributionTemplate> templates = new ArrayList<>();

	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, Component> find = new Finder<>(
			Component.class);

	public Component() {
		super();
	}

	public Component(Campaign c, ComponentDefinition definition) {
		super();
		this.definition = definition;
		this.populateDefaultMilestones(c.getTemplate());
	}

	public Component(Date startDate, Date endDate, Campaign campaign,
			ComponentDefinition definition, List<Config> campaignPhaseConfigs) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.definition = definition;
		this.resourceSpace = new ResourceSpace(ResourceSpaceTypes.COMPONENT);
		this.uuid = UUID.randomUUID();
	}

	public Component(Campaign c, ComponentDefinition definition,
			List<Config> configs) {
		super();
		this.definition = definition;
		this.resourceSpace = new ResourceSpace(ResourceSpaceTypes.COMPONENT);
		this.uuid = UUID.randomUUID();
	}

	public Long getComponentId() {
		return componentId;
	}

	public void setComponentId(Long componentId) {
		this.componentId = componentId;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ComponentTypes getType() {
		return type;
	}

	public void setType(ComponentTypes type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	public Date getStartDate() {
		return startDate;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	public Date getEndDate() {
		return endDate;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int order) {
		this.position = order;
	}

	public int getTimeline() {
		return timeline;
	}

	public void setTimeline(int timeline) {
		this.timeline = timeline;
	}

	public Boolean getLinked() {
		return linked;
	}



	public void setLinked(Boolean linked) {
		this.linked = linked;
	}

	public List<CampaignTimelineEdge> getFromEdges() {
		return fromEdges;
	}

	public void setFromEdges(List<CampaignTimelineEdge> timelineGraph) {
		this.fromEdges = timelineGraph;
	}
	
	public List<CampaignTimelineEdge> getToEdges() {
		return toEdges;
	}

	public void setToEdges(List<CampaignTimelineEdge> timelineGraph) {
		this.toEdges = timelineGraph;
	}

	public ComponentDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ComponentDefinition definition) {
		if (title == null || title.isEmpty())
			this.title = definition.getName();
		if (description == null || description.isEmpty())
			this.description = definition.getDescription();
		this.definition = definition;
	}

	/*
	 * Basic Data operations
	 */

	public List<ComponentMilestone> getMilestones() {
		List<ComponentMilestone> milestones = resourceSpace
				.getMilestones();
		Collections.sort(milestones, new ComponentMilestone());
		return milestones;
	}

	public void setMilestones(List<ComponentMilestone> milestones) {
		if (milestones != null) {
			if (!milestones.isEmpty()) {
				Collections.sort(milestones, new ComponentMilestone()); // Sorts the list of milestones
				ComponentMilestone firstMilestone = milestones.get(0);
				ComponentMilestone lastMilestone = milestones.get(milestones.size() - 1);
				this.startDate = firstMilestone.getDate();

				Calendar cal = Calendar.getInstance();
				cal.setTime(lastMilestone.getDate());
				cal.add(Calendar.DATE, lastMilestone.getDays()); // add duration
																	// of
																	// milestone
				this.endDate = cal.getTime();
			}
			this.resourceSpace.setMilestones(milestones);
			this.milestones = milestones;
		}
	}

	private void populateDefaultMilestones(CampaignTemplate ct) {
		if (this.definition != null && ct != null) {
			List<ComponentRequiredMilestone> reqMilestones = ct.getReqMilestones();
			Collections.sort(reqMilestones, new ComponentRequiredMilestone());
			ComponentMilestone previous = null;
			for (ComponentRequiredMilestone requiredMilestone : reqMilestones) {
				if (requiredMilestone.getTargetComponentUuid().equals(this.definition.getUuid())) {
					ComponentMilestone m = new ComponentMilestone(requiredMilestone, previous);
					this.addMilestone(m);
					previous = m;
				}
			}
		}
	}

	private void addMilestone(ComponentMilestone m) {
		this.resourceSpace.getMilestones().add(m);
	}

	public List<Contribution> getContributions() {
		return resourceSpace.getContributions();
	}

	public void setContributions(List<Contribution> contributions) {
		this.resourceSpace.setContributions(contributions);
	}

	public List<ContributionTemplate> getTemplates() {
		return resourceSpace.getTemplates();
	}

	public void setTemplates(List<ContributionTemplate> templates) {
		this.resourceSpace.setTemplates(templates);
	}

	public ResourceSpace getResourceSpace() {
		return resourceSpace;
	}

	public void setResourceSpace(ResourceSpace resources) {
		this.resourceSpace = resources;
	}

	public Long getResourceSpaceId() {
		return this.resourceSpace != null ? this.resourceSpace
				.getResourceSpaceId() : null;
	}

	public void setResourceSpaceId(Long id) {
		if (this.resourceSpace != null
				&& this.resourceSpace.getResourceSpaceId() == null)
			this.resourceSpace.setResourceSpaceId(id);
	}

	public String getResourceSpaceUUID() {
		return resourceSpace != null ? resourceSpace.getResourceSpaceUuid().toString() : null;
	}

	
	public List<Config> getConfigs() {
		return this.resourceSpace.getConfigs();
	}
	
	public void setConfigs(List<Config> configs) {
		this.resourceSpace.setConfigs(configs);
	}
	
	public void addConfig(Config c) {
		this.resourceSpace.addConfig(c);
	}

	public List<ResourceSpace> getContainingSpaces() {
		return containingSpaces;
	}

	public void setContainingSpaces(List<ResourceSpace> containingSpaces) {
		this.containingSpaces = containingSpaces;
	}
	
	public static Component read(Long componentId) {
		return find.ref(componentId);
	}


	public static Component read(Long campaignId,
			Long componentId) {
		ExpressionList<Component> components = find.where()
				.eq("containingSpaces.campaign.campaignId", campaignId)
				.eq("componentId", componentId);
		Component component = components.findUnique();
		return component;
	}

	public static List<Component> findAll(Long campaignId) {
		ExpressionList<Component> campaignPhases = find.where().eq(
				"containingSpaces.campaign.campaignId", campaignId);
		List<Component> campaignPhaseList = campaignPhases.findList();
		return campaignPhaseList;
	}

	public static ComponentTypes getCurrentComponentType(Long campaignId) {
		ExpressionList<Component> campaignPhases = find.where()
				.eq("containingSpaces.campaign.campaignId", campaignId)
				.ge("endDate", new Date())
				.le("startDate", new Date());
		List<Component> campaignPhaseList = campaignPhases
				.orderBy("startDate asc")
				.findList();
		if (campaignPhaseList == null || campaignPhaseList.isEmpty()) {
			return null;
		} else {
			return campaignPhaseList.get(0).getType();
		}
	}

	public static List<Component> findByAssemblyAndCampaign(Long aid,
			Long campaignId) {
		ExpressionList<Component> campaignPhases = find.where().eq(
				"containingSpaces.campaign.campaignId", campaignId);
		List<Component> campaignPhaseList = campaignPhases.findList();
		return campaignPhaseList;
	}

	public static Component create(Long campaignId,
			Component phase) {
		ComponentDefinition phaseDefinition = null;
		if (phase.getDefinition().getComponentDefId() != null) {
			phaseDefinition = ComponentDefinition.read(phase.getDefinition()
					.getComponentDefId());
		} else if (phase.getDefinition().getName() != null) {
			phaseDefinition = ComponentDefinition.readByName(phase.getDefinition()
					.getName());
		}
		phase.setDefinition(phaseDefinition);
		phase.save();
		phase.refresh();
		return phase;
	}

	public static Component createObject(Component object) {
		object.save();
		return object;
	}

	public static void delete(Long campaignId, Long componentId) {
		ExpressionList<Component> campaignPhases = find.where()
				.eq("containingSpaces.campaign.campaignId", campaignId)
				.eq("componentId", componentId);
		Component phase = campaignPhases.findUnique();
		phase.delete();
	}

	public static Component update(Component cp) {
		cp.update();
		cp.refresh();
		return cp;
	}

	@Override
	public int compare(Component o1, Component o2) {
		return o1.getPosition() - o2.getPosition();
	}

	public static List<Component> findVotingByStartingDay(Date startDate, Date endDate){

		Query<Component> q = find.where()
				.eq("type",ComponentTypes.VOTING.toString())
				.between("startDate",startDate,endDate)
				.query();
		List<Component> membs = q.findList();
		return membs;

	}

	public ComponentMilestone getMilestoneById (Long id) {
		List<ComponentMilestone> milestones = this.getMilestones();
		if (milestones != null && milestones.size()>0)
		{
			Optional<ComponentMilestone> filtered = milestones.stream().filter(m -> m.getComponentMilestoneId().equals(id)).findFirst();
			if (filtered!=null) {
				return filtered.get();
			}
		}
		return  null;
	}
}
