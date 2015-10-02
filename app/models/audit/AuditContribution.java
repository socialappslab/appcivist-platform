package models.audit;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import models.Contribution;

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import enums.ContributionTypes;

@Entity 
public class AuditContribution extends AuditAppCivistBaseModel {

	@Id @GeneratedValue
	private Long auditContributionId;
	@Index
	private Long contributionId;
	@Index @JsonIgnore
	private UUID uuid;
	private String title;
	private String text;
	@Enumerated(EnumType.STRING)
	private ContributionTypes type;
		
	/* 
	 * Fields specific to the type ACTION_ITEM
	 */
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date actionDueDate; 
	private Boolean actionDone;
	private String action; 
	
	// Fields Fields specific to the type ASSESSMENT
	private String assessmentSummary;
		
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, AuditContribution> find = new Finder<>(AuditContribution.class);

	public AuditContribution(Contribution c) {
		super(c.getCreation(),c.getLastUpdate(),c.getLang(),c.getRemoval(),c.getRemoved());
		this.contributionId=c.getContributionId();
		this.uuid = c.getUuid();
		this.title = c.getTitle();
		this.text = c.getText();
		this.type = c.getType();
		this.actionDueDate = c.getActionDueDate();
		this.actionDone = c.getActionDone();
		this.action = c.getAction();
		this.assessmentSummary = c.getAssessmentSummary();
	}

	public AuditContribution() {
		super();
	}

	public Long getAuditContributionId() {
		return auditContributionId;
	}

	public void setAuditContributionId(Long auditContributionId) {
		this.auditContributionId = auditContributionId;
	}

	public Long getContributionId() {
		return contributionId;
	}

	public void setContributionId(Long contributionId) {
		this.contributionId = contributionId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public ContributionTypes getType() {
		return type;
	}

	public void setType(ContributionTypes type) {
		this.type = type;
	}

	public Date getActionDueDate() {
		return actionDueDate;
	}

	public void setActionDueDate(Date actionDueDate) {
		this.actionDueDate = actionDueDate;
	}

	public Boolean getActionDone() {
		return actionDone;
	}

	public void setActionDone(Boolean actionDone) {
		this.actionDone = actionDone;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAssessmentSummary() {
		return assessmentSummary;
	}

	public void setAssessmentSummary(String assessmentSummary) {
		this.assessmentSummary = assessmentSummary;
	}	


}
