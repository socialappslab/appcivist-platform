package models;

import models.Location.Geo;
import play.db.ebean.Model;

import javax.persistence.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Resource extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    @Column(name="resource_id")
    private Long resourceId;
    private String type;
    private URL externalURL;

    @ManyToMany(mappedBy = "resources")
    private List<Phase> phases = new ArrayList<Phase>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name="RELATED_RESOURCES",
            joinColumns={@JoinColumn(name="source", referencedColumnName="resource_id")},
            inverseJoinColumns={@JoinColumn(name="target", referencedColumnName="resource_id")})
    private List<Resource> resources = new ArrayList<Resource>();

    @ManyToMany(mappedBy = "resources")
    private List<Issue> issues = new ArrayList<Issue>();

    @ManyToMany(mappedBy = "resources")
    private List<WorkingGroup> workingGroups = new ArrayList<WorkingGroup>();

    @OneToOne
    private Geo location;

    public Geo getLocation() {
        return location;
    }

    public void setLocation(Geo location) {
        this.location = location;
    }
    /*
    @OneToMany(cascade = CascadeType.ALL, mappedBy="resource")
    private List<Meeting> meetings = new ArrayList<Meeting>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy="resource")
    private List<Note> notes = new ArrayList<Note>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy="resource")
    private List<Task> tasks = new ArrayList<Task>();*/

    public Resource(User creator, Date creation, Date removal, String lang, Long resourceId, String type, URL externalURL, List<Phase> phases, List<Resource> resources, List<Issue> issues, List<WorkingGroup> workingGroups) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.resourceId = resourceId;
        this.type = type;
        this.externalURL = externalURL;
        this.phases = phases;
        this.resources = resources;
        this.issues = issues;
        this.workingGroups = workingGroups;
    }

    public static Model.Finder<Long, Resource> find = new Model.Finder<Long, Resource>(
            Long.class, Resource.class);

    public static Resource read(Long resourceId) {
        return find.ref(resourceId);
    }

    public static List<Resource> findAll() {
        return find.all();
    }

    public static Resource create(Resource resource) {
        resource.save();
        resource.refresh();
        return resource;
    }

    public static Resource createObject(Resource resource) {
        resource.save();
        return resource;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
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

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public URL getExternalURL() {
        return externalURL;
    }

    public void setExternalURL(URL externalURL) {
        this.externalURL = externalURL;
    }

    public List<Phase> getPhases() {
        return phases;
    }

    public void setPhases(List<Phase> phases) {
        this.phases = phases;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public List<WorkingGroup> getWorkingGroups() {
        return workingGroups;
    }

    public void setWorkingGroups(List<WorkingGroup> workingGroups) {
        this.workingGroups = workingGroups;
    }

}
