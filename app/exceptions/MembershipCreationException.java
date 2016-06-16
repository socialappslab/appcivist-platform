package exceptions;

@SuppressWarnings("serial")
public class MembershipCreationException extends Exception {
	public MembershipCreationException() {
		super();
	}
	
	public MembershipCreationException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public MembershipCreationException(String message) {
		super(message);
	}

	public MembershipCreationException(Throwable throwable) {
		super(throwable);
	}
}
