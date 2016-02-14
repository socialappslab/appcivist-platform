package models.transfer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import models.ComponentMilestone;
import models.Contribution;
import models.Hashtag;
import models.Resource;
import models.Theme;
import models.WorkingGroup;
import models.location.Location;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import enums.ContributionTypes;

public class ContributionTransfer {
	private Long contributionId;
	private UUID uuid = UUID.randomUUID();
	private String uuidAsString;
	private String title;
	@Column(name="text", columnDefinition="text")
	private String text;
	@Enumerated(EnumType.STRING)
	private ContributionTypes type;
	@Column(name="text_index", columnDefinition="text")
	private String textIndex;
	private Location location;
	private String budget;
	@JsonIgnoreProperties({ "providers", "roles", "permissions", "sessionKey", "identifier"})
	private List<UserTransfer> authors = new ArrayList<>();
	@JsonIgnoreProperties({ "supportedMembership", "managementType",
			"resources", "forum", "containingSpaces", "themes", "configs",
			"forumPosts", "brainstormingContributions", "proposals" })
	private List<WorkingGroupTransfer> workingGroupAuthors = new ArrayList<>();
	private ContributionStatisticsTransfer stats;
	private List<Theme> themes;
	private List<Resource> attachments;
	private List<Hashtag> hashtags = new ArrayList<Hashtag>();
	private List<Contribution> comments = new ArrayList<Contribution>();
	private List<ComponentMilestone> associatedMilestones = new ArrayList<ComponentMilestone>();

	/* 
	 * Fields specific to the type ACTION_ITEM
	 */
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date actionDueDate; 
	private Boolean actionDone = false;
	private String action; 
	private String assessmentSummary;
	
	// Fields specific to the type PROPOSAL and ASSESSMENT 
	private Resource extendedTextPad;

	// Fields specific to the type PROPOSAL
	private List<Contribution> assessments;
	
	private List<WorkingGroup> responsibleWorkingGroups;
	private List<Hashtag> existingHashtags;
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

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
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

	public List<Theme> getThemes() {
		return themes;
	}

	public void setThemes(List<Theme> themes) {
		this.themes = themes;
	}

	public List<Resource> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Resource> attachments) {
		this.attachments = attachments;
	}

	public List<Hashtag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(List<Hashtag> hashtags) {
		this.hashtags = hashtags;
	}

	public List<Contribution> getComments() {
		return comments;
	}

	public void setComments(List<Contribution> comments) {
		this.comments = comments;
	}

	public List<ComponentMilestone> getAssociatedMilestones() {
		return associatedMilestones;
	}

	public void setAssociatedMilestones(
			List<ComponentMilestone> associatedMilestones) {
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

	public Resource getExtendedTextPad() {
		return extendedTextPad;
	}

	public void setExtendedTextPad(Resource extendedTextPad) {
		this.extendedTextPad = extendedTextPad;
	}

	public List<Contribution> getAssessments() {
		return assessments;
	}

	public void setAssessments(List<Contribution> assessments) {
		this.assessments = assessments;
	}

	public List<WorkingGroup> getResponsibleWorkingGroups() {
		return responsibleWorkingGroups;
	}

	public void setResponsibleWorkingGroups(
			List<WorkingGroup> responsibleWorkingGroups) {
		this.responsibleWorkingGroups = responsibleWorkingGroups;
	}

	public List<Hashtag> getExistingHashtags() {
		return existingHashtags;
	}

	public void setExistingHashtags(List<Hashtag> existingHashtags) {
		this.existingHashtags = existingHashtags;
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
