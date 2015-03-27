package models.services;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class ServiceResource extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3024829103843292751L;

	@Id
	private Long id;
	private String url;
	private String type; // TODO use enum
	private String keyValue;
	private String keyName;

	@OneToMany(cascade = CascadeType.ALL)
	private List<ServiceParameter> parameters;

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

}
