package models.services;

import java.util.List;

import models.services.ServiceDefinition;
import play.db.ebean.Model;

public class ServiceDefinitionCollection extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7213623744477425962L;
	private List<ServiceDefinition> serviceDefinitions;

	public List<ServiceDefinition> getServiceDefinitions() {
		return serviceDefinitions;
	}

	public void setServiceDefinitions(List<ServiceDefinition> serviceDefinitions) {
		this.serviceDefinitions = serviceDefinitions;
	}
}
