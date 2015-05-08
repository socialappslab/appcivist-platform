package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import enums.ResponseStatus;
import models.services.Service;
import models.services.ServiceResource;
import play.db.ebean.*;
import utils.GlobalData;

@Entity
public class Assembly extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 128881028915968230L;

	@Id
	@GeneratedValue
	private Long  assemblyId;
	private String name;
	private String description;
	private String city;
	private String icon = GlobalData.APPCIVIST_ASSEMBLY_DEFAULT_ICON;
	private String url;
	
//	private String test;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="assembly")
	private List<Issue> issues = new ArrayList<Issue>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy="assembly")
	private List<Service> connectedServices = new ArrayList<Service>();
	
	
	@Transient
	private Map<String, ServiceResource> resourceMappings = new HashMap<String,ServiceResource>();

	@Transient
	private Map<String, Service> operationServiceMappings = new HashMap<String,Service>();
	
//	@OneToMany(cascade = CascadeType.ALL, mappedBy="assembly")
//	private List<OperationServiceMappings> operationServiceMappings = new ArrayList<Service>();
	
	
	
	/*
	 * Basic Data Queries
	 */
	
	public static Model.Finder<Long, Assembly> find = new Model.Finder<Long, Assembly>(
			Long.class, Assembly.class);

	/**
	 * Empty constructor
	 */
	public Assembly() {
		super();
	}
	
	public Assembly(String assemblyTitle, String assemblyDescription,
			String assemblyCity) {
		this.name=assemblyTitle;
		this.description=assemblyDescription;
		this.city=assemblyCity;
	}

	public static AssemblyCollection findAll() {
		List<Assembly> assemblies = find.all();
		AssemblyCollection assemblyCollection = new AssemblyCollection();
		assemblyCollection.setAssemblies(assemblies);
		return assemblyCollection;
	}

	public static void create(Assembly assembly) {
		if (assembly.getAssemblyId()!=null && (assembly.getUrl()==null || assembly.getUrl()=="")) {
			assembly.setUrl(GlobalData.APPCIVIST_ASSEMBLY_BASE_URL+"/"+assembly.getAssemblyId());
		}
		
		assembly.save();
		assembly.refresh();
		
		if (assembly.getUrl()==null || assembly.getUrl()=="") {
			assembly.setUrl(GlobalData.APPCIVIST_ASSEMBLY_BASE_URL+"/"+assembly.getAssemblyId());
		}
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
	
	public Map<String, ServiceResource> getResourceMappings() {
		return resourceMappings;
	}

	public void setResourceMappings(Map<String, ServiceResource> resourceMappings) {
		this.resourceMappings = resourceMappings;
	}

	public void addResourceMappings(String key, ServiceResource value) {
		this.resourceMappings.put(key,value);
	}
	
	public void removeResourceMappings(String key) {
		this.resourceMappings.remove(key);
	}

	public Map<String, Service> getOperationServiceMappings() {
		return operationServiceMappings;
	}
	
	public Service getServiceForOperation(String operationKey) {
		return this.operationServiceMappings.get(operationKey);
	}

	public void setOperationServiceMappings(Map<String, Service> operationServiceMappings) {
		this.operationServiceMappings = operationServiceMappings;
	}

	public void addOperationServiceMapping(String opName, Service service) {
		this.operationServiceMappings.put(opName,service);
	}
	
	public void removeOperationServiceMapping(String opName) {
		this.operationServiceMappings.remove(opName);
	}	

	/*
	 * Other Queries 
	 */

}
