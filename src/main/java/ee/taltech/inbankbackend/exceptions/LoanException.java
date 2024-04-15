package ee.taltech.inbankbackend.exceptions;

/**
 * Custom exception class for various loan-related errors.
 */
public class LoanException extends Throwable {
    private final String message;
    private final Throwable cause;

    public LoanException(String message) {
        this(message, null);
    }

    public LoanException(String message, Throwable cause) {
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

