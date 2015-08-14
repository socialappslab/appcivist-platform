package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.avaje.ebean.ExpressionList; 
import com.avaje.ebean.annotation.Formula;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ResourceSpaceTypes;
import enums.Visibility;

@Entity
public class Campaign extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	@Column(name="campaign_id")
	private Long campaignId;
	private String title; // e.g., "PB for Vallejo 2015"
	private String shortname;
	private String goal;	
	private Boolean active = true;
	private String url;
	private UUID uuid;
	@Transient
	private String uuidAsString;
	@Enumerated(EnumType.STRING)
	private Visibility visibility = Visibility.PUBLIC;

	// Relationships	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonIgnoreProperties({"uuid"})
	@JsonInclude(Include.NON_EMPTY)
	private ResourceSpace resources;

	@ManyToOne(cascade = CascadeType.ALL)
	private CampaignType type;

	@OneToMany(mappedBy="campaign", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<CampaignPhase> phases = new ArrayList<CampaignPhase>();
	
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
			String url, CampaignType type) {
		super();
		this.title = title;
		this.active = active;
		this.url = url;
		this.type = type;
		this.uuid =  UUID.randomUUID(); 
		this.resources = new ResourceSpace(ResourceSpaceTypes.CAMPAIGN);

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
			String url, CampaignType type,
			List<Config> configs) {
		super();
		this.title = title;
		this.active = active;
		this.url = url;
		this.type = type;

		this.uuid =  UUID.randomUUID(); 
		this.resources = new ResourceSpace(ResourceSpaceTypes.CAMPAIGN);

		// automatically populate the phases based on the campaign type
		if (type != null && type.getDefaultPhases() != null) this.populateDefaultPhases(type.getDefaultPhases());
	}

	public Campaign(String title, String shortname, Visibility visibility, CampaignType type,
String uuidAsString, List<CampaignPhase> phases) {
		super();
		this.title = title;
		this.shortname = shortname;
		this.visibility = visibility;
		this.type = type;
		this.uuidAsString = uuidAsString;
		this.uuid =  UUID.fromString(uuidAsString);
		this.phases = phases;
		this.resources = new ResourceSpace(ResourceSpaceTypes.CAMPAIGN);

		// automatically populate the phases based on the campaign type
		if (type != null && type.getDefaultPhases() != null) this.populateDefaultPhases(type.getDefaultPhases());
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

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	public ResourceSpace getAssemblyResourceSet() {
		return resources;
	}

	public void setAssemblyResourceSet(ResourceSpace assembly) {
		this.resources = assembly;
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

	private Date getStartDate() {
		if (phases != null) {
			CampaignPhase firstPhase = phases.get(0);
			return firstPhase.getStartDate();
		}
		return null;
	}

	private Date getEndDate() {
		if (phases != null) {
			CampaignPhase lastPhase = phases.get(phases.size()-1);
			return lastPhase.getStartDate();
		}
		return null;
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
	
	
	private void populateDefaultPhases(List<PhaseDefinition> defaultPhaseDefinitions) {
		List<PhaseDefinition> defaultPhases = this.type.getDefaultPhases();
		for (PhaseDefinition phaseDefinition : defaultPhases) {
			CampaignPhase phase = new CampaignPhase(this, phaseDefinition);
			this.addPhase(phase);
		}
	}

	
}
