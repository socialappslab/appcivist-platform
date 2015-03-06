package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.ebean.*;

@Entity
public class Assembly extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 128881028915968230L;

	@Id
	private Long  id;
	private String name;
	private String description;
	private String city;
	private String icon;
	private String url;
	
	@OneToMany(cascade = CascadeType.ALL)
	private List<Campaign> campaigns;

	public static Model.Finder<Long, Assembly> find = new Model.Finder<Long, Assembly>(
			Long.class, Assembly.class);

	public static AssemblyCollection findAll() {
		List<Assembly> assemblies = find.all();
		AssemblyCollection assemblyCollection = new AssemblyCollection();
		assemblyCollection.setAssemblies(assemblies);
		return assemblyCollection;
	}

	public static void create(Assembly assembly) {
		assembly.save();
		assembly.refresh();
	}

	public static Assembly read(Long assemblyId) {
		return find.ref(assemblyId);
	}

	public static Assembly createObject(Assembly assembly) {
		assembly.save();
		return assembly;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Campaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(List<Campaign> campaigns) {
		this.campaigns = campaigns;
	}

	

}
