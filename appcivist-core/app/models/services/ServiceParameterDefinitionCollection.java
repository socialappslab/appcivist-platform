package models.services;

import java.util.List;

import play.db.ebean.Model;

public class ServiceParameterDefinitionCollection extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3871404091472430228L;

	private List<ServiceParameterDefinition> serviceParameterDefinitions;

	public List<ServiceParameterDefinition> getServiceParameterDefinitions() {
		return serviceParameterDefinitions;
	}

	public void setServiceParameterDefinitions(
			List<ServiceParameterDefinition> serviceParameterDefinitions) {
		this.serviceParameterDefinitions = serviceParameterDefinitions;
	}
}