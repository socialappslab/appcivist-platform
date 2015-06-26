package models.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;

@Entity
public class ServiceCampaign extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3367429873420318943L;

	@Id
	private Long campaignId;
	private String name;
	private String url;
	private String startDate;
	private String endDate;
	private Boolean enabled = true;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "previous_campaign")
	private ServiceCampaign previousCampaign;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "next_campaign")
	private ServiceCampaign nextCampaign;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "issue_issue_id")
	private ServiceIssue issue;

	/*
	 * Properties that allow the tracking of the evolution of a campaign
	 */
	@ManyToMany(cascade=CascadeType.ALL)
	private List<ServiceOperation> availableOperations;
	
	@ManyToMany(cascade=CascadeType.ALL)
	private List<ServiceResource> campaignResources;
	
	@Transient
	private Map<String, ServiceResource> campaignResourcesMap = new HashMap<String, ServiceResource>();
	
	@ManyToMany(cascade=CascadeType.ALL)
	private List<ServiceResource> inputResources;
	
	@ManyToOne
	private ServiceOperation startOperation;
	
	private String startOperationType;
	
	/*
	 * Basic Data Operations
	 */
	
	public static Model.Finder<Long, ServiceCampaign> find = new Model.Finder<Long, ServiceCampaign>(
			Long.class, ServiceCampaign.class);

	public static List<ServiceCampaign> findAll() {
		List<ServiceCampaign> campaigns = find.all();
		return campaigns;
	}

	public static void create(ServiceCampaign campaign) {
		campaign.save();
		campaign.refresh();
	}

	public static ServiceCampaign read(Long campaignId) {
		return find.ref(campaignId);
	}

	public static ServiceCampaign createObject(ServiceCampaign campaign) {
		campaign.save();
		return campaign;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	/*
	 * Getters and Setters
	 */
	public Long getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Long id) {
		this.campaignId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public ServiceCampaign getPreviousCampaign() {
		return previousCampaign;
	}

	public void setPreviousCampaign(ServiceCampaign previousCampaign) {
		this.previousCampaign = previousCampaign;
	}

	public ServiceCampaign getNextCampaign() {
		return nextCampaign;
	}

	public void setNextCampaign(ServiceCampaign nextCampaign) {
		this.nextCampaign = nextCampaign;
	}

	/*
	 * Other Queries
	 */

	public ServiceIssue getIssue() {
		return issue;
	}

	public void setIssue(ServiceIssue issue) {
		this.issue = issue;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public List<ServiceOperation> getAvailableOperations() {
		return availableOperations;
	}

	public void setAvailableOperations(List<ServiceOperation> availableOperations) {
		this.availableOperations = availableOperations;
	}

	public List<ServiceResource> getCampaignResources() {
		return campaignResources;
	}

	public void setCampaignResources(List<ServiceResource> campaignResources) {
		this.campaignResources = campaignResources;
	}
	
	public void addCampaignResource(ServiceResource r) {
		this.campaignResources.add(r);
		this.campaignResourcesMap.put(r.getType(), r);
	}

	public List<ServiceResource> getInputResources() {
		return inputResources;
	}

	public void setInputResources(List<ServiceResource> inputResources) {
		this.inputResources = inputResources;
	}

	public ServiceOperation getStartOperation() {
		return startOperation;
	}

	public void setStartOperation(ServiceOperation startOperation) {
		this.startOperation = startOperation;
	}

	public String getStartOperationType() {
		return startOperationType;
	}

	public void setStartOperationType(String startOperationType) {
		this.startOperationType = startOperationType;
	}

	/**
	 * Obtain the campaign cid of issue iid, part of assembly aid
	 * 
	 * @param aid
	 * @param iid
	 * @param cid
	 * @return
	 */
	public static ServiceCampaign readCampaignOfIssue(Long aid, Long iid, Long cid) {
	// TODO for simplification, first version of models has all entities to have an 
	// 		unique id, change this to have relative ids in the future
		return find.where()
				.eq("issue_issue_id", iid) // TODO this is not needed now, but we should have relative ids
				.eq("campaignId", cid).findUnique();
	}
}
