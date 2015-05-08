

import static org.fest.assertions.Assertions.assertThat;

import org.mindrot.jbcrypt.BCrypt;

import providers.MyLoginUsernamePasswordAuthUser;

public class SimpleTestMain {

	public static void main(String[] args) {
		MyLoginUsernamePasswordAuthUser authUser = new MyLoginUsernamePasswordAuthUser(
				"secret", " bob@example.com");

		// Hash a password for the first time
		String hashed = BCrypt.hashpw(authUser.getPassword(), BCrypt.gensalt());
		String hashed2 = authUser.getHashedPassword();
		System.out.println("Bcrypt hash for BOB is: " + hashed);
		System.out.println("authUser hash hash for BOB is: " + hashed);
		assertThat(hashed == hashed2);

		// gensalt's log_rounds parameter determines the complexity
		// the work factor is 2**log_rounds, and the default is 10
		// String hashed3 = BCrypt.hashpw(authUser.getHashedPassword(),
		// BCrypt.gensalt(12));
		//System.out.println("Bcrypt complex hash is: " + hashed);

		// Check that an unencrypted password matches one that has
		// previously been hashed
		assertThat(BCrypt.checkpw("secret", hashed));

		authUser = new MyLoginUsernamePasswordAuthUser("secret",
				" jane@example.com");

		// Hash a password for the first time
		hashed = BCrypt.hashpw(authUser.getPassword(), BCrypt.gensalt());
		hashed2 = authUser.getHashedPassword();
		System.out.println("Bcrypt hash hash for JANE is: " + hashed);
		System.out.println("authUser hash for JANE is: " + hashed);
		assertThat(hashed == hashed2);

		// gensalt's log_rounds parameter determines the complexity
		// the work factor is 2**log_rounds, and the default is 10
		// String hashed3 = BCrypt.hashpw(authUser.getHashedPassword(),
		// BCrypt.gensalt(12));
		// System.out.println("Bcrypt complex hash is: " + hashed);

		// Check that an unencrypted password matches one that has
		// previously been hashed
		assertThat(BCrypt.checkpw("testing-password", hashed));

		authUser = new MyLoginUsernamePasswordAuthUser("secret",
				" jeff@example.com");

		// Hash a password for the first time
		hashed = BCrypt.hashpw(authUser.getPassword(), BCrypt.gensalt());
		hashed2 = authUser.getHashedPassword();
		System.out.println("Bcrypt hash for JEFF is: " + hashed);
		System.out.println("authUser hash JEFF is: " + hashed);
		assertThat(hashed == hashed2);

		// gensalt's log_rounds parameter determines the complexity
		// the work factor is 2**log_rounds, and the default is 10
		// String hashed3 = BCrypt.hashpw(authUser.getHashedPassword(),
		// BCrypt.gensalt(12));
		// System.out.println("Bcrypt complex hash is: " + hashed);

		// Check that an unencrypted password matches one that has
		// previously been hashed
		assertThat(BCrypt.checkpw("testing-password", hashed));
	}

}
