package models.audit;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

import enums.AuditEventTypes;

// TODO replace this auditing with History annotations http://ebean-orm.github.io/docs/history

@MappedSuperclass
public class AuditAppCivistBaseModel extends Model {

	/** 
	 * Properties that are common to all the entities in the model
	 * 
	 */
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date creation; // by Default, the creation is NOW
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date lastUpdate; // by Default, the creation is NOW
	private String lang; // defaults language to English 
													 // TODO get the language automatically from 
													 // from the requests that creates it
	
	// Fields to implement 'soft' deletion 
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date removal;
	@Column(name="removed")
	private Boolean removed;
			
	
	// auditing fields
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date auditDate = new Date();
	@Enumerated(EnumType.STRING)
	private AuditEventTypes auditEvent;
	private Long auditUserId;
	
	/**
	 * Empty constructor
	 */
	public AuditAppCivistBaseModel() {
		super();
	}
	
	public AuditAppCivistBaseModel(Date creation, String lang) {
		super();
		this.creation = creation;
		this.lang = lang;
	}
	
	

	public AuditAppCivistBaseModel(Date creation, Date lastUpdate, String lang,
			Date removal, Boolean removed) {
		super();
		this.creation = creation;
		this.lastUpdate = lastUpdate;
		this.lang = lang;
		this.removal = removal;
		this.removed = removed;
	}

	/** 
	 * Constructor with a specific language
	 * @param lang
	 */
	public AuditAppCivistBaseModel(String lang) {
		super();
		this.lang = lang;
	}

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Date getRemoval() {
		return removal;
	}

	public void setRemoval(Date removal) {
		this.removal = removal;
	}

	public Boolean getRemoved() {
		return removed;
	}

	public void setRemoved(Boolean removed) {
		this.removed = removed;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
	
	public Date getAuditDate() {
		return auditDate;
	}

	public void setAuditDate(Date auditDate) {
		this.auditDate = auditDate;
	}

	public AuditEventTypes getAuditEvent() {
		return auditEvent;
	}

	public void setAuditEvent(AuditEventTypes auditEvent) {
		this.auditEvent = auditEvent;
	}

	public Long getAuditUserId() {
		return auditUserId;
	}

	public void setAuditUserId(Long auditUserId) {
		this.auditUserId = auditUserId;
	}

	public void updateNow() {
		this.lastUpdate = new Date();
	}
	
	public void softRemove() {
		this.removal = new Date();
		this.removed = true;
		this.save();
	}
	
	public static void softRemove(AuditAppCivistBaseModel baseAppcivistObject) {
		baseAppcivistObject.removal = new Date();
		baseAppcivistObject.removed = true;
		baseAppcivistObject.save();
	}

	@PreUpdate
	public void onUpdate() {
		this.lastUpdate = new Date();
	}
}
