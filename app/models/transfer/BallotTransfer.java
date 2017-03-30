package models.transfer;

import enums.BallotCandidateTypes;
import enums.BallotStatus;
import enums.VotingSystemTypes;
import models.BallotCandidate;
import models.BallotConfiguration;
import models.Component;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BallotTransfer {
    private Long id;
    private UUID uuid;
    private String password;
    private String instructions;
    private String notes;
    private VotingSystemTypes votingSystemType;
    private BallotStatus status;
    private Boolean requireRegistration;
    private Boolean userUuidAsSignature;
    private String decisionType;
    private String entityType;
    private Component component;
    private Date startsAt;
    private Date endsAt;
    private List<BallotCandidate> ballotCandidates;
    @Enumerated(EnumType.ORDINAL)
    private BallotCandidateTypes candidateType;
    List<BallotConfiguration> ballotConfigs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public VotingSystemTypes getVotingSystemType() {
        return votingSystemType;
    }

    public void setVotingSystemType(VotingSystemTypes votingSystemType) {
        this.votingSystemType = votingSystemType;
    }

    public BallotStatus getStatus() {
        return status;
    }

    public void setStatus(BallotStatus status) {
        this.status = status;
    }

    public Boolean getRequireRegistration() {
        return requireRegistration;
    }

    public void setRequireRegistration(Boolean requireRegistration) {
        this.requireRegistration = requireRegistration;
    }

    public Boolean getUserUuidAsSignature() {
        return userUuidAsSignature;
    }

    public void setUserUuidAsSignature(Boolean userUuidAsSignature) {
        this.userUuidAsSignature = userUuidAsSignature;
    }

    public String getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(String decisionType) {
        this.decisionType = decisionType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public Date getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(Date startsAt) {
        this.startsAt = startsAt;
    }

    public Date getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Date endsAt) {
        this.endsAt = endsAt;
    }

    public List<BallotCandidate> getBallotCandidates() {
        return ballotCandidates;
    }

    public void setBallotCandidates(List<BallotCandidate> ballotCandidates) {
        this.ballotCandidates = ballotCandidates;
    }

    public BallotCandidateTypes getCandidateType() {
        return candidateType;
    }

    public void setCandidateType(BallotCandidateTypes candidateType) {
        this.candidateType = candidateType;
    }

    public List<BallotConfiguration> getBallotConfigs() {
        return ballotConfigs;
    }

    public void setBallotConfigs(List<BallotConfiguration> ballotConfigs) {
        this.ballotConfigs = ballotConfigs;
    }
}
