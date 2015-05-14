package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Module extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    private Long modId;
    private Boolean enabled;
    private String name;
    private Config configuration;

    @ManyToMany(mappedBy = "modules")
    private List<Assembly> assemblies = new ArrayList<Assembly>();

    @ManyToMany(mappedBy = "modules")
    private List<Phase> phases = new ArrayList<Phase>();

    public Module(User creator, Date creation, Date removal, String lang, Long modId, Boolean enabled, String name, Config configuration, List<Assembly> assemblies, List<Phase> phases) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.modId = modId;
        this.enabled = enabled;
        this.name = name;
        this.configuration = configuration;
        this.assemblies = assemblies;
        this.phases = phases;
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

    public Long getModId() {
        return modId;
    }

    public void setModId(Long modId) {
        this.modId = modId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Config getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Config configuration) {
        this.configuration = configuration;
    }

    public List<Assembly> getAssemblies() {
        return assemblies;
    }

    public void setAssemblies(List<Assembly> assemblies) {
        this.assemblies = assemblies;
    }

    public List<Phase> getPhases() {
        return phases;
    }

    public void setPhases(List<Phase> phases) {
        this.phases = phases;
    }
}
