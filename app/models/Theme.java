package models;

import play.db.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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

    /*@ManyToMany(mappedBy = "themes")
    private List<Organization> organizations = new ArrayList<Organization>();*/

    public Theme(Long themeId, String title, String description, List<Issue> issues, List<Assembly> assemblies){
        this.themeId = themeId;
        this.title = title;
        this.description = description;
        this.setIssues(issues);
        this.setAssemblies(assemblies);
    }

    public Long getThemeId() {
        return themeId;
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
