package models;

import java.util.*;

import play.db.ebean.*;

public class AssemblyCollection extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7213623744477425962L;
	private List<Assembly> assemblies;

	List<Assembly> getAssemblies() {
		return assemblies;
	}

	void setAssemblies(List<Assembly> assemblies) {
		this.assemblies = assemblies;
	}
	
	
}
