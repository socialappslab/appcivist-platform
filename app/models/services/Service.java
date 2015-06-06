package models.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.services.ServiceAssembly;
import play.db.ebean.Model;

@Entity
public class Service extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9219771609664792759L;

	@Id
	private Long serviceId;
	private String name;
	private String baseUrl;

	@OneToMany(cascade=CascadeType.ALL,fetch = FetchType.EAGER)
	private List<ServiceAuthentication> auth;

	@OneToMany(cascade = CascadeType.ALL, mappedBy="service")
	private List<ServiceOperation> operations = new ArrayList<ServiceOperation>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy="service")
	private List<ServiceResource> resources = new ArrayList<ServiceResource>();

	@JsonIgnore
	@ManyToOne
	private ServiceAssembly assembly;

	@ManyToOne
	private ServiceDefinition serviceDefinition;
	
	
	// TODO: implement an entity for storing operationMappings
	@Transient
	private Map<String,String> operationMappings = new HashMap<String,String>();
	
	private Boolean trailingSlash = false;

	/*
	 * Basic Data Queries
	 */
	
	public static Model.Finder<Long, Service> find = new Model.Finder<Long, Service>(
			Long.class, Service.class);

	public static ServiceCollection findAll() {
		List<Service> services = find.all();
		ServiceCollection serviceCollection = new ServiceCollection();
		serviceCollection.setServices(services);
		return serviceCollection;
	}

	public static void create(Service service) {
		service.save();
		service.refresh();
	}

	public static Service read(Long serviceId) {
		return find.ref(serviceId);
	}

	public static Service createObject(Service service) {
		service.save();
		return service;
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
	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long id) {
		this.serviceId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public List<ServiceAuthentication> getAuth() {
		return auth;
	}

	public void setAuth(List<ServiceAuthentication> auth) {
		this.auth = auth;
	}

	public List<ServiceOperation> getOperations() {
		return operations;
	}

	public void setOperations(List<ServiceOperation> operations) {
		this.operations = operations;
	}
	
	public void addServiceOperation(ServiceOperation operation) {
        operation.setService(this);
        operations.add(operation);
    }

    public void removeServiceOperation(ServiceOperation operation) {
        operations.remove(operation);
        if (operation != null) {
            operation.setService(null);
        }
    }

	public List<ServiceResource> getResources() {
		return resources;
	}

	public void setResources(List<ServiceResource> resources) {
		this.resources = resources;
	}
	
	/*
	 * Other Queries
	 */

	public ServiceAssembly getAssembly() {
		return assembly;
	}

	public void setAssembly(ServiceAssembly assembly) {
		this.assembly = assembly;
	}

	public ServiceDefinition getServiceDefinition() {
		return serviceDefinition;
	}

	public void setServiceDefinition(ServiceDefinition serviceDefinition) {
		this.serviceDefinition = serviceDefinition;
	}

	public Map<String,String> getOperationMappings() {
		return operationMappings;
	}

	public void setOperationMappings(Map<String,String> operationMappings) {
		this.operationMappings = operationMappings;
	}
	
	public void addOperationMapping(String key, String value) {
		this.operationMappings.put(key, value);
	}

	public void removeOperationMapping(String key) {
		this.operationMappings.remove(key);
	}
	
	public String getDefinitionKeyForOperation(String key) {
		return this.operationMappings.get(key);
	}

	public Boolean getTrailingSlash() {
		return trailingSlash;
	}

	public void setTrailingSlash(Boolean trailingSlash) {
		this.trailingSlash = trailingSlash;
	}

	/**
	 * Obtain the service sid of assembly aid
	 * 
	 * @param aid
	 * @param sid
	 * @return
	 */
	public static Service readServiceOfAssembly(Long aid, Long sid) {
	// TODO for simplification, first version of models has all entities to have an 
	// 		unique id, change this to have relative ids in the future
		return find.where()
				.eq("assembly_assembly_id", aid) // TODO this is not needed now, but implement relative ids
				.eq("serviceId", sid).findUnique();
	}
	
	
}
