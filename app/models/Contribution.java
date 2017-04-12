package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import models.location.Location;
import models.misc.Views;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import play.data.validation.Constraints.Required;
import utils.TextUtils;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import enums.ContributionStatus;
import enums.ContributionTypes;
import enums.ResourceSpaceTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
@Where(clause = "removed=false")
@ApiModel(value = "Contribution", description = "Generic model for citizen contributions. A contribuiton represents IDEAS, PROPOSALS, DISCUSSION, COMMENTS, NOTES, ISSUES, ETC. ")
public class Contribution extends AppCivistBaseModel {

    @Id
    @GeneratedValue
    @ApiModelProperty(value="Contribution numerical ID", position=0)
    private Long contributionId;

    @JsonView(Views.Public.class)
    @Index
    @ApiModelProperty(value="Contribution Universal ID (meant to be valid accross intances of the platform)", position=1)
    private UUID uuid = UUID.randomUUID();

    @Transient
    @ApiModelProperty(hidden=true, notes="String version of the UUID to facilitate processing in server side. To be removed.")
    private String uuidAsString;

    @JsonView(Views.Public.class)
    @Required
    @ApiModelProperty(value="Title of the contribution", position=2)
    private String title;

    @JsonView(Views.Public.class)
    @Required
    @ApiModelProperty(value="Text describing the contribution", position=3)
    @Column(name = "text", columnDefinition = "text")
    private String text;
    
    @JsonView(Views.Public.class)
    @ApiModelProperty(value="Text describing the contribution, in plain text format", position=3)
    @Column(name = "plain_text", columnDefinition = "plain_text")
    private String plainText;

    @JsonView(Views.Public.class)
    @Enumerated(EnumType.STRING)
    @Required
    @ApiModelProperty(value="Type of Contribution", position=4)
    private ContributionTypes type;

    @JsonView(Views.Public.class)
    @Enumerated(EnumType.STRING)
    @Required
    @ApiModelProperty(value="Status of the Contribution (e.g., new, in progress, published, etc.)", position=5)
    private ContributionStatus status;

    @JsonIgnore
    @Index
    @Column(name = "text_index", columnDefinition = "text")
    @ApiModelProperty(hidden=true)
    private String textIndex;

    @Column(name = "moderation_comment", columnDefinition = "text")
    @ApiModelProperty(value="Comment explaining why a contribution is moderated (e.g., deleted, changed status, etc.)", position=6)
    private String moderationComment;

    @OneToOne(cascade = CascadeType.ALL)
    @Index
    private Location location;

    @JsonView(Views.Public.class)
    @ManyToOne(cascade = CascadeType.ALL)
    @ApiModelProperty(value="Author associated to the contribution when it is not an AppCivist User", position=7)
    private NonMemberAuthor nonMemberAuthor;

    // TODO: Needed? 
    private String budget;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @Where(clause = "${ta}.active=true")
    @JsonIgnoreProperties({"providers", "roles", "permissions", "sessionKey", "identifier"})
    @JsonView(Views.Public.class) 
    @ApiModelProperty(name="authors", value="List of authors when is more then one but not a working group")
    private List<User> authors = new ArrayList<User>();

    @JsonView(Views.Public.class)
    private Integer popularity = 0;

    @JsonView(Views.Public.class)
    private Integer commentCount = 0;

    @JsonView(Views.Public.class)
    private Integer forumCommentCount = 0;    

    @JsonView(Views.Public.class)
    private Integer totalComments = 0;     
    
    @JsonView(Views.Public.class) 
    private Boolean pinned = false;

    @Transient
    @ApiModelProperty(value="Read only property displaying the first information", readOnly=true)
    private User firstAuthor;

    @Transient
    @ApiModelProperty(value="Read only property displaying the first author name", readOnly=true)
    private String firstAuthorName;
    
    @Transient
    @ApiModelProperty(value="Read only property displaying the Assembly where this Contribution was created", readOnly=true)
    private Long assemblyId;


