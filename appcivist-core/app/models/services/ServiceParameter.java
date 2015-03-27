package models.services;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

@Entity
public class ServiceParameter extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2192872809975724216L;

	@Id
	private String value;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id")
	private ServiceParameterDefinition serviceParameter;

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

}
