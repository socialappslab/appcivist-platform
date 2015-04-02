package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import enums.ResponseStatus;
import models.services.Service;
import play.db.ebean.*;

@Entity
public class Assembly extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 128881028915968230L;

	@Id
	private Long  assemblyId;
	private String name;
	private String description;
	private String city;
	private String icon;
	private String url;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="assembly")
	private List<Issue> issues = new ArrayList<Issue>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy="assembly")
	private List<Service> connectedServices = new ArrayList<Service>();
	
	/*
	 * Basic Data Queries
	 */
	
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
	
	/*
	 * Getters and Setters
	 */
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

	public Long getAssemblyId() {
		return assemblyId;
	}

	public void setAssemblyId(Long id) {
		this.assemblyId = id;
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

	public List<Issue> getIssues() {
		return issues;
	}

	public void setIssues(List<Issue> issues) {
		this.issues = issues;
	}
	
	public void addIssue(Issue i) {
		this.issues.add(i);
	}

	public void removeIssue(Issue i) {
		this.issues.remove(i);
	}

	public List<Service> getConnectedServices() {
		return connectedServices;
	}

	public void setConnectedServices(List<Service> connectedServices) {
		this.connectedServices = connectedServices;
	}

	public void addConnectedService(Service s) {
		this.connectedServices.add(s);
	}

	public void removeConnectedService(Service s) {
		this.connectedServices.remove(s);
	}
	/*
	 * Other Queries 
	 */

}
