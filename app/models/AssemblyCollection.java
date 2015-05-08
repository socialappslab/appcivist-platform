package models;

import java.util.*;

import play.db.ebean.*;

public class AssemblyCollection extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7213623744477425962L;
	private List<Assembly> assemblies;

	public List<Assembly> getAssemblies() {
		return assemblies;
	}

	public void setAssemblies(List<Assembly> assemblies) {
		this.assemblies = assemblies;
	}	
	
	
}
