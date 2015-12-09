package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity(name="voting_ballot_registration_field")
@JsonInclude(Include.NON_EMPTY)
public class VotingBallotRegistrationField extends AppCivistBaseModel {

	@Id @GeneratedValue
	private Long votingBallotRegistrationFieldId;
	private String fieldName; // e.g., Secret Code
	private String fieldDescription; // e.g., Enter the secret code received fromt he Assembly
	private String expectedValue; // TODO: encrypt, for prototype, leave it cleartext
	private String value; 
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */	
	public static Finder<Long, VotingBallotRegistrationField> find = new Finder<>(VotingBallotRegistrationField.class);

	public VotingBallotRegistrationField() {
		super();
	}

	/* Basic Data Queries */

	/*
	 * Returns all the voting candidates in our system
	 * 
	 * @return
	 */
	public static List<VotingBallotRegistrationField> findAll() {
		return find.all();
	}

	public static void create(VotingBallotRegistrationField a) {
		a.save();
		a.refresh();
	}

	public static VotingBallotRegistrationField read(Long ballotId) {
		return find.ref(ballotId);
	}

	public static VotingBallotRegistrationField createObject(
			VotingBallotRegistrationField ballot) {
		ballot.save();
		return ballot;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void softDelete(Long id) {
		VotingBallotRegistrationField b = find.ref(id);
		b.setRemoved(true);
		b.setRemoval(new Date());
		b.update();
	}

	public static void softRecovery(Long id) {
		VotingBallotRegistrationField b = find.ref(id);
		b.setRemoved(false);
		b.setRemoval(new Date());
		b.update();
	}

	public static VotingBallotRegistrationField update(
			VotingBallotRegistrationField a) {
		a.update();
		a.refresh();
		return a;
	}

	/* Getters and setters */
	
	public Long getVotingBallotRegistrationFieldId() {
		return votingBallotRegistrationFieldId;
	}

	public void setVotingBallotRegistrationFieldId(
			Long votingBallotRegistrationFormId) {
		this.votingBallotRegistrationFieldId = votingBallotRegistrationFormId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldDescription() {
		return fieldDescription;
	}

	public void setFieldDescription(String fieldDescription) {
		this.fieldDescription = fieldDescription;
	}

	public String getExpectedValue() {
		return expectedValue;
	}

	public void setExpectedValue(String expectedValue) {
		this.expectedValue = expectedValue;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	// TODO check entered values against expected ones
	// TODO generate voter signature
}
