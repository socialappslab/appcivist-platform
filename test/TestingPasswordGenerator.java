import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

import providers.MyLoginUsernamePasswordAuthUser;

public class TestingPasswordGenerator {
	private static Random random = new Random();
	private static SecureRandom secureRandom = new SecureRandom();
	
	public static void main(String[] args) {
		String[] emails = { 
				"carmen@example.com",
				"jane@example.com",
				"jeff@example.com",
				"sara@example.com",
				"alicia@example.com",
				"ted@example.com",
				"leia@example.com",
				"luke@example.com", 
				"ahsoka@example.com", 
				"bob@exampe.com"};
		
		String[] passwords = { "sip", "hop", "vouch", "value", "walk",
				"act", "stray", "call", "curl", "stay", "yell", "tack",
				"sing", "yawn", "open", "read", "edit",
				"spell", "fix", "love", "knit", "like",
				"praise", "hide", "clip" };

		HashMap<String, String> emailPass = new HashMap<>();
		
		for (String email : emails) {
			Integer passIndex = random.nextInt(25);
			String pass = passwords[passIndex] + nextPassword();
			MyLoginUsernamePasswordAuthUser authUser = 
					new MyLoginUsernamePasswordAuthUser(pass, email);
			// Hash a password for the first time
			String hashed = authUser.getHashedPassword();
			System.out.println(email+", clear=" + pass+", hashed="+hashed);
			emailPass.put(email, hashed);
		}
		MyLoginUsernamePasswordAuthUser authUser = 
				new MyLoginUsernamePasswordAuthUser("secret", "carmen@example.com");
		// Hash a password for the first time
		String hashed = authUser.getHashedPassword();
		
		String[] adminGmails = {
				"appcivistapp+EN_ADMIN@gmail.com", 
				"appcivistapp+ES_ADMIN@gmail.com", 
				"appcivistapp+FR_ADMIN@gmail.com", 
				"appcivistapp+DE_ADMIN@gmail.com", 
				"appcivistapp+IT_ADMIN@gmail.com"};
	
		for (String email : adminGmails) {
			String pass = "a44@c1v1st.2017." + nextPassword();
			authUser = new MyLoginUsernamePasswordAuthUser(pass, email);
			// Hash a password for the first time
			hashed = authUser.getHashedPassword();
			System.out.println(email+", clear=" + pass+", hashed="+hashed);
			emailPass.put(email, hashed);
		}
		
		String[] gmails = {
				"appcivistapp+EN@gmail.com", 
				"appcivistapp+ES@gmail.com", 
				"appcivistapp+FR@gmail.com", 
				"appcivistapp+DE@gmail.com", 
				"appcivistapp+IT@gmail.com"};
		
		for (String email : gmails) {
			String pass = "appcivist.2017" + nextPassword();
			authUser = new MyLoginUsernamePasswordAuthUser(pass, email);
			// Hash a password for the first time
			hashed = authUser.getHashedPassword();
			System.out.println(email+", clear=" + pass+", hashed="+hashed);
			emailPass.put(email, hashed);
		}
	}
	
	public static String nextPassword() {
		return new BigInteger(20, secureRandom).toString(32);
	}
}
