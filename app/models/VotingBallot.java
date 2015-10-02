package models;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.VotingSystemTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class VotingBallot extends AppCivistBaseModel {

	@Id @GeneratedValue
	private Long votingBallotId;
	@Index
	private UUID uuid = UUID.randomUUID();
	private String uuidAsString;
	private String instructions;
	private String notes;
	@Enumerated(EnumType.STRING)
	private VotingSystemTypes system;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date starts;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date ends;
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<VotingCandidate> candidates;
	
	public static Finder<Long, VotingBallot> find = new Finder<>(
			VotingBallot.class);

	public VotingBallot() {
		super();
	}

	public Long getVotingBallotId() {
		return votingBallotId;
	}

	public void setVotingBallotId(Long votingBallotId) {
		this.votingBallotId = votingBallotId;
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

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public VotingSystemTypes getSystem() {
		return system;
	}

	public void setSystem(VotingSystemTypes system) {
		this.system = system;
	}

	public Date getStarts() {
		return starts;
	}

	public void setStarts(Date start) {
		this.starts = start;
	}

	public Date getEnds() {
		return ends;
	}

	public void setEnds(Date end) {
		this.ends = end;
	}

	public List<VotingCandidate> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<VotingCandidate> candidates) {
		this.candidates = candidates;
	}
}
