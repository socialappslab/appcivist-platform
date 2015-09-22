package models;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class ComponentRequiredMilestone extends AppCivistBaseModel implements Comparator<ComponentRequiredMilestone> {

	@Id
	@GeneratedValue
	private Long componentRequiredMilestoneId;

	private String title; // name of milestone
    private int position;	
    
    @Index 
	private UUID targetUuid;
    @Transient
    private String targetUuidAsString;
    
	@ManyToOne
	@JsonBackReference
	private CampaignTemplate campaignTemplate;

    
	/**
	 * The find property is a static property that facilitates database query creation
	 */
	public static Finder<Long, ComponentRequiredMilestone> find = new Finder<>(ComponentRequiredMilestone.class);
	
	public ComponentRequiredMilestone(Long requiredMilestoneId, String title) {
		super();
		this.componentRequiredMilestoneId = requiredMilestoneId;
		this.title = title;
	}
	
	public ComponentRequiredMilestone() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getComponentRequiredMilestoneId() {
		return componentRequiredMilestoneId;
	}

	public void setComponentRequiredMilestoneId(Long requiredMilestoneId) {
		this.componentRequiredMilestoneId = requiredMilestoneId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int order) {
		this.position = order;
	}

	public UUID getTargetUuid() {
		return targetUuid;
	}

	public void setTargetUuid(UUID targetUuid) {
		this.targetUuid = targetUuid;
	}

	public String getTargetUuidAsString() {
		this.targetUuidAsString = this.targetUuid.toString();
		return targetUuidAsString;
	}

	public void setTargetUuidAsString(String targetUuuidAsString) {
		this.targetUuidAsString = targetUuuidAsString;
		this.targetUuid = UUID.fromString(targetUuuidAsString);
	}

	public CampaignTemplate getCampaignTemplate() {
		return campaignTemplate;
	}

	public void setCampaignTemplate(CampaignTemplate campaignTemplate) {
		this.campaignTemplate = campaignTemplate;
	}
	
	public Component getComponent() {
		return Component.findByUUID(this.targetUuid);
	}

	public static ComponentRequiredMilestone read(Long id) {
        return find.ref(id);
    }

    public static List<ComponentRequiredMilestone> findAll() {
        return find.all();
    }

    public static ComponentRequiredMilestone create(ComponentRequiredMilestone object) {
        object.save();
        object.refresh();
        return object;
    }

    public static ComponentRequiredMilestone createObject(ComponentRequiredMilestone object) {
        object.save();
        return object;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }

	@Override
	public int compare(ComponentRequiredMilestone o1,
			ComponentRequiredMilestone o2) {
		return o1.getPosition() - o2.getPosition();
	}
}
