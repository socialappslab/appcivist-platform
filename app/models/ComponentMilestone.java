package models;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ContributionTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class ComponentMilestone extends AppCivistBaseModel implements Comparator<ComponentMilestone> {

	@Id
	@GeneratedValue
	private Long componentMilestoneId;

	private String title; // name of milestone
	private int position;
	@Column(name="description", columnDefinition="text")
	private String description;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date start = Calendar.getInstance().getTime();	// starting date of the milestone
	private Integer days = 1; // duration in dates
	private UUID uuid = UUID.randomUUID(); 
	@Transient
	private String uuidAsString;
	
	private ContributionTypes mainContributionType = ContributionTypes.BRAINSTORMING;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "milestones")
	private List<ResourceSpace> containingSpaces;
	
	/**
	 * The find property is a static property that facilitates database query creation
	 */
	public static Finder<Long, ComponentMilestone> find = new Finder<>(ComponentMilestone.class);
	
	public ComponentMilestone(Long milestoneId, String title,
			Date start, Integer days) {
		super();
		this.componentMilestoneId = milestoneId;
		this.title = title;
		this.start = start;
		this.days = days;
	}
	
	public ComponentMilestone() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ComponentMilestone(
			ComponentRequiredMilestone requiredComponentMilestone,
			ComponentMilestone previousInstance) {
		super(requiredComponentMilestone.getLang());

		this.title = requiredComponentMilestone.getTitle();
		this.description = requiredComponentMilestone.getDescription();

		if (previousInstance == null) {
			this.start = Calendar.getInstance().getTime();
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(previousInstance.getStart());
			cal.add(Calendar.DATE, previousInstance.getDays() + 1);
			this.start = cal.getTime();
		}
	}

	public Long getComponentMilestoneId() {
		return componentMilestoneId;
	}

	public void setComponentMilestoneId(Long milestoneId) {
		this.componentMilestoneId = milestoneId;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getUuidAsString() {
		this.uuidAsString = this.uuid.toString();
		return uuidAsString;
	}

	public void setUuidAsString(String uuidAsString) {
		this.uuidAsString = uuidAsString;
		this.uuid = UUID.fromString(uuidAsString);
	}

	public ContributionTypes getMainContributionType() {
		return mainContributionType;
	}

	public void setMainContributionType(ContributionTypes mainContributionType) {
		this.mainContributionType = mainContributionType;
	}

	public static ComponentMilestone read(Long id) {
        return find.ref(id);
    }

    public static List<ComponentMilestone> findAll() {
        return find.all();
    }

    public static ComponentMilestone create(ComponentMilestone object) {
        object.save();
        object.refresh();
        return object;
    }

    public static ComponentMilestone createObject(ComponentMilestone object) {
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
	public int compare(ComponentMilestone o1,
			ComponentMilestone o2) {
//		return o1.getStart().after(o2.getStart()) ? 1 : o1.getStart().equals(o2.getStart()) ? 0 : -1;
		return o1.getPosition() - o2.getPosition();
	}
}
