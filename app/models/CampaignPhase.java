package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;

@Entity
public class CampaignPhase extends AppCivistBaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4311376060179356946L;

	@Id
	@GeneratedValue
	private Long phaseId;
	private Date startDate;
	private Date endDate;

	@JsonIgnore
	@ManyToOne
	private Campaign campaign;

	@OneToOne
	private PhaseDefinition definition;
	
	@JsonIgnore
	@OneToMany(cascade=CascadeType.ALL)
	private List<Config> campaignPhaseConfigs = new ArrayList<Config>();
	
	private Boolean canOverlap = false;
	
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Model.Finder<Long, CampaignPhase> find = new Model.Finder<Long, CampaignPhase>(
			Long.class, CampaignPhase.class);

	public CampaignPhase() {
		super();
	}

	public CampaignPhase(Campaign c, PhaseDefinition definition) {
		super();
		this.campaign = c;
		this.definition = definition;
	}
	
	public CampaignPhase(Date startDate, Date endDate, Campaign campaign,
			PhaseDefinition definition, List<Config> campaignPhaseConfigs) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.campaign = campaign;
		this.definition = definition;
		this.campaignPhaseConfigs = campaignPhaseConfigs;
	}

	public CampaignPhase(Campaign c, PhaseDefinition definition,
			List<Config> configs) {
		super();
		this.campaign = c;
		this.definition = definition;
		this.campaignPhaseConfigs = configs;
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

	public List<Config> getCampaignPhaseConfigs() {
		return campaignPhaseConfigs;
	}

	public void setCampaignPhaseConfigs(List<Config> campaignPhaseConfigs) {
		this.campaignPhaseConfigs = campaignPhaseConfigs;
	}
	
	/*
	 * Basic Data operations
	 */
	
	public Boolean getCanOverlap() {
		return canOverlap;
	}

	public void setCanOverlap(Boolean canOverlap) {
		this.canOverlap = canOverlap;
	}

	public static CampaignPhase read(Long campaignId, Long phaseId) {
		ExpressionList<CampaignPhase> campaignPhases = find.where().eq("campaign_campaign_id", campaignId).eq("phase_id",phaseId);
		CampaignPhase phase = campaignPhases.findList().get(0);
		return phase;
    }

    public static List<CampaignPhase> findAll(Long campaignId) {
		ExpressionList<CampaignPhase> campaignPhases = find.where().eq("campaign_campaign_id",campaignId);
		List<CampaignPhase> campaignPhaseList = campaignPhases.findList();
		return campaignPhaseList;
    }

    public static CampaignPhase create(CampaignPhase object) {
        object.save();
        object.refresh();
        return object;
    }

    public static CampaignPhase createObject(CampaignPhase object) {
        object.save();
        return object;
    }

    public static void delete(Long campaignId, Long phaseId) {
		ExpressionList<CampaignPhase> campaignPhases = find.where().eq("campaign_campaign_id", campaignId).eq("phase_id",phaseId);
		CampaignPhase phase = campaignPhases.findList().get(0);
		phase.delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }
}
