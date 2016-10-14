package models;

import io.swagger.annotations.ApiModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.Model;
import com.feth.play.module.pa.user.AuthUser;

@Entity
@Table(name="Linked_Account")
@ApiModel(value="LinkedAccount", description="Connected accounts to users (e.g., google, facebok, etc.)")
public class LinkedAccount extends Model {

	@Id
	@GeneratedValue
	@Column(name="account_id")
	private Long accountId;

	@ManyToOne
	//@MapsId
	@JoinColumn(name="user_id")
	private User user;

	@Column(name="provider_user_id")
	private String providerUserId;

	@Column(name="provider_key")
	private String providerKey;

	public static final Finder<Long, LinkedAccount> find = new Finder<>(LinkedAccount.class);

	public static LinkedAccount findByProviderKey(final User user, String key) {
		return find.where().
				eq("user.userId", user.getUserId()).
				eq("providerKey", key)
				.findUnique();
	}

	public static LinkedAccount create(final AuthUser authUser) {
		final LinkedAccount ret = new LinkedAccount();
		ret.update(authUser);
		return ret;
	}
	
	public void update(final AuthUser authUser) {
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();
	}

	public static LinkedAccount create(final LinkedAccount acc) {
		final LinkedAccount ret = new LinkedAccount();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;

		return ret;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long id) {
		this.accountId = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getProviderUserId() {
		return providerUserId;
	}

	public void setProviderUserId(String providerUserId) {
		this.providerUserId = providerUserId;
	}

	public String getProviderKey() {
		return providerKey;
	}

	public void setProviderKey(String providerKey) {
		this.providerKey = providerKey;
	}
}