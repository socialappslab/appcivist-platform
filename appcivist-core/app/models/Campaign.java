package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

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

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "previous_campaign")
	private Campaign previousCampaign;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "next_campaign")
	private Campaign nextCampaign;

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
