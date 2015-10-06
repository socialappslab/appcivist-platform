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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.avaje.ebean.Model.Finder;
import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ResourceSpaceTypes;
import enums.VotingSystemTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class VotingBallotRegistrationForm extends AppCivistBaseModel {

	@Id @GeneratedValue
	private Long votingBallotRegistrationFormId;
	@Index
	private UUID uuid = UUID.randomUUID();
	private String uuidAsString;
	
	@OneToMany
	private List<VotingBallotRegistrationFormField> fields;
		
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
