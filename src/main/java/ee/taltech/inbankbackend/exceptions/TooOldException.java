package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when requested loan amount is invalid.
 */
public class TooOldException extends Throwable {
    private final String message;
    private final Throwable cause;

    public TooOldException(String message) {
        this(message, null);
    }

    public TooOldException(String message, Throwable cause) {
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
