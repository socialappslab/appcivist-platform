package models.services;

import java.util.List;

import models.services.Service;
import play.db.ebean.Model;

public class ServiceCollection extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7213623744477425962L;
	private List<Service> services;

	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}
}
