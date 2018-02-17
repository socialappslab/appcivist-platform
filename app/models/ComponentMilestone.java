package models;

import com.avaje.ebean.Query;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ComponentMilestoneTypes;
import enums.ContributionTypes;
import models.misc.Views;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="ComponentMilestone", description="Milestone associated to a component. It represents a deadline within that component")
public class ComponentMilestone extends AppCivistBaseModel implements Comparator<ComponentMilestone> {

	@Id
	@GeneratedValue
	private Long componentMilestoneId;
	@JsonView(Views.Public.class)
	private String title; // name of milestone
	@JsonView(Views.Public.class)
	private String key; // key of milestone
	@JsonView(Views.Public.class)
	private int position;
	@JsonView(Views.Public.class)
	@Column(name="description", columnDefinition="text")
	private String description;
	@JsonView(Views.Public.class)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date date = Calendar.getInstance().getTime();	// starting date of the milestone
	@JsonView(Views.Public.class)
	private Integer days = 1; // duration in dates
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;
	@Enumerated(EnumType.STRING)
	@JsonView(Views.Public.class)
	private ComponentMilestoneTypes type = ComponentMilestoneTypes.END;
	@JsonView(Views.Public.class)
	private ContributionTypes mainContributionType = ContributionTypes.BRAINSTORMING;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "milestones")
	private List<ResourceSpace> containingSpaces;

	@JsonView(Views.Public.class)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	@Column(name = "end_date")
	private Date endDate;	// ending date of the milestone


	public List<ResourceSpace> getContainingSpaces() {
		return containingSpaces;
	}

	public void setContainingSpaces(List<ResourceSpace> containingSpaces) {
		this.containingSpaces = containingSpaces;
	}
	
	/**
	 * The find property is a static property that facilitates database query creation
	 */
	public static Finder<Long, ComponentMilestone> find = new Finder<>(ComponentMilestone.class);
	
	public ComponentMilestone(Long milestoneId, String title,
			Date start, Integer days) {
		super();
		this.componentMilestoneId = milestoneId;
		this.title = title;
		this.date = start;
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
			this.date = Calendar.getInstance().getTime();
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(previousInstance.getDate());
			cal.add(Calendar.DATE, previousInstance.getDays() + 1);
			this.date = cal.getTime();
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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date start) {
		this.date = start;
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

	public ComponentMilestoneTypes getType() {
		return type;
	}

	public void setType(ComponentMilestoneTypes type) {
		this.type = type;
	}

	public ContributionTypes getMainContributionType() {
		return mainContributionType;
	}

	public void setMainContributionType(ContributionTypes mainContributionType) {
		this.mainContributionType = mainContributionType;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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

	public static ComponentMilestone readByUUID(UUID resourceUUID) {
		return find.where().eq("uuid",resourceUUID).findUnique();
	}

	public static List<ComponentMilestone> getMilestoneByDate(Date startDate, Date endDate){

		Query<ComponentMilestone> q = find.where().between("date",startDate,endDate).query();
		List<ComponentMilestone> membs = q.findList();
		return membs;

	}
}
