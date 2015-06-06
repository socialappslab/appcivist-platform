package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.db.ebean.Model;
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
	private Long upVotes;
	private Long downVotes;
	private User creator;
	
	
	// Who is the group that moved or copied this contribution to its Campaign Phase
	@OneToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "contribution_connection_group", 
		joinColumns = 
			@JoinColumn(name = "group_id", referencedColumnName = "group_id"), 
		inverseJoinColumns = 
			@JoinColumn(name = "contribution_connection_id", referencedColumnName = "contribution_connection_id")
	)
	private WorkingGroup ownerGroup;

	@ManyToOne(cascade=CascadeType.ALL)
	private Contribution sourceContribution;

	@ManyToOne(cascade=CascadeType.ALL)
	private Contribution targetContribution;
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Model.Finder<Long, ContributionConnection> find = new Model.Finder<Long, ContributionConnection>(
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

	public Long getUpVotes() {
		return upVotes;
	}

	public void setUpVotes(Long upVotes) {
		this.upVotes = upVotes;
	}

	public Long getDownVotes() {
		return downVotes;
	}

	public void setDownVotes(Long downVotes) {
		this.downVotes = downVotes;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public WorkingGroup getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(WorkingGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
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

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}
}
