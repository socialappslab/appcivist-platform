package models;

import io.swagger.annotations.ApiModel;

import java.net.URL;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="NonMemberAuthor", description="Author of contributions who are not users of the platform")
public class NonMemberAuthor extends Model {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String email;

    private URL url;
    
    private String gender; 
    
    private Integer age; 

    /**
     * The find property is an static property that facilitates database query creation
     */
    public static Finder<Long, NonMemberAuthor> find = new Finder<>(NonMemberAuthor.class);

    /*
     * Basic Data operations
     */
    public static NonMemberAuthor read(Long themeId) {
        return find.ref(themeId);
    }

    public static List<NonMemberAuthor> findAll() {
        return find.all();
    }

    public static NonMemberAuthor create(NonMemberAuthor author) {
        author.save();
        author.refresh();
        return author;
    }

    public static NonMemberAuthor createObject(NonMemberAuthor author) {
        author.save();
        return author;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}
}
