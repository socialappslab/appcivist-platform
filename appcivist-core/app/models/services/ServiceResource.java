package models.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	private String body; // TODO how to store datamodel

	@OneToMany(cascade = CascadeType.ALL, mappedBy="serviceResource")
	private List<ServiceParameter> parameters;
	
	@JsonIgnore
	@ManyToOne
	private Service service;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="parentResource") 
	private List<ServiceResource> relatedResources;
	
	@Transient
	private Map<String, ServiceResource> relatedResourcesMap = new HashMap<String, ServiceResource>();
	

	@JsonIgnore
	@ManyToOne
	private ServiceResource parentResource;

//	@JsonIgnore
//	@ManyToOne
//	private ServiceOperation linkedOperation;

	
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

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<ServiceResource> getRelatedResources() {
		return relatedResources;
	}

	public void setRelatedResources(List<ServiceResource> relatedResources) {
		this.relatedResources = relatedResources;
	}
	
	public void addRelatedResource(ServiceResource relatedResource) {
		this.relatedResources.add(relatedResource);
		this.relatedResourcesMap.put(relatedResource.getType(),relatedResource);
	}

	public Map<String, ServiceResource> getRelatedResourcesMap() {
		return relatedResourcesMap;
	}

	public void setRelatedResourcesMap(
			Map<String, ServiceResource> relatedResourcesMap) {
		this.relatedResourcesMap = relatedResourcesMap;
	}

	public ServiceResource getParentResource() {
		return parentResource;
	}

	public void setParentResource(ServiceResource parentResource) {
		this.parentResource = parentResource;
	}
	
	
	/*
	 * Other Queries
	 */
}
