package models;

import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Phase extends Model {

    @Id
    private Long phaseId;
    private Date start;
    private Date end;
    private Date update;
    private String name;

    //private Module capability;

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @ManyToMany(mappedBy = "phases")
    private List<Assembly> assemblies = new ArrayList<Assembly>();

    /*@ManyToMany(cascade = CascadeType.ALL)
    private List<Resource> resources = new ArrayList<Resource>();*/


    public Phase(Long phaseId, Date start, Date end, Date update, String name, User creator, Date creation, Date removal, String lang, List<Assembly> assemblies) {
        this.phaseId = phaseId;
        this.start = start;
        this.end = end;
        this.update = update;
        this.name = name;
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.assemblies = assemblies;
    }

    public Long getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(Long phaseId) {
        this.phaseId = phaseId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Date getUpdate() {
        return update;
    }

    public void setUpdate(Date update) {
        this.update = update;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<Assembly> getAssemblies() {
        return assemblies;
    }

    public void setAssemblies(List<Assembly> assemblies) {
        this.assemblies = assemblies;
    }
}
