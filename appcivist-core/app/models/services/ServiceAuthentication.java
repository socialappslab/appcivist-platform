package models.services;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class ServiceAuthentication extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6076927164603033420L;

	@Id
	private Long id;
	private String authType; // TODO: replace with Enum
	private String token;
	private String tokenInjection; // TODO: replace with Enum
	private String tokenParamName;

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

}
