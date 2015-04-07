package models.services;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;

@Entity
public class ServiceAuthentication extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6076927164603033420L;

	@Id
	private Long serviceAuthenticationId;
	private String authType; // TODO: replace with Enum
	@Column(length=2048) 	// TODO: find a way of making token length unlimited
	private String token;
	private String tokenInjection; // TODO: replace with Enum
	private String tokenParamName;
//	private String test;

	@JsonIgnore
	@ManyToOne
	private Service service;

	/*
	 * Basic Data Queries
	 */
	
	public static Model.Finder<Long, ServiceAuthentication> find = new Model.Finder<Long, ServiceAuthentication>(
			Long.class, ServiceAuthentication.class);

	public static void create(ServiceAuthentication serviceAuth) {
		serviceAuth.save();
		serviceAuth.refresh();
	}

	public static ServiceAuthentication read(Long serviceAuthId) {
		return find.ref(serviceAuthId);
	}

	public static ServiceAuthentication createObject(ServiceAuthentication serviceAuth) {
		serviceAuth.save();
		return serviceAuth;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	/*
	 * Getters and Setters
	 */
	
	public Long getServiceAuthenticationId() {
		return serviceAuthenticationId;
	}

	public void setServiceAuthenticationId(Long id) {
		this.serviceAuthenticationId = id;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenInjection() {
		return tokenInjection;
	}

	public void setTokenInjection(String tokenInjection) {
		this.tokenInjection = tokenInjection;
	}

	public String getTokenParamName() {
		return tokenParamName;
	}

	public void setTokenParamName(String tokenParamName) {
		this.tokenParamName = tokenParamName;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	/*
	 * Other Queries
	 */
}
