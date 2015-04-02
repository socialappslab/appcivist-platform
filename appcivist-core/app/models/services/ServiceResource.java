package models.services;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.Issue;
import models.User;
import play.db.ebean.Model;

@Entity
public class ServiceResource extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3024829103843292751L;

	@Id
	private Long serviceResourceId;
	private String url;
	private String type; // TODO use enum
	private String keyValue;
	private String keyName;

	@OneToMany(cascade = CascadeType.ALL, mappedBy="serviceResource")
	private List<ServiceParameter> parameters;
	
	@JsonIgnore
	@ManyToOne
	private Service service;
	
	/*
	 * Basic Data Queries
	 */
	
	public static Model.Finder<Long, ServiceResource> find = new Model.Finder<Long, ServiceResource>(
			Long.class, ServiceResource.class);

	public static ServiceResourceCollection findAll() {
		List<ServiceResource> serviceResources = find.all();
		ServiceResourceCollection serviceResourceCollection = new ServiceResourceCollection();
		serviceResourceCollection.setServiceResources(serviceResources);
		return serviceResourceCollection;
	}

	public static void create(ServiceResource serviceResource) {
		serviceResource.save();
		serviceResource.refresh();
	}

	public static ServiceResource read(Long serviceResourceId) {
		return find.ref(serviceResourceId);
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
	
	public Long getServiceResourceId() {
		return serviceResourceId;
	}

	public void setServiceResourceId(Long id) {
		this.serviceResourceId = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<ServiceParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ServiceParameter> parameters) {
		this.parameters = parameters;
	}

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}
	
	/*
	 * Other Queries
	 */
}
