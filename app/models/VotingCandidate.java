package models;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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
	
	public static Finder<Long, VotingCandidate> find = new Finder<>(
			VotingCandidate.class);

	public VotingCandidate() {
		super();
	}

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
}
