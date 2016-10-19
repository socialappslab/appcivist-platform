package models.transfer;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonFormat;

import enums.ContributionTypes;

public class ComponentMilestoneTransfer {
	private Long milestoneId;
	private String title;
	private int position;
	private String description;
	private String key;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	private Date start = Calendar.getInstance().getTime(); 
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	private Date date = Calendar.getInstance().getTime();
	private Integer days = 1; 
	private UUID uuid = UUID.randomUUID();
	private String uuidAsString;
	@Enumerated(EnumType.STRING)
	private ContributionTypes mainContributionType = ContributionTypes.BRAINSTORMING;

	public ComponentMilestoneTransfer() {
		super();
	}
	
	public Long getMilestoneId() {
		return milestoneId;
	}

	public void setMilestoneId(Long milestoneId) {
		this.milestoneId = milestoneId;
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

	public void setPosition(int position) {
		this.position = position;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
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
		return uuidAsString;
	}

	public void setUuidAsString(String uuidAsString) {
		this.uuidAsString = uuidAsString;
	}

	public ContributionTypes getMainContributionType() {
		return mainContributionType;
	}

	public void setMainContributionType(ContributionTypes mainContributionType) {
		this.mainContributionType = mainContributionType;
	}
}
