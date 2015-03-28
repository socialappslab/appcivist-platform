package models.services;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import models.Campaign;
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
