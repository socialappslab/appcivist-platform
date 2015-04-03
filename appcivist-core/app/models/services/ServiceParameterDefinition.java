package models.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class ServiceParameterDefinition extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = -299889021493432330L;

	@Id
	private Long parameterDefinitionId;
	private String name;
	private String type; // TODO convert in Enum
	private String dataType;
	
//	@ManyToMany(cascade = CascadeType.ALL)
	
	// TODO: find a way of mapping this into the database
	private Map<String, String> dataModel = new HashMap<String,String>();
	
//	private String dataModel;

	/*
	 * Basic Data Queries 
	 */
	
	public static Model.Finder<Long, ServiceParameterDefinition> find = new Model.Finder<Long, ServiceParameterDefinition>(
			Long.class, ServiceParameterDefinition.class);

	public static ServiceParameterDefinitionCollection findAll() {
		List<ServiceParameterDefinition> serviceParameterDefinitions = find
				.all();
		ServiceParameterDefinitionCollection serviceParameterDefinitionCollection = new ServiceParameterDefinitionCollection();
		serviceParameterDefinitionCollection
				.setServiceParameterDefinitions(serviceParameterDefinitions);
		return serviceParameterDefinitionCollection;
	}

	public static void create(
			ServiceParameterDefinition serviceParameterDefinition) {
		serviceParameterDefinition.save();
		serviceParameterDefinition.refresh();
	}

	public static ServiceParameterDefinition read(
			Long serviceParameterDefinitionId) {
		return find.ref(serviceParameterDefinitionId);
	}

	public static ServiceParameterDefinition createObject(
			ServiceParameterDefinition serviceParameterDefinition) {
		serviceParameterDefinition.save();
		return serviceParameterDefinition;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	/*
	 * Getters and Setters
	 */
	public Long getParameterDefinitionId() {
		return parameterDefinitionId;
	}

	public void setParameterDefinitionId(Long id) {
		this.parameterDefinitionId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

//	public String getDataModel() {
//		return dataModel;
//	}
//
//	public void setDataModel(String dataModel) {
//		this.dataModel = dataModel;
//	}

	public Map<String, String> getDataModel() {
		return dataModel;
	}

	public void setDataModel(Map<String, String> dataModel) {
		this.dataModel = dataModel;
	}
	
	
	/* 
	 * Other Queries
	 */
}
