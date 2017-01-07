package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

import models.misc.Views;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;


import utils.GlobalData;

@MappedSuperclass
@Where(clause="removed=false")
@ApiModel(value="AppCivistBaseModel", description="AppCivist base data model")
public class AppCivistBaseModel extends Model {

	/** 
	 * Properties that are common to all the entities in the model
	 * 
	 */
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	@ApiModelProperty(name="creation", value="Date in which this resource was created", notes="By default set to NOW")
	@JsonView(Views.Public.class)
	private Date creation = new Date(); // by Default, the creation is NOW
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	@ApiModelProperty(name="lastUpdate", value="Date in which this resource was last updated", notes="By default set to NOW")
	private Date lastUpdate = new Date(); // by Default, the creation is NOW
	
	@ApiModelProperty(name="lang", value="Language of the content in this resource", notes="By default set to en-US")
	private String lang = GlobalData.DEFAULT_LANGUAGE; // defaults language to English 
													 // TODO get the language automatically from 
													 // from the requests that creates it
	
	// Fields to implement 'soft' deletion 
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	@ApiModelProperty(name="removal", value="Date in which this resource was removed")
	private Date removal = null;
	@Column(name="removed")
	@ApiModelProperty(name="removed", value="Indicates if this resource is logically deleted")
	private Boolean removed = false;
			
	@Transient
	@ApiModelProperty(hidden=true, readOnly=true)
	private Long contextUserId;
	/**
	 * Empty constructor
	 */
	public AppCivistBaseModel() {
		super();
	}
	
	public AppCivistBaseModel(Date creation, String lang) {
		super();
		this.creation = creation;
		this.lang = lang;
	}

	public AppCivistBaseModel(Date creation, Date lastUpdate, String lang,
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
	public AppCivistBaseModel(String lang) {
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
	
	public Long getContextUserId() {
		return contextUserId;
	}

	public void setContextUserId(Long contextUserId) {
		this.contextUserId = contextUserId;
	}

	public void updateNow() {
		this.lastUpdate = new Date();
	}
	
	public void softRemove() {
		this.removal = new Date();
		this.removed = true;
		this.save();
	}
	
	public static void softRemove(AppCivistBaseModel baseAppcivistObject) {
		baseAppcivistObject.removal = new Date();
		baseAppcivistObject.removed = true;
		baseAppcivistObject.save();
	}

	@PreUpdate
	public void onUpdate() {
		this.lastUpdate = new Date();
	}
}
