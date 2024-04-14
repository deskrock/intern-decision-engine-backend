package ee.taltech.inbankbackend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Holds the response data of the REST endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
public class DecisionResponse {
    private Integer loanAmount;
    private Integer loanPeriod;
    private String errorMessage;
}
