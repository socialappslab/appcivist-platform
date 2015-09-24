package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.*;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ContributionTypes;
import enums.ResourceSpaceTypes;
import models.location.Location;

@Entity
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(name = "TYPE")
@JsonInclude(Include.NON_EMPTY)
public class Contribution extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long contributionId;
	@Index
	private UUID uuid = UUID.randomUUID();
	private String title;
	private String text;
	
	@Transient
	@Enumerated(EnumType.STRING)
	private ContributionTypes type = ContributionTypes.COMMENT;
	@JsonIgnore
	@Index
	private String textIndex;
	@OneToOne
	private Location location;
	
	@ManyToMany(cascade = CascadeType.ALL)
	private List<User> authors = new ArrayList<User>();

	@Transient	
	private List<Theme> themes;

	@Transient
	private List<Resource> attachments;

	@Transient
	private List<Hashtag> hashtags = new ArrayList<Hashtag>();

	// TODO: check if it works
	@ManyToMany(fetch=FetchType.LAZY,mappedBy="contributions")
	private List<ResourceSpace> targetSpaces;
	
	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonInclude(Include.NON_EMPTY)
	private ResourceSpace resourceSpace = new ResourceSpace(ResourceSpaceTypes.CONTRIBUTION);

	@OneToOne(cascade = CascadeType.ALL)
	@JsonIgnoreProperties({"contributionStatisticsId"})
	@JsonManagedReference
	private ContributionStatistics stats = new ContributionStatistics();
	
	// TODO think of how to connect and move through to the campaign phases

	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, Contribution> find = new Finder<>(Contribution.class);

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
		this.resourceSpace.setThemes(themes);
	}

	public void addTheme(Theme t) {
		this.resourceSpace.addTheme(t);
	}
	
	public List<Resource> getAttachments() {
		return resourceSpace.getResources();
	}

	public void setAttachments(List<Resource> attachments) {
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
		this.resourceSpace.setHashtags(hashtags);
	}
	
	public void addHashtag(Hashtag h) {
		this.resourceSpace.addHashtag(h);
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

	/*
	 * Basic Data Operations
	 */
	public static Contribution create(User creator, String title, String text,
			ContributionTypes type) {
		Contribution c = new Contribution(creator, title, text, type);
		c.save();
		return c;
	}

	public static List<Contribution> findAll() {
		List<Contribution> contribs = find.all();
		return contribs;
	}
	
	public static void create(Contribution c) {
		c.save();
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

	public static List<Contribution> findAllByTargetSpace(Long sid) {
		List<Contribution> contribs = find.where()
				.eq("targetSpaces.resourceSpaceId", sid).findList();
		return contribs;
	}

	public static List<Contribution> findAllByTargetSpaceAndQuery(Long sid, String query) {
		List<Contribution> contribs = find.where()
				.eq("targetSpaces.resourceSpaceId", sid)
				.ilike("textIndex", "%"+query+"%")
				.findList();
		return contribs;
	}
	
	public static List<Contribution> findAllByTargetSpaceAndUUID(UUID uuid) {
		List<Contribution> contribs = find.where()
				.eq("targetSpaces.uuid", uuid).findList();
		return contribs;
	}

	public static List<Contribution> readContributionsOfSpace(Long resourceSpaceId) {
		return find.where().eq("targetSpaces.resourceSpaceId", resourceSpaceId).findList();
	}
	
	public static Contribution readByIdAndType(Long resourceSpaceId,
			Long contributionId, ContributionTypes type) {
		return find.where().eq("targetSpaces.resourceSpaceId", resourceSpaceId)
				.eq("contributionId", contributionId).eq("type", type)
				.findUnique();
	}

	public static List<Contribution> readListByTargetSpaceAndType(Long resourceSpaceId,
			ContributionTypes type) {
		return find.where().eq("targetSpaces.resourceSpaceId", resourceSpaceId).eq("type", type)
				.findList();
	}

	public static void deleteContributionByIdAndType(Long contributionId,ContributionTypes cType) {
		find.where().eq("contributionId", contributionId).eq("type", cType).findUnique().delete();
	}

	public static List<Contribution> readByCreator(User u) {
		return find.where().eq("authors.userId",u.getUserId()).findList();
	}
	
	public static List<Contribution> findAllByTargetSpaceAndType(
			ResourceSpace rs, String t) {
		return find.where().eq("targetSpaces", rs).eq("type", t.toUpperCase())
				.findList();
	}
	
	public static List<Contribution> findAllByTargetSpaceAndTypeAndQuery(
			ResourceSpace rs, String t, String query) {
		return find.where().eq("targetSpaces", rs).eq("type", t.toUpperCase())
				.ilike("textIndex", "%"+query+"%")
				.findList();
	}
	
	
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
		return readListByTargetSpaceAndType(resourceSpaceId, ContributionTypes.ISSUE);
	}

	public static List<Contribution> readIdeasOfSpace(Long resourceSpaceId) {
		return readListByTargetSpaceAndType(resourceSpaceId, ContributionTypes.IDEA);
	}

	public static List<Contribution> readQuestionsOfSpace(Long resourceSpaceId) {
		return readListByTargetSpaceAndType(resourceSpaceId, ContributionTypes.QUESTION);
	}

	public static List<Contribution> readCommentsOfSpace(Long resourceSpaceId) {
		return readListByTargetSpaceAndType(resourceSpaceId, ContributionTypes.COMMENT);
	}

	// auxiliary
	
	// Extract hashtags from Text
	@PrePersist
	@PreUpdate
	public void beforeSavingUpdating() {
		// 1. Update text index
		this.textIndex = this.title+"\n"+this.text;
		// 2. extractHashtags
		Pattern MY_PATTERN = Pattern.compile("#(\\w+|\\W+)");
		Matcher mat = MY_PATTERN.matcher(this.textIndex);
		while (mat.find()) {
		  //System.out.println(mat.group(1));
		  this.hashtags.add(new Hashtag(mat.group(1)));
		}		
		
		this.stats.setReplies(new Long(this.resourceSpace.getContributions().size()));
	}
}
