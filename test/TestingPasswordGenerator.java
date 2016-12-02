import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import providers.MyLoginUsernamePasswordAuthUser;

public class TestingPasswordGenerator {
	private static Random random = new Random();
	private static SecureRandom secureRandom = new SecureRandom();
	
	public static void main(String[] args) {
		try {		
			HashMap<String, String> emailPass = new HashMap<>();
			String filedir = args[0];
			String filename = args[1];
			Path path = Paths.get(filedir + "/" + filename);
			Files.lines(path)
				.onClose(() -> System.out.println("File closed"))
				.filter(s -> s.contains("@"))
				.forEach((s) -> {
					generatePassword(s, emailPass);
				});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generatePassword(String email, HashMap<String, String> emailPass) {
		String[] seedPasswords = { "sip", "hop", "vouch", "value", "walk",
				"act", "stray", "call", "curl", "stay", "yell", "tack",
				"sing", "yawn", "open", "read", "edit",
				"spell", "fix", "love", "knit", "like",
				"praise", "hide", "clip" };
		Integer passIndex = random.nextInt(25);
		String pass = seedPasswords[passIndex] + nextPassword();
		MyLoginUsernamePasswordAuthUser authUser = 
				new MyLoginUsernamePasswordAuthUser(pass, email);
		// Hash a password for the first time
		String hashed = authUser.getHashedPassword();
		System.out.println(email+", clear=" + pass+", hashed="+hashed);
		emailPass.put(pass, hashed);
	}

	public static String nextPassword() {
		return new BigInteger(20, secureRandom).toString(32);
	}
}
