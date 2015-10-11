package models.services;

import java.util.List;

import com.avaje.ebean.Model;

public class ServiceOperationCollection extends Model {
	private List<ServiceOperation> serviceOperations;

	public List<ServiceOperation> getServiceOperations() {
		return serviceOperations;
	}

	public void setServiceOperations(List<ServiceOperation> serviceOperations) {
		this.serviceOperations = serviceOperations;
	}	
}
