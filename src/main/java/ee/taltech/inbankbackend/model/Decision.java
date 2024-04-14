package ee.taltech.inbankbackend.model;


/**
 * Holds the response data of the REST endpoint.
 */
public record Decision(int loanAmount, int loanPeriod, String errorMessage) {}
