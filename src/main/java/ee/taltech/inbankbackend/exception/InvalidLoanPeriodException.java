package ee.taltech.inbankbackend.exception;

/**
 * Thrown when requested loan period is invalid.
 */
public class InvalidLoanPeriodException extends Exception {
    public InvalidLoanPeriodException(String message) {
        super(message);
    }
}
