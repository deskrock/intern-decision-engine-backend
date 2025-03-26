package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.*;
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
        debtorPersonalCode = "49002010965";  // Debt, creditModifier = 0
        segment1PersonalCode = "49002010976"; // Segment 1, creditModifier = 100
        segment2PersonalCode = "49002010987"; // Segment 2, creditModifier = 300
        segment3PersonalCode = "49002010998"; // Segment 3, creditModifier = 1000
    }

    @Test
    void testDebtorPersonalCode_throwsNoValidLoanException() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 4000L, 12));
    }

    @Test
    void testSegment1PersonalCode_returnsAdjustedLoan() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, 12);
        assertEquals(4000, decision.loanAmount());
        assertEquals(40, decision.loanPeriod());
    }

    @Test
    void testSegment2PersonalCode_returnsApprovedLoan() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 4000L, 12);
        assertEquals(3600, decision.loanAmount());
        assertEquals(12, decision.loanPeriod());
    }

    @Test
    void testSegment3PersonalCode_returnsMaxLoan() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode, 4000L, 12);
        assertEquals(10000, decision.loanAmount());
        assertEquals(12, decision.loanPeriod());
    }

    @Test
    void testInvalidPersonalCode_throwsException() {
        assertThrows(InvalidPersonalCodeException.class,
                () -> decisionEngine.calculateApprovedLoan("12345678901", 4000L, 12));
    }

    @Test
    void testInvalidLoanAmount_throwsException() {
        Long tooLow = DecisionEngineConstants.MINIMUM_LOAN_AMOUNT - 1L;
        Long tooHigh = DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT + 1L;

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateApprovedLoan(segment3PersonalCode, tooLow, 12));
        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateApprovedLoan(segment3PersonalCode, tooHigh, 12));
    }

    @Test
    void testInvalidLoanPeriod_throwsException() {
        int tooShort = DecisionEngineConstants.MINIMUM_LOAN_PERIOD - 1;
        int tooLong = DecisionEngineConstants.MAXIMUM_LOAN_PERIOD + 1;

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateApprovedLoan(segment3PersonalCode, 4000L, tooShort));
        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateApprovedLoan(segment3PersonalCode, 4000L, tooLong));
    }

    @Test
    void testSegment1LowerAmount_returnsAdjustedLoan() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 2000L, 12);
        assertEquals(2000, decision.loanAmount());
        assertEquals(20, decision.loanPeriod());
    }

    @Test
    void testSegment2HigherAmount_returnsMaxLoan() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 9000L, 12);
        assertEquals(3600, decision.loanAmount());
        assertEquals(12, decision.loanPeriod());
    }
}