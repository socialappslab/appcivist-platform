package models.services;

import java.util.List;

import models.services.Service;
import com.avaje.ebean.Model;

public class ServiceCollection extends Model {
	private List<Service> services;

	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}
}
