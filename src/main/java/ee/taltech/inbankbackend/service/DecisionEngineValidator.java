package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeParser;
import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exceptions.UnderageException;
import org.springframework.stereotype.Component;
import java.time.Period;

/**
 * A Component class that provides a method for validating a personal ID code
 */
@Component
public class DecisionEngineValidator {

    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private final EstonianPersonalCodeParser parser = new EstonianPersonalCodeParser();

    /**
     * Verify that all inputs are valid according to business rules.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws UnderageException If the customer is still underage
     */
    public void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException,
            InvalidLoanPeriodException, UnderageException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                || !(loanAmount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_PERIOD <= loanPeriod)
                || !(loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }

        final int age;
        try {
            age = parser.getAge(personalCode).getYears();
        } catch (PersonalCodeException e) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (age < DecisionEngineConstants.MINIMUM_AGE) {
            throw new UnderageException("Customer is underage!");
        }
    }

    /**
     * Get the age in months from the provided personal ID code
     *
     * @param personalCode Provided personal ID code
     * @return Age in months
     */
    public int getAgeInMonths(String personalCode) throws InvalidPersonalCodeException {
        try {
            Period period = parser.getAge(personalCode);
            return period.getMonths() + period.getYears() * 12;
        } catch (PersonalCodeException e) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
    }
}