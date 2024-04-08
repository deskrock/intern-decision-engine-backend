package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when no valid loan is found.
 */
public class NoValidLoanException extends Throwable {
    private final String message;
    private final Throwable cause;

    public NoValidLoanException(String message) {
        this(message, null);
    }

    public NoValidLoanException(String message, Throwable cause) {
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
