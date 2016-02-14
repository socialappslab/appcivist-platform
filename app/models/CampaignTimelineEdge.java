package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class CampaignTimelineEdge extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long edgeId;
	
	@ManyToOne(cascade=CascadeType.ALL)
	@JsonBackReference
	private Campaign campaign;
	
	private Boolean start = false;
	
	@ManyToOne
	@JsonManagedReference
	private Component fromComponent;
	@Transient
	private Long fromComponentId;
	@ManyToOne
	@JsonManagedReference
	private Component toComponent;	
	@Transient
	private Long toComponentId;

	public static Finder<Long, CampaignTimelineEdge> find = new Finder<>(CampaignTimelineEdge.class);
	   
    public CampaignTimelineEdge() {
    	super();
    }

    public Long getEdgeId() {
		return edgeId;
	}

	public void setEdgeId(Long timelineId) {
		this.edgeId = timelineId;
	}

	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	public Boolean getStart() {
		return start;
	}

	public void setStart(Boolean start) {
		this.start = start;
	}

	public Component getFromComponent() {
		return fromComponent;
	}

	public void setFromComponent(Component fromComponent) {
		this.fromComponent = fromComponent;
	}

	public Component getToComponent() {
		return toComponent;
	}

	public void setToComponent(Component toComponent) {
		this.toComponent = toComponent;
	}

	public Long getFromComponentId() {
		return fromComponent.getComponentId();
	}

	public Long getToComponentId() {
		return toComponent.getComponentId();
	}

	/*
	 * Basic Queries
	 */
    public static List<CampaignTimelineEdge> findAll() {
        return find.all();
    }
    
    public static CampaignTimelineEdge findByCampaign(Long campaignId) {
        return find.where().eq("campaign.campaignId", campaignId).findUnique();
    }
    
	public static CampaignTimelineEdge read(Long id) {
        return find.ref(id);
    }

    public static CampaignTimelineEdge create(CampaignTimelineEdge timeline) {
        timeline.save();
        timeline.refresh();
        return timeline;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static CampaignTimelineEdge update(CampaignTimelineEdge timeline) {
    	timeline.update();
    	timeline.refresh();
    	return timeline;
    }
}
