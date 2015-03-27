package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.services.ServiceResource;
import play.db.ebean.Model;

@Entity
public class Issue extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7576572204861603387L;


	@Id
	private Long issueId;
	private String title;
	private String brief;
	private String type; // TODO convert in enum

	@ManyToOne(cascade = CascadeType.ALL)
	private ServiceResource resource;

	@OneToMany(cascade = CascadeType.ALL)
	private List<Campaign> decisionWorkflow;

	public static Model.Finder<Long, Issue> find = new Model.Finder<Long, Issue>(
			Long.class, Issue.class);

	public static IssueCollection findAll() {
		List<Issue> issues = find.all();
		IssueCollection issueCollection = new IssueCollection();
		issueCollection.setIssues(issues);
		return issueCollection;
	}

	public static void create(Issue issue) {
		issue.save();
		issue.refresh();
	}

	public static Issue read(Long issueId) {
		return find.ref(issueId);
	}

	public static Issue createObject(Issue issue) {
		issue.save();
		return issue;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	public List<Campaign> getDecisionWorkflow() {
		return decisionWorkflow;
	}

	public void setDecisionWorkflow(List<Campaign> campaigns) {
		this.decisionWorkflow = campaigns;
	}

	public Long getIssueId() {
		return issueId;
	}

	public void setIssueId(Long id) {
		this.issueId = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBrief() {
		return brief;
	}

	public void setBrief(String brief) {
		this.brief = brief;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ServiceResource getResource() {
		return resource;
	}

	public void setResource(ServiceResource resource) {
		this.resource = resource;
	}

}
