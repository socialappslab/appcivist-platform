package models;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import models.misc.Views;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="CampaignTimelineEdge", description="Edge in the graph that represents the workflow of components associated to a campaign with a process")
public class CampaignTimelineEdge extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long edgeId;
	
	@ManyToOne(cascade=CascadeType.ALL)
	@JsonBackReference
	private Campaign campaign;

	@JsonView(Views.Public.class)
	private Boolean start = false;
	
	@ManyToOne
	@JsonManagedReference
	@JsonView(Views.Public.class)
	private Component fromComponent;
	@Transient
	private Long fromComponentId;
	@ManyToOne
	@JsonManagedReference
	@JsonView(Views.Public.class)
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
		return fromComponent!=null ? fromComponent.getComponentId() : null;
	}

	public Long getToComponentId() {
		return toComponent!=null ? toComponent.getComponentId() : null;
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
