package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import models.audit.AuditContribution;
import models.location.Location;
//newly added
import delegates.RedundanciesDelegate;
import controllers.Redundancies; 

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.AuditEventTypes;
import enums.ContributionTypes;
import enums.ResourceSpaceTypes;

@Entity @JsonInclude(Include.NON_EMPTY)
@Where(clause="removed=false")
public class Contribution extends AppCivistBaseModel {

	@Id @GeneratedValue
	private Long contributionId;
	@Index @JsonIgnore
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;
	private String title;
	private String text;
	@Enumerated(EnumType.STRING)
	private ContributionTypes type;
	@JsonIgnore @Index
	private String textIndex;
	@OneToOne @Index
	private Location location;
	@ManyToMany(cascade = CascadeType.REFRESH)
	@Where(clause="${ta}.active=true")
	@JsonIgnoreProperties({ "providers", "roles", "permissions", "sessionKey", "identifier"})
	private List<User> authors = new ArrayList<User>();
	@JsonIgnore 
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "contributions")
	private List<ResourceSpace> containingSpaces;
	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) 
	@JsonIgnoreProperties({ "contributionStatisticsId" })
	private ResourceSpace resourceSpace = new ResourceSpace(ResourceSpaceTypes.CONTRIBUTION);
	@OneToOne(cascade = CascadeType.ALL) 
	@JsonIgnoreProperties({ "contributionStatisticsId" })
	@JsonManagedReference
	private ContributionStatistics stats = new ContributionStatistics();

	//newly added

	@Transient 
	private List<Long> similarContri =  new ArrayList<Long>();

	/* 
	 * Transient properties that take their values from the associated resource space
	 */
	@Transient
	private List<Theme> themes;
	@Transient
	private List<Resource> attachments;
	@Transient
	private List<Hashtag> hashtags = new ArrayList<Hashtag>();
	@Transient
	private List<Contribution> comments = new ArrayList<Contribution>();
	@Transient
	private List<ComponentInstanceMilestone> associatedMilestones = new ArrayList<ComponentInstanceMilestone>();

	/* 
	 * The following fields are specific to each type of contribution
	 */
	
	/* 
	 * Fields specific to the type ACTION_ITEM
	 */
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date actionDueDate; 
	private Boolean actionDone = false;
	private String action; 
	
	// Fields Fields specific to the type ASSESSMENT
	private String assessmentSummary;
	
	// Fields specific to the type PROPOSAL and ASSESSMENT 
	@OneToOne(cascade=CascadeType.ALL)
	private Resource extendedTextPad;

	// Fields specific to the type PROPOSAL
	@Transient
	private List<Contribution> assessments;

	@Transient
	private List<WorkingGroup> responsibleWorkingGroups;
	
	/* 
	 * @Transient existing entities in resource space
	 */
	@Transient
	private List<Hashtag> existingHashtags;
	@Transient
	private List<WorkingGroup> existingResponsibleWorkingGroups;
	@Transient
	private List<Contribution> existingContributions;
	@Transient
	private List<Resource> existingResources;	
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, Contribution> find = new Finder<>(
			Contribution.class);

	public Contribution(User creator, String title, String text,
			ContributionTypes type) {
		super();
		this.authors.add(creator);
		this.title = title;
		this.text = text;
		this.type = type;
		//newly added
		this.similarContri = RedundanciesDelegate.match_keywords(this.getContributionId());

	}

	public Contribution() {
		super();
	}

	/*
	 * Getters and Setters
	 */

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
		return uuid.toString();
	}

	public void setUuidAsString(String uuidAsString) {
		this.uuid = UUID.fromString(uuidAsString);
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

	public List<User> getAuthors() {
		return authors;
	}

	public void setAuthors(List<User> authors) {
		this.authors = authors;
	}

	public void addAuthor(User author) {
		this.authors.add(author);
	}

	public List<Theme> getThemes() {
		return resourceSpace.getThemes();
	}

	public void setThemes(List<Theme> themes) {
		this.themes = themes;
		this.resourceSpace.setThemes(themes);
	}

	public void addTheme(Theme t) {
		this.resourceSpace.addTheme(t);
	}

	public List<Resource> getAttachments() {
		return resourceSpace.getResources();
	}

	public void setAttachments(List<Resource> attachments) {
		this.attachments = attachments;
		this.resourceSpace.setResources(attachments);
	}

	public void addAttachment(Resource attach) {
		this.resourceSpace.addResource(attach);
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public List<Hashtag> getHashtags() {
		return resourceSpace.getHashtags();
	}

	public void setHashtags(List<Hashtag> hashtags) {
		this.hashtags = hashtags;
		this.resourceSpace.setHashtags(hashtags);
	}

	public void addHashtag(Hashtag h) {
		this.resourceSpace.addHashtag(h);
	}
	
	public List<Contribution> getComments() {
		return resourceSpace.getContributionsFilteredByType(ContributionTypes.COMMENT);
	}

	public void addComment(Contribution c) {
		if (c.getType() == ContributionTypes.COMMENT) 
			this.resourceSpace.addContribution(c);
	}
	
	public List<ComponentInstanceMilestone> getAssociatedMilestones() {
		return this.resourceSpace.getMilestones();
	}

	public void setAssociatedMilestones(
			List<ComponentInstanceMilestone> associatedMilestones) {
		this.associatedMilestones = associatedMilestones;
		this.resourceSpace.setMilestones(associatedMilestones);
	}

	public ResourceSpace getResourceSpace() {
		return resourceSpace;
	}

	public void setResourceSpace(ResourceSpace resourceSpace) {
		this.resourceSpace = resourceSpace;
	}

	public ContributionStatistics getStats() {
		return stats;
	}

	public void setStats(ContributionStatistics stats) {
		this.stats = stats;
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
		return this.resourceSpace.getContributionsFilteredByType(ContributionTypes.ASSESSMENT);
	}

	public void addAssessment(Contribution assessment) {
		if(assessment.getType()==ContributionTypes.ASSESSMENT)
			this.resourceSpace.addContribution(assessment);
	}

	public List<WorkingGroup> getResponsibleWorkingGroups() {
		return this.resourceSpace.getWorkingGroups();
	}

	public void setResponsibleWorkingGroups(
			List<WorkingGroup> responsibleWorkingGroups) {
		this.resourceSpace.setWorkingGroups(responsibleWorkingGroups);
	}

	/* 
	 * @Transient getting/setting methods existing entities in resource space
	 */

	@JsonIgnore
	public List<Hashtag> getExistingHashtags() {
		return existingHashtags;
	}

	public void setExistingHashtags(List<Hashtag> existingHashtags) {
		this.existingHashtags = existingHashtags;
	}

	@JsonIgnore
	public List<WorkingGroup> getExistingResponsibleWorkingGroups() {
		return existingResponsibleWorkingGroups;
	}

	public void setExistingResponsibleWorkingGroups(
			List<WorkingGroup> existingResponsibleWorkingGroups) {
		this.existingResponsibleWorkingGroups = existingResponsibleWorkingGroups;
	}

	@JsonIgnore
	public List<Contribution> getExistingContributions() {
		return existingContributions;
	}

	public void setExistingContributions(List<Contribution> existingContributions) {
		this.existingContributions = existingContributions;
	}

	@JsonIgnore
	public List<Resource> getExistingResources() {
		return existingResources;
	}

	public void setExistingResources(List<Resource> existingResources) {
		this.existingResources = existingResources;
	}

	// newly added
	public void setSimilarContributions(List<Long> similarContri) {
		this.similarContri = similarContri; 
	}

	public List<Long> getSimilarContributions() {
		return this.similarContri; 
	}

	/*
	 * Basic Data Operations
	 */
	public static Contribution create(User creator, String title, String text,
			ContributionTypes type) {
		Contribution c = new Contribution(creator, title, text, type);
		c.save();
		c.update();
		return c;
	}

	public static List<Contribution> findAll() {
		List<Contribution> contribs = find.all();
		return contribs;
	}

	public static void create(Contribution c) {
		
		// 1. Check first for existing entities in ManyToMany relationships.
		// Save them for later update
		//List<User> authors = c.getAuthors();
		List<Theme> themes = c.getThemes(); // new themes are never created from contributions
		c.setThemes(new ArrayList<>());
		List<ComponentInstanceMilestone> associatedMilestones = c.getAssociatedMilestones(); // new milestones are never created from contributions
		c.setAssociatedMilestones(new ArrayList<>());
		List<Hashtag> existingHashtags = c.getExistingHashtags();
		List<WorkingGroup> existingWorkingGroups = c.getExistingResponsibleWorkingGroups();
		List<Contribution> existingContributions = c.getExistingContributions();
		List<Resource> existingResources = c.getExistingResources();
		
		c.save();

		// 3. Add existing entities in relationships to the manytomany resources
		// then update
		ResourceSpace cResSpace = c.getResourceSpace();
		if (themes != null && !themes.isEmpty())
			cResSpace.getThemes().addAll(themes);
		if (associatedMilestones != null && !associatedMilestones.isEmpty())
			cResSpace.getMilestones().addAll(associatedMilestones);
		if (existingWorkingGroups != null && !existingWorkingGroups.isEmpty())
			cResSpace.getWorkingGroups().addAll(existingWorkingGroups);
		if (existingHashtags!= null && !existingHashtags.isEmpty())
			cResSpace.getHashtags().addAll(existingHashtags);
		if (existingContributions!= null && !existingContributions.isEmpty())
			cResSpace.getContributions().addAll(existingContributions);
		if (existingResources!= null && !existingResources.isEmpty())
			cResSpace.getResources().addAll(existingResources);
		
		cResSpace.update();
		
		c.refresh();
	}

	public static Contribution read(Long contributionId) {
		return find.ref(contributionId);
	}

	public static Contribution createObject(Contribution c) {
		c.save();
		return c;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void delete(Contribution c) {
		c.delete();
	}

	public static void softDelete(Long id) {
		Contribution c = find.ref(id);
		c.setRemoved(true);
		c.setRemoval(new Date());
		c.update();
	}
	
	public static void softDelete(Contribution c) {
		c.setRemoved(true);
		c.setRemoval(new Date());
		c.update();
	}

	public static void softRecovery(Long id) {
		Contribution c = find.ref(id);
		c.setRemoved(false);
		c.setRemoval(null);
		c.update();
	}
	
	public static void softRecovery(Contribution c) {
		c.setRemoved(false);
		c.setRemoval(null);
		c.update();
	}

	public static Contribution update(Contribution c) {
		c.update();
		c.refresh();
		return c;
	}

	/*
	 * Other Queries
	 */

	public static Contribution readByUUID(UUID contributionUUID) {
		return find.where().eq("uuid", contributionUUID).findUnique();
	}

	public static Integer readByTitle(String title) {
		ExpressionList<Contribution> contributions = find.where().eq("title",
				title);
		return contributions.findList().size();
	}

	public static List<Contribution> findAllByContainingSpace(Long sid) {
		List<Contribution> contribs = find.where()
				.eq("containingSpaces.resourceSpaceId", sid).findList();
		return contribs;
	}

	public static List<Contribution> findAllByContainingSpaceAndQuery(Long sid,
			String query) {
		List<Contribution> contribs = find.where()
				.eq("containingSpaces.resourceSpaceId", sid)
				.ilike("textIndex", "%" + query + "%").findList();
		return contribs;
	}

	public static List<Contribution> findAllByContainingSpaceAndUUID(UUID uuid) {
		List<Contribution> contribs = find.where()
				.eq("containingSpaces.uuid", uuid).findList();
		return contribs;
	}

	public static List<Contribution> readContributionsOfSpace(
			Long resourceSpaceId) {
		return find.where().eq("containingSpaces.resourceSpaceId", resourceSpaceId)
				.findList();
	}

	public static Contribution readByIdAndType(Long resourceSpaceId,
			Long contributionId, ContributionTypes type) {
		return find.where().eq("containingSpaces.resourceSpaceId", resourceSpaceId)
				.eq("contributionId", contributionId).eq("type", type)
				.findUnique();
	}

	public static List<Contribution> readListByContainingSpaceAndType(
			Long resourceSpaceId, ContributionTypes type) {
		return find.where().eq("containingSpaces.resourceSpaceId", resourceSpaceId)
				.eq("type", type).findList();
	}

	public static void deleteContributionByIdAndType(Long contributionId,
			ContributionTypes cType) {
		find.where().eq("contributionId", contributionId).eq("type", cType)
				.findUnique().delete();
	}

	public static List<Contribution> readByCreator(User u) {
		return find.where().eq("authors.userId", u.getUserId()).findList();
	}

	public static List<Contribution> findAllByContainingSpaceAndType(
			ResourceSpace rs, String t) {
		return find.where().eq("containingSpaces", rs).eq("type", t.toUpperCase())
				.findList();
	}

	public static List<Contribution> findAllByContainingSpaceAndTypeAndQuery(
			ResourceSpace rs, String t, String query) {
		return find.where().eq("containingSpaces", rs).eq("type", t.toUpperCase())
				.ilike("textIndex", "%" + query + "%").findList();
	}

	/* Single Contribution queries */
	
	public static Contribution readIssueOfSpace(Long resourceSpaceId,
			Long contributionId) {
		return readByIdAndType(resourceSpaceId, contributionId,
				ContributionTypes.ISSUE);
	}

	public static Contribution readIdeaOfSpace(Long resourceSpaceId,
			Long contributionId) {
		return readByIdAndType(resourceSpaceId, contributionId,
				ContributionTypes.IDEA);
	}

	public static Contribution readQuestionOfSpace(Long resourceSpaceId,
			Long contributionId) {
		return readByIdAndType(resourceSpaceId, contributionId,
				ContributionTypes.QUESTION);
	}

	public static Contribution readCommentOfSpace(Long resourceSpaceId,
			Long contributionId) {
		return readByIdAndType(resourceSpaceId, contributionId,
				ContributionTypes.COMMENT);
	}
	
	
	/* List Contribution queries */
	
	public static Contribution readForumPostOfSpace(Long resourceSpaceId,
			Long contributionId) {
		return readByIdAndType(resourceSpaceId, contributionId,
				ContributionTypes.FORUM_POST);
	}

	public static Contribution readProposalOfSpace(Long resourceSpaceId,
			Long contributionId) {
		return readByIdAndType(resourceSpaceId, contributionId,
				ContributionTypes.PROPOSAL);
	}

	public static List<Contribution> readIssuesOfSpace(Long resourceSpaceId) {
		return readListByContainingSpaceAndType(resourceSpaceId,
				ContributionTypes.ISSUE);
	}

	public static List<Contribution> readIdeasOfSpace(Long resourceSpaceId) {
		return readListByContainingSpaceAndType(resourceSpaceId,
				ContributionTypes.IDEA);
	}

	public static List<Contribution> readQuestionsOfSpace(Long resourceSpaceId) {
		return readListByContainingSpaceAndType(resourceSpaceId,
				ContributionTypes.QUESTION);
	}

	public static List<Contribution> readCommentsOfSpace(Long resourceSpaceId) {
		return readListByContainingSpaceAndType(resourceSpaceId,
				ContributionTypes.COMMENT);
	}

	@PrePersist
	private void onCreate() {
		// 3. Check if there is not a type
		if (this.type == null)
			this.type = ContributionTypes.COMMENT;
		AuditContribution ac = new AuditContribution(this);
		ac.setAuditEvent(AuditEventTypes.CREATION);
		ac.setAuditUserId(this.getContextUserId());
		ac.save();
	}
	
	@PreRemove
	private void onDelete() {
		AuditContribution ac = new AuditContribution(this);
		ac.setAuditEvent(AuditEventTypes.DELETE);
		ac.setAuditUserId(this.getContextUserId());
		ac.save();
	}

	
	@PreUpdate
	private void onUpdateContribution() {
		// 1. Update text index if needed
		String newTextIndex = this.title + "\n" + this.text;
		if (this.textIndex != null && !this.textIndex.equals(newTextIndex))
			this.textIndex = newTextIndex;

		// 4. Add auditing
		AuditContribution ac = new AuditContribution(this);
		ac.setAuditEvent(AuditEventTypes.UPDATE);
		// get user from context? 
		ac.setAuditUserId(this.getContextUserId());		
		ac.save();
	}

	@PostUpdate
	private void afterUpdate() {
		// 2. Update replies stats
		int numberComments = this.resourceSpace.getContributionsFilteredByType(ContributionTypes.COMMENT).size();
		if (numberComments != this.stats.getReplies())
			this.stats.setReplies(new Long(numberComments));
		this.stats.update();
	}
}
