
package models.services;

import java.util.List;
import com.avaje.ebean.Model;

public class ServiceResourceCollection extends Model {
	private List<ServiceResource> serviceResources;

	public List<ServiceResource> getServices() {
		return serviceResources;
	}

	public void setServiceResources(List<ServiceResource> serviceResources) {
		this.serviceResources = serviceResources;
	}
}