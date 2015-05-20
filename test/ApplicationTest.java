import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import models.User;

import org.junit.*;
import org.mindrot.jbcrypt.BCrypt;

import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;
import play.twirl.api.Content;
import providers.MyLoginUsernamePasswordAuthUser;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

/**
 *
 * Simple (JUnit) tests that can call all parts of a play app. If you are
 * interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ApplicationTest {

	@Test
	@Ignore
	public void simpleCheck() {
		int a = 1 + 1;
		assertThat(a).isEqualTo(2);
	}

	@Test
	@Ignore
	public void renderTemplate() {
		Content html = views.html.index.render();
		assertThat(contentType(html)).isEqualTo("text/html");
	}

	@Test
	@Ignore
	public void testPasswords() {
		MyLoginUsernamePasswordAuthUser authUser = new MyLoginUsernamePasswordAuthUser(
				"secret", " bob@example.com");

		// Hash a password for the first time
		String hashed = BCrypt.hashpw(authUser.getPassword(), BCrypt.gensalt());
		String hashed2 = authUser.getHashedPassword();
		System.out.println("Bcrypt hash is: " + hashed);
		System.out.println("authUser hash is: " + hashed);
		assertThat(hashed == hashed2);

		// gensalt's log_rounds parameter determines the complexity
		// the work factor is 2**log_rounds, and the default is 10
		// String hashed3 = BCrypt.hashpw(authUser.getHashedPassword(),
		// BCrypt.gensalt(12));
		System.out.println("Bcrypt complex hash is: " + hashed);

		// Check that an unencrypted password matches one that has
		// previously been hashed
		assertThat(BCrypt.checkpw("secret", hashed));

		authUser = new MyLoginUsernamePasswordAuthUser("secret",
				" jane@example.com");

		// Hash a password for the first time
		hashed = BCrypt.hashpw(authUser.getPassword(), BCrypt.gensalt());
		hashed2 = authUser.getHashedPassword();
		System.out.println("Bcrypt hash is: " + hashed);
		System.out.println("authUser hash is: " + hashed);
		assertThat(hashed == hashed2);

		// gensalt's log_rounds parameter determines the complexity
		// the work factor is 2**log_rounds, and the default is 10
		// String hashed3 = BCrypt.hashpw(authUser.getHashedPassword(),
		// BCrypt.gensalt(12));
		System.out.println("Bcrypt complex hash is: " + hashed);

		// Check that an unencrypted password matches one that has
		// previously been hashed
		assertThat(BCrypt.checkpw("testing-password", hashed));

		authUser = new MyLoginUsernamePasswordAuthUser("secret",
				" jeff@example.com");

		// Hash a password for the first time
		hashed = BCrypt.hashpw(authUser.getPassword(), BCrypt.gensalt());
		hashed2 = authUser.getHashedPassword();
		System.out.println("Bcrypt hash is: " + hashed);
		System.out.println("authUser hash is: " + hashed);
		assertThat(hashed == hashed2);

		// gensalt's log_rounds parameter determines the complexity
		// the work factor is 2**log_rounds, and the default is 10
		// String hashed3 = BCrypt.hashpw(authUser.getHashedPassword(),
		// BCrypt.gensalt(12));
		System.out.println("Bcrypt complex hash is: " + hashed);

		// Check that an unencrypted password matches one that has
		// previously been hashed
		assertThat(BCrypt.checkpw("testing-password", hashed));

	}

}
