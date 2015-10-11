package providers;

import org.mindrot.jbcrypt.BCrypt;

import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;

public class MyLoginUsernamePasswordAuthUser extends
		DefaultUsernamePasswordAuthUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The session timeout in seconds
	 * Defaults to two weeks (24 hours * 14 days * 60 minutes * 60 seconds)
	 */
	public final static long SESSION_TIMEOUT = 24 * 14 * 3600;
	private long expiration;

	/**
	 * For logging the user in automatically
	 * 
	 * @param email
	 */
	public MyLoginUsernamePasswordAuthUser(final String email) {
		this(null, email);
	}

	public MyLoginUsernamePasswordAuthUser(final String clearPassword,
			final String email) {
		super(clearPassword, email);

		expiration = System.currentTimeMillis() + 1000 * SESSION_TIMEOUT;
	}

	@Override
	public long expires() {
		return expiration;
	}
	
	/**
	 * You *SHOULD* provide your own implementation of this which implements your own security.
	 */
	@Override
	protected String createPassword(final String clearString) {
		return BCrypt.hashpw(clearString, BCrypt.gensalt());
	}
	
	/**
	 * You *SHOULD* provide your own implementation of this which implements your own security.
	 */
	@Override
	public boolean checkPassword(final String hashed, final String candidate) {
		if(hashed == null || candidate == null) {
			return false;
		}
		return BCrypt.checkpw(candidate, hashed);
	}
}
