package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when requested age is invalid.
 */
public class AgeRestrictionException extends Throwable {
    private final String message;
    private final Throwable cause;

    public AgeRestrictionException(String message) {
        this(message, null);
    }

    public AgeRestrictionException(String message, Throwable cause) {
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
