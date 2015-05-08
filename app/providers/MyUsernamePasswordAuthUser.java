package providers;

import providers.MyUsernamePasswordAuthProvider.MySignup;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;
//import com.feth.play.module.pa.user.PicturedIdentity;

public class MyUsernamePasswordAuthUser extends UsernamePasswordAuthUser
		implements NameIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String name;
	private Long userId;


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
}
