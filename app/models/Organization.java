package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.collections.transformation.SortedList;
import models.Location.Geo;
import play.db.ebean.Model;

import javax.persistence.*;
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
    private String address;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Theme> themes = new ArrayList<Theme>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Assembly> assemblies = new ArrayList<Assembly>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<Message>();

    @JsonIgnore
    @OneToMany(mappedBy="organization", cascade = CascadeType.ALL)
    private List<OrganizationMembership> memberships = new ArrayList<OrganizationMembership>();

    @OneToOne
    private Geo location;

    public Organization(User creator, Date creation, Date removal, String lang, Long orgId, String name, String description, List<Theme> themes, List<Assembly> assemblies, List<Message> messages) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.orgId = orgId;
        this.name = name;
        this.description = description;
        this.themes = themes;
        this.assemblies = assemblies;
        this.messages = messages;
    }

    public Organization(){
        super();
    }

    public static Model.Finder<Long, Organization> find = new Model.Finder<Long, Organization>(
            Long.class, Organization.class);

    public static Organization read(Long organizationId) {
        return find.ref(organizationId);
    }

    public List<OrganizationMembership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<OrganizationMembership> memberships) {
        this.memberships = memberships;
    }

    public Geo getLocation() {
        return location;
    }

    public void setLocation(Geo location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

}
