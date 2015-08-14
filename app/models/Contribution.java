package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.ContributionTypes;
import models.location.Geo;
import models.location.Location;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE")
public class Contribution extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long contributionId;
	private UUID uuid = UUID.randomUUID();
	private String title;
	private String text;
	@Enumerated(EnumType.STRING)
	private ContributionTypes type = ContributionTypes.COMMENT;
	private User author;

	@ManyToMany(cascade = CascadeType.ALL)
	private List<User> additionalAuthors = new ArrayList<User>();

	@ManyToOne
	private Assembly assembly;

	@ManyToMany(cascade = CascadeType.ALL)
	private List<Theme> contributionCategories = new ArrayList<Theme>();

	@OneToMany(cascade = CascadeType.ALL)
	private List<Resource> attachments = new ArrayList<Resource>();

	@OneToOne
	private Location location;

	@ManyToMany(cascade = CascadeType.ALL)
	private List<Hashtag> hashtags = new ArrayList<Hashtag>();

	@OneToOne(cascade = CascadeType.ALL)
	@JsonIgnoreProperties({"contributionStatisticsId"})
	@JsonManagedReference
	private ContributionStatistics stats;

	// TODO think of how to connect and move through to the campaign phases

	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, Contribution> find = new Finder<>(Contribution.class);

	public Contribution(User creator, String title, String text,
			ContributionTypes type) {
		super();
		this.author = creator;
		this.title = title;
		this.text = text;
		this.type = type;
	}

	public Contribution(User creator, String title, String brief,
			ContributionTypes type, Assembly assembly) {
		this.author = creator;
		this.title = title;
		this.text = brief;
		this.type = type;
		this.assembly = assembly;
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

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User creator) {
		this.author = creator;
	}

	public List<User> getAdditionalAuthors() {
		return additionalAuthors;
	}

	public void setAdditionalAuthors(List<User> additionalAuthors) {
		this.additionalAuthors = additionalAuthors;
	}

	public Assembly getAssembly() {
		return assembly;
	}

	public void setAssembly(Assembly assembly) {
		this.assembly = assembly;
	}

	public List<Theme> getContributionCategories() {
		return contributionCategories;
	}

	public void setContributionCategories(List<Theme> contributionCategories) {
		this.contributionCategories = contributionCategories;
	}

	public List<Resource> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Resource> attachments) {
		this.attachments = attachments;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public List<Hashtag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(List<Hashtag> hashtags) {
		this.hashtags = hashtags;
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

	public static List<Contribution> findAllByAssembly(Long aid) {
		List<Contribution> contribs = find.where()
				.eq("assembly.assemblyId", aid).findList();
		return contribs;
	}

	public static void create(Contribution issue) {
		issue.save();
		issue.refresh();
	}

	public static Contribution read(Long issueId) {
		return find.ref(issueId);
	}

	public static Integer readByTitle(String title) {
		ExpressionList<Contribution> contributions = find.where().eq("title",
				title);
		return contributions.findList().size();
	}

	public static Contribution createObject(Contribution issue) {
		issue.save();
		return issue;
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
	 * Other Queries DEPRECATED
	 */
	public static Contribution readIssueOfAssembly(Long assemblyId,
			Long contributionId) {
		return readByIdAndType(assemblyId, contributionId,
				ContributionTypes.ISSUE);
	}

	public static Contribution readIdeaOfAssembly(Long assemblyId,
			Long contributionId) {
		return readByIdAndType(assemblyId, contributionId,
				ContributionTypes.IDEA);
	}

	public static Contribution readQuestionOfAssembly(Long assemblyId,
			Long contributionId) {
		return readByIdAndType(assemblyId, contributionId,
				ContributionTypes.QUESTION);
	}

	public static Contribution readCommentOfAssembly(Long assemblyId,
			Long contributionId) {
		return readByIdAndType(assemblyId, contributionId,
				ContributionTypes.COMMENT);
	}

	public static List<Contribution> readContributionsOfAssembly(Long assemblyId) {
		return find.where().eq("assemblyId", assemblyId).findList();
	}

	public static List<Contribution> readIssuesOfAssembly(Long assemblyId) {
		return readListByAssemblyAndType(assemblyId, ContributionTypes.ISSUE);
	}

	public static List<Contribution> readIdeasOfAssembly(Long assemblyId) {
		return readListByAssemblyAndType(assemblyId, ContributionTypes.IDEA);
	}

	public static List<Contribution> readQuestionsOfAssembly(Long assemblyId) {
		return readListByAssemblyAndType(assemblyId, ContributionTypes.QUESTION);
	}

	public static List<Contribution> readCommentsOfAssembly(Long assemblyId) {
		return readListByAssemblyAndType(assemblyId, ContributionTypes.COMMENT);
	}

	public static Contribution readByIdAndType(Long assemblyId,
			Long contributionId, ContributionTypes type) {
		return find.where().eq("assemblyId", assemblyId)
				.eq("contributionId", contributionId).eq("type", type)
				.findUnique();
	}

	public static List<Contribution> readListByAssemblyAndType(Long assemblyId,
			ContributionTypes type) {
		return find.where().eq("assemblyId", assemblyId).eq("type", type)
				.findList();
	}

	public static void deleteContributionByIdAndType(Long contributionId,ContributionTypes cType) {
		find.where().eq("contributionId", contributionId).eq("type", cType).findUnique().delete();
	}
}
