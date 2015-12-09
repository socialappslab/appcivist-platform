package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class VotingBallotVote extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long votingBallotVoteId;
	@Index
	private UUID uuid = UUID.randomUUID();
	private String signature; // based on input in the registration form
	private String status = "DRAFT"; // "DRAFT", "FINISHED"

	@OneToMany(cascade=CascadeType.ALL)
	private List<VotingCandidateVote> voteValues = new ArrayList<>();
	
	@ManyToOne
	private VotingBallot ballot;

	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, VotingBallotVote> find = new Finder<>(
			VotingBallotVote.class);

	public VotingBallotVote() {
		super();
		this.uuid = UUID.randomUUID();
	}

	/* Basic Data Queries */

	/*
	 * Returns all the voting candidates in our system
	 * 
	 * @return
	 */
	public static List<VotingBallotVote> findAll() {
		return find.all();
	}

	public static void create(VotingBallotVote a) {
		a.save();
		a.refresh();
	}

	public static VotingBallotVote read(Long votingBallotVoteId) {
		return find.ref(votingBallotVoteId);
	}
	
	public static VotingBallotVote createObject(VotingBallotVote ballotVote) {
		ballotVote.save();
		return ballotVote;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void softDelete(Long id) {
		VotingBallotVote b = find.ref(id);
		b.setRemoved(true);
		b.setRemoval(new Date());
		b.update();
	}

	public static void softRecovery(Long id) {
		VotingBallotVote b = find.ref(id);
		b.setRemoved(false);
		b.setRemoval(new Date());
		b.update();
	}

	public static VotingBallotVote update(VotingBallotVote a) {
		a.update();
		a.refresh();
		return a;
	}

	/* Getters and setters */

	public Long getVotingBallotVoteId() {
		return votingBallotVoteId;
	}

	public void setVotingBallotVoteId(Long votingBallotVote) {
		this.votingBallotVoteId = votingBallotVote;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<VotingCandidateVote> getVoteValues() {
		return voteValues;
	}

	public void setVoteValues(List<VotingCandidateVote> voteValues) {
		this.voteValues = voteValues;
	}
	
	public void addVoteValues(List<VotingCandidateVote> voteValues) {
		this.voteValues.addAll(voteValues);
	}

	public VotingBallot getBallot() {
		return ballot;
	}

	public void setBallot(VotingBallot ballot) {
		this.ballot = ballot;
	}
	
	
	
	public static VotingBallotVote findBySignature(String s) {
		return find.where().eq("signature", s).findUnique();
	}
}
