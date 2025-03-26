package ee.taltech.inbankbackend.service;

/**
 * Holds the response data of the REST endpoint.
 */
public record Decision(Integer loanAmount, Integer loanPeriod) {
}