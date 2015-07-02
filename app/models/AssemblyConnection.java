package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import enums.AssemblyConnectionTypes;

@Entity
public class AssemblyConnection extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long assemblyConnectionId;
	private AssemblyConnectionTypes type;

	@ManyToOne(cascade=CascadeType.ALL)
	private Assembly sourceAssembly;

	@ManyToOne(cascade=CascadeType.ALL)
	private Assembly targetAssembly;

	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, AssemblyConnection> find = new Finder<Long, AssemblyConnection>(
			Long.class, AssemblyConnection.class);

	public AssemblyConnection(AssemblyConnectionTypes type,
			Assembly sourceAssembly, Assembly targetAssembly) {
		super();
		this.type = type;
		this.sourceAssembly = sourceAssembly;
		this.targetAssembly = targetAssembly;
	}

	public AssemblyConnection() {
		super();
	}

	/*
	 * Getters and Setters
	 */

	public Long getAssemblyConnectionId() {
		return assemblyConnectionId;
	}

	public void setAssemblyConnectionId(Long assemblyConnectionId) {
		this.assemblyConnectionId = assemblyConnectionId;
	}

	public AssemblyConnectionTypes getType() {
		return type;
	}

	public void setType(AssemblyConnectionTypes type) {
		this.type = type;
	}

	public Assembly getSourceAssembly() {
		return sourceAssembly;
	}

	public void setSourceAssembly(Assembly sourceAssembly) {
		this.sourceAssembly = sourceAssembly;
	}

	public Assembly getTargetAssembly() {
		return targetAssembly;
	}

	public void setTargetAssembly(Assembly targetAssembly) {
		this.targetAssembly = targetAssembly;
	}

	public static AssemblyConnection read(Long id) {
		return find.ref(id);
	}

	/*
	 * Basic Data operations
	 */

	public static List<AssemblyConnection> findAll() {
		return find.all();
	}

	public static AssemblyConnection create(AssemblyConnection object) {
		object.save();
		object.refresh();
		return object;
	}

	public static AssemblyConnection createObject(AssemblyConnection object) {
		object.save();
		return object;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}
}
