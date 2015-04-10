package models;

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

import org.apache.commons.collections.map.HashedMap;

import models.services.ServiceOperation;
import models.services.ServiceResource;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;
import scala.collection.mutable.HashMap;

@Entity
public class Campaign extends Model {

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
	private Campaign previousCampaign;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "next_campaign")
	private Campaign nextCampaign;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "issue_issue_id")
	private Issue issue;

	/*
	 * Properties that allow the tracking of the evolution of a campaign
	 */
	@ManyToMany(cascade=CascadeType.ALL)
	private List<ServiceOperation> availableOperations;
	
	@ManyToMany(cascade=CascadeType.ALL)
	private List<ServiceResource> campaignResources;
	
	@Transient
	private Map<String, ServiceResource> campaignResourcesMap = new HashMap<String,ServiceResource>();
	
	@ManyToMany(cascade=CascadeType.ALL)
	private List<ServiceResource> inputResources;
	
	@ManyToOne
	private ServiceOperation startOperation;
	
	private String startOperationType;
	
	/*
	 * Basic Data Operations
	 */
	
	public static Model.Finder<Long, Campaign> find = new Model.Finder<Long, Campaign>(
			Long.class, Campaign.class);

	public static List<Campaign> findAll() {
		List<Campaign> campaigns = find.all();
		return campaigns;
	}

	public static void create(Campaign campaign) {
		campaign.save();
		campaign.saveManyToManyAssociations("availableOperations");
		campaign.saveManyToManyAssociations("campaignResources");
		campaign.refresh();
	}

	public static Campaign read(Long campaignId) {
		return find.ref(campaignId);
	}

	public static Campaign createObject(Campaign campaign) {
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

	public Campaign getPreviousCampaign() {
		return previousCampaign;
	}

	public void setPreviousCampaign(Campaign previousCampaign) {
		this.previousCampaign = previousCampaign;
	}

	public Campaign getNextCampaign() {
		return nextCampaign;
	}

	public void setNextCampaign(Campaign nextCampaign) {
		this.nextCampaign = nextCampaign;
	}

	/*
	 * Other Queries
	 */

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
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
	public static Campaign readCampaignOfIssue(Long aid, Long iid, Long cid) {
	// TODO for simplification, first version of models has all entities to have an 
	// 		unique id, change this to have relative ids in the future
		return find.where()
				.eq("issue_issue_id", iid) // TODO this is not needed now, but we should have relative ids
				.eq("campaignId", cid).findUnique();
	}
}
