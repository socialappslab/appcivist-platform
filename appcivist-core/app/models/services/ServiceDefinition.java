package models.services;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;

@Entity
public class ServiceDefinition extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9219771609664792759L;

	@Id
	private Long serviceDefinitionId;
	private String name;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL)
	private List<Service> services = new ArrayList<Service>();
	
	@OneToMany(cascade = CascadeType.ALL)
	private List<ServiceOperationDefinition> operations = new ArrayList<ServiceOperationDefinition>();
	
	public static Model.Finder<Long, ServiceDefinition> find = new Model.Finder<Long, ServiceDefinition>(
			Long.class, ServiceDefinition.class);
	
	public static ServiceDefinitionCollection findAll() {
		List<ServiceDefinition> servicesDefinitions = find.all();
		ServiceDefinitionCollection serviceDefinitionCollection = new ServiceDefinitionCollection();
		serviceDefinitionCollection.setServiceDefinitions(servicesDefinitions);
		return serviceDefinitionCollection;
	}

	public static void create(ServiceDefinition service) {
		service.save();
		service.refresh();
	}

	public static ServiceDefinition read(Long serviceId) {
		return find.ref(serviceId);
	}

	public static ServiceDefinition createObject(ServiceDefinition service) {
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
	public Long getServiceDefinitionId() {
		return serviceDefinitionId;
	}

	public void setServiceDefinitionId(Long id) {
		this.serviceDefinitionId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ServiceOperationDefinition> getOperations() {
		return operations;
	}

	public void setOperations(List<ServiceOperationDefinition> operations) {
		this.operations = operations;
	}
	
	public void addOperation(ServiceOperationDefinition operation) {
        operation.setServiceDefinition(this);
        operations.add(operation);
    }

    public void removeServiceOperationDefinition(ServiceOperationDefinition operation) {
        operations.remove(operation);
        if (operation != null) {
            operation.setServiceDefinition(null);
        }
    }

	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}
	
	/*
	 * Other Queries
	 */

}
