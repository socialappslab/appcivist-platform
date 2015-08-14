package models;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class CampaignPhaseMilestone extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long campaignPhaseMilestoneId;

	private String title; // name of milestone
	private Date start = Calendar.getInstance().getTime();	// starting date of the milestone
	private Integer days = 0; // duration in dates
	private UUID uuid = UUID.randomUUID(); 
	
	/**
	 * The find property is a static property that facilitates database query creation
	 */
	public static Finder<Long, CampaignPhaseMilestone> find = new Finder<>(CampaignPhaseMilestone.class);
	
	public CampaignPhaseMilestone(Long campaignPhaseMilestoneId, String title,
			Date start, Integer days) {
		super();
		this.campaignPhaseMilestoneId = campaignPhaseMilestoneId;
		this.title = title;
		this.start = start;
		this.days = days;
	}
	
	public CampaignPhaseMilestone() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CampaignPhaseMilestone(
			RequiredCampaignPhaseMilestone requiredCampaignPhaseMilestone) {
		super(requiredCampaignPhaseMilestone.getLang());
		this.title = requiredCampaignPhaseMilestone.getTitle();
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

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Integer getDays() {
		return days;
	}

	public void setDays(Integer days) {
		this.days = days;
	}
	
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public static CampaignPhaseMilestone read(Long id) {
        return find.ref(id);
    }

    public static List<CampaignPhaseMilestone> findAll() {
        return find.all();
    }

    public static CampaignPhaseMilestone create(CampaignPhaseMilestone object) {
        object.save();
        object.refresh();
        return object;
    }

    public static CampaignPhaseMilestone createObject(CampaignPhaseMilestone object) {
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
