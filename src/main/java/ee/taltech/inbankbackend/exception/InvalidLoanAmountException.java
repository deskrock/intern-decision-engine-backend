package ee.taltech.inbankbackend.exception;

/**
 * Thrown when requested loan amount is invalid.
 */
public class InvalidLoanAmountException extends Exception {
    public InvalidLoanAmountException(String message) {
        super(message);
    }
}
