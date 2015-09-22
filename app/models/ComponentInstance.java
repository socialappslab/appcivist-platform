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
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ResourceSpaceTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class ComponentInstance extends AppCivistBaseModel implements Comparator<ComponentInstance>{

	@Id
	@GeneratedValue
	@Column(name="component_instance_id")
	private Long componentInstanceId;
	private Date startDate;
	private Date endDate;
	private UUID uuid = UUID.randomUUID();
	private int position; 
	private int timeline;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private Component component;

	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonIgnoreProperties({"uuid"})
	@JsonInclude(Include.NON_EMPTY)
	private ResourceSpace resources;
	
	// TODO: check if it works
	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "components")
	private List<ResourceSpace> targetSpaces;
	
	// TODO: probably, each milestone must have its resource space of contributions
	@OneToMany(cascade=CascadeType.ALL, mappedBy="componentInstance")
	private List<ComponentInstanceMilestone> milestones = new ArrayList<ComponentInstanceMilestone>();
		
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, ComponentInstance> find = new Finder<>(ComponentInstance.class);

	public ComponentInstance() {
		super();
	}

	public ComponentInstance(Campaign c, Component component) {
		super();
		this.component = component;
		this.populateDefaultMilestones(c.getType());
	}
	
	public ComponentInstance(Date startDate, Date endDate, Campaign campaign,
			Component component, List<Config> campaignPhaseConfigs) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.component = component;
		this.resources = new ResourceSpace(ResourceSpaceTypes.COMPONENT);
		this.uuid = UUID.randomUUID();
	}

	public ComponentInstance(Campaign c, Component component,
			List<Config> configs) {
		super();
		this.component = component;
		this.resources = new ResourceSpace(ResourceSpaceTypes.COMPONENT);
		this.uuid = UUID.randomUUID();
	}

	public Long getComponentInstanceId() {
		return componentInstanceId;
	}

	public void setComponentInstanceId(Long componentInstanceId) {
		this.componentInstanceId = componentInstanceId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

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

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component definition) {
		this.component = definition;
	}

	/*
	 * Basic Data operations
	 */
	
	public List<ComponentInstanceMilestone> getMilestones() {
		return milestones;
	}

	public void setMilestones(List<ComponentInstanceMilestone> milestones) {
		this.milestones = milestones;
	    if (milestones != null) {
	    	Collections.sort(milestones, new ComponentInstanceMilestone()); // Sorts the array list
			
			ComponentInstanceMilestone firstMilestone = milestones.get(0);
			ComponentInstanceMilestone lastMilestone = milestones.get(milestones.size()-1);
			this.startDate = firstMilestone.getStart();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(lastMilestone.getStart());
			cal.add(Calendar.DATE, lastMilestone.getDays()); // add duration of milestone 
			this.endDate = cal.getTime(); 
		}
	}	
	
	private void populateDefaultMilestones(CampaignTemplate ct) {
		if(this.component!=null && ct != null) {
			List<ComponentRequiredMilestone> reqMilestones = ct.getRequiredMilestones();
			Collections.sort(reqMilestones,new ComponentRequiredMilestone());
			ComponentInstanceMilestone previous = null;
			for (ComponentRequiredMilestone requiredMilestone : reqMilestones) {
				if(requiredMilestone.getTargetUuid().equals(this.component.getUuid())) {
					ComponentInstanceMilestone m = new ComponentInstanceMilestone(requiredMilestone, previous);
					this.addMilestone(m);
					previous = m;
				}
			}
		}
	}

	private void addMilestone(ComponentInstanceMilestone m) {
		this.milestones.add(m);		
	}
	
	public ResourceSpace getResources() {
		return resources;
	}

	public void setResources(ResourceSpace resources) {
		this.resources = resources;
	}

	public List<ResourceSpace> getTargetSpaces() {
		return targetSpaces;
	}

	public void setTargetSpaces(List<ResourceSpace> targetSpaces) {
		this.targetSpaces = targetSpaces;
	}

	public static ComponentInstance read(Long campaignId, Long componentInstanceId) {
		ExpressionList<ComponentInstance> campaignPhases = find.where()
				.eq("targetSpaces.campaigns.campaignId",campaignId)
				.eq("componentInstanceId", componentInstanceId);
		ComponentInstance phase = campaignPhases.findUnique();
		return phase;
    }

    public static List<ComponentInstance> findAll(Long campaignId) {
		ExpressionList<ComponentInstance> campaignPhases = find.where()
				.eq("targetSpaces.campaigns.campaignId",campaignId);
		List<ComponentInstance> campaignPhaseList = campaignPhases.findList();
		return campaignPhaseList;
    }

    public static List<ComponentInstance> findByAssemblyAndCampaign(Long aid, Long campaignId) {
		ExpressionList<ComponentInstance> campaignPhases = find.where()
				.eq("targetSpaces.campaigns.campaignId",campaignId);
		List<ComponentInstance> campaignPhaseList = campaignPhases.findList();
		return campaignPhaseList;
    }

    public static ComponentInstance create(Long campaignId, ComponentInstance phase) {
       Component phaseDefinition = null;
		if(phase.getComponent().getComponentId() != null){
			phaseDefinition = Component.read(phase.getComponent().getComponentId());
		}
		else if(phase.getComponent().getName() != null){
			phaseDefinition = Component.readByName(phase.getComponent().getName());
		}
		phase.setComponent(phaseDefinition);
		phase.save();
        phase.refresh();
        return phase;
    }

    public static ComponentInstance createObject(ComponentInstance object) {
        object.save();
        return object;
    }

	public static void delete(Long campaignId, Long componentInstanceId) {
		ExpressionList<ComponentInstance> campaignPhases = find.where()
				.eq("targetSpaces.campaigns.campaignId", campaignId)
				.eq("componentInstanceId", componentInstanceId);
		ComponentInstance phase = campaignPhases.findUnique();
		phase.delete();
	}

    public static ComponentInstance update(ComponentInstance cp) {
        cp.update();
        cp.refresh();
        return cp;
    }

	@Override
	public int compare(ComponentInstance o1, ComponentInstance o2) {
		return o1.getPosition() - o2.getPosition();
	}
}
