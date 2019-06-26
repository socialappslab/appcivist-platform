package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.Contributions;
import delegates.NotificationsDelegate;
import delegates.WorkingGroupsDelegate;
import enums.*;
import exceptions.MembershipCreationException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.location.Location;
import models.misc.Views;
import org.geojson.FeatureCollection;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.libs.F;
import utils.LocationUtilities;
import utils.security.HashGenerationException;
import utils.services.PeerDocWrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@JsonInclude(Include.NON_EMPTY)
@Where(clause = "removed=false")
@ApiModel(value = "Contribution", description = "Generic model for citizen contributions. A contribuiton represents IDEAS, PROPOSALS, DISCUSSION, COMMENTS, NOTES, ISSUES, ETC. ")
public class Contribution extends AppCivistBaseModel {

    @Id
    @GeneratedValue
    @ApiModelProperty(value="Contribution numerical ID", position=0)
    @JsonView({Views.Report.class})
    private Long contributionId;

    @JsonView({Views.Public.class, Views.Report.class})
    @Index
    @ApiModelProperty(value="Contribution Universal ID (meant to be valid accross intances of the platform)", position=1)
    private UUID uuid = UUID.randomUUID();

    @Transient
    @ApiModelProperty(hidden=true, notes="String version of the UUID to facilitate processing in server side. To be removed.")
    private String uuidAsString;

    @JsonView({Views.Public.class, Views.Report.class})
    @Required
    @ApiModelProperty(value="Title of the contribution", position=2)
    private String title;

    @JsonView({Views.Public.class, Views.Report.class})
    @ApiModelProperty(value="Text describing the contribution", position=3)
    @Column(name = "text", columnDefinition = "text")
    private String text;

    @JsonView({Views.Public.class, Views.Report.class})
    @ApiModelProperty(value="Text describing the contribution, in plain text format", position=3)
    @Column(name = "plain_text", columnDefinition = "plain_text")
    private String plainText;

    @JsonView({Views.Public.class, Views.Report.class})
    @Enumerated(EnumType.STRING)
    @Required
    @ApiModelProperty(value="Type of Contribution", position=4)
    private ContributionTypes type;

    @JsonView({Views.Public.class, Views.Report.class})
    @Enumerated(EnumType.STRING)
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

    @JsonView({Views.Public.class, Views.Report.class})
    @Column(name = "source")
    private String source;

    @JsonView({Views.Public.class, Views.Report.class})
    @Column(name = "source_url", columnDefinition = "text")
    private String sourceUrl;

    @OneToOne(cascade = CascadeType.ALL)
    @Index
    private Location location;

    @JsonView(Views.Public.class)
    @ManyToOne(cascade = CascadeType.ALL)
    @ApiModelProperty(value="Author associated to the contribution when it is not an AppCivist User", position=7)
    private NonMemberAuthor nonMemberAuthor;

    @JsonView(Views.Public.class)
    @ManyToOne(cascade = CascadeType.ALL)
    private User creator;

