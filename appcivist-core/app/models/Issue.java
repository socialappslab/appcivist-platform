package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

	@OneToMany(cascade = CascadeType.ALL, mappedBy="issue")
	private List<Campaign> decisionWorkflow = new ArrayList<Campaign>();;

//	private String test;
	
	@JsonIgnore
	@ManyToOne
	private Assembly assembly;
	/*
	 * Basic Queries
	 */
	
	public static Model.Finder<Long, Issue> find = new Model.Finder<Long, Issue>(
			Long.class, Issue.class);

	
	public Issue(Long issueId, String title, String brief, String type,
			ServiceResource resource) {
		super();
		this.issueId = issueId;
		this.title = title;
		this.brief = brief;
		this.type = type;
		this.resource = resource;
	}

	public Issue(Long issueId, String title, String brief, String type,
			ServiceResource resource, Campaign c) {
		super();
		this.issueId = issueId;
		this.title = title;
		this.brief = brief;
		this.type = type;
		this.resource = resource;
		this.addCampaign(c);
	}


	public Issue(Long issueId, String title, String brief, String type,
			ServiceResource resource, List<Campaign> c) {
		super();
		this.issueId = issueId;
		this.title = title;
		this.brief = brief;
		this.type = type;
		this.resource = resource;
		this.setDecisionWorkflow(c);
	}
	
	 public Issue() {
		 super();
	}

	public static Issue create(Long issueId, String title, String brief, String type,
				ServiceResource resource, List<Campaign> c) {
	        Issue i = new Issue(issueId, title, brief, type, resource, c);
	        i.save();
	        return i;
	    }
	
	
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
	
	/*
	 * Getters and Setters
	 */
	public List<Campaign> getDecisionWorkflow() {
		return decisionWorkflow;
	}

	public void setDecisionWorkflow(List<Campaign> campaigns) {
		this.decisionWorkflow = campaigns;
	}

	public void addCampaign(Campaign c) {
		this.decisionWorkflow.add(c);
	}
	
	public void removeCampaign(Campaign c) {
		this.decisionWorkflow.add(c);
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

	/*
	 * Other Queries
	 */
	
	public Assembly getAssembly() {
		return assembly;
	}

	public void setAssembly(Assembly assembly) {
		this.assembly = assembly;
	}

	public static Issue readIssueOfAssembly(Long assemblyId, Long issueId) {
		return find.where()
			.eq("assembly_assembly_id", assemblyId) // TODO as of now this is not neede but we should have relative ids
			.eq("issueId", issueId).findUnique();
	}
	
}
