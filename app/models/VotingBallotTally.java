package models;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class VotingBallotTally extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long votingBallotTallyId;
	@Index
	private UUID uuid = UUID.randomUUID();
	private String status = "PARTIAL"; // PARTIAL, FINISHED
	private int numberOfWinners = 3;
	
	@OneToMany(cascade=CascadeType.ALL)
	private Set<VotingCandidateVoteResult> talliedResults = new TreeSet<>();
	
	@OneToOne
	private VotingBallot ballot;

	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, VotingBallotTally> find = new Finder<>(
			VotingBallotTally.class);

	public VotingBallotTally() {
		super();
		this.uuid = UUID.randomUUID();
	}

	/* Basic Data Queries */

	/*
	 * Returns all the voting candidates in our system
	 * 
	 * @return
	 */
	public static List<VotingBallotTally> findAll() {
		return find.all();
	}

	public static void create(VotingBallotTally a) {
		a.save();
		a.refresh();
	}

	public static VotingBallotTally read(Long ballotId) {
		return find.ref(ballotId);
	}

	public static VotingBallotTally createObject(VotingBallotTally ballot) {
		ballot.save();
		return ballot;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void softDelete(Long id) {
		VotingBallotTally b = find.ref(id);
		b.setRemoved(true);
		b.setRemoval(new Date());
		b.update();
	}

	public static void softRecovery(Long id) {
		VotingBallotTally b = find.ref(id);
		b.setRemoved(false);
		b.setRemoval(new Date());
		b.update();
	}

	public static VotingBallotTally update(VotingBallotTally a) {
		a.update();
		a.refresh();
		return a;
	}

	/* Getters and setters */

	public Long getVotingBallotVote() {
		return votingBallotTallyId;
	}

	public void setVotingBallotVote(Long votingBallotVote) {
		this.votingBallotTallyId = votingBallotVote;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public VotingBallot getBallot() {
		return ballot;
	}

	public void setBallot(VotingBallot ballot) {
		this.ballot = ballot;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getNumberOfWinners() {
		return numberOfWinners;
	}

	public void setNumberOfWinners(int numberOfWinners) {
		this.numberOfWinners = numberOfWinners;
	}
}
