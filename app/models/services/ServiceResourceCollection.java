package models.services;

import java.util.List;

import play.db.ebean.Model;

public class ServiceResourceCollection extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8515346991912046246L;
	private List<ServiceResource> serviceResources;

	public List<ServiceResource> getServices() {
		return serviceResources;
	}

	public void setServiceResources(List<ServiceResource> serviceResources) {
		this.serviceResources = serviceResources;
	}
}