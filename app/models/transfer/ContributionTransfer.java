package models.transfer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import enums.ContributionTypes;

public class ContributionTransfer {
	private Long contributionId;
	private UUID uuid = UUID.randomUUID();
	private String uuidAsString;
	private String title;
	private String text;
	@Enumerated(EnumType.STRING)
	private ContributionTypes type;
	private String textIndex;
	private LocationTransfer location;
	private String budget;
	@JsonIgnoreProperties({ "providers", "roles", "permissions", "sessionKey", "identifier"})
	private List<UserTransfer> authors = new ArrayList<>();
	@JsonIgnoreProperties({ "supportedMembership", "managementType",
			"resources", "forum", "containingSpaces", "themes", "configs",
			"forumPosts", "brainstormingContributions", "proposals" })
	private List<WorkingGroupTransfer> workingGroupAuthors = new ArrayList<>();
	private ContributionStatisticsTransfer stats;
	private List<ThemeTransfer> themes;
	private List<ResourceTransfer> attachments;
	private List<ContributionTransfer> comments = new ArrayList<ContributionTransfer>();
	private List<ComponentMilestoneTransfer> associatedMilestones = new ArrayList<ComponentMilestoneTransfer>();

	/* 
	 * Fields specific to the type ACTION_ITEM
	 */
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date actionDueDate; 
	private Boolean actionDone = false;
	private String action; 
	private String assessmentSummary;
	
	// Fields specific to the type PROPOSAL and ASSESSMENT 
	private ResourceTransfer extendedTextPad;

	// Fields specific to the type PROPOSAL
	private List<ContributionTransfer> assessments;
	private List<WorkingGroupTransfer> existingResponsibleWorkingGroups;
	private List<ContributionTransfer> existingContributions;
	
	public ContributionTransfer() {
		super();
	}

	public Long getContributionId() {
		return contributionId;
	}

	public void setContributionId(Long contributionId) {
		this.contributionId = contributionId;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public ContributionTypes getType() {
		return type;
	}

	public void setType(ContributionTypes type) {
		this.type = type;
	}

	public String getTextIndex() {
		return textIndex;
	}

	public void setTextIndex(String textIndex) {
		this.textIndex = textIndex;
	}

	public LocationTransfer getLocation() {
		return location;
	}

	public void setLocation(LocationTransfer location) {
		this.location = location;
	}

	public String getBudget() {
		return budget;
	}

	public void setBudget(String budget) {
		this.budget = budget;
	}

	public List<UserTransfer> getAuthors() {
		return authors;
	}

	public void setAuthors(List<UserTransfer> authors) {
		this.authors = authors;
	}

	public List<WorkingGroupTransfer> getWorkingGroupAuthors() {
		return workingGroupAuthors;
	}

	public void setWorkingGroupAuthors(
			List<WorkingGroupTransfer> workingGroupAuthors) {
		this.workingGroupAuthors = workingGroupAuthors;
	}

	public ContributionStatisticsTransfer getStats() {
		return stats;
	}

	public void setStats(ContributionStatisticsTransfer stats) {
		this.stats = stats;
	}

	public List<ThemeTransfer> getThemes() {
		return themes;
	}

	public void setThemes(List<ThemeTransfer> themes) {
		this.themes = themes;
	}

	public List<ResourceTransfer> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<ResourceTransfer> attachments) {
		this.attachments = attachments;
	}

	public List<ContributionTransfer> getComments() {
		return comments;
	}

	public void setComments(List<ContributionTransfer> comments) {
		this.comments = comments;
	}

	public List<ComponentMilestoneTransfer> getAssociatedMilestones() {
		return associatedMilestones;
	}

	public void setAssociatedMilestones(
			List<ComponentMilestoneTransfer> associatedMilestones) {
		this.associatedMilestones = associatedMilestones;
	}

	public Date getActionDueDate() {
		return actionDueDate;
	}

	public void setActionDueDate(Date actionDueDate) {
		this.actionDueDate = actionDueDate;
	}

	public Boolean getActionDone() {
		return actionDone;
	}

	public void setActionDone(Boolean actionDone) {
		this.actionDone = actionDone;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAssessmentSummary() {
		return assessmentSummary;
	}

	public void setAssessmentSummary(String assessmentSummary) {
		this.assessmentSummary = assessmentSummary;
	}

	public ResourceTransfer getExtendedTextPad() {
		return extendedTextPad;
	}

	public void setExtendedTextPad(ResourceTransfer extendedTextPad) {
		this.extendedTextPad = extendedTextPad;
	}

	public List<ContributionTransfer> getAssessments() {
		return assessments;
	}

	public void setAssessments(List<ContributionTransfer> assessments) {
		this.assessments = assessments;
	}

	public List<WorkingGroupTransfer> getExistingResponsibleWorkingGroups() {
		return existingResponsibleWorkingGroups;
	}

	public void setExistingResponsibleWorkingGroups(
			List<WorkingGroupTransfer> existingResponsibleWorkingGroups) {
		this.existingResponsibleWorkingGroups = existingResponsibleWorkingGroups;
	}

	public List<ContributionTransfer> getExistingContributions() {
		return existingContributions;
	}

	public void setExistingContributions(
			List<ContributionTransfer> existingContributions) {
		this.existingContributions = existingContributions;
	}
}
