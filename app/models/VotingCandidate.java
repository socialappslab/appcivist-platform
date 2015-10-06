package models;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;

import enums.AppcivistResourceTypes;

@Entity
public class VotingCandidate extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long votingCandidateId;
	private UUID uuid = UUID.randomUUID();
	private String uuidAsString;
	private UUID targetUuid;
	private AppcivistResourceTypes candidateType = AppcivistResourceTypes.CONTRIBUTION_PROPOSAL;

	@ManyToOne(cascade=CascadeType.ALL)
	@JsonBackReference
	private VotingBallot ballot;
	
	/** 
 	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, VotingCandidate> find = new Finder<>(VotingCandidate.class);

	public VotingCandidate() {
		super();
		this.uuid = UUID.randomUUID();
	}

	/* Basic Data Queries */

	/*
 	 * Returns all the voting candidates in our system
	 * 
	 * @return
	 */
	public static List<VotingCandidate> findAll() {
		return find.all();
	}

	public static void create(VotingCandidate a) {
		a.save();
		a.refresh();
	}

	public static VotingCandidate read(Long ballotId) {
		return find.ref(ballotId);
	}

	public static VotingCandidate createObject(VotingCandidate ballot) {
		ballot.save();
		return ballot;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}
	
	public static void softDelete(Long id) {
		VotingCandidate b = find.ref(id);
		b.setRemoved(true);
		b.setRemoval(new Date());
		b.update();
	}
	
	public static void softRecovery(Long id) {
		VotingCandidate b = find.ref(id);
		b.setRemoved(false);
		b.setRemoval(new Date());
		b.update();
	}
	
	public static VotingCandidate update(VotingCandidate a) {
		a.update();
		a.refresh();
		return a;
	}

	
	/* Getters and setters */
	
	public Long getVotingCandidateId() {
		return votingCandidateId;
	}

	public void setVotingCandidateId(Long votingCandidateId) {
		this.votingCandidateId = votingCandidateId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getUuidAsString() {
		return uuidAsString;
	}

	public void setUuidAsString(String uuidAsString) {
		this.uuidAsString = uuidAsString;
	}

	public UUID getTargetUuid() {
		return targetUuid;
	}

	public void setTargetUuid(UUID targetUuid) {
		this.targetUuid = targetUuid;
	}

	public AppcivistResourceTypes getCandidateType() {
		return candidateType;
	}

	public void setCandidateType(AppcivistResourceTypes candidateType) {
		this.candidateType = candidateType;
	}
	
	/* Other queries */

	public static List<VotingCandidate> findByBallot(Long ballotId) {
		return find.where().eq("ballot.votingBallotId", ballotId).findList();
	}

}
