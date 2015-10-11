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
public class VotingBallotRegistrationFormField extends AppCivistBaseModel {

	@Id @GeneratedValue
	private Long votingBallotRegistrationFormFieldId;
	private String fieldName; // e.g., Secret Code
	private String fieldDescription; // e.g., Enter the secret code received fromt he Assembly
	private String expectedValue; // TODO: encrypt, for prototype, leave it cleartext
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */	
	public static Finder<Long, VotingBallotRegistrationFormField> find = new Finder<>(VotingBallotRegistrationFormField.class);

	public VotingBallotRegistrationFormField() {
		super();
	}

	/* Basic Data Queries */

	/*
	 * Returns all the voting candidates in our system
	 * 
	 * @return
	 */
	public static List<VotingBallotRegistrationFormField> findAll() {
		return find.all();
	}

	public static void create(VotingBallotRegistrationFormField a) {
		a.save();
		a.refresh();
	}

	public static VotingBallotRegistrationFormField read(Long ballotId) {
		return find.ref(ballotId);
	}

	public static VotingBallotRegistrationFormField createObject(
			VotingBallotRegistrationFormField ballot) {
		ballot.save();
		return ballot;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void softDelete(Long id) {
		VotingBallotRegistrationFormField b = find.ref(id);
		b.setRemoved(true);
		b.setRemoval(new Date());
		b.update();
	}

	public static void softRecovery(Long id) {
		VotingBallotRegistrationFormField b = find.ref(id);
		b.setRemoved(false);
		b.setRemoval(new Date());
		b.update();
	}

	public static VotingBallotRegistrationFormField update(
			VotingBallotRegistrationFormField a) {
		a.update();
		a.refresh();
		return a;
	}

	/* Getters and setters */
	
	public Long getVotingBallotRegistrationFormFieldId() {
		return votingBallotRegistrationFormFieldId;
	}

	public void setVotingBallotRegistrationFormFieldId(
			Long votingBallotRegistrationFormId) {
		this.votingBallotRegistrationFormFieldId = votingBallotRegistrationFormId;
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

	// TODO check entered values against expected ones
	// TODO generate voter signature
}
