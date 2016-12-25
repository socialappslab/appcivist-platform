package exceptions;

/**
 * Created by javierpf on 25/12/16.
 */
public class ConfigurationException extends Exception{

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable throwable) {
        super(throwable);
    }
}
