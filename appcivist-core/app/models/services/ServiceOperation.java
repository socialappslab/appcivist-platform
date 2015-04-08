package models.services;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;

@Entity
public class ServiceOperation extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1132649868643820086L;

	@Id
	private Long serviceOperationId;
	private String appCivistOperation; // TODO: replace with Enum or Class

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "operation_definition_id")
	private ServiceOperationDefinition definition;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "service_service_id")
	private Service service;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="serviceOperation")
	private List<ServiceParameter> parameters = new ArrayList<ServiceParameter>();

	// TODO: maybe is better not having resources attached to the operations that either 
	// 		 created them or will use them, attaching only resources to services 
//	@OneToMany(cascade=CascadeType.ALL, mappedBy="serviceResource")
//	private List<ServiceResource> resources = new ArrayList<ServiceResource>();
	/* 
	 * Basic Data Queries
	 */
	
	public static Model.Finder<Long, ServiceOperation> find = new Model.Finder<Long, ServiceOperation>(
			Long.class, ServiceOperation.class);

	public static ServiceOperationCollection findAll() {
		List<ServiceOperation> serviceOperations = find.all();
		ServiceOperationCollection serviceOperationCollection = new ServiceOperationCollection();
		serviceOperationCollection.setServiceOperations(serviceOperations);
		return serviceOperationCollection;
	}

	public static void create(ServiceOperation serviceOperation) {
		serviceOperation.save();
		serviceOperation.refresh();
	}

	public static ServiceOperation read(Long serviceOperationId) {
		return find.ref(serviceOperationId);
	}

	public static ServiceOperation createObject(
			ServiceOperation serviceOperation) {
		serviceOperation.save();
		return serviceOperation;
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

	public Long getServiceOperationId() {
		return serviceOperationId;
	}

	public void setServiceOperationId(Long id) {
		this.serviceOperationId = id;
	}

	public String getAppCivistOperation() {
		return appCivistOperation;
	}

	public void setAppCivistOperation(String appCivistOperation) {
		this.appCivistOperation = appCivistOperation;
	}

	public ServiceOperationDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ServiceOperationDefinition definition) {
		this.definition = definition;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}
	
	public List<ServiceParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ServiceParameter> parameters) {
		this.parameters = parameters;
	}
	
	public void addParameter(ServiceParameter parameter) {
		this.parameters.add(parameter);
	}

//	public List<ServiceResource> getResources() {
//		return resources;
//	}
//
//	public void setResources(List<ServiceResource> resources) {
//		this.resources = resources;
//	}

	/*
	 * Other Queries
	 */
	
	/**
	 * Obtain the operation oid of service sid, part of assembly aid
	 * 
	 * @param aid
	 * @param sid
	 * @param oid
	 * @return
	 */
	public static ServiceOperation readOperationOfService(Long aid, Long sid, Long oid) {
		// TODO for simplification, first version of models has all entities to have an 
		// 		unique id, change this to have relative ids in the future
			return find.where()
					.eq("serviceOperation.serviceId", sid)
					.eq("serviceOperation.serviceOperationId", oid).findUnique();
		}
}
