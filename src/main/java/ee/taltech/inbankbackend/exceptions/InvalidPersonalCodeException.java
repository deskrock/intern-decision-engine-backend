package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when provided personal ID code is invalid.
 */
public class InvalidPersonalCodeException extends Throwable {
    private final String message;
    private final Throwable cause;

    public InvalidPersonalCodeException(String message) {
        this(message, null);
    }

    public InvalidPersonalCodeException(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
