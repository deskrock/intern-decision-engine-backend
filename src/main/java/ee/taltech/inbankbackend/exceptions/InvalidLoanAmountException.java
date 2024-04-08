package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when requested loan amount is invalid.
 */
public class InvalidLoanAmountException extends Throwable {
    private final String message;
    private final Throwable cause;

    public InvalidLoanAmountException(String message) {
        this(message, null);
    }

    public InvalidLoanAmountException(String message, Throwable cause) {
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
