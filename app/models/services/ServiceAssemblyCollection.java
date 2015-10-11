package models.services;

import java.util.*;

import com.avaje.ebean.Model;

public class ServiceAssemblyCollection extends Model {
	private List<ServiceAssembly> serviceAssemblies;

	public List<ServiceAssembly> getServiceAssemblies() {
		return serviceAssemblies;
	}

	public void setServiceAssemblies(List<ServiceAssembly> assemblies) {
		this.serviceAssemblies = assemblies;
	}	
	
	
}
