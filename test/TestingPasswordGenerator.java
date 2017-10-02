import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

import providers.MyLoginUsernamePasswordAuthUser;

import javax.xml.bind.DatatypeConverter;

public class TestingPasswordGenerator {
    private static Random random = new Random();
    private static SecureRandom secureRandom = new SecureRandom();

    public static void main(String[] args) {
        try {
            if (args != null && args.length > 0) {
                System.out.println("Received args = "+args.length);
                System.out.println("Received args[0] = "+args[0]);

                if (args[0].equals("-s") || args[0].equals("--single")) {
                    String email = args[1];
                    String pass = args[2];
                    System.out.println("BCrypt hash for " + pass + "  is = " + getBCryptPasswordFromStringAndEmail(pass, email));
                    String md5Hash = hashString(email);
                    System.out.println("Email MD5 = " + md5Hash);

                } else if (args[0].equals("-f") || args[0].equals("--file")) {
                    HashMap<String, String> emailPass = new HashMap<>();
                    String filedir = args[1];
                    String filename = args[2];
                    Path path = Paths.get(filedir + "/" + filename);
                    Files.lines(path)
                            .onClose(() -> System.out.println("File closed"))
                            .filter(s -> s.contains("@"))
                            .forEach((s) -> {
                                generatePassword(s, emailPass);
                            });
                }
            } else {
                String email = "stephanie.poujade@crosaquitaine.org";
                String pass = "kitkom33*APP";
                System.out.println("BCrypt hash for " + pass + "  is = " + getBCryptPasswordFromStringAndEmail(pass, email));
                String md5Hash = hashString(email);
                System.out.println("Email MD5 = " + md5Hash);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generatePassword(String email, HashMap<String, String> emailPass) {
        String[] seedPasswords = {"sip", "hop", "vouch", "value", "walk",
                "act", "stray", "call", "curl", "stay", "yell", "tack",
                "sing", "yawn", "open", "read", "edit",
                "spell", "fix", "love", "knit", "like",
                "praise", "hide", "clip"};
        Integer passIndex = random.nextInt(25);
        String pass = seedPasswords[passIndex] + nextPassword();
        MyLoginUsernamePasswordAuthUser authUser =
                new MyLoginUsernamePasswordAuthUser(pass, email);
        // Hash a password for the first time
        String hashed = authUser.getHashedPassword();
        System.out.println(email + ", clear=" + pass + ", hashed=" + hashed);
        emailPass.put(pass, hashed);
    }

    private static String getBCryptPasswordFromStringAndEmail(String pass, String email) {
        MyLoginUsernamePasswordAuthUser authUser =
                new MyLoginUsernamePasswordAuthUser(pass, email);
        return authUser.getHashedPassword();
    }

    public static String nextPassword() {
        return new BigInteger(20, secureRandom).toString(32);
    }

    public static String hashString(String text)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(text.getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter
                .printHexBinary(digest);
        return myHash;
    }
}
