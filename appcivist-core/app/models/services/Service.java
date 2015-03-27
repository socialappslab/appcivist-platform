package models.services;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

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

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "serviceAuthenticationId")
	private ServiceAuthentication auth;

	@OneToMany(cascade = CascadeType.ALL)
	private List<ServiceOperation> operations;

	@OneToMany(cascade = CascadeType.ALL)
	private List<ServiceResource> resources;

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

	public ServiceAuthentication getAuth() {
		return auth;
	}

	public void setAuth(ServiceAuthentication auth) {
		this.auth = auth;
	}

	public List<ServiceOperation> getOperations() {
		return operations;
	}

	public void setOperations(List<ServiceOperation> operations) {
		this.operations = operations;
	}

	public List<ServiceResource> getResources() {
		return resources;
	}

	public void setResources(List<ServiceResource> resources) {
		this.resources = resources;
	}
}
