package models.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;

@Entity
public class ServiceParameterDataModel extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3700830166739070027L;

	@Id
	@GeneratedValue
	private Long dataModelId;
	private String dataKey;
	private String annotations;
	
	@JsonIgnore
	@ManyToOne
	private ServiceParameterDefinition definition;
	
	/*
	 * Basic Data Queries 
	 */	
	public static Model.Finder<Long, ServiceParameterDataModel> find = new Model.Finder<Long, ServiceParameterDataModel>(
			Long.class, ServiceParameterDataModel.class);

	public static void create(
			ServiceParameterDataModel serviceParameterDataModel) {
		serviceParameterDataModel.save();
		serviceParameterDataModel.refresh();
	}
	
	public static ServiceParameterDataModel read(
			Long id) {
		return find.ref(id);
	}	

	public static ServiceParameterDataModel createObject(
			ServiceParameterDataModel serviceParameterDataModel) {
		serviceParameterDataModel.save();
		return serviceParameterDataModel;
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
	public Long getDataModelId() {
		return dataModelId;
	}

	public void setDataModelId(Long dataModelId) {
		this.dataModelId = dataModelId;
	}

	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getAnnotations() {
		return annotations;
	}

	public void setAnnotations(String annotations) {
		this.annotations = annotations;
	}

	public ServiceParameterDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ServiceParameterDefinition definition) {
		this.definition = definition;
	}
	
	/* 
	 * Other Queries
	 */
}
