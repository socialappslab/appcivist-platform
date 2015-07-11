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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.Visibility;

@Entity
public class Campaign extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long campaignId;
	private String title; // e.g., "PB for Vallejo 2015"
	private Date startDate;
	private Date endDate;
	private Boolean active = true;
	private String url;
	private Visibility visibility;

	// Relationships

	@ManyToOne
	@JsonBackReference
	private Assembly assembly;

	@OneToOne(cascade = CascadeType.ALL)
	private CampaignType type;

	@OneToMany(mappedBy="campaign", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<CampaignPhase> phases = new ArrayList<CampaignPhase>();

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<Config> campaignConfigs = new ArrayList<Config>();

	@OneToOne
	private ResourcePad proposalTemplate;
	

	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, Campaign> find = new Finder<Long, Campaign>(
			Long.class, Campaign.class);

	public Campaign() {
		super();
	}

	public Campaign(String title, Date startDate, Date endDate, Boolean active,
			String url, Assembly assembly, CampaignType type) {
		super();
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
		this.active = active;
		this.url = url;
		this.assembly = assembly;
		this.type = type;

		// automatically populate the phases based on the campaign type
		if (type != null && type.getDefaultPhases() != null) {
			List<PhaseDefinition> defaultPhases = type.getDefaultPhases();

			for (PhaseDefinition phaseDefinition : defaultPhases) {
				CampaignPhase phase = new CampaignPhase(this, phaseDefinition);
				this.addPhase(phase);
			}
		}
	}

	public Campaign(String title, Date startDate, Date endDate, Boolean active,
			String url, Assembly assembly, CampaignType type,
			List<Config> configs) {
		super();
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
		this.active = active;
		this.url = url;
		this.assembly = assembly;
		this.type = type;
		this.campaignConfigs = configs;

		// automatically populate the phases based on the campaign type
		if (type != null && type.getDefaultPhases() != null) {
			List<PhaseDefinition> defaultPhases = type.getDefaultPhases();

			for (PhaseDefinition phaseDefinition : defaultPhases) {
				CampaignPhase phase = new CampaignPhase(this, phaseDefinition);
				this.addPhase(phase);
			}
		}
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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	public Assembly getAssembly() {
		return assembly;
	}

	public void setAssembly(Assembly assembly) {
		this.assembly = assembly;
	}

	public CampaignType getType() {
		return type;
	}

	public void setType(CampaignType type) {
		this.type = type;
	}

	public List<CampaignPhase> getPhases() {
		return phases;
	}

	public void setPhases(List<CampaignPhase> phases) {
		this.phases = phases;
	}

	public List<Config> getCampaignConfigs() {
		return campaignConfigs;
	}

	public void setCampaignConfigs(List<Config> campaignConfigs) {
		this.campaignConfigs = campaignConfigs;
	}
	
	public ResourcePad getProposalTemplate() {
		return proposalTemplate;
	}

	public void setProposalTemplate(ResourcePad proposalTemplate) {
		this.proposalTemplate = proposalTemplate;
	}

	public static List<Campaign> findAll() {
		List<Campaign> campaigns = find.all();
		return campaigns;
	}

	public static List<Campaign> findByAssembly(Long aid) {
		return find.where().eq("assembly.assemblyId", aid).findList();
	}
	
	private void addPhase(CampaignPhase phase) {
		this.phases.add(phase);
	}

	/*
	 * Basic Data Operations
	 */
	public static void create(Campaign campaign) {
		campaign.save();
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
}
