package models.services;

import java.util.List;

import com.avaje.ebean.Model;

public class ServiceParameterDefinitionCollection extends Model {

	private List<ServiceParameterDefinition> serviceParameterDefinitions;

	public List<ServiceParameterDefinition> getServiceParameterDefinitions() {
		return serviceParameterDefinitions;
	}

	public void setServiceParameterDefinitions(
			List<ServiceParameterDefinition> serviceParameterDefinitions) {
		this.serviceParameterDefinitions = serviceParameterDefinitions;
	}
}