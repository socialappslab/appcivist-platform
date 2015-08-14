package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class RequiredCampaignPhaseMilestone extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long campaignPhaseMilestoneId;

	private String title; // name of milestone
	
	/**
	 * The find property is a static property that facilitates database query creation
	 */
	public static Finder<Long, RequiredCampaignPhaseMilestone> find = new Finder<>(RequiredCampaignPhaseMilestone.class);
	
	public RequiredCampaignPhaseMilestone(Long campaignPhaseMilestoneId, String title) {
		super();
		this.campaignPhaseMilestoneId = campaignPhaseMilestoneId;
		this.title = title;
	}
	
	public RequiredCampaignPhaseMilestone() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getCampaignPhaseMilestoneId() {
		return campaignPhaseMilestoneId;
	}

	public void setCampaignPhaseMilestoneId(Long campaignPhaseMilestoneId) {
		this.campaignPhaseMilestoneId = campaignPhaseMilestoneId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public static RequiredCampaignPhaseMilestone read(Long id) {
        return find.ref(id);
    }

    public static List<RequiredCampaignPhaseMilestone> findAll() {
        return find.all();
    }

    public static RequiredCampaignPhaseMilestone create(RequiredCampaignPhaseMilestone object) {
        object.save();
        object.refresh();
        return object;
    }

    public static RequiredCampaignPhaseMilestone createObject(RequiredCampaignPhaseMilestone object) {
        object.save();
        return object;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }
}
