package models;

import play.db.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Theme extends Model {

    @Id
    private Long themeId;
    private String title;
    private String description;

    @ManyToMany(mappedBy = "themes")
    private List<Issue> issues = new ArrayList<Issue>();

    @ManyToMany(mappedBy = "themes")
    private List<Assembly> assemblies = new ArrayList<Assembly>();

    @ManyToMany(mappedBy = "themes")
    private List<Organization> organizations = new ArrayList<Organization>();

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    public Theme(Long themeId, String title, String description, List<Issue> issues, List<Assembly> assemblies, List<Organization> organizations, User creator, Date creation, Date removal, String lang){
        this.themeId = themeId;
        this.title = title;
        this.description = description;
        this.setIssues(issues);
        this.setAssemblies(assemblies);
        this.setOrganizations(organizations);
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
    }

    public static Model.Finder<Long, Theme> find = new Model.Finder<Long, Theme>(
            Long.class, Theme.class);

    public static Theme read(Long themeId) {
        return find.ref(themeId);
    }

    public static List<Theme> findAll() {
        return find.all();
    }

    public static Theme create(Theme theme) {
        theme.save();
        theme.refresh();
        return theme;
    }

    public static Theme createObject(Theme theme) {
        theme.save();
        return theme;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }

    public Long getThemeId() {
        return themeId;
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

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }

    public void setThemeId(Long themeId) {

        this.themeId = themeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public List<Assembly> getAssemblies() {
        return assemblies;
    }

    public void setAssemblies(List<Assembly> assemblies) {
        this.assemblies = assemblies;
    }
}
