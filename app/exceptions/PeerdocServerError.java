package exceptions;

/**
 * Created by javierpf on 25/12/16.
 */
public class PeerdocServerError extends Exception{

    public PeerdocServerError() {
        super();
    }

    public PeerdocServerError(String message, Throwable throwable) {
        super(message, throwable);
    }

    public PeerdocServerError(String message) {
        super(message);
    }

    public PeerdocServerError(Throwable throwable) {
        super(throwable);
    }
}
