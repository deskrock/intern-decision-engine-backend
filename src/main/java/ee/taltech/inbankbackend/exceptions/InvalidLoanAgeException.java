package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when provided age is invalid.
 */
public class InvalidLoanAgeException extends Throwable {
    private final String message;
    private final Throwable cause;

    public InvalidLoanAgeException(String message) {
        this(message, null);
    }

    public InvalidLoanAgeException(String message, Throwable cause) {
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
