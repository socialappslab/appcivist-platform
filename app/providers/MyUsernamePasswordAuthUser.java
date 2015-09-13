package providers;

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
	private String picture;
	
	/**
	 * Added userId to send in response after login or signup
	 */
	private Long userId;
	private String lang;

	public MyUsernamePasswordAuthUser(final MySignup signup) {
		super(signup.password, signup.email);
		this.name = signup.name;
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
	
	
}
