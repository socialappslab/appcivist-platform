package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.ResourceSpaceTypes;
import enums.VotingSystemTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class VotingBallot extends AppCivistBaseModel {

	@Id @GeneratedValue
	private Long votingBallotId;
	@Index
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;
	private String instructions;
	private String notes;
	private String password; // TODO: encrypt, for prototype, leave it cleartext
	@Enumerated(EnumType.STRING)
	private VotingSystemTypes systemType;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date starts;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date ends;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="ballot")
	@JsonManagedReference
	private List<VotingCandidate> candidates = new ArrayList<>();

	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private List<Config> configs = new ArrayList<>();
	
	@OneToOne(cascade=CascadeType.ALL)
	private VotingBallotRegistrationForm registrationForm;
		
	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonInclude(Include.NON_EMPTY)
	@Where(clause="${ta}.removed=false")
	@JsonIgnore
	private ResourceSpace resources = new ResourceSpace(ResourceSpaceTypes.VOTING_BALLOT);
	
	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "ballots")
	@JsonInclude(Include.NON_EMPTY)
	private List<ResourceSpace> containingSpaces;
	
	/** 
 	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, VotingBallot> find = new Finder<>(VotingBallot.class);

	public VotingBallot() {
		super();
		this.uuid = UUID.randomUUID();
	}

	/* Basic Data Queries */

	/*
 	 * Returns all the assemblies in our system
	 * 
	 * @return
	 */
	public static List<VotingBallot> findAll() {
		return find.all();
	}

	public static void create(VotingBallot a) {
		a.save();
		a.refresh();
	}

	public static VotingBallot read(Long ballotId) {
		return find.ref(ballotId);
	}

	public static VotingBallot createObject(VotingBallot ballot) {
		ballot.save();
		return ballot;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}
	
	public static void softDelete(Long id) {
		VotingBallot b = find.ref(id);
		b.setRemoved(true);
		b.setRemoval(new Date());
		b.update();
	}
	
	public static void softRecovery(Long id) {
		VotingBallot b = find.ref(id);
		b.setRemoved(false);
		b.setRemoval(new Date());
		b.update();
	}
	
	public static VotingBallot update(VotingBallot a) {
		a.update();
		a.refresh();
		return a;
	}

	/* Getters and setters */
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
		return uuid.toString();
	}

	public void setUuidAsString(String uuidAsString) {
		this.uuid = UUID.fromString(uuidAsString);
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

	public VotingSystemTypes getSystemType() {
		return systemType;
	}

	public void setSystemType(VotingSystemTypes system) {
		this.systemType = system;
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
	
	public void addCandidates(List<VotingCandidate> candidates) {
		this.candidates.addAll(candidates);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Config> getConfigs() {
		return resources.getConfigs();
	}

	public void setConfigs(List<Config> configs) {
		this.resources.setConfigs(configs);
	}

	public VotingBallotRegistrationForm getRegistrationForm() {
		return registrationForm;
	}

	public void setRegistrationForm(VotingBallotRegistrationForm registrationForm) {
		this.registrationForm = registrationForm;
	}
	
	/* Other Data Queries */

	/* YADEL: creating a readbyUUID method,ASK:the difference b/n query
	 */
//	public static VotingBallot readByUUID(UUID uuid) {
//		return find.where().eq("uuid", uuid).findUnique();
//	}
	/* 
	* @params uuid, queries database based on uuid
	*/
	public static VotingBallot queryByUUID(UUID uuid) {
		return find.where().eq("uuid", uuid).findUnique();
	}
}
