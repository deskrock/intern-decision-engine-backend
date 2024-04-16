package ee.taltech.inbankbackend.model;


/**
 * Holds the decision data from the service layer. It is for internal usage and not open to the end-user.
 */
public record Decision(int loanAmount, int loanPeriod, String errorMessage) {
}
