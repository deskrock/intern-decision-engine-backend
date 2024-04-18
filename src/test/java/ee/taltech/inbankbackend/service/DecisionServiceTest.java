package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.Constants;
import ee.taltech.inbankbackend.exceptions.*;
import ee.taltech.inbankbackend.model.Decision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    @InjectMocks
    private DecisionService decisionService;

    private String debtorPersonalCode;
    private String segment1PersonalCode;
    private String segment2PersonalCode;
    private String segment3PersonalCode;
    private String maleOldPersonalCode;
    private String femaleOldPersonalCode;
    private String youngPersonalCode;

    @BeforeEach
    void setUp() {
        debtorPersonalCode = "37605030299";
        segment1PersonalCode = "50307172740";
        segment2PersonalCode = "38411266610";
        segment3PersonalCode = "45701228184";
        maleOldPersonalCode = "29511069930";
        femaleOldPersonalCode = "13009249326";
        youngPersonalCode = "50908223426";
    }

    @Test
    void testDebtorPersonalCode() {
        assertThrows(NoValidLoanException.class,
                () -> decisionService.calculateApprovedLoan(debtorPersonalCode, 4000L, 12));
    }

    @Test
    void testSegment1PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanAgeException {
        Decision decision = decisionService.calculateApprovedLoan(segment1PersonalCode, 4000L, 12);
        assertEquals(2000, decision.getLoanAmount());
        assertEquals(20, decision.getLoanPeriod());
    }

    @Test
    void testSegment2PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanAgeException {
        Decision decision = decisionService.calculateApprovedLoan(segment2PersonalCode, 4000L, 12);
        assertEquals(3600, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testSegment3PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanAgeException {
        Decision decision = decisionService.calculateApprovedLoan(segment3PersonalCode, 4000L, 12);
        assertEquals(10000, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testInvalidPersonalCode() {
        String invalidPersonalCode = "12345678901";
        assertThrows(InvalidPersonalCodeException.class,
                () -> decisionService.calculateApprovedLoan(invalidPersonalCode, 4000L, 12));
    }

    @Test
    void testInvalidLoanAmount() {
        Long tooLowLoanAmount = Constants.MINIMUM_LOAN_AMOUNT - 1L;
        Long tooHighLoanAmount = Constants.MAXIMUM_LOAN_AMOUNT + 1L;

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionService.calculateApprovedLoan(segment1PersonalCode, tooLowLoanAmount, 12));

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionService.calculateApprovedLoan(segment1PersonalCode, tooHighLoanAmount, 12));
    }

    @Test
    void testInvalidLoanPeriod() {
        int tooShortLoanPeriod = Constants.MINIMUM_LOAN_PERIOD - 1;
        Integer tooLongLoanPeriod = Constants.MAXIMUM_LOAN_PERIOD + 1;

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionService.calculateApprovedLoan(segment1PersonalCode, 4000L, tooShortLoanPeriod));

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionService.calculateApprovedLoan(segment1PersonalCode, 4000L, tooLongLoanPeriod));
    }

    @Test
    void testInvalidLoanAge() {
        assertThrows(InvalidLoanAgeException.class,
                () -> decisionService.calculateApprovedLoan(youngPersonalCode, 2500L, 35));

        assertThrows(InvalidLoanAgeException.class,
                () -> decisionService.calculateApprovedLoan(maleOldPersonalCode, 2500L, 35));

        assertThrows(InvalidLoanAgeException.class,
                () -> decisionService.calculateApprovedLoan(femaleOldPersonalCode, 2500L, 35));
    }

    @Test
    void testFindSuitableLoanPeriod() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanAgeException {
        Decision decision = decisionService.calculateApprovedLoan(segment2PersonalCode, 2000L, 12);
        assertEquals(3600, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testNoValidLoanFound() {
        assertThrows(NoValidLoanException.class,
                () -> decisionService.calculateApprovedLoan(debtorPersonalCode, 10000L, 60));
    }

}

