package exceptions;

@SuppressWarnings("serial")
public class TokenNotValidException extends Exception {
	public TokenNotValidException() {
		super();
	}
	
	public TokenNotValidException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public TokenNotValidException(String message) {
		super(message);
	}

	public TokenNotValidException(Throwable throwable) {
		super(throwable);
	}
}
