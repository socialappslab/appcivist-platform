package models.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.services.ServiceAssembly;
import play.db.ebean.Model;

@Entity
public class ServiceIssue extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7576572204861603387L;

	@Id
	private Long issueId;
	private String title;
	private String brief;
	private String type; // TODO convert in enum
	private Long likes;

	@JsonIgnore
	@ManyToOne
	private ServiceAssembly assembly;

	@ManyToOne(cascade = CascadeType.ALL)
	private ServiceResource resource;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "issue")
	private List<ServiceCampaign> decisionWorkflow = new ArrayList<ServiceCampaign>();

	public static Model.Finder<Long, ServiceIssue> find = new Model.Finder<Long, ServiceIssue>(
			Long.class, ServiceIssue.class);

	public ServiceIssue(Long issueId, String title, String brief, String type,
			ServiceResource resource) {

		super();
		this.issueId = issueId;
		this.title = title;
		this.brief = brief;
		this.type = type;
		this.resource = resource;
	}

	public ServiceIssue(Long issueId, String title, String brief, String type,
			ServiceResource resource, ServiceCampaign c) {
		super();
		this.issueId = issueId;
		this.title = title;
		this.brief = brief;
		this.type = type;
		this.resource = resource;
		this.addCampaign(c);
	}

	public ServiceIssue(Long issueId, String title, String brief, String type,
			ServiceResource resource, List<ServiceCampaign> c) {
		super();
		this.issueId = issueId;
		this.title = title;
		this.brief = brief;
		this.type = type;
		this.resource = resource;
		this.setDecisionWorkflow(c);
	}

	public ServiceIssue(Date creation, Date removal, String lang,
			Long issueId, String title, String brief, String type, Long likes,
			ServiceResource resource, List<ServiceCampaign> decisionWorkflow,
			ServiceAssembly assembly) {
		this.issueId = issueId;
		this.title = title;
		this.brief = brief;
		this.type = type;
		this.likes = likes;
		this.resource = resource;
		this.decisionWorkflow = decisionWorkflow;
		this.assembly = assembly;
	}

	public ServiceIssue() {
		super();
	}

	public static ServiceIssue create(Long issueId, String title, String brief,
			String type, ServiceResource resource, List<ServiceCampaign> c) {
		ServiceIssue i = new ServiceIssue(issueId, title, brief, type,
				resource, c);
		i.save();
		return i;
	}

	public static ServiceIssueCollection findAll() {
		List<ServiceIssue> issues = find.all();
		ServiceIssueCollection issueCollection = new ServiceIssueCollection();
		issueCollection.setIssues(issues);
		return issueCollection;
	}

	public static void create(ServiceIssue issue) {
		issue.save();
		issue.refresh();
	}

	public static ServiceIssue read(Long issueId) {
		return find.ref(issueId);
	}

	public static ServiceIssue createObject(ServiceIssue issue) {
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
	public List<ServiceCampaign> getDecisionWorkflow() {
		return decisionWorkflow;
	}

	public void setDecisionWorkflow(List<ServiceCampaign> campaigns) {
		this.decisionWorkflow = campaigns;
	}

	public void addCampaign(ServiceCampaign c) {
		this.decisionWorkflow.add(c);
	}

	public void removeCampaign(ServiceCampaign c) {
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

	public Long getLikes() {
		return likes;
	}

	public void setLikes(Long likes) {
		this.likes = likes;
	}

	/*
	 * Other Queries
	 */

	public ServiceAssembly getAssembly() {
		return assembly;
	}

	public void setAssembly(ServiceAssembly assembly) {
		this.assembly = assembly;
	}

	public static ServiceIssue readIssueOfServiceAssembly(Long assemblyId, Long issueId) {
		return find.where().eq("assembly_assembly_id", assemblyId) // TODO as of
																	// now this
																	// is not
																	// neede but
																	// we should
																	// have
																	// relative
																	// ids
				.eq("issueId", issueId).findUnique();
	}

}
