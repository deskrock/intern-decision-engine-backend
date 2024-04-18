package ee.taltech.inbankbackend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Holds the response data of the REST endpoint.
 */
@Getter
@Setter
@Component
public class DecisionResponse {

    private Long loanAmount;
    private Integer loanPeriod;
    private String errorMessage;
}