   // @JsonIgnore
   @JsonView(Views.Public.class)
    @JsonManagedReference
    @Transient
    @ApiModelProperty(value="Working Groups to which this Contribution is associated")
    private List<WorkingGroup> workingGroupAuthors = new ArrayList<WorkingGroup>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "contributions", cascade = CascadeType.PERSIST)
    private List<ResourceSpace> containingSpaces;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    private ResourceSpace resourceSpace = new ResourceSpace(ResourceSpaceTypes.CONTRIBUTION);
    
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    @JsonInclude(Include.NON_EMPTY)
    @JsonView(Views.Public.class)
    private ResourceSpace forum = new ResourceSpace(ResourceSpaceTypes.CONTRIBUTION);

    @JsonView(Views.Public.class)
    @Transient
    private ContributionStatistics stats = new ContributionStatistics(this.contributionId);

    @OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="contribution")
    @JsonIgnore
    private List<ContributionFeedback> contributionFeedbacks;


    /*
     * Transient properties that take their values from the associated resource
     * space
     */
    @JsonView(Views.Public.class)
    @Transient
    private List<Theme> themes;
    @JsonView(Views.Public.class)
    @Transient
    private List<Resource> attachments;
    @Transient
    private List<Hashtag> hashtags = new ArrayList<Hashtag>();
    @Transient
    //@JsonView(Views.Public.class)
    @JsonIgnore
    private List<Contribution> comments = new ArrayList<Contribution>();
    @Transient
    private List<ComponentMilestone> associatedMilestones = new ArrayList<ComponentMilestone>();
    //@Transient
    //private List<Contribution> inspirations;
    
    @JsonIgnoreProperties({"contributionId", "uuidAsString", "textIndex", "moderationComment", "location",
            "budget", "firstAuthor", "assemblyId", "containingSpaces", "resourceSpace", "stats",
            "attachments", "hashtags", "comments", "associatedMilestones", "associatedContributions", "actionDueDate",
            "actionDone", "action", "assessmentSummary", "extendedTextPad", "sourceCode", "assessments", "existingHashtags",
            "existingResponsibleWorkingGroups", "existingContributions", "existingResources", "existingThemes", "addedThemes"
    })
    @JsonIgnore
    @Transient
    private List<Contribution> associatedContributions;

    /*
     * The following fields are specific to each type of contribution
     */

    /*
     * Fields specific to the type ACTION_ITEM
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    private Date actionDueDate;

    private Boolean actionDone = false;

    private String action;

    // Fields Fields specific to the type ASSESSMENT
    private String assessmentSummary;

    // Fields specific to the type PROPOSAL and ASSESSMENT
    @OneToOne(cascade = CascadeType.ALL)
    @JsonView(Views.Public.class)
    private Resource extendedTextPad;

    // Fields specific to the type PROPOSAL and ASSESSMENT
    @Transient
    @JsonIgnore
    private List<ContributionPublishHistory> publicRevisionHistory = new ArrayList<>();

    @Transient
    @JsonView(Views.Public.class)
    private Integer publicRevision;

    // Fields specific to the type PROPOSAL and ASSESSMENT
//    @OneToOne(cascade = CascadeType.ALL)
//    private Resource publicRevision;

    @Column(name = "source_code")
    private String sourceCode;

    // Fields specific to the type PROPOSAL
    @Transient
    private List<Contribution> assessments;

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

    @Transient
    private List<Theme> existingThemes;

    @Transient
    private List<Theme> addedThemes;
    
    @JsonView(Views.Public.class)
    private String document;

    /**
     * The find property is an static property that facilitates database query
     * creation
     */
    public static Finder<Long, Contribution> find = new Finder<>(
            Contribution.class);

    public static Finder<Long, ResourceSpace> containingSpacesFinder = new Finder<>(
            ResourceSpace.class);

    public static Finder<Long, WorkingGroup> workingGroupFinder = new Finder<>(
            WorkingGroup.class);

    @Transient
    private List<Long> campaignIds;
    
    @JsonView(Views.Public.class)
    @Transient
    private List<UUID> campaignUuids;

    /**
     * This field will help assign contributions to working groups.
     **/
    @Transient
    private List<WorkingGroup> workingGroups;

    public Contribution(User creator, String title, String text,
                        ContributionTypes type) {
        super();
        this.authors.add(creator);
        this.title = title;
        this.text = text;
        this.type = type;
    }

    public Contribution() {
        super();
    }

    /*
     * Getters and Setters
     */
    public List<ResourceSpace> getContainingSpaces() {
        return containingSpaces;
    }

    @Transient
    public User getFirstAuthor() {
        return authors != null && authors.size() > 0 ? authors.get(0) : null;
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

    public String getModerationComment() {
        return moderationComment;
    }

    public void setModerationComment(String moderationComment) {
        this.moderationComment = moderationComment;
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
    
    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String text) {
        this.plainText = text;
    }

    public ContributionTypes getType() {
        return type;
    }

    public void setType(ContributionTypes type) {
        this.type = type;
    }

    public ContributionStatus getStatus() {
        return status;
    }

    public void setStatus(ContributionStatus status) {
        this.status = status;
    }

    public List<User> getAuthors() {
        return authors;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }


    public List<ContributionFeedback> getContributionFeedbacks() {
        return contributionFeedbacks;
    }

    public void setContributionFeedbacks(List<ContributionFeedback> contributionFeedbacks) {
        this.contributionFeedbacks = contributionFeedbacks;
    }

    @Transient
    public void setFirstAuthor(User u) {
        u.setProviders(null);
        if (authors.size() > 0)
            authors.set(0, u);
        else {
            authors = new ArrayList<User>();
            authors.add(u);
        }
    }

    @Transient
    public String getFirstAuthorName() {
        User fa = getFirstAuthor();
        return fa != null ? fa.getName() : null;
    }

    @Transient
    public void setFirstAuthorName(String name) {
        this.firstAuthorName = name;
    }

    @Transient
    public Long getAssemblyId() {
        return assemblyId;
    }

    @Transient
    public void setAssemblyId(Long aid) {
        this.assemblyId = aid;
    }

    public void setAuthors(List<User> authors) {
        this.authors = authors;
    }

    public void addAuthor(User author) {
        this.authors.add(author);
    }

    /**
     * When we create a contribution, the "workingGroupAuthors" list contains
     * "existing working groups" that will take on the development of the
     * contribution When we read an existing contribution, the
     * "workingGroupAuthors" is queried from WorkingGroups finder that can match
     * the contribution id with the existing resources for that WorkingGroup
     */
    public List<WorkingGroup> getWorkingGroupAuthors() {
        if (this.workingGroupAuthors == null
                || this.workingGroupAuthors.size() == 0)
            this.workingGroupAuthors = listWorkingGroupAuthors(this.contributionId);
        return this.workingGroupAuthors;
    }

    public void setWorkingGroupAuthors(List<WorkingGroup> workingGroupAuthors) {
        this.workingGroupAuthors = workingGroupAuthors;
    }

    public void addWorkingGroupAuthor(WorkingGroup workingGroupAuthor) {
        this.workingGroupAuthors.add(workingGroupAuthor);
        workingGroupAuthor.addContribution(this);
        workingGroupAuthor.update();
        workingGroupAuthor.refresh();
    }

    public List<Theme> getThemes() {
        return resourceSpace != null ? resourceSpace.getThemes() : null;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
        if (resourceSpace != null ) this.resourceSpace.setThemes(themes);
    }

    public void addTheme(Theme t) {
        this.resourceSpace.addTheme(t);
    }

    public List<Resource> getAttachments() {
        return resourceSpace != null ? resourceSpace.getResources() : null;
    }

    public void setAttachments(List<Resource> attachments) {
        this.attachments = attachments;
        if (resourceSpace != null) this.resourceSpace.setResources(attachments);
    }

    public void addAttachment(Resource attach) {
        if(resourceSpace != null) this.resourceSpace.addResource(attach);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public NonMemberAuthor getNonMemberAuthor() {
        return nonMemberAuthor;
    }

    public void setNonMemberAuthor(NonMemberAuthor nonMemberAuthor) {
        this.nonMemberAuthor = nonMemberAuthor;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public List<Hashtag> getHashtags() {
        return resourceSpace != null ? resourceSpace.getHashtags() : null;
    }

    public void setHashtags(List<Hashtag> hashtags) {
        this.hashtags = hashtags;
        if (resourceSpace != null) this.resourceSpace.setHashtags(hashtags);
    }

    public void addHashtag(Hashtag h) {
        if (resourceSpace != null) this.resourceSpace.addHashtag(h);
    }

    public Long getResourceSpaceId() {
        return this.resourceSpace != null ? this.resourceSpace
                .getResourceSpaceId() : null;
    }

    @JsonView(Views.Public.class)
    public UUID getResourceSpaceUUID() {
        return this.resourceSpace != null ? this.resourceSpace
                .getResourceSpaceUuid() : null;
    }

    public void setResourceSpaceId(Long id) {
        if (this.resourceSpace != null
                && this.resourceSpace.getResourceSpaceId() == null)
            this.resourceSpace.setResourceSpaceId(id);
    }

    public ResourceSpace getForum() {
        return forum;
    }

    public void setForum(ResourceSpace forum) {
        this.forum = forum;
    }

    public Long getForumResourceSpaceId() {
        return this.forum != null ? this.forum
                .getResourceSpaceId() : null;
    }

    @JsonView(Views.Public.class)
    public UUID getForumResourceSpaceUUID() {
        return this.forum != null ? this.forum 
                .getResourceSpaceUuid() : null;
    }    
   
    public List<Contribution> getComments() {
        return resourceSpace != null ? resourceSpace.getContributionsFilteredByType(ContributionTypes.COMMENT) : null;
    }

    @JsonIgnore
    public List<Contribution> getTransientComments() {
        return this.comments;
    }

    public void addComment(Contribution c) {
        if (c.getType() == ContributionTypes.COMMENT)
            this.resourceSpace.addContribution(c);
    }

//    public List<Contribution> getInspirations() {
//        return this.inspirations;
//    }
//
//    @JsonIgnore
//    public List<Contribution> getTransientInspirations() {
//        return this.inspirations;
//    }

//    public void setInspirations(List<Contribution> inspirations) {
//        this.inspirations = inspirations;
//        this.getResourceSpace().getContributions().addAll(inspirations);
//    }

    public List<Contribution> getAssociatedContributions(){
        return resourceSpace != null ? resourceSpace.getContributions() : null;
    }

    public List<Contribution> getPagedAssociatedContributions(Integer page, Integer pageSize) {
        Finder<Long, Contribution> find = new Finder<>(Contribution.class);
        return find.where().eq("containingSpaces", this.resourceSpace).
                findPagedList(page, pageSize).getList();
    }

    public void setAssociatedContributions(List<Contribution> contributions){
        this.associatedContributions = contributions;
        if (resourceSpace != null) resourceSpace.getContributions().addAll(associatedContributions);
    }

//    public void addInspiration(Contribution c) {
//        if (c.getType() == ContributionTypes.BRAINSTORMING)
//            this.resourceSpace.addContribution(c);
//    }

    public String getReadOnlyPadUrl() {
        return extendedTextPad != null ? extendedTextPad.getUrlAsString()
                : null;
    }

    // TODO see if setting contributions on resource space is better through
    // updating the space directly
    public void setComments(List<Contribution> comments) {
        this.comments = comments;
        if (resourceSpace != null) this.resourceSpace.setContributionsFilteredByType(comments,
                ContributionTypes.COMMENT);
    }

    public void setAssessments(List<Contribution> assessments) {
        this.assessments = assessments;
        if (resourceSpace != null) this.resourceSpace.setContributionsFilteredByType(assessments,
                ContributionTypes.ASSESSMENT);
    }

    public List<ComponentMilestone> getAssociatedMilestones() {
        this.associatedMilestones = resourceSpace != null ? resourceSpace.getMilestones() : null;
        return this.associatedMilestones;
    }

    public void setAssociatedMilestones(
            List<ComponentMilestone> associatedMilestones) {
        this.associatedMilestones = associatedMilestones;
        this.resourceSpace.setMilestones(associatedMilestones);
    }

    @JsonIgnore
    public ResourceSpace getResourceSpace() {
        return resourceSpace;
    }

    public void setResourceSpace(ResourceSpace resourceSpace) {
        this.resourceSpace = resourceSpace;
    }

    public ContributionStatistics getStats() {
        this.stats = new ContributionStatistics(this.contributionId);
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
        this.assessments = resourceSpace != null ? this.resourceSpace
                .getContributionsFilteredByType(ContributionTypes.ASSESSMENT) : null;
        return this.assessments;
    }

    public void addAssessment(Contribution assessment) {
        // if(assessment.getType()==ContributionTypes.ASSESSMENT)
        // this.resourceSpace.addContribution(assessment);
    }

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

    public void setExistingContributions(
            List<Contribution> existingContributions) {
        this.existingContributions = existingContributions;
    }

    @JsonIgnore
    public List<Resource> getExistingResources() {
        return existingResources;
    }

    public void setExistingResources(List<Resource> existingResources) {
        this.existingResources = existingResources;
    }

    @JsonIgnore
    public List<Theme> getExistingThemes() {
        return existingThemes;
    }

    public void setExistingThemes(List<Theme> existingThemes) {
        this.existingThemes = existingThemes;
    }

    @JsonIgnore
    public List<Theme> getAddedThemes() {
        return addedThemes;
    }

    public void setAddedThemes(List<Theme> addedThemes) {
        this.addedThemes = addedThemes;
    }

    /*
     * Basic Data Operations
     */
    public static Contribution create(User creator, String title, String text,
                                      ContributionTypes type) {
        Contribution c = new Contribution(creator, title, text, type);
        c.save();
        c.update();
        ContributionHistory.createHistoricFromContribution(c);
        return c;
    }

    public static List<Contribution> findAll() {
        List<Contribution> contribs = find.all();
        return contribs;
    }

    public static void create(Contribution c) {

        // 1. Check first for existing entities in ManyToMany relationships.
        // Save them for later update
        // List<User> authors = c.getAuthors();
        List<Theme> themes = c.getThemes(); // new themes are never created from
        // contributions
        c.setThemes(new ArrayList<>());
        List<ComponentMilestone> associatedMilestones = c
                .getAssociatedMilestones(); // new milestones are never created
        // from contributions
        c.setAssociatedMilestones(new ArrayList<>());

        List<Hashtag> existingHashtags = c.getExistingHashtags();
        List<WorkingGroup> existingWorkingGroups = c
                .getExistingResponsibleWorkingGroups();
        List<Contribution> existingContributions = c.getExistingContributions();
        List<Resource> existingResources = c.getExistingResources();
        List<Theme> existingThemes = c.getExistingThemes();

        // 2. Check if there are working group associated and save the groups in
        // a list to add the contribution to them later
        List<WorkingGroup> workingGroupAuthors = c.getWorkingGroupAuthors();
        
        // Set plain text if text is HTML
        if (TextUtils.isHtml(c.getText())) {
            c.setText(Jsoup.clean(c.getText(), Whitelist.basic()));
            c.setPlainText(Jsoup.parse(c.getText()).text());
        }
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
        if (existingHashtags != null && !existingHashtags.isEmpty())
            cResSpace.getHashtags().addAll(existingHashtags);
        if (existingContributions != null && !existingContributions.isEmpty())
            cResSpace.getContributions().addAll(existingContributions);
        if (existingResources != null && !existingResources.isEmpty())
            cResSpace.getResources().addAll(existingResources);
        if (existingThemes != null && !existingThemes.isEmpty())
            cResSpace.getThemes().addAll(existingThemes);

        cResSpace.update();

        // 5. Add contribution to working group authors
        for (WorkingGroup workingGroup : workingGroupAuthors) {
            workingGroup.addContribution(c);
            workingGroup.update();
            workingGroup.refresh();
        }

        c.refresh();
        ContributionHistory.createHistoricFromContribution(c);
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
        ContributionHistory.createHistoricFromContribution(c);
    }

    public static void softDelete(Contribution c) {
        c.setRemoved(true);
        c.setRemoval(new Date());
        c.update();
        ContributionHistory.createHistoricFromContribution(c);
    }

    public static void softRecovery(Long id) {
        Contribution c = find.ref(id);
        c.setRemoved(false);
        c.setRemoval(null);
        c.update();
        ContributionHistory.createHistoricFromContribution(c);
    }

    public static void softRecovery(Contribution c) {
        c.setRemoved(false);
        c.setRemoval(null);
        c.update();
        ContributionHistory.createHistoricFromContribution(c);
    }

    public static Contribution update(Contribution c) {
        c.update();
        c.refresh();
        ContributionHistory.createHistoricFromContribution(c);
        return c;
    }

    public static Contribution readAndUpdate(Contribution newContribution,
            Long contributionId, Long authorId) throws IllegalArgumentException, IllegalAccessException {

        // Set plain text if text is HTML
        if (TextUtils.isHtml(newContribution.getText())) {
            newContribution.setText(Jsoup.clean(newContribution.getText(),
                    Whitelist.basicWithImages()));
            newContribution.setPlainText(Jsoup.parse(newContribution.getText())
                    .text());
        }
        Contribution existingContribution = Contribution.read(contributionId);
        
// TODO: find a way for reflections to work with Ebean updates in Play
// See        http://stackoverflow.com/questions/38655024/playframework-2-5-ebean-update-using-reflection
// Option: use java.beans.Statement http://stackoverflow.com/questions/10009052/invoking-setter-method-using-java-reflection/10009255#10009255
// In order for Ebean to know that the entity is updated, we have to use the setters/getters in play
//        for (Field field : existingContribution.getClass().getDeclaredFields()) {
//            field.setAccessible(true);
//            if (field.getName().toLowerCase().contains("ebean")
//                    || field.isAnnotationPresent(ManyToMany.class)
//                    || field.isAnnotationPresent(ManyToOne.class)
//                    || field.isAnnotationPresent(OneToMany.class)
//                    || field.isAnnotationPresent(OneToOne.class)) {
//                continue;
//            }
//            field.set(existingContribution, field.get(newContribution));
//        }
        
        existingContribution.setAction(newContribution.getAction());
        existingContribution.setActionDone(newContribution.getActionDone());
        existingContribution.setActionDueDate(newContribution.getActionDueDate());
        existingContribution.setAssessmentSummary(newContribution.getAssessmentSummary());
        existingContribution.setBudget(newContribution.getBudget());
        existingContribution.setLang(newContribution.getLang());
        existingContribution.setLastUpdate(new Date());
        existingContribution.setLocation(newContribution.getLocation());
        existingContribution.setModerationComment(newContribution.getModerationComment());
        existingContribution.setPinned(newContribution.getPinned());
        existingContribution.setSourceCode(newContribution.getSourceCode());
        existingContribution.setStatus(newContribution.getStatus());
        existingContribution.setText(newContribution.getText());
        existingContribution.setTitle(newContribution.getTitle());
        existingContribution.setType(newContribution.getType());
        existingContribution.setContextUserId(authorId);
        existingContribution.setAttachments(newContribution.getAttachments());

        return Contribution.update(existingContribution);
    }

    private void setContainingSpaces(List<ResourceSpace> containingSpaces2) {
        this.containingSpaces = containingSpaces2;
    }

    public List<WorkingGroup> getWorkingGroups() {
        return workingGroups;
    }

    public void setWorkingGroups(List<WorkingGroup> workingGroups) {
        this.workingGroups = workingGroups;
    }

    /*
     * Other Queries
     */
    public static Contribution findByResourceSpaceId(Long sid) {
        Contribution c = find.where().eq("resourceSpace.resourceSpaceId", sid).findUnique();
        return c;
    }

    public static Contribution findByForumResourceSpaceId(Long sid) {
        Contribution c = find.where().eq("forum.resourceSpaceId", sid).findUnique();
        return c;
    }
    

    public static Contribution readByUUID(UUID contributionUUID) {
        return find.where().eq("uuid", contributionUUID).eq("removed", false).findUnique();
    }

    public static Integer readByTitle(String title) {
        ExpressionList<Contribution> contributions = find.where().eq("title",
                title);
        return contributions.findList().size();
    }

    // TODO change get(0)
    public static Contribution readBySourceCode(String sourceCode) {
        ExpressionList<Contribution> contributions = find.where().eq("sourceCode",
                sourceCode);
        return contributions.findList() != null && !contributions.findList().isEmpty() ? contributions.findList().get(0) : null;
    }

    public static List<Contribution> findAllByContainingSpace(Long sid) {
        List<Contribution> contribs = find.where()
                .eq("containingSpaces.resourceSpaceId", sid).findList();
        return contribs;
    }

    public static List<Contribution> findPagedByContainingSpace(Long sid, Integer page, Integer pageSize) {
        List<Contribution> contribs = find.where()
                .eq("containingSpaces.resourceSpaceId", sid).findPagedList(page, pageSize).getList();
        return contribs;
    }

    public static List<Contribution> findAllByContainingSpaceAndType(
            ResourceSpace rs, Integer t) {
        return find.where().eq("containingSpaces", rs).eq("type", t).findList();
    }
             
    public static List<Contribution> findAllByContainingSpaceAndQuery(Long sid,
                                                                      String query) {
        List<Contribution> contribs = find.where()
                .eq("containingSpaces.resourceSpaceId", sid)
                .ilike("textIndex", "%" + query + "%").findList();
        return contribs;
    }

    public static List<Contribution> findPagedByContainingSpaceAndQuery(Long sid,
                                                                      String query, Integer page, Integer pageSize) {
        List<Contribution> contribs = find.where()
                .eq("containingSpaces.resourceSpaceId", sid)
                .ilike("textIndex", "%" + query + "%").findPagedList(page, pageSize).getList();
        return contribs;
    }

    public static List<Contribution> findAllByContainingSpaceAndUUID(UUID uuid) {
        List<Contribution> contribs = find.where()
                .eq("containingSpaces.uuid", uuid).findList();
        return contribs;
    }

    public static List<Contribution> readContributionsOfSpace(
            Long resourceSpaceId) {
        return find.where()
                .eq("containingSpaces.resourceSpaceId", resourceSpaceId)
                .findList();
    }
 
    public static Contribution readByIdAndType(Long resourceSpaceId,
                                               Long contributionId, ContributionTypes type) {
        return find.where()
                .eq("containingSpaces.resourceSpaceId", resourceSpaceId)
                .eq("contributionId", contributionId).eq("type", type)
                .findUnique();
    }

    public static List<Contribution> readListByContainingSpaceAndType(
            Long resourceSpaceId, ContributionTypes type) {
        return find.where()
                .eq("containingSpaces.resourceSpaceId", resourceSpaceId)
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
        ContributionTypes type = ContributionTypes.valueOf(t.toUpperCase());
        return find.where().eq("containingSpaces", rs)
                .eq("type", type).findList();
    }

    public static List<Contribution> findPagedByContainingSpaceAndType(
            ResourceSpace rs, String t, Integer page, Integer pageSize) {
        ContributionTypes type = ContributionTypes.valueOf(t.toUpperCase());
        return find.where().eq("containingSpaces", rs)
                .eq("type", type).findPagedList(page, pageSize).getList();
    }

    public static List<Contribution> findAllByContainingSpaceAndTypeAndQuery(
            ResourceSpace rs, String t, String query) {
        ContributionTypes type = ContributionTypes.valueOf(t.toUpperCase());
        return find.where().eq("containingSpaces", rs)
                .eq("type", type)
                .ilike("textIndex", "%" + query + "%").findList();
    }

    public static List<Contribution> findPagedByContainingSpaceAndTypeAndQuery(
            ResourceSpace rs, String t, String query, Integer page, Integer pageSize) {
        ContributionTypes type = ContributionTypes.valueOf(t.toUpperCase());
        return find.where().eq("containingSpaces", rs)
                .eq("type", type)
                .ilike("textIndex", "%" + query + "%").findPagedList(page, pageSize).getList();
    }

    public static List<Contribution> findAllByContainingSpaceIdAndType(
            ResourceSpace rs, String t) {
        return find.where().eq("containingSpaces.resourceSpaceId", rs)
                .eq("type", t.toUpperCase()).findList();
    }

    public static List<Contribution> findAllByContainingSpaceIdAndTypeAndQuery(
            ResourceSpace rs, String t, String query) {
        return find.where().eq("containingSpaces.resourceSpaceId", rs)
                .eq("type", t.toUpperCase())
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


    @PreUpdate
    private void onUpdateContribution() {
        // 1. Update text index if needed
        String newTextIndex = this.title + "\n" + this.text;
        if (this.textIndex != null && !this.textIndex.equals(newTextIndex))
            this.textIndex = newTextIndex;
    }

    public static boolean isUserAuthor(User u, Long contributionId) {
        return find.where().eq("contributionId", contributionId)
                .eq("authors.userId", u.getUserId()).findUnique() != null
                || find.where()
                .eq("contributionId", contributionId)
                .eq("workingGroupAuthors.members.user.userId",
                        u.getUserId()).findUnique() != null;
    }

    public static List<WorkingGroup> listWorkingGroupAuthors(Long contributionId) {
        List<WorkingGroup> wgs = workingGroupFinder.where()
                .eq("resources.contributions.contributionId", contributionId)
                .findList();
        return wgs;
    }

    public List<Long> getCampaignIds() {
        campaignIds = new ArrayList<>();
        List<ResourceSpace> spaces = this.containingSpaces.stream()
                .filter(p -> p.getType() == ResourceSpaceTypes.CAMPAIGN)
                .collect(Collectors.toList());

        for (ResourceSpace resourceSpace : spaces) {
            Campaign a = resourceSpace.getCampaign();
            if (a != null) {
                campaignIds.add(a.getCampaignId());
            }
        }
        return campaignIds;
    }

    public void setCampaignIds(List<Long> cids) {
        this.campaignIds = cids;
        for (Long long1 : cids) {
            ResourceSpace rs = ResourceSpace.read(long1);
            this.containingSpaces.add(rs);
        }
    }

    public List<UUID> getCampaignUuids() {
        campaignUuids = new ArrayList<>();
        List<ResourceSpace> spaces = this.containingSpaces.stream()
                .filter(p -> p.getType() == ResourceSpaceTypes.CAMPAIGN)
                .collect(Collectors.toList());

        for (ResourceSpace resourceSpace : spaces) {
            Campaign a = resourceSpace.getCampaign();
            if (a != null) {
                campaignUuids.add(a.getUuid());
            }
        }
        return campaignUuids;
    }

    public List<ContributionPublishHistory> getPublicRevisionHistory() {
        this.publicRevisionHistory = ContributionPublishHistory.getContributionsPublishHistory(this);
        return this.publicRevisionHistory;
    }

//    public void setPublicRevisionHistory(List<ContributionPublishHistory> publicRevisionHistory) {
//        this.publicRevisionHistory = publicRevisionHistory;
//    }

    /**
     * Adds revision to history
     * @param revision
     */
    public void addRevisionToContributionPublishHistory(Integer revision){
        ContributionPublishHistory contributionPublishHistory = new ContributionPublishHistory();
        contributionPublishHistory.setContributionId(this.getContributionId());
        contributionPublishHistory.setResourceId(this.extendedTextPad.getResourceId());
        contributionPublishHistory.setRevision(revision);
        contributionPublishHistory.save();
    }

    public Integer getPublicRevision(){
        List<ContributionPublishHistory> publishHistories = this.getPublicRevisionHistory();
        if(publishHistories != null && !publishHistories.isEmpty()){
            this.publicRevision = publishHistories.get(publishHistories.size() - 1).getRevision();
        }
        return this.publicRevision;
    }

    public Integer getPopularity() {
        return popularity != null ? popularity : 0;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    public Integer getCommentCount() {
        return commentCount != null ? commentCount : 0;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getForumCommentCount() {
        return forumCommentCount != null ? forumCommentCount : 0;
    }

    public void setForumCommentCount(Integer forumCommentCount) {
        this.forumCommentCount = forumCommentCount;
    }

    public Integer getTotalComments() {
        return totalComments != null ? totalComments : 0;
    }

    public void setTotalComments(Integer totalComments) {
        this.totalComments = totalComments;
    }    
    
    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public static List<Contribution> findPinnedInSpace(Long sid, ContributionTypes type) {
        if (type!=null) {
            return find.where()
                .eq("pinned", true)
                .eq("type", type)
                .eq("containingSpaces.resourceSpaceId", sid)
                .not(Expr.eq("removed",true))
                .findList();
        } else {
            return find.where()
                    .eq("pinned", true)
                    .eq("containingSpaces.resourceSpaceId", sid)
                    .not(Expr.eq("removed",true))
                    .findList();            
        }
    }

    public static List<Contribution> findPinnedInSpace(Long sid,
            ContributionTypes type, ContributionStatus status) {
        if (type!=null) {
            return find.where()
                    .eq("pinned", true)
                    .eq("type", type)
                    .eq("containingSpaces.resourceSpaceId", sid)
                    .eq("status", status)
                    .not(Expr.eq("removed",true))
                    .findList();    
        } else {
            return find.where()
                    .eq("pinned", true)
                    .eq("containingSpaces.resourceSpaceId", sid)
                    .eq("status", status)
                    .not(Expr.eq("removed",true))
                    .findList();            
        }
        
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public static List<Contribution> findContributionsInSpaceByTypeStatus(Long sid,
                                                       ContributionTypes type, ContributionStatus status) {
            return find.where()
                    .eq("type", type)
                    .eq("containingSpaces.resourceSpaceId", sid)
                    .eq("status", status)
                    .not(Expr.eq("removed",true))
                    .findList();
    }
}