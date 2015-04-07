package models.services;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;

@Entity
public class ServiceParameter extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2192872809975724216L;

	@Id
	private Long serviceParameterId; 
	
	private String value;
	
	@ManyToOne(fetch = FetchType.EAGER)
	//@JoinColumn(referencedColumnName="parameter_definition_id")
	private ServiceParameterDefinition serviceParameter;

	@JsonIgnore
	@ManyToOne
	//@JoinColumn(referencedColumnName="service_resource_id")
	private ServiceResource serviceResource;

	@JsonIgnore
	@ManyToOne
	//@JoinColumn(referencedColumnName="service_operation_id")
	private ServiceOperation serviceOperation;

	
	/*
	 * Basic Data Queries
	 */

	public static Model.Finder<Long, ServiceParameter> find = new Model.Finder<Long, ServiceParameter>(
			Long.class, ServiceParameter.class);

	public static ServiceParameterCollection findAll() {
		List<ServiceParameter> serviceParameters = find.all();
		ServiceParameterCollection serviceParameterCollection = new ServiceParameterCollection();
		serviceParameterCollection.setServiceParameters(serviceParameters);
		return serviceParameterCollection;
	}

	public static void create(ServiceResource serviceResource) {
		serviceResource.save();
		serviceResource.refresh();
	}

	public static ServiceParameter read(Long serviceParameterId) {
		return find.ref(serviceParameterId);
	}

	public static ServiceResource createObject(ServiceResource serviceResource) {
		serviceResource.save();
		return serviceResource;
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
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ServiceParameterDefinition getServiceParameter() {
		return serviceParameter;
	}

	public void setServiceParameter(ServiceParameterDefinition serviceParameter) {
		this.serviceParameter = serviceParameter;
	}

	public Long getServiceParameterId() {
		return serviceParameterId;
	}

	public void setServiceParameterId(Long id) {
		this.serviceParameterId = id;
	}

	public ServiceResource getServiceResource() {
		return serviceResource;
	}

	public void setServiceResource(ServiceResource serviceResource) {
		this.serviceResource = serviceResource;
	}

	public ServiceOperation getServiceOperation() {
		return serviceOperation;
	}

	public void setServiceOperation(ServiceOperation serviceOperation) {
		this.serviceOperation = serviceOperation;
	}

	/*
	 * Other Queries
	 */
}
