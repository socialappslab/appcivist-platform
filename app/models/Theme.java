package models;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.annotations.ApiModel;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ThemeTypes;
import models.misc.Views;

import java.util.List;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="Theme", description="A Theme represents a top level categorization information")
public class Theme extends AppCivistBaseModel {

    @Id
    @GeneratedValue    
    @JsonView(Views.Public.class)
    private Long themeId;

    @JsonView(Views.Public.class)
    private String title;
    @JsonView(Views.Public.class)
    @Column(name = "description", columnDefinition = "text")
    private String description;
    @JsonView(Views.Public.class)
    private String icon;
    @JsonView(Views.Public.class)
    private String cover;
    @JsonView(Views.Public.class)
    @Enumerated(EnumType.STRING)
    private ThemeTypes type = ThemeTypes.EMERGENT;
    

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "themes", cascade = CascadeType.ALL)
    private List<ResourceSpace> containingSpaces;

    /**
     * The find property is an static property that facilitates database query creation
     */
    public static Finder<Long, Theme> find = new Finder<>(Theme.class);

    /*
     * Getters and Setters
	 */
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public ThemeTypes getType() {
		return type;
	}

	public void setType(ThemeTypes type) {
		this.type = type;
	}

	public List<ResourceSpace> getContainingSpaces() {
        return containingSpaces;
    }

    public void setContainingSpaces(List<ResourceSpace> containingSpaces) {
        this.containingSpaces = containingSpaces;
    }

    /*
     * Basic Data operations
     */
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

	public static List<Theme> findByTitle(String t) {
		return find.where().eq("title",t).findList();
	}
}
