import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;

import play.twirl.api.Content;
import providers.MyLoginUsernamePasswordAuthUser;

/**
 *
 * Simple (JUnit) tests that can call all parts of a play app. If you are
 * interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ApplicationTest {

	@Test
	public void simpleCheck() {
		int a = 1 + 1;
		assertThat(a, equalTo(2));
		
	}

	@Test
	public void renderTemplate() {
		Content html = views.html.index.render();
		assertThat(html.contentType(),equalTo("text/html"));
		
	}

	@Test
	public void testPasswords() {
		MyLoginUsernamePasswordAuthUser authUser = 
				new MyLoginUsernamePasswordAuthUser("secret", " bob@example.com");

		// Hash a password for the first time
		String hashed = authUser.getHashedPassword();
		System.out.println("Auth user clear text password: " + authUser.getPassword());
		String hashed2 = BCrypt.hashpw(authUser.getPassword(), hashed);
		System.out.println("Bcrypt hash is: " + hashed);
		System.out.println("authUser hash is: " + hashed2);
		System.out.println("Equals? "+hashed.equals(hashed2));
		assertThat(hashed, equalTo(hashed2));

		// gensalt's log_rounds parameter determines the complexity
		// the work factor is 2**log_rounds, and the default is 10
		// String hashed3 = BCrypt.hashpw(authUser.getHashedPassword(),
		// BCrypt.gensalt(12));
		System.out.println("Bcrypt complex hash is: " + hashed);

		// Check that an unencrypted password matches one that has
		// previously been hashed
		assertThat(BCrypt.checkpw("secret", hashed), equalTo(true));
	}

}
