package models.services;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class ServiceOperationDefinition extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6066143924703318234L;

	@Id
	private Long operationDefinitionId;
	private String name;
	private String type;
	private String method;

	@OneToMany(cascade = CascadeType.ALL)
	private List<ServiceParameterDefinition> parameters;

	/*
	 * Basic Data Queries 
	 */
	
	public static Model.Finder<Long, ServiceOperationDefinition> find = new Model.Finder<Long, ServiceOperationDefinition>(
			Long.class, ServiceOperationDefinition.class);

	public static void create(
			ServiceOperationDefinition serviceOperationDefinition) {
		serviceOperationDefinition.save();
		serviceOperationDefinition.refresh();
	}

	public static ServiceOperationDefinition read(
			Long serviceOperationDefinitionId) {
		return find.ref(serviceOperationDefinitionId);
	}

	public static ServiceOperationDefinition createObject(
			ServiceOperationDefinition serviceOperationDefinition) {
		serviceOperationDefinition.save();
		return serviceOperationDefinition;
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
	public Long getOperationDefinitionId() {
		return operationDefinitionId;
	}

	public void setOperationDefinitionId(Long id) {
		this.operationDefinitionId = id;
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

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public List<ServiceParameterDefinition> getParameters() {
		return parameters;
	}

	public void setParameters(List<ServiceParameterDefinition> parameters) {
		this.parameters = parameters;
	}
	
	/*
	 * Other Queries
	 */

}
