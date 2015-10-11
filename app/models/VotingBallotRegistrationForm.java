package models;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@Entity(name="voting_ballot_registration_form")
@JsonInclude(Include.NON_EMPTY)
public class VotingBallotRegistrationForm extends AppCivistBaseModel {

	@Id @GeneratedValue
	private Long votingBallotRegistrationFormId;
	@Index
	private UUID uuid = UUID.randomUUID();
	private String uuidAsString;
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<VotingBallotRegistrationField> fields;
		
	/** 
 	 * The find property is an static property that facilitates database query
	 * creation
	 */	
	public static Finder<Long, VotingBallotRegistrationForm> find = new Finder<>(VotingBallotRegistrationForm.class);

	public VotingBallotRegistrationForm() {
		super();
		this.uuid = UUID.randomUUID();
	}

	/* Basic Data Queries */

	/*
 	 * Returns all the voting candidates in our system
	 * 
	 * @return
	 */
	public static List<VotingBallotRegistrationForm> findAll() {
		return find.all();
	}

	public static void create(VotingBallotRegistrationForm a) {
		a.save();
		a.refresh();
	}

	public static VotingBallotRegistrationForm read(Long ballotId) {
		return find.ref(ballotId);
	}

	public static VotingBallotRegistrationForm createObject(VotingBallotRegistrationForm ballot) {
		ballot.save();
		return ballot;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}
	
	public static void softDelete(Long id) {
		VotingBallotRegistrationForm b = find.ref(id);
		b.setRemoved(true);
		b.setRemoval(new Date());
		b.update();
	}
	
	public static void softRecovery(Long id) {
		VotingBallotRegistrationForm b = find.ref(id);
		b.setRemoved(false);
		b.setRemoval(new Date());
		b.update();
	}
	
	public static VotingBallotRegistrationForm update(VotingBallotRegistrationForm a) {
		a.update();
		a.refresh();
		return a;
	}
	
	/* Getters and setters */
	
	public Long getVotingBallotRegistrationFormId() {
		return votingBallotRegistrationFormId;
	}

	public void setVotingBallotRegistrationFormId(
			Long votingBallotRegistrationFormId) {
		this.votingBallotRegistrationFormId = votingBallotRegistrationFormId;
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
	// TODO check entered values against expected ones
	// TODO generate voter signature
}
