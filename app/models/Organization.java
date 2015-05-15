package models;

import javafx.collections.transformation.SortedList;
import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Organization extends Model {

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    private Long orgId;
    private String name;
    private String description;
    
    private SortedList<Phase> phases;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Theme> themes = new ArrayList<Theme>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Assembly> assemblies = new ArrayList<Assembly>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<Message>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<OrganizationMembership> organizationMemberships = new ArrayList<OrganizationMembership>();

    public Organization(User creator, Date creation, Date removal, String lang, Long orgId, String name, String description, SortedList<Phase> phases, List<Theme> themes, List<Assembly> assemblies, List<Message> messages, List<OrganizationMembership> organizationMemberships) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.orgId = orgId;
        this.name = name;
        this.description = description;
        this.phases = phases;
        this.themes = themes;
        this.assemblies = assemblies;
        this.messages = messages;
        this.organizationMemberships = organizationMemberships;
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

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SortedList<Phase> getPhases() {
        return phases;
    }

    public void setPhases(SortedList<Phase> phases) {
        this.phases = phases;
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

    public List<Assembly> getAssemblies() {
        return assemblies;
    }

    public void setAssemblies(List<Assembly> assemblies) {
        this.assemblies = assemblies;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<OrganizationMembership> getOrganizationMemberships() {
        return organizationMemberships;
    }

    public void setOrganizationMemberships(List<OrganizationMembership> organizationMemberships) {
        this.organizationMemberships = organizationMemberships;
    }
}
