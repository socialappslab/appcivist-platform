package models.services;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.avaje.ebean.Model;

@Entity
public class ServiceParameterDefinition extends Model {

	@Id
	private Long parameterDefinitionId;
	private String name;
	private String type; // TODO convert in Enum
	private String dataType;

// ElementColleciton is not supported by Ebean, the underlying JPA provider of play :(
//	@ElementCollection
//	@MapKeyColumn(name="data_key")
//	@Column(name="data_annotations")
//	@CollectionTable(name="service_parameter_data_model", joinColumns=@JoinColumn(name="data_model_id"))
//	private Map<String,String> dataModel = new HashMap<String,String>();
		
	@OneToMany(cascade=CascadeType.ALL, mappedBy="definition")
	private List<ServiceParameterDataModel> dataModel = new ArrayList<ServiceParameterDataModel>();
	
	private Integer pathOrder = 0; // if this is a path param, when must be included in the url
	
	private String defaultValue; // what to put on the path if is not specified
	
	private Boolean required = false;
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

	public List<ServiceParameterDataModel> getDataModel() {
		return dataModel;
	}

	public void setDataModel(List<ServiceParameterDataModel> dataModel) {
		this.dataModel = dataModel;
	}

	public Integer getPathOrder() {
		return pathOrder;
	}

	public void setPathOrder(Integer pathOrder) {
		this.pathOrder = pathOrder;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

//	public Map<String, String> getDataModel() {
//		return dataModel;
//	}
//
//	public void setDataModel(Map<String, String> dataModel) {
//		this.dataModel = dataModel;
//	}
	
	
	/* 
	 * Other Queries
	 */
}
