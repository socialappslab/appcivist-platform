package models.services;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;
import utils.GlobalData;

@Entity
public class ServiceAssembly extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 128881028915968230L;

	@Id
	@GeneratedValue
	private Long assemblyId;
	private String name;
	private String description;
	private String city;
	private String icon = GlobalData.APPCIVIST_ASSEMBLY_DEFAULT_ICON;
	private String url;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "assembly")
	private List<ServiceIssue> serviceIssues = new ArrayList<ServiceIssue>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "assembly")
	private List<Service> connectedServices = new ArrayList<Service>();

	@Transient
	private Map<String, ServiceResource> resourceMappings = new HashMap<String, ServiceResource>();

	@Transient
	private Map<String, Service> operationServiceMappings = new HashMap<String, Service>();

	// @OneToMany(cascade = CascadeType.ALL, mappedBy="assembly")
	// private List<OperationServiceMappings> operationServiceMappings = new
	// ArrayList<Service>();

	/*
	 * Basic Data Queries
	 */

	public static Finder<Long, ServiceAssembly> find = new Finder<Long, ServiceAssembly>(
			Long.class, ServiceAssembly.class);

	/**
	 * Empty constructor
	 */
	public ServiceAssembly() {
		super();
	}

	public ServiceAssembly(String assemblyTitle, String assemblyDescription,
			String assemblyCity) {
		this.name = assemblyTitle;
		this.description = assemblyDescription;
		this.city = assemblyCity;
	}

	public ServiceAssembly(String name, String description, String city,
			String icon, String url, Date creation, Date removal,
			String lang, List<ServiceIssue> issues,
			List<Service> connectedServices,
			Map<String, ServiceResource> resourceMappings,
			Map<String, Service> operationServiceMappings) {
		this.name = name;
		this.description = description;
		this.city = city;
		this.icon = icon;
		this.url = url;
		this.serviceIssues = issues;
		this.connectedServices = connectedServices;
		this.resourceMappings = resourceMappings;
		this.operationServiceMappings = operationServiceMappings;
	}

	public static ServiceAssemblyCollection findAll() {
		List<ServiceAssembly> assemblies = find.all();
		ServiceAssemblyCollection assemblyCollection = new ServiceAssemblyCollection();
		assemblyCollection.setServiceAssemblies(assemblies);
		return assemblyCollection;
	}

	public static void create(ServiceAssembly assembly) {
		if (assembly.getAssemblyId() != null
				&& (assembly.getUrl() == null || assembly.getUrl() == "")) {
			assembly.setUrl(GlobalData.APPCIVIST_ASSEMBLY_BASE_URL + "/"
					+ assembly.getAssemblyId());
		}

		assembly.save();
		assembly.refresh();

		if (assembly.getUrl() == null || assembly.getUrl() == "") {
			assembly.setUrl(GlobalData.APPCIVIST_ASSEMBLY_BASE_URL + "/"
					+ assembly.getAssemblyId());
		}
	}

	public static ServiceAssembly read(Long assemblyId) {
		return find.ref(assemblyId);
	}

	public static ServiceAssembly createObject(ServiceAssembly assembly) {
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

	public List<ServiceIssue> getServiceIssues() {
		return serviceIssues;
	}

	public void setServiceIssues(List<ServiceIssue> issues) {
		this.serviceIssues = issues;
	}

	public void addServiceIssue(ServiceIssue i) {
		this.serviceIssues.add(i);
	}

	public void removeServiceIssue(ServiceIssue i) {
		this.serviceIssues.remove(i);
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

	public void setResourceMappings(
			Map<String, ServiceResource> resourceMappings) {
		this.resourceMappings = resourceMappings;
	}

	public void addResourceMappings(String key, ServiceResource value) {
		this.resourceMappings.put(key, value);
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

	public void setOperationServiceMappings(
			Map<String, Service> operationServiceMappings) {
		this.operationServiceMappings = operationServiceMappings;
	}

	public void addOperationServiceMapping(String opName, Service service) {
		this.operationServiceMappings.put(opName, service);
	}

	public void removeOperationServiceMapping(String opName) {
		this.operationServiceMappings.remove(opName);
	}

	/*
	 * Other Queries
	 */

}
