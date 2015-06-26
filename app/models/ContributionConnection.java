package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import enums.ContributionConnectionStatuses;
import enums.ContributionConnectionTypes;

@Entity
public class ContributionConnection extends AppCivistBaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -926173390197852051L;

	@Id
	@GeneratedValue
	private Long contributionConnectionId;
	private ContributionConnectionTypes type;
	private ContributionConnectionStatuses status = ContributionConnectionStatuses.NEW; 
	private User author;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private Contribution sourceContribution;

	@ManyToOne(cascade=CascadeType.ALL)
	private Contribution targetContribution;
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, ContributionConnection> find = new Finder<Long, ContributionConnection>(
			Long.class, ContributionConnection.class);

	public ContributionConnection(ContributionConnectionTypes type,
			Contribution source, Contribution target) {
		super();
		this.type = type;
		this.sourceContribution = source;
		this.targetContribution = target;
	}

	public ContributionConnection() {
		super();
	}

	/*
	 * Getters and Setters
	 */

	public Long getContributionConnectionId() {
		return contributionConnectionId;
	}

	public void setContributionConnectionId(Long contributionConnectionId) {
		this.contributionConnectionId = contributionConnectionId;
	}

	public ContributionConnectionTypes getType() {
		return type;
	}

	public void setType(ContributionConnectionTypes type) {
		this.type = type;
	}

	public ContributionConnectionStatuses getStatus() {
		return status;
	}

	public void setStatus(ContributionConnectionStatuses status) {
		this.status = status;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User creator) {
		this.author = creator;
	}

	public Contribution getSourceContribution() {
		return sourceContribution;
	}

	public void setSourceContribution(Contribution sourceContribution) {
		this.sourceContribution = sourceContribution;
	}

	public Contribution getTargetContribution() {
		return targetContribution;
	}

	public void setTargetContribution(Contribution targetContribution) {
		this.targetContribution = targetContribution;
	}
	
	/*
	 * Basic Data operations
	 */

	public static ContributionConnection read(Long id) {
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
