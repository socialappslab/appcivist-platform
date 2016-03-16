package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ResourceSpaceTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
@SequenceGenerator(name="componentSeq", initialValue=9, allocationSize=50)
public class Component extends AppCivistBaseModel implements Comparator<Component> {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="componentSeq")
	@Column(name = "component_id")
	private Long componentId;
	private String title;
	private String key;
	@Column(name = "description", columnDefinition = "text")
	private String description;
	private Date startDate;
	private Date endDate;
	private UUID uuid = UUID.randomUUID();
	private int position;
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
	
	// TODO: check if it works
	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "components")
	private List<ResourceSpace> containingSpaces;

	@Transient
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
			this.resourceSpace.setMilestones(milestones);
			if (!milestones.isEmpty()) {
				Collections.sort(milestones, new ComponentMilestone()); // Sorts
																				// the
																				// array
																				// list
				ComponentMilestone firstMilestone = milestones.get(0);
				ComponentMilestone lastMilestone = milestones
						.get(milestones.size() - 1);
				this.startDate = firstMilestone.getDate();

				Calendar cal = Calendar.getInstance();
				cal.setTime(lastMilestone.getDate());
				cal.add(Calendar.DATE, lastMilestone.getDays()); // add duration
																	// of
																	// milestone
				this.endDate = cal.getTime();
			}
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
}
