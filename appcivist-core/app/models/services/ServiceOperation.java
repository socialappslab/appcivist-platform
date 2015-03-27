package models.services;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

@Entity
public class ServiceOperation extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1132649868643820086L;

	@Id
	private Long id;
	private String appCivistOperation; // TODO: replace with Enum or Class

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id")
	private ServiceOperationDefinition definition;

	public static Model.Finder<Long, ServiceOperation> find = new Model.Finder<Long, ServiceOperation>(
			Long.class, ServiceOperation.class);

	public static ServiceOperationCollection findAll() {
		List<ServiceOperation> serviceOperations = find.all();
		ServiceOperationCollection serviceOperationCollection = new ServiceOperationCollection();
		serviceOperationCollection.setServiceOperations(serviceOperations);
		return serviceOperationCollection;
	}

	public static void create(ServiceOperation serviceOperation) {
		serviceOperation.save();
		serviceOperation.refresh();
	}

	public static ServiceOperation read(Long serviceOperationId) {
		return find.ref(serviceOperationId);
	}

	public static ServiceOperation createObject(
			ServiceOperation serviceOperation) {
		serviceOperation.save();
		return serviceOperation;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAppCivistOperation() {
		return appCivistOperation;
	}

	public void setAppCivistOperation(String appCivistOperation) {
		this.appCivistOperation = appCivistOperation;
	}

	public ServiceOperationDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ServiceOperationDefinition definition) {
		this.definition = definition;
	}
}
