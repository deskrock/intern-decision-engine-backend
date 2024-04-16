package ee.taltech.inbankbackend.controller;

import ee.taltech.inbankbackend.dto.DecisionRequest;
import ee.taltech.inbankbackend.dto.DecisionResponse;
import ee.taltech.inbankbackend.model.Decision;
import ee.taltech.inbankbackend.service.DecisionEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loan")
@CrossOrigin
public class DecisionEngineController {

    private final DecisionEngine decisionEngine;

    DecisionEngineController(DecisionEngine decisionEngine) {
        this.decisionEngine = decisionEngine;
    }

    /**
     * A REST endpoint that handles requests for loan decisions.
     * The endpoint accepts POST requests with a request body containing the customer's personal ID code,
     * requested loan amount, and loan period.<br><br>
     * - If the loan amount or period is invalid, the endpoint returns a bad request response with an error message.<br>
     * - If the personal ID code is invalid, the endpoint returns a bad request response with an error message.<br>
     * - If an unexpected error occurs, the endpoint returns an internal server error response with an error message.<br>
     * - If no valid loans can be found, the endpoint returns a not found response with an error message.<br>
     * - If a valid loan is found, a DecisionResponse is returned containing the approved loan amount and period.
     *
     * @param request The request body containing the customer's personal ID code, requested loan amount, and loan period
     * @return A ResponseEntity with a DecisionResponse body containing the approved loan amount and period, and an error message (if any)
     */
    @PostMapping("/decision")
    public ResponseEntity<DecisionResponse> requestDecision(@RequestBody DecisionRequest request) throws Exception {
        DecisionResponse response = new DecisionResponse();
        Decision decision = decisionEngine.calculateApprovedLoan(request.getPersonalCode(), request.getLoanAmount(), request.getLoanPeriod());
        response.setLoanAmount(decision.loanAmount());
        response.setLoanPeriod(decision.loanPeriod());
        response.setErrorMessage(decision.errorMessage());
        return ResponseEntity.ok(response);
    }
}
