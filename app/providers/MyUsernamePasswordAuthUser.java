package providers;

import java.util.UUID;

import models.transfer.AssemblyTransfer;
import providers.MyUsernamePasswordAuthProvider.MySignup;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;
//import com.feth.play.module.pa.user.PicturedIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;

public class MyUsernamePasswordAuthUser extends UsernamePasswordAuthUser
		implements NameIdentity, PicturedIdentity, ExistingGroupSignupIdentity, GroupSignupIdentity, InvitationSignupIdentity, LanguageSignupIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String name;
	private String picture;
	
	/**
	 * Added userId to send in response after login or signup
	 */
	private Long userId;
	private String lang;
	/*
	 * Added to support GroupSignup
	 */
	private AssemblyTransfer newAssembly;
	/*
	 * Added to support ExistingGroupSignup
	 */
	private AssemblyTransfer existingAssembly;
	/*
	 * Added to support Signup with Invitation
	 */
	private UUID invitationToken;

	public MyUsernamePasswordAuthUser(final MySignup signup) {
		super(signup.password, signup.email);
		this.name = signup.name;
		this.lang = signup.lang;
		this.newAssembly = signup.getNewAssembly();
		this.existingAssembly = signup.getExistingAssembly();
		this.invitationToken = signup.getInvitationToken();
	}

	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public MyUsernamePasswordAuthUser(final String password) {
		super(password, null);
		this.name = null;
	}
	
	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public MyUsernamePasswordAuthUser(String name,Long userId, String picture, String email, String password) {
		super(password, email);
		this.name = null;
		this.picture=picture;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPicture() {
		return this.picture;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public AssemblyTransfer getNewAssembly() {
		return newAssembly;
	}

	public void setNewAssembly(AssemblyTransfer newAssembly) {
		this.newAssembly = newAssembly;
	}

	public UUID getInvitationToken() {
		return invitationToken;
	}

	public void setInvitationToken(UUID invitationToken) {
		this.invitationToken = invitationToken;
	}

	@Override
	public String getLanguage() {
		return this.lang;
	}
	
	public void setLanguage(String l) {
		this.lang = l;
	}

	public AssemblyTransfer getExistingAssembly() {
		return existingAssembly;
	}

	public void setExistingAssembly(AssemblyTransfer existingAssembly) {
		this.existingAssembly = existingAssembly;
	}
}
