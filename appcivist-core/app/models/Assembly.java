package models;

import play.db.ebean.*;


public class Assembly extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6696204550522424537L;

	private String name;
	private String description;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	
	
}
