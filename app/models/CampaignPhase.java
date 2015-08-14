package models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.annotation.Formula;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ResourceSpaceTypes;

@Entity
public class CampaignPhase extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	@Column(name="phase_id")
	private Long phaseId;
	private Date startDate;
	private Date endDate;
	private UUID uuid = UUID.randomUUID();
	
	@ManyToOne
	@JsonBackReference
	private Campaign campaign;

	@ManyToOne(cascade=CascadeType.ALL)
	private PhaseDefinition definition;

	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonIgnoreProperties({"uuid"})
	@JsonInclude(Include.NON_EMPTY)
	private ResourceSpace resources;
	
	// TODO: probably, each milestone must have its resource space of contributions
	@OneToMany(cascade=CascadeType.ALL)
	private List<CampaignPhaseMilestone> milestones = new ArrayList<CampaignPhaseMilestone>();
	
	private Boolean canOverlap = false;
	
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, CampaignPhase> find = new Finder<>(CampaignPhase.class);

	public CampaignPhase() {
		super();
	}

	public CampaignPhase(Campaign c, PhaseDefinition definition) {
		super();
		this.campaign = c;
		this.definition = definition;
		this.populateDefaultMilestones();
	}
	
	public CampaignPhase(Date startDate, Date endDate, Campaign campaign,
			PhaseDefinition definition, List<Config> campaignPhaseConfigs) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.campaign = campaign;
		this.definition = definition;
		this.resources = new ResourceSpace(ResourceSpaceTypes.CAMPAIGN_PHASE);
		this.uuid = UUID.randomUUID();
	}

	public CampaignPhase(Campaign c, PhaseDefinition definition,
			List<Config> configs) {
		super();
		this.campaign = c;
		this.definition = definition;
		this.resources = new ResourceSpace(ResourceSpaceTypes.CAMPAIGN_PHASE);
		this.uuid = UUID.randomUUID();
	}

	public Long getPhaseId() {
		return phaseId;
	}

	public void setPhaseId(Long phaseId) {
		this.phaseId = phaseId;
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

	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	public PhaseDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(PhaseDefinition definition) {
		this.definition = definition;
	}

	/*
	 * Basic Data operations
	 */
	
	public List<CampaignPhaseMilestone> getMilestones() {
		return milestones;
	}

	public void setMilestones(List<CampaignPhaseMilestone> milestones) {
		this.milestones = milestones;
		if (milestones != null) {
			CampaignPhaseMilestone firstMilestone = milestones.get(0);
			CampaignPhaseMilestone lastMilestone = milestones.get(milestones.size()-1);
			this.startDate = firstMilestone.getStart();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(lastMilestone.getStart());
			cal.add(Calendar.DATE, lastMilestone.getDays()); // add 10 days
			 
			this.endDate = cal.getTime(); 
		}
	}	
	
	private void populateDefaultMilestones() {
		if(this.definition!=null) {
			List<RequiredCampaignPhaseMilestone> reqMilestones = this.definition.getRequiredMilestones();
			for (RequiredCampaignPhaseMilestone requiredCampaignPhaseMilestone : reqMilestones) {
				CampaignPhaseMilestone m = new CampaignPhaseMilestone(requiredCampaignPhaseMilestone);
				this.addMilestone(m);
			}
		}
	}

	private void addMilestone(CampaignPhaseMilestone m) {
		this.milestones.add(m);		
	}
	
	public Boolean getCanOverlap() {
		return canOverlap;
	}

	public void setCanOverlap(Boolean canOverlap) {
		this.canOverlap = canOverlap;
	}

	public static CampaignPhase read(Long campaignId, Long phaseId) {
		ExpressionList<CampaignPhase> campaignPhases = find.where()
				.eq("campaign.campaignId",campaignId)
				.eq("phaseId", phaseId);
		CampaignPhase phase = campaignPhases.findUnique();
		return phase;
    }

    public static List<CampaignPhase> findAll(Long campaignId) {
//		ExpressionList<CampaignPhase> campaignPhases = find.where().eq("campaign_campaign_id",campaignId);
		ExpressionList<CampaignPhase> campaignPhases = find.where()
				.eq("campaign.campaignId",campaignId);
		List<CampaignPhase> campaignPhaseList = campaignPhases.findList();
		return campaignPhaseList;
    }

    public static List<CampaignPhase> findByAssemblyAndCampaign(Long aid, Long campaignId) {
		ExpressionList<CampaignPhase> campaignPhases = find.where()
				.eq("campaign.campaignId",campaignId)
				.eq("campaign.assembly.assemblyId", aid);
		List<CampaignPhase> campaignPhaseList = campaignPhases.findList();
		return campaignPhaseList;
    }

    public static CampaignPhase create(Long campaignId, CampaignPhase phase) {
        Campaign campaign = Campaign.read(campaignId);
		PhaseDefinition phaseDefinition = null;
		if(phase.getDefinition().getPhaseDefinitionId() != null){
			phaseDefinition = PhaseDefinition.read(phase.getDefinition().getPhaseDefinitionId());
		}
		else if(phase.getDefinition().getName() != null){
			phaseDefinition = PhaseDefinition.readByName(phase.getDefinition().getName());
		}
		phase.setCampaign(campaign);
		phase.setDefinition(phaseDefinition);
		phase.save();
        phase.refresh();
        return phase;
    }

    public static CampaignPhase createObject(CampaignPhase object) {
        object.save();
        return object;
    }

    public static void delete(Long campaignId, Long phaseId) {
		ExpressionList<CampaignPhase> campaignPhases = find.where().eq("campaign_campaign_id", campaignId).eq("phase_id",phaseId);
		CampaignPhase phase = campaignPhases.findUnique();
		phase.delete();
    }

    public static CampaignPhase update(CampaignPhase cp) {
        cp.update();
        cp.refresh();
        return cp;
    }
}
