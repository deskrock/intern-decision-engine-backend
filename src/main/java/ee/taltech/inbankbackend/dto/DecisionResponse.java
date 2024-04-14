package ee.taltech.inbankbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Holds the response data of the REST endpoint.
 */
@Getter
@Setter
@AllArgsConstructor
public class DecisionResponse {
    private Integer loanAmount;
    private Integer loanPeriod;
    private String errorMessage;
}
