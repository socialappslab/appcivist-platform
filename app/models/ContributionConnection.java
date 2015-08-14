package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.avaje.ebean.annotation.Formula;

import enums.ContributionConnectionTypes;

@Entity
public class ContributionConnection extends AppCivistBaseModel {

	@Id
	private UUID uuid;
	private ContributionConnectionTypes type;
	private User author;
	private UUID sourceUuid;
	private UUID targetUuid;
	
	@Formula(select="select c from contribution c where c.uuid=${ta}.sourceUuid")
	private Contribution sourceContribution;

	@Formula(select="select c from contribution c where c.uuid=${ta}.targetUuid")
	private Contribution targetContribution;
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<UUID, ContributionConnection> find = new Finder<>(ContributionConnection.class);

	public ContributionConnection(ContributionConnectionTypes type,
			Contribution source, Contribution target) {
		super();
		this.uuid = UUID.randomUUID();
		this.type = type;
//		this.sourceContribution = source;
//		this.targetContribution = target;
	}

	public ContributionConnection() {
		super();
		this.uuid = UUID.randomUUID();
	}

	/*
	 * Getters and Setters
	 */

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID contributionConnectionId) {
		this.uuid = contributionConnectionId;
	}

	public ContributionConnectionTypes getType() {
		return type;
	}

	public void setType(ContributionConnectionTypes type) {
		this.type = type;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User creator) {
		this.author = creator;
	}

	public UUID getSourceUuid() {
		return sourceUuid;
	}

	public void setSourceUuid(UUID sourceUuid) {
		this.sourceUuid = sourceUuid;
	}

	public UUID getTargetUuid() {
		return targetUuid;
	}

	public void setTargetUuid(UUID targetUuid) {
		this.targetUuid = targetUuid;
	}

//	public Contribution getSourceContribution() {
//		return sourceContribution;
//	}

//	public void setSourceContribution(Contribution sourceContribution) {
//		this.sourceContribution = sourceContribution;
//		this.sourceUuid = targetContribution.getUuid();
//	}
//
//	public Contribution getTargetContribution() {
//		return targetContribution;
//	}
//
//	public void setTargetContribution(Contribution targetContribution) {
//		this.targetContribution = targetContribution;
//		this.targetUuid = targetContribution.getUuid();
//	}
	
	/*
	 * Basic Data operations
	 */

	public static ContributionConnection read(UUID id) {
		return find.ref(id);
	}

	public static List<ContributionConnection> findAll() {
		return find.all();
	}

	public static ContributionConnection create(ContributionConnection object) {
		object.save();
		object.refresh();
		return object;
	}

	public static ContributionConnection createObject(ContributionConnection object) {
		object.save();
		return object;
	}

	public static void delete(ContributionConnection cc) {
		cc.delete();
	}

	public static ContributionConnection update(ContributionConnection cc) {
		cc.update();
		cc.refresh();
		return cc;
	}
}
