package ee.taltech.inbankbackend.utils;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.exception.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exception.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exception.InvalidPersonalCodeException;
import org.springframework.stereotype.Component;

@Component
public class Validator {
    private final EstonianPersonalCodeValidator personalCodeValidator;

    public Validator() {
        personalCodeValidator = new EstonianPersonalCodeValidator();
    }

    /**
     * Verify that personal code is valid according to business rules.
     * If input is invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     */
    public void verifyPersonalCode(String personalCode) throws InvalidPersonalCodeException {
        if (!personalCodeValidator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
    }

    /**
     * Verify that personal code is valid according to business rules.
     * If input is invalid, then throws corresponding exceptions.
     *
     * @param loanAmount Requested loan amount
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     */
    public void verifyLoanAmount(Long loanAmount) throws InvalidLoanAmountException {
        if ((loanAmount < DecisionEngineConstant.MINIMUM_LOAN_AMOUNT)
                || (loanAmount > DecisionEngineConstant.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
    }

    /**
     * Verify that personal code is valid according to business rules.
     * If input is invalid, then throws corresponding exceptions.
     *
     * @param loanPeriod Requested loan period
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     */
    public void verifyLoanPeriod(int loanPeriod) throws InvalidLoanPeriodException {
        if ((loanPeriod < DecisionEngineConstant.MINIMUM_LOAN_PERIOD)
                || (loanPeriod > DecisionEngineConstant.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }
    }
}
