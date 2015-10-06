package models;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class VotingCandidateVoteResult extends AppCivistBaseModel {

	@Id @GeneratedValue
	private Long votingCandidateVoteId;
	@Index
	private UUID uuid = UUID.randomUUID();
	
	@OneToOne
	private VotingCandidate selectedCandidate;
	
	private String voteValue; // "80/100"
	private String voteValueType; // "RANGE"
	
	/** 
 	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, VotingCandidateVoteResult> find = new Finder<>(VotingCandidateVoteResult.class);

	public VotingCandidateVoteResult() {
		super();
		this.uuid = UUID.randomUUID();
	}

	/* Basic Data Queries */

	/*
 	 * Returns all the voting candidates in our system
	 * 
	 * @return
	 */
	public static List<VotingCandidateVoteResult> findAll() {
		return find.all();
	}

	public static void create(VotingCandidateVoteResult a) {
		a.save();
		a.refresh();
	}

	public static VotingCandidateVoteResult read(Long ballotId) {
		return find.ref(ballotId);
	}

	public static VotingCandidateVoteResult createObject(VotingCandidateVoteResult ballot) {
		ballot.save();
		return ballot;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}
	
	public static void softDelete(Long id) {
		VotingCandidateVoteResult b = find.ref(id);
		b.setRemoved(true);
		b.setRemoval(new Date());
		b.update();
	}
	
	public static void softRecovery(Long id) {
		VotingCandidateVoteResult b = find.ref(id);
		b.setRemoved(false);
		b.setRemoval(new Date());
		b.update();
	}
	
	public static VotingCandidateVoteResult update(VotingCandidateVoteResult a) {
		a.update();
		a.refresh();
		return a;
	}

	
	/* Getters and setters */
	
	public Long getVotingCandidateVoteId() {
		return votingCandidateVoteId;
	}

	public void setVotingCandidateVoteId(Long votingCandidateVoteId) {
		this.votingCandidateVoteId = votingCandidateVoteId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public VotingCandidate getSelectedCandidate() {
		return selectedCandidate;
	}

	public void setSelectedCandidate(VotingCandidate selectedCandidate) {
		this.selectedCandidate = selectedCandidate;
	}

	public String getVoteValue() {
		return voteValue;
	}

	public void setVoteValue(String voteValue) {
		this.voteValue = voteValue;
	}

	public String getVoteValueType() {
		return voteValueType;
	}

	public void setVoteValueType(String voteValueType) {
		this.voteValueType = voteValueType;
	}

	// TODO check entered values against expected ones
	// TODO generate voter signature
}
