package providers;

import models.ResourcePicture;
import providers.MyUsernamePasswordAuthProvider.MySignup;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;
//import com.feth.play.module.pa.user.PicturedIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;

public class MyUsernamePasswordAuthUser extends UsernamePasswordAuthUser
		implements NameIdentity, PicturedIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String name;
	private Long userId;
	private String picture;


	public MyUsernamePasswordAuthUser(final MySignup signup) {
		super(signup.password, signup.email);
		this.name = signup.name;
		this.userId = signup.getUserId();
	}
	
	/**
	 * just for testing purpose, should not be used for the app itself
	 * @param name
	 * @param email
	 * @param password
	 */
	@Deprecated
	public MyUsernamePasswordAuthUser(final String name, final Long userId, final String email, final String password) {
		super(password, email);
		this.name = name;
		this.userId = userId;
	}

	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public MyUsernamePasswordAuthUser(final String password) {
		super(password, null);
		this.name = null;
		this.userId = null;
	}
	
	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public MyUsernamePasswordAuthUser(String name,Long userId, String picture, String email, String password) {
		super(password, email);
		this.name = null;
		this.userId = null;
		this.picture=picture;
	}

	@Override
	public String getName() {
		return name;
	}

	public Long getUserId() {
		return userId;
	}
	
	public void setUserId (Long userId) {
		this.userId=userId;
	}

	@Override
	public String getPicture() {
		return this.picture;
	}
}
