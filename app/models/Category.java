package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.util.List;

@Entity
public class Category extends AppCivistBaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2546530838000950929L;
	@Id
	@GeneratedValue
    private Long categoryId;
    private String title;
    private String description;
    private String icon;
    private String cover; 

    /**
	 * The find property is an static property that facilitates database query creation
	 */
    public static Finder<Long, Category> find = new Finder<Long, Category>(
            Long.class, Category.class);

    /*
	 * Getters and Setters
	 */
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long themeId) {

        this.categoryId = themeId;
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

	/*
	 * Basic Data operations
	 */
    public static Category read(Long themeId) {
        return find.ref(themeId);
    }

    public static List<Category> findAll() {
        return find.all();
    }

    public static Category create(Category theme) {
        theme.save();
        theme.refresh();
        return theme;
    }

    public static Category createObject(Category theme) {
        theme.save();
        return theme;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }
}
