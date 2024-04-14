package ee.taltech.inbankbackend.exception;

/**
 * Thrown when no valid loan is found.
 */
public class NoValidLoanException extends Exception {
    public NoValidLoanException(String message) {
        super(message);
    }
}
