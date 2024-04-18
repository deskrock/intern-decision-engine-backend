package ee.taltech.inbankbackend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Holds the response data of the REST endpoint.
 */
@Getter
@AllArgsConstructor
public class Decision {

    private final Long loanAmount;
    private final Integer loanPeriod;
    private final String errorMessage;
}
