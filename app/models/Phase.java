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
    private Date start_date;
    private Date end_date;
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

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Module> modules = new ArrayList<Module>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Resource> resources = new ArrayList<Resource>();


    public Phase(Long phaseId, Date start, Date end, Date update, String name, User creator, Date creation, Date removal, String lang, List<Assembly> assemblies, List<Module> modules, List<Resource> resources) {
        this.phaseId = phaseId;
        this.start_date = start;
        this.end_date = end;
        this.update = update;
        this.name = name;
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.assemblies = assemblies;
        this.modules = modules;
        this.resources = resources;
    }

    public Phase(){
        super();
    }

    public static Model.Finder<Long, Phase> find = new Model.Finder<Long, Phase>(
            Long.class, Phase.class);

    public static Phase read(Long phaseId) {
        return find.ref(phaseId);
    }

    public static List<Phase> findAll() {
        return find.all();
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public Long getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(Long phaseId) {
        this.phaseId = phaseId;
    }

    public Date getStart_date() {
        return start_date;
    }

    public void setStart_date(Date start_date) {
        this.start_date = start_date;
    }

    public Date getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
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
