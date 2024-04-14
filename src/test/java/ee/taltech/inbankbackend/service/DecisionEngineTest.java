package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.DecisionEngineConstant;
import ee.taltech.inbankbackend.exception.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exception.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exception.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exception.NoValidLoanException;
import ee.taltech.inbankbackend.model.Decision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DecisionEngineTest {

    @InjectMocks
    private DecisionEngine decisionEngine;

    private String debtorPersonalCode;
    private String segment1PersonalCode;
    private String segment2PersonalCode;
    private String segment3PersonalCode;

    @BeforeEach
    void setUp() {
        debtorPersonalCode = "37605030299";
        segment1PersonalCode = "50307172740";
        segment2PersonalCode = "38411266610";
        segment3PersonalCode = "35006069515";
    }

    @Test
    void testDebtorPersonalCode() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 4000L, 12));
    }

    @Test
    void testSegment1PersonalCode() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, 12);
        assertEquals(2000, decision.loanAmount());
        assertEquals(20, decision.loanPeriod());
    }

    @Test
    void testSegment2PersonalCode() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 4000L, 12);
        assertEquals(3600, decision.loanAmount());
        assertEquals(12, decision.loanPeriod());
    }

    @Test
    void testSegment3PersonalCode() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode, 4000L, 12);
        assertEquals(10000, decision.loanAmount());
        assertEquals(12, decision.loanPeriod());
    }

    @Test
    void testInvalidPersonalCode() {
        String invalidPersonalCode = "12345678901";
        assertThrows(InvalidPersonalCodeException.class,
                () -> decisionEngine.calculateApprovedLoan(invalidPersonalCode, 4000L, 12));
    }

    @Test
    void testInvalidLoanAmount() {
        Long tooLowLoanAmount = DecisionEngineConstant.MINIMUM_LOAN_AMOUNT - 1L;
        Long tooHighLoanAmount = DecisionEngineConstant.MAXIMUM_LOAN_AMOUNT + 1L;

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooLowLoanAmount, 12));

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooHighLoanAmount, 12));
    }

    @Test
    void testInvalidLoanPeriod() {
        int tooShortLoanPeriod = DecisionEngineConstant.MINIMUM_LOAN_PERIOD - 1;
        int tooLongLoanPeriod = DecisionEngineConstant.MAXIMUM_LOAN_PERIOD + 1;

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, tooShortLoanPeriod));

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, tooLongLoanPeriod));
    }

    @Test
    void testFindSuitableLoanPeriod() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 2000L, 12);
        assertEquals(3600, decision.loanAmount());
        assertEquals(12, decision.loanPeriod());
    }

    @Test
    void testNoValidLoanFound() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 10000L, 60));
    }

}

