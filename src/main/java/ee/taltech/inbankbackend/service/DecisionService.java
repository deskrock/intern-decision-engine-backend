package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.common.Gender;
import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeParser;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.Constants;
import ee.taltech.inbankbackend.exceptions.*;
import ee.taltech.inbankbackend.model.Decision;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Period;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier,
 * which is determined by the last four digits of their ID code.
 */
@Service
public class DecisionService {

    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private final EstonianPersonalCodeParser parser = new EstonianPersonalCodeParser();

    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 60 months (inclusive).
     * The loan amount must be between 2000 and 10000€ months (inclusive).
     * @param personalCode ID code of the customer that made the request.
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws NoValidLoanException If there is no valid loan found for the given ID code, loan amount and loan period
     */
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, Integer loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException, InvalidLoanAgeException {
        try {
            verifyInputs(personalCode, loanAmount, loanPeriod);
            validateAge(personalCode);
        } catch (Exception e) {
            return new Decision(null, null, e.getMessage());
        }
        Long creditModifier = getCreditModifier(personalCode);
        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found!");
        }

        return highestValidLoanAmount(loanPeriod, creditModifier);
    }

    /**
     * Calculates the largest valid loan for the current credit modifier and loan period.
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     */
    private Decision highestValidLoanAmount(Integer loanPeriod, Long creditModifier) throws InvalidLoanAmountException {
        Long finalLoanAmount =  creditModifier * loanPeriod;

        if(finalLoanAmount >= Constants.MAXIMUM_LOAN_AMOUNT) {
            return new Decision(Constants.MAXIMUM_LOAN_AMOUNT, loanPeriod, null);
        }

        while(finalLoanAmount < Constants.MINIMUM_LOAN_AMOUNT && loanPeriod <= Constants.MAXIMUM_LOAN_PERIOD) {
            loanPeriod++;
            finalLoanAmount =  (creditModifier * loanPeriod);
        }

        if (finalLoanAmount < Constants.MINIMUM_LOAN_AMOUNT) {
            throw new InvalidLoanAmountException("No valid loan found");
        }
        return new Decision(finalLoanAmount, loanPeriod, null);
    }

    /**
     * Validates age.
     * @param personalCode ID code of the customer that made the request.
     * @throws PersonalCodeException If the personal code is invalid
     * @throws InvalidLoanAgeException If the age is invalid
     */

    private void validateAge(String personalCode) throws InvalidLoanAgeException, PersonalCodeException {
        LocalDate dateOfBirth = parser.getDateOfBirth(personalCode);
        if (Period.between(dateOfBirth, LocalDate.now()).getYears() < Constants.MINIMUM_LOAN_AGE) {
            throw new InvalidLoanAgeException("Invalid age");
        }
        if (parser.getGender(personalCode).equals(Gender.MALE)) {
            if (dateOfBirth.plusYears(Constants.MAXIMUM_LOAN_AGE_MALE).isBefore(LocalDate.now().plusMonths(60))){
                throw new  InvalidLoanAgeException("Invalid age");
            }
        } else if (parser.getGender(personalCode).equals(Gender.FEMALE)) {
            if (dateOfBirth.plusYears(Constants.MAXIMUM_LOAN_AGE_FEMALE).isBefore(LocalDate.now().plusMonths(60))){
                throw new  InvalidLoanAgeException("Invalid age");
            }
        }
    }

    /**
     * Calculates the credit modifier of the customer to according to the last four digits of their ID code.
     * Debt - 0000...2499
     * Segment 1 - 2500...4999
     * Segment 2 - 5000...7499
     * Segment 3 - 7500...9999
     *
     * @param personalCode ID code of the customer that made the request.
     * @return Segment to which the customer belongs.
     */
    private Long getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));

        if (segment < 2500) {
            return 0L;
        } else if (segment < 5000) {
            return Constants.SEGMENT_1_CREDIT_MODIFIER;
        } else if (segment < 7500) {
            return Constants.SEGMENT_2_CREDIT_MODIFIER;
        }
        return Constants.SEGMENT_3_CREDIT_MODIFIER;
    }

    /**
     * Verify that all inputs are valid according to business rules.
     * If inputs are invalid, then throws corresponding exceptions.
     * @param personalCode Provided personal ID code
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     */
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (!(Constants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                || !(loanAmount <= Constants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (!(Constants.MINIMUM_LOAN_PERIOD <= loanPeriod)
                || !(loanPeriod <= Constants.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }

    }
}
