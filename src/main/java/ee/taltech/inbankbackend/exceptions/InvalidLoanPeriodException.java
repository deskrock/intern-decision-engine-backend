package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when requested loan period is invalid.
 */
public class InvalidLoanPeriodException extends Throwable {
    private final String message;
    private final Throwable cause;

    public InvalidLoanPeriodException(String message) {
        this(message, null);
    }

    public InvalidLoanPeriodException(String message, Throwable cause) {
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
