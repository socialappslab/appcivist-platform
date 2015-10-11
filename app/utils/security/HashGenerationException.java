package utils.security;

/**
 * HashGenerationException
 * @author www.codejava.net
 * Source: http://www.codejava.net/coding/how-to-calculate-md5-and-sha-hash-values-in-java
 */
@SuppressWarnings("serial")
public class HashGenerationException extends Exception {

	public HashGenerationException() {
		super();
	}
	
	public HashGenerationException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public HashGenerationException(String message) {
		super(message);
	}

	public HashGenerationException(Throwable throwable) {
		super(throwable);
	}
}
