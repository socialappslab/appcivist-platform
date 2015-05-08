package models.services;

import java.util.List;

import play.db.ebean.Model;

public class ServiceParameterCollection extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2518203490144486212L;

	private List<ServiceParameter> serviceParameters;

	public List<ServiceParameter> getServiceParameters() {
		return serviceParameters;
	}

	public void setServiceParameters(List<ServiceParameter> serviceParameters) {
		this.serviceParameters = serviceParameters;
	}
}