package models.services;

import java.util.List;

import com.avaje.ebean.Model;

public class ServiceParameterCollection extends Model {

	private List<ServiceParameter> serviceParameters;

	public List<ServiceParameter> getServiceParameters() {
		return serviceParameters;
	}

	public void setServiceParameters(List<ServiceParameter> serviceParameters) {
		this.serviceParameters = serviceParameters;
	}
}