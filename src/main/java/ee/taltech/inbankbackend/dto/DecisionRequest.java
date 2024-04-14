package ee.taltech.inbankbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Holds the request data of the REST endpoint
 */
@Getter
@AllArgsConstructor
public class DecisionRequest {
    private String personalCode;
    private Long loanAmount;
    private int loanPeriod;
}