    @JsonView({Views.Public.class, Views.Report.class})
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "contribution_non_member_author",
            joinColumns = { @JoinColumn(name = "contribution_id", referencedColumnName = "contribution_id", updatable = true, insertable = true) },
            inverseJoinColumns = { @JoinColumn(name = "non_member_author_id", referencedColumnName = "id", updatable = true, insertable = true) }
    )
    private List<NonMemberAuthor> nonMemberAuthors = new ArrayList<NonMemberAuthor>();
    // TODO: Needed? 
    private String budget;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @Where(clause = "${ta}.active=true")
    @JsonIgnoreProperties({"providers", "roles", "permissions", "sessionKey", "identifier"})
    @JsonView({Views.Public.class, Views.Report.class})
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

    @JsonView(Views.Public.class)
    private Integer extendedTextPadResourceNumber;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Contribution> children;

    @JoinColumn(name = "parent_id", referencedColumnName = "contribution_id")
    @JsonInclude(Include.NON_EMPTY)
    @JsonView(Views.Public.class)
    @ManyToOne(fetch = FetchType.LAZY)
    private Contribution parent;

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
    @JsonView({Views.Public.class, Views.Report.class})
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

    @JsonView({Views.Public.class, Views.Report.class})
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

    @Transient
    private List<Long> assignToContributions;

    @Transient
    private String errorsInExtendedTextPad;
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
    @JsonView({Views.Public.class, Views.Report.class})
    private Resource extendedTextPad;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonView(Views.Public.class)
    private Resource cover;

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

    @JsonView({Views.Public.class, Views.Report.class})
    @Transient
    private List<Theme> officialThemes;

    @JsonView({Views.Public.class, Views.Report.class})
    @Transient
    private List<Theme> emergentThemes;
    
    @JsonView(Views.Public.class)
    @Transient
    private String document;

    @JsonIgnore
    @Transient
    private String documentSimple;

    @Transient
    @JsonView(Views.Public.class)
    List<CustomFieldValue> customFieldValues;

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

    @Transient
    private List<Long> containingContributionsIds;
    
    @JsonView(Views.Public.class)
    @Transient
    private List<UUID> campaignUuids;

    /**
     * This field will help assign contributions to working groups.
     **/
    @Transient
    private List<WorkingGroup> workingGroups;

    @JsonView(Views.Public.class)
    @Transient
    private Integer forks;

    @JsonView(Views.Public.class)
    @Transient
    private Integer acceptedForks;

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
        if (authors!=null && authors.size() > 0){
            return authors.get(0);
        }
        // If the author is a Working Group, the coordinator of that group will be sent as
        // firstAuthor (if exists)
        List<WorkingGroup> groups = this.getWorkingGroupAuthors();
        if (groups.size() > 0){
            List<MembershipGroup> members = groups.get(0).getMembers();
            if (members.size() > 0){
                for (MembershipGroup member : members){
                    User user_member = member.getUser();
                    List<SecurityRole> roles = user_member.getRoles();
                    for (SecurityRole role : roles){
                        if(role.getName().equals(MyRoles.COORDINATOR.getName())){
                            return user_member;
                        }
                    }
                }
            }
        }
        return null;
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

    public List<NonMemberAuthor> getNonMemberAuthors() {
        return nonMemberAuthors;
    }

    public void setNonMemberAuthors(List<NonMemberAuthor> nonMemberAuthors) {
        this.nonMemberAuthors = nonMemberAuthors;
    }

    public List<ContributionFeedback> getContributionFeedbacks() {
        return contributionFeedbacks;
    }

    public void setContributionFeedbacks(List<ContributionFeedback> contributionFeedbacks) {
        this.contributionFeedbacks = contributionFeedbacks;
    }

    public static List<Contribution> getByNoMemberAuthorMail(String email) {
        return new ArrayList<>(find.where().eq("nonMemberAuthors.email", email).findSet());
    }

    public static Contribution getByPeerDocId(String peerdocId) {

        List<Contribution> contributions = find.where().contains("extendedTextPad.url", peerdocId).findList();
        if(!contributions.isEmpty()) {
            return contributions.get(0);
        } else {
            return null;
        }

    }

    public static List<Contribution> getAllWithPeerDoc() {

        return find.where().contains("extendedTextPad.resourceType", ResourceTypes.PEERDOC.name()).findList();


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
        if ((this.workingGroupAuthors == null
                || this.workingGroupAuthors.size() == 0) && this.contributionId != null)
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

    public List<Theme> getOfficialThemes() {
    	officialThemes = officialThemes !=null ? officialThemes : new ArrayList<Theme>();        
    	if(contributionId!=null){
            List<Theme> allThemes = resourceSpace != null ? resourceSpace.getThemes() : new ArrayList<Theme>();
            for (Theme theme: allThemes) {
                if(theme.getType() !=null && theme.getType().equals(ThemeTypes.OFFICIAL_PRE_DEFINED)){
                    officialThemes.add(theme);
                }
            }
            return officialThemes;
        }
        return officialThemes;
    }

    public void setOfficialThemes(List<Theme> officialThemes) {
        this.officialThemes = officialThemes;
    }

    public List<Theme> getEmergentThemes() {
    	emergentThemes = emergentThemes !=null ? emergentThemes : new ArrayList<Theme>();        
    	if(contributionId!=null){
            List<Theme> allThemes = resourceSpace != null ? resourceSpace.getThemes() : new ArrayList<Theme>();
            for (Theme theme: allThemes) {
                if(theme.getType() !=null && theme.getType().equals(ThemeTypes.EMERGENT)){
                	emergentThemes.add(theme);
                }
            }
            return emergentThemes;
        }
        return emergentThemes;
    }

    public void setEmergentThemes(List<Theme> emergentThemes) {
        this.emergentThemes = emergentThemes;
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

    public List<Long> getAssignToContributions() {
        return assignToContributions;
    }

    public void setAssignToContributions(List<Long> assignToContributions) {
        this.assignToContributions = assignToContributions;
    }

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

    // Returns the cover or the first image in the list of attachments
    public Resource getCover() {
        if (cover!=null) {
            return cover;
        } else {
            List<Resource> pics = this.getAttachments().stream()
                    .filter(p -> p.getResourceType() == ResourceTypes.PICTURE)
                    .collect(Collectors.toList());
            if (pics!=null && pics.size()>0) {
                return pics.get(0);
            } else {
                return null;
            }
        }
    }

    public void setCover(Resource cover) {
        this.cover = cover;
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

    /**
     * Create in ResourceSpace
     * @param c
     * @param rs
     */
    public static void create(Contribution c, ResourceSpace rs) {

        // 1. Check first for existing entities in ManyToMany relationships.
        // Save them for later update
        // List<User> authors = c.getAuthors();
        List<Theme> themes = c.getThemes();
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
        c.setText(Jsoup.clean(c.getText(), Whitelist.basic()));
        c.setPlainText(Jsoup.parse(c.getText()).text());

        // Make sure there is an status
        if (c.getStatus()==null) {
        	c.setStatus(ContributionStatus.PUBLISHED);
        }

        //geojson logic
        geoJsonLogic(c, rs);

        c.save();
        c.refresh();
        // 3. Add existing entities in relationships to the manytomany resources
        // then update
        ResourceSpace cResSpace = c.getResourceSpace();
        if (themes != null && !themes.isEmpty()) {
            cResSpace.getThemes().addAll(themes);
        }
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
        Set<Theme> themesA = new HashSet<>(cResSpace.getThemes());
        cResSpace.getThemes().clear();
        cResSpace.getThemes().addAll(themesA);
        cResSpace.update();

        // 5. Add contribution to working group authors
        WorkingGroupsDelegate.addContributionToWorkingGroups(c, workingGroupAuthors, true);
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

    public static Contribution unpublishContribution(Contribution c) {
        update(c);

        List<User> authors = c.getAuthors();
        List<Subscription> subscriptions = Subscription.findSubscriptionBySpaceId(c.getResourceSpace().getUuidAsString());
        // if the subscription is for a non author user, we delete it when the contribution is unpublished
        for(Subscription subscription: subscriptions) {
            boolean delete = true;
            String userName = "";
            for(User user: authors) {
                if (user.getUuidAsString().equals(subscription.getUserId())) {
                    delete = false;
                    userName = user.getUsername();
                }
            }
            if(delete) {
                Logger.info("Deleting suscription for user " + userName);
                subscription.delete();
            }
        }
        F.Promise.promise(() -> {
            Logger.debug("Sending notification for published contribution");
            NotificationsDelegate.publishedContributionInResourceSpace(c.getResourceSpace(),
                    c);
            return Optional.ofNullable(null);
        });

        return c;
    }

    public static Contribution publishContribution(Contribution c) {

        update(c);

        // We create subscription to the contribution for all the wg members, but ignoring the comment and
        // amendments events.

        HashMap<String, Boolean> ignoredEvents = new HashMap<>();

        ignoredEvents.put(NotificationEventName.NEW_CONTRIBUTION_COMMENT.name(), true);
        ignoredEvents.put(NotificationEventName.NEW_CONTRIBUTION_FORK.name(), true);
        ignoredEvents.put(NotificationEventName.NEW_CONTRIBUTION_MERGE.name(), true);


        for (WorkingGroup wg: c.getWorkingGroupAuthors()) {
            for(MembershipGroup mg: wg.getMembers()) {
                Logger.info("Creating subscriptions to wg members");
                Subscription.createRegularSubscription(mg.getUser(), c.getResourceSpace(), ignoredEvents);
            }
        }

        // We create subscription to the contribution for all the authors
        for(User author: c.getAuthors()) {
            Logger.info("Creating subscriptions to author " + author.getUsername());
            Subscription.createRegularSubscription(author, c.getResourceSpace(), null);
        }

        // and then we notified all that the contribution was updated
        F.Promise.promise(() -> {
            Logger.debug("Sending notification for published contribution");
            NotificationsDelegate.publishedContributionInResourceSpace(c.getResourceSpace(),
                    c);
            return Optional.ofNullable(null);
        });

        return c;
    }

    public static Contribution update(Contribution c) {
        c.update();
        c.refresh();
        ContributionHistory.createHistoricFromContribution(c);
        return c;
    }

    public static Contribution readAndUpdate(Contribution newContribution,
            Long contributionId, Long authorId) throws IllegalArgumentException, IllegalAccessException {

        if (newContribution.getText() != null) {
            newContribution.setText(Jsoup.clean(newContribution.getText(),
                    Whitelist.basicWithImages()));
            newContribution.setPlainText(Jsoup.parse(newContribution.getText())
                    .text());
        }

        Contribution existingContribution = Contribution.find.byId(contributionId);
        
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
        existingContribution.setPlainText(newContribution.getPlainText());
        existingContribution.setTitle(newContribution.getTitle());
        existingContribution.setType(newContribution.getType());
        existingContribution.setContextUserId(authorId);
        existingContribution.setCover(newContribution.getCover());

        return Contribution.update(existingContribution);
    }

    public void setContainingSpaces(List<ResourceSpace> containingSpaces2) {
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

    public static List<Contribution> findAllByContainingSpaceOrTypes(
            ResourceSpace rs, ContributionTypes t, ContributionTypes t2) {
        return find.where().eq("containingSpaces", rs).or(Expr.eq("type", t),Expr.eq("type", t2)).findList();
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

    public static Contribution findBySourceCodeAndSource(String source,
                                               String sourceCode) {
        List<Contribution> contributions =  find.where()
                .eq("source", source)
                .eq("sourceCode", sourceCode)
                .eq("removed", false)
                .findList();
        if(contributions.isEmpty()) {
            return null;
        } else {
            return contributions.get(0);
        }
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
        Contribution contribution = read(contributionId);
        Boolean isAuthor = false;

        User creator = contribution.getCreator();
        // if user is creator return true by default, no need to look into the list of authors
        if (u.getUserId() != null && creator != null && u.getUserId().equals(creator.getUserId())) {
            return true;
        }

        isAuthor = find.where().eq("contributionId", contributionId)
                .eq("authors.userId", u.getUserId()).findUnique() != null;
        if(!isAuthor) {
            List<WorkingGroup> wgs = contribution.getWorkingGroupAuthors();
            for (WorkingGroup wg:wgs
                 ) {
                List<MembershipGroup> members= wg.getMembers();
                for (MembershipGroup member:members
                     ) {
                    if(member.getUser().getUserId().equals(u.getUserId())){
                        isAuthor =true;
                    }
                }
            }
        }
        return isAuthor;
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

    public List<Long> getContainingContributionsIds() {
        containingContributionsIds = new ArrayList<>();
        for (ResourceSpace resourceSpace : this.containingSpaces) {
            if(resourceSpace.getType() == ResourceSpaceTypes.CONTRIBUTION){
                Contribution c = Contribution.findByResourceSpaceId(resourceSpace.getResourceSpaceId());
                if (c!=null)
                	containingContributionsIds.add(c.getContributionId());
            }
        }
        return containingContributionsIds;
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
        return 0;
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

    public Integer getExtendedTextPadResourceNumber() {
        return this.extendedTextPadResourceNumber;
    }

    public void setExtendedTextPadResourceNumber(Integer number) {
        this.extendedTextPadResourceNumber = number;
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

    public String getDocumentSimple() {
        return documentSimple;
    }

    public void setDocumentSimple(String documentSimple) {
        this.documentSimple = documentSimple;
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

    public static List<Contribution> findLatestContributionIdeas(ResourceSpace rs, Integer days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, - days);
        return find.where()
                .eq("containingSpaces.resourceSpaceId", rs.getResourceSpaceId())
                .eq("type", ContributionTypes.IDEA)
                .ge("creation", calendar.getTime())
                .findList();
    }

    public String getSource(){
        return source;
    }

    public void setSource(String source){
        this.source = source;
    }

    public String getSourceUrl(){
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl){
        this.sourceUrl = sourceUrl;
    }

    public List<CustomFieldValue> getCustomFieldValues() {
        return this.resourceSpace.getCustomFieldValues();
    }

    public void setCustomFieldValues(List<CustomFieldValue> customFieldValues) {
        this.customFieldValues = customFieldValues;
    }

    /**
     * Given a user, removes it from all contribution where it is a non member
     * set as author and update peerdoc and send mails
     * @param user
     */
    public static void updateContributionAuthors(User user) {
        List<Contribution> contributions = Contribution.getByNoMemberAuthorMail(user.getEmail());
        List<Long> contributionsId = new ArrayList<>();
        Logger.debug(" " + contributions.size() + " found where the author is no member");
        NonMemberAuthor toDelete;
        for(Contribution contribution: contributions) {
            contributionsId.add(contribution.getContributionId());
            toDelete = null;
            boolean isAuthor = false;
            for(NonMemberAuthor nonMemberAuthor: contribution.getNonMemberAuthors()) {
                if(nonMemberAuthor.getEmail() != null && nonMemberAuthor.getEmail().equals(user.getEmail())) {
                    toDelete = nonMemberAuthor;
                    break;
                }
            }
            if(toDelete != null) {
                contribution.getNonMemberAuthors().remove(toDelete);
                Logger.debug("Deleting non member " + toDelete.getEmail());

            }
            for(User author: contribution.getAuthors()) {
                if(author.getEmail() != null && author.getEmail().equals(user.getEmail())) {
                    isAuthor = true;
                    break;
                }
            }
            if(!isAuthor) {
                contribution.addAuthor(user);
                Logger.debug("Contribution updated ");
            }
            contribution.update();
            contribution.refresh();
        }

        for(Long contributionId: contributionsId) {
            Contribution contribution = Contribution.read(contributionId);
            F.Promise.promise(() -> {
                Contributions.sendAuthorAddedMail(null, contribution.getNonMemberAuthors(), contribution,
                        contribution.getContainingSpaces().get(0));
                PeerDocWrapper peerDocWrapper = new PeerDocWrapper(user);
                peerDocWrapper.updatePeerdocPermissions(contribution);
                return Optional.ofNullable(null);
            });
        }

    }

    private static void geoJsonLogic(Contribution c, ResourceSpace rs) {
        //if the contribution has a geojson
        if(c.getLocation() != null && c.getLocation().getGeoJson() != null) {
            Logger.debug("Contribution has location and geoJson");
            try {
                FeatureCollection featureCollection =
                        new ObjectMapper().readValue(c.getLocation().getGeoJson()
                                .replaceAll("'","\""), FeatureCollection.class);
                    featureCollection.getFeatures().get(0).setGeometry(LocationUtilities.polygonCenter(featureCollection));
                    String json= new ObjectMapper().writeValueAsString(featureCollection);
                    c.getLocation().setGeoJson(json.replaceAll("\"","'"));
                } catch (IOException e) {
                    Logger.error("Error calculating center point ", e);
                }
        } else {
            //if the contribution doesnt has a geojson use contribution or working group geojson
            if(c.getLocation() != null) {
                Logger.debug("Contribution has not geoJson");
                if(c.getType().equals(ContributionTypes.IDEA) && rs.getCampaign().getLocation()!=null) {
                    Logger.debug("Using campaign geoJson");
                    c.getLocation().setGeoJson(rs.getCampaign().getLocation().getGeoJson());
                } else if(c.getType().equals(ContributionTypes.PROPOSAL) && rs.getWorkingGroupResources() != null) {
                    List<Location> locations = rs.getWorkingGroupResources().getLocations();
                    if(!locations.isEmpty() && locations.get(0).getGeoJson() != null) {
                        Logger.debug("Using WG geoJson");
                        c.getLocation().setGeoJson(locations.get(0).getGeoJson());
                    } else if(rs.getCampaign().getLocation() != null){
                        Logger.debug("Using campaign geoJson");
                        c.getLocation().setGeoJson(rs.getCampaign().getLocation().getGeoJson());
                    }
                }
            }
            Logger.debug("Using none geoJson");
        }
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public static Long getIdByUUID(UUID uuid) {
        Contribution c = find.where().eq("uuid",uuid.toString()).findUnique();
        return c == null ? null : c.getContributionId();
    }

    public static Contribution getByUUID(UUID uuid) {
        return find.where().eq("uuid",uuid.toString()).findUnique();
    }

    public static List<Contribution> findChildrenOrParents(UUID cuuid, String type) {

        type = type.toUpperCase();
        Contribution contribution = Contribution.getByUUID(cuuid);
        if(contribution == null) {
            return null;
        }
        switch (type) {
            case "FORKS":
                return find.where().eq("parent.contributionId", contribution.getContributionId())
                        .or(Expr.eq("status", ContributionStatus.FORKED_PUBLIC_DRAFT.name()),
                                Expr.eq("status", ContributionStatus.FORKED_PUBLISHED.name())).findList();
            case "MERGES":
                return find.where().eq("parent.contributionId", contribution.getContributionId())
                        .eq("status", ContributionStatus.MERGED.name()).findList();
            case "PARENT":
                return Collections.singletonList(contribution.getParent());
            default:
                return null;
        }

    }

    public static Set<User> findMergeAuthors(UUID cuuid) {

        Contribution contribution = Contribution.getByUUID(cuuid);
        if(contribution == null) {
            return null;
        }

        List<User> authors = new ArrayList<>();
        List<Contribution> contributions =  find.where().eq("parent.contributionId", contribution.getContributionId())
                        .ilike("status", "%MERGE%").findList();
        for(Contribution contribution1: contributions) {
            if(!authors.contains(contribution1.getCreator())) {
                authors.add(contribution1.getCreator());
            }
        }
        return new HashSet<>(authors);
    }

    public static Contribution fork (Contribution parent, User author) throws NoSuchPaddingException,
            InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, HashGenerationException, MalformedURLException, MembershipCreationException {

        Logger.debug("Start forking contribution " + parent.getContributionId());
        PeerDocWrapper peerDocWrapper  = new PeerDocWrapper(author);
        JsonNode peerdocResponse = peerDocWrapper.fork(parent.getExtendedTextPad());
        if(peerdocResponse == null || peerdocResponse.get("path") == null) {
            Logger.debug("Non successful response from peerdoc, not forking");
            return null;
        }
        Logger.debug("Successful response from peerdoc " + peerdocResponse.get("path"));
        try {
            Ebean.beginTransaction();
            Contribution newContribution = new Contribution();

            newContribution.setTitle(parent.getTitle());
            newContribution.setText(parent.getText());
            newContribution.setPlainText(parent.getPlainText());
            newContribution.setType(parent.getType());
            newContribution.setStatus(ContributionStatus.FORKED_PRIVATE_DRAFT);
            newContribution.setModerationComment(parent.getModerationComment());
            newContribution.setSource(parent.getSource());
            newContribution.setSourceUrl(parent.getSourceUrl());
            newContribution.setLocation(parent.getLocation());
            newContribution.setCreator(author);
            newContribution.setAuthors(new ArrayList<>());
            newContribution.getAuthors().add(author);
            newContribution.setBudget(parent.getBudget());
            newContribution.setParent(parent);

            newContribution.setForum(parent.getForum());
            newContribution.setActionDueDate(parent.getActionDueDate());
            newContribution.setActionDone(parent.getActionDone());
            newContribution.setAction(parent.getAction());
            newContribution.setAssessmentSummary(parent.getAssessmentSummary());

            String padId = UUID.randomUUID().toString();

            Logger.debug("Creating resource ");
            String path =         peerdocResponse.get("path").toString().replace("\"","");
            Resource r = new Resource(new URL(peerDocWrapper.getPeerDocServerUrl() +  path));
            Logger.debug("PEERDOC URL FORK " + r.getUrlAsString());
            r.setPadId(padId);
            r.setResourceType(ResourceTypes.PEERDOC);
            r.setReadOnlyPadId(null);
            r.setCreator(author);

            newContribution.setCover(parent.getCover());
            newContribution.setSourceCode(parent.getSourceCode());
            newContribution.setLang(parent.getLang());

            r.save();
            newContribution.setExtendedTextPad(r);
            newContribution.save();
            Logger.debug("Resource and contribution saved ");
            newContribution.refresh();
            newContribution.setThemes(new ArrayList<>());
            newContribution.getThemes().addAll(parent.getThemes());
            newContribution.setHashtags(new ArrayList<>());
            newContribution.getHashtags().addAll(parent.getHashtags());
            newContribution.setCustomFieldValues(new ArrayList<>());
            newContribution.getCustomFieldValues().addAll(parent.getCustomFieldValues());
            newContribution.update();

            List<ResourceSpace> parentResources = parent.getContainingSpaces();
            int numberRS = parentResources != null ? parentResources.size() : 0;
            Logger.debug("Parend of fork in " + numberRS + " Resource Spaces");
            for(ResourceSpace rs: parentResources) {
                if(rs.getType().equals(ResourceSpaceTypes.WORKING_GROUP)
                        || rs.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
                    Logger.debug("Adding fork to the Resource Space "+rs.getResourceSpaceId()+" of type "+rs.getType());
                    rs.addContribution(newContribution);
                    newContribution.getContainingSpaces().add(rs);
                    rs.update();
                }
            }
            parent.refresh();
            newContribution.refresh();
            Logger.debug("Contribution resource spaces saved ");
            if(parent.getWorkingGroupAuthors() != null && parent.getWorkingGroupAuthors().size() > 0) {
                Logger.debug("Adding author to working group");
                addContributionAuthorsToWG(newContribution, parent.getWorkingGroupAuthors().get(0).getResources());
            }


            Ebean.commitTransaction();
            F.Promise.promise(() -> {
                Logger.debug("Sending notification");
                try {
                    NotificationsDelegate.forkMergeContributionInResourceSpace(parent.getResourceSpace(),
                            newContribution, NotificationEventName.NEW_CONTRIBUTION_FORK);
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.error(e.getMessage());
                }
                return Optional.ofNullable(null);
            });

            return newContribution;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Error forking transaction " + e.getMessage());
            throw e;
        } finally {
            Ebean.endTransaction();
        }
    }


    public static Contribution merge(Contribution parent, Contribution children, User author) throws
            NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException,
            IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            HashGenerationException {
        Logger.debug("Start forking contribution " + parent.getContributionId());
        PeerDocWrapper peerDocWrapper  = new PeerDocWrapper(author);
        if(!peerDocWrapper.merge(parent, children)) {
            Logger.debug("Non successful response from peerdoc, not merging");
            return null;
        }

        children.setStatus(ContributionStatus.MERGED);

        children.update();
        children.refresh();
        F.Promise.promise(() -> {
            Logger.debug("Sending notification");
            NotificationsDelegate.forkMergeContributionInResourceSpace(parent.getResourceSpace(),
                    children, NotificationEventName.NEW_CONTRIBUTION_MERGE);
            return Optional.ofNullable(null);
        });

        return children;
    }

    public static void addContributionAuthorsToWG(Contribution contribution, ResourceSpace rs) throws MembershipCreationException {

        if (!rs.getType().equals(ResourceSpaceTypes.WORKING_GROUP) || !contribution.getType().equals(ContributionTypes.PROPOSAL)) {
            return;
        }
        WorkingGroup wg = rs.getWorkingGroupResources();
        for(User user : contribution.getAuthors()) {
            List<Membership> m = Membership.findByUser(user,"GROUP");
            if (m!=null || m.size() == 0) {
                Logger.debug("Author " + user.getUsername() + " is already a member of " + wg.getName() + "");
            } else {
                Logger.debug("Adding author " + user.getUsername() + " to working group " + wg.getName() + "");
                List<SecurityRole> roles = new ArrayList<SecurityRole>();
                roles.add(SecurityRole.findByName("MEMBER"));
                WorkingGroup.createMembership(wg.getGroupId(), user, roles);
            }
        }
    }
    public String getErrorsInExtendedTextPad() {
        return errorsInExtendedTextPad;
    }

    public void setErrorsInExtendedTextPad(String errorsInExtendedTextPad) {
        this.errorsInExtendedTextPad = errorsInExtendedTextPad;
    }

    public List<Contribution> getChildren() {
        return children;
    }

    public void setChildren(List<Contribution> children) {
        this.children = children;
    }

    public Contribution getParent() {
        return parent;
    }

    public void setParent(Contribution parent) {
        this.parent = parent;
    }

    public Integer getForks() {
        List<Contribution> contributions = Contribution.findChildrenOrParents(this.uuid, "FORKS");
        return contributions != null ? contributions.size() : 0;

    }

    public void setForks(Integer forks) {
        this.forks = forks;
    }

    public Integer getAcceptedForks() {
        List<Contribution> contributions = Contribution.findChildrenOrParents(this.uuid, "MERGES");
        return contributions != null ? contributions.size() : 0;
    }

    public void setAcceptedForks(Integer acceptedForks) {
        this.acceptedForks = acceptedForks;
    }
}