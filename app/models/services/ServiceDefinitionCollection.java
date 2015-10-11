package models.services;

import java.util.List;

import models.services.ServiceDefinition;
import com.avaje.ebean.Model;

public class ServiceDefinitionCollection extends Model {
	private List<ServiceDefinition> serviceDefinitions;

	public List<ServiceDefinition> getServiceDefinitions() {
		return serviceDefinitions;
	}

	public void setServiceDefinitions(List<ServiceDefinition> serviceDefinitions) {
		this.serviceDefinitions = serviceDefinitions;
	}
}
