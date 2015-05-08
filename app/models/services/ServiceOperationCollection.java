package models.services;

import java.util.List;

import play.db.ebean.Model;

public class ServiceOperationCollection extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5619919749952686277L;
	private List<ServiceOperation> serviceOperations;

	public List<ServiceOperation> getServiceOperations() {
		return serviceOperations;
	}

	public void setServiceOperations(List<ServiceOperation> serviceOperations) {
		this.serviceOperations = serviceOperations;
	}	
}
