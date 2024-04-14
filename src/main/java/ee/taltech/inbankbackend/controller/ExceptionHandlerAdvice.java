package ee.taltech.inbankbackend.controller;

import ee.taltech.inbankbackend.dto.DecisionResponse;
import ee.taltech.inbankbackend.exception.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exception.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exception.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exception.NoValidLoanException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler({InvalidPersonalCodeException.class, InvalidLoanAmountException.class, InvalidLoanPeriodException.class})
    public ResponseEntity<DecisionResponse> handleBadRequest(Exception e) {
        DecisionResponse response = new DecisionResponse(null, null, e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoValidLoanException.class)
    public ResponseEntity<DecisionResponse> handleNotFound(NoValidLoanException e) {
        DecisionResponse response = new DecisionResponse(null, null, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DecisionResponse> handleInternalServerError(Exception e) {
        DecisionResponse response = new DecisionResponse(null, null, "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
