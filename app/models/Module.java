package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class Module extends AppCivistBaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1215339626510577704L;
	@Id
	@GeneratedValue
	private Long modId;
	private Boolean enabled = false;
	private String name;
	private User creator;

	@OneToMany(cascade = CascadeType.ALL)
	private List<Config> configs = new ArrayList<Config>();

	public Module(User creator, 
			Boolean enabled, String name) {
		this.creator = creator;
		this.enabled = enabled;
		this.name = name;
	}

	public Module() {
		super();
	}

	public static Model.Finder<Long, Module> find = new Model.Finder<Long, Module>(
			Long.class, Module.class);

	public static Module read(Long moduleId) {
		return find.ref(moduleId);
	}

	public static List<Module> findAll() {
		return find.all();
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
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
}
