package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import models.misc.Views;
import play.Logger;
import utils.GlobalData;

import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;

import enums.BallotStatus;
import enums.VotesLimitMeanings;
import enums.VotingSystemTypes;

@Entity(name="ballot")
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="Ballot", description="Represents a ballot used for voting on AppCivist Core resources (contributions, users, working groups, themes, etc.)")
public class Ballot extends Model {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="ballots_id_seq")
	@Index
    private Long id;
	@JsonView(Views.Public.class)
    private UUID uuid = UUID.randomUUID();
    @Transient
    private String uuidAsString;
	private String password;
	@JsonView(Views.Public.class)
	@Column(name = "instructions", columnDefinition = "text")
	private String instructions;
	@JsonView(Views.Public.class)
	@Column(name = "notes", columnDefinition = "text")
	private String notes;
	@JsonView(Views.Public.class)
	@Enumerated(EnumType.STRING)
	private VotingSystemTypes votingSystemType;
	@JsonView(Views.Public.class)
	@Enumerated(EnumType.ORDINAL)
	private BallotStatus status = BallotStatus.ACTIVE;
	private Boolean requireRegistration = true;
    private Boolean userUuidAsSignature = false;
	@JsonView(Views.Public.class)
    private String decisionType = "BINDING";
	@JsonView(Views.Public.class)
	@Column(name = "entity_type")
	private String entityType;
    @ManyToOne
    @JoinColumn(name="component")
    @JsonIgnore 
    private Component component;
    @Transient
    private Long componenId;
	@JsonView(Views.Public.class)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date startsAt = Calendar.getInstance().getTime();
	@JsonView(Views.Public.class)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date endsAt = Calendar.getInstance().getTime();
	@JsonView(Views.Public.class)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date createdAt = Calendar.getInstance().getTime();	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date updatedAt = Calendar.getInstance().getTime();	
	private Boolean removed = false;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date removedAt;
	@JsonView(Views.Public.class)
	@Column(name = "votes_limit")
    @ApiModelProperty(value="The limit on the votes a voter can give on this ballot")
	private String votesLimit; 
	@JsonView(Views.Public.class)
	@Column(name = "votes_limit_meaning")
	@Enumerated(EnumType.STRING)
    @ApiModelProperty(value="Meaning of the limit on the votes")
	private VotesLimitMeanings votesLimitMeaning;

    /**
	 * The find property is an static property that facilitates database query creation
	 */
    public static Finder<Long, Ballot> find = new Finder<>(Ballot.class);

    /*
	 * Basic Data operations
	 */
    public static Ballot read(Long ballotId) {
        return find.ref(ballotId);
    }

	public static Ballot findBySpace(Long ballotId) {
		return find.ref(ballotId);
	}

    public static List<Ballot> findAll() {
        return find.all();
    }

    public static Ballot create(Ballot ballot) {
        ballot.save();
        ballot.refresh();
        return ballot;
    }

    public static Ballot createObject(Ballot ballot) {
		ballot.save();
        return ballot;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }

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
	
	public void setUuidAsString(String uuid) {
		this.uuidAsString = uuid;
		this.uuid = UUID.fromString(uuid);
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
	
	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}
	
	public Long getComponentId() {
		return this.component != null ? component.getComponentId() : null;
	}

	public void setComponentId(Long componentId) {
		this.component = Component.read(componentId);
	}

	public Date getStartsAt() {
		return startsAt;
	}

	public void setStartsAt(Date d) {
		this.startsAt = d;
	}
	
	public Date getEndsAt() {
		return endsAt;
	}

	public void setEndsAt(Date d) {
		this.endsAt = d;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Boolean getRemoved() {
		return removed;
	}

	public void setRemoved(Boolean removed) {
		this.removed = removed;
	}

	public Date getRemovedAt() {
		return removedAt;
	}

	public void setRemovedAt(Date removedAt) {
		this.removedAt = removedAt;
	}

	public BallotStatus getStatus() {
		return status;
	}

	public void setStatus(BallotStatus status) {
		this.status = status;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getVotesLimit() {
		return votesLimit;
	}

	public void setVotesLimit(String votesLimit) {
		this.votesLimit = votesLimit;
	}

	public VotesLimitMeanings getVotesLimitMeaning() {
		return votesLimitMeaning;
	}

	public void setVotesLimitMeaning(VotesLimitMeanings votesLimitMeaning) {
		this.votesLimitMeaning = votesLimitMeaning;
	}

	public static Ballot findByUUID(UUID uuid) {
		return find.where().eq("uuid", uuid).findUnique();
	}

	@JsonIgnore
	public List<BallotCandidate> getBallotCandidates(){
		Finder<Long, BallotCandidate> ballotCandidateFinder = new Finder<>(
				BallotCandidate.class);
		return ballotCandidateFinder.where().eq("ballotId", this.id).findList();
	}

	public static Ballot createConsensusBallotForWorkingGroup(WorkingGroup workingGroup){
		ResourceSpace groupResources = workingGroup.getResources();
		Ballot consensusBallot = new Ballot();
		Date startBallot = Calendar.getInstance().getTime();
		// TODO: add the due date for reaching consensus in the WG creation form
		//       by default, the groups gets 30 days for reaching consensus
		Calendar cal = Calendar.getInstance();
		cal.setTime(startBallot);
		cal.add(Calendar.DATE, 30);
		Date endBallot = cal.getTime();

		Logger.info("Creating consensus ballot for Working Group: " + workingGroup.getName());
		consensusBallot.setStartsAt(startBallot);
		consensusBallot.setEndsAt(endBallot);
		consensusBallot.setPassword(workingGroup.getUuid().toString());
		consensusBallot.setVotingSystemType(VotingSystemTypes.PLURALITY);
		consensusBallot.setRequireRegistration(false);
		consensusBallot.setUserUuidAsSignature(true);
		consensusBallot.setDecisionType("BINDING");
		consensusBallot.save();
		consensusBallot.refresh();

		workingGroup.setConsensusBallotAsString(consensusBallot.getUuid().toString());
		groupResources.addBallot(consensusBallot);

		// TODO: figure out why updates trigger inserts in the resource space
		// for resources that already exist
		workingGroup.update();
		groupResources.update();

		// Add Ballot configurations

		// VOTING SYSTEM
		BallotConfiguration ballotConfig = new BallotConfiguration();
		ballotConfig.setBallotId(consensusBallot.getId());
		ballotConfig.setKey(GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM);
		ballotConfig.setValue("PLURALITY");
		ballotConfig.save();

		// VOTING SYSTEM BLOCK THRESHOLD
		ballotConfig = new BallotConfiguration();
		ballotConfig.setBallotId(consensusBallot.getId());
		ballotConfig.setKey(GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM_PLURALITY_TYPE);
		if (workingGroup.getBlockMajority()!=null && workingGroup.getBlockMajority() ) {
			ballotConfig.setValue("YES/NO/ABSTAIN/BLOCK");
			ballotConfig.save();
			ballotConfig = new BallotConfiguration();
			ballotConfig.setBallotId(consensusBallot.getId());
			ballotConfig
					.setKey(GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM_PLURALITY_BLOCK_THRESHOLD);
			ballotConfig
					.setValue(workingGroup.getMajorityThreshold() != null ? workingGroup
							.getMajorityThreshold() : "SIMPLE");
			ballotConfig.save();
		} else {
			ballotConfig.setValue("YES/NO/ABSTAIN");
			ballotConfig.save();
		}

		return consensusBallot;

	}

	public static Ballot createConsensusBallotForResourceSpace(ResourceSpace resourceSpace,List<BallotConfiguration> ballotConfigs){
		Ballot consensusBallot = new Ballot();
		Date startBallot = Calendar.getInstance().getTime();
		// TODO: add the due date for reaching consensus in the WG creation form
		//       by default, the groups gets 30 days for reaching consensus
		Calendar cal = Calendar.getInstance();
		cal.setTime(startBallot);
		cal.add(Calendar.DATE, 30);
		Date endBallot = cal.getTime();

		Logger.info("Creating consensus ballot for Resource Space: " + resourceSpace.getName());
		consensusBallot.setStartsAt(startBallot);
		consensusBallot.setEndsAt(endBallot);
		consensusBallot.setPassword(resourceSpace.getUuid().toString());
		consensusBallot.setVotingSystemType(VotingSystemTypes.PLURALITY);
		consensusBallot.setRequireRegistration(false);
		consensusBallot.setUserUuidAsSignature(true);
		consensusBallot.setDecisionType("BINDING");
		consensusBallot.save();
		consensusBallot.refresh();

		resourceSpace.addBallot(consensusBallot);

		// TODO: figure out why updates trigger inserts in the resource space
		// for resources that already exist
		resourceSpace.setConsensusBallot(consensusBallot.getUuid());
		resourceSpace.update();

		// Add Ballot configurations
		for (BallotConfiguration ballotConfig: ballotConfigs
			 ) {
			ballotConfig.setBallotId(consensusBallot.getId());
			ballotConfig.save();
		}

		return consensusBallot;

	}

	public static List<Ballot> getBalltoByDateStart(Date startDate, Date endDate){

		Query<Ballot> q = find.where().between("startsAt",startDate,endDate).query();
		List<Ballot> membs = q.findList();
		return membs;

	}

	public static List<Ballot> getBalltoByDateEnd(Date startDate, Date endDate){

		Query<Ballot> q = find.where().between("endsAt",startDate,endDate).query();
		List<Ballot> membs = q.findList();
		return membs;

	}
}
