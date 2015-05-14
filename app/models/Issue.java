package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.services.ServiceResource;
import play.db.ebean.Model;

@Entity
public class Issue extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7576572204861603387L;

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

	@Id
	private Long issueId;
	private String title;
	private String brief;
	private String type; // TODO convert in enum
    private Long likes;

	@ManyToOne(cascade = CascadeType.ALL)
	private ServiceResource resource;

	@OneToMany(cascade = CascadeType.ALL, mappedBy="issue")
	private List<Campaign> decisionWorkflow = new ArrayList<Campaign>();

//	private String test;
	
	@JsonIgnore
	@ManyToOne
	private Assembly assembly;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Theme> themes = new ArrayList<Theme>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Resource> resources = new ArrayList<Resource>();

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

    public Issue(User creator, Date creation, Date removal, String lang, Long issueId, String title, String brief, String type, Long likes, ServiceResource resource, List<Campaign> decisionWorkflow, Assembly assembly, List<Theme> themes, List<Resource> resources) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.issueId = issueId;
        this.title = title;
        this.brief = brief;
        this.type = type;
        this.likes = likes;
        this.resource = resource;
        this.decisionWorkflow = decisionWorkflow;
        this.assembly = assembly;
        this.themes = themes;
        this.resources = resources;
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

    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public Date getRemoval() {
        return removal;
    }

    public void setRemoval(Date removal) {
        this.removal = removal;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
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
