package models.services;

import java.util.*;

import play.db.ebean.*;

public class ServiceAssemblyCollection extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7213623744477425962L;
	private List<ServiceAssembly> serviceAssemblies;

	public List<ServiceAssembly> getServiceAssemblies() {
		return serviceAssemblies;
	}

	public void setServiceAssemblies(List<ServiceAssembly> assemblies) {
		this.serviceAssemblies = assemblies;
	}	
	
	
}
