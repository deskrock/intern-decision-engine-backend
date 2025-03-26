package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exceptions.NoValidLoanException;
import org.springframework.stereotype.Service;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier, determined by their personal ID code.
 */
@Service
public class DecisionEngine {

    // Used to check for the validity of the presented ID code.
    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private int creditModifier = 0;

    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 48 months (inclusive).
     * The loan amount must be between 2000 and 10000€ months (inclusive).
     *
     * @param personalCode ID code of the customer that made the request.
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws NoValidLoanException If there is no valid loan found for the given ID code, loan amount and loan period
     */
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException {
        verifyInputs(personalCode, loanAmount, loanPeriod);

        creditModifier = getCreditModifier(personalCode);
        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found due to debt!");
        }

        // Find the maximum loan amount that the customer can get for the requested loan period
        int maxLoanAmount = findMaxLoanAmount(loanPeriod);
        if (maxLoanAmount != 0) {
            return new Decision(maxLoanAmount, loanPeriod);
        }

        // Find the minimum loan period that the customer can get the requested loan amount for
        int minLoanPeriod = findMinLoanPeriod(loanAmount.intValue());
        if (minLoanPeriod != 0) {
            return new Decision(loanAmount.intValue(), minLoanPeriod);
        }

        // If no valid loan found, throw an exception
        throw new NoValidLoanException("No valid loan found within constraints!");

    }

    /**
     * Finds the maximum loan amount that the customer can get for the requested loan period.
     *
     * @param loanPeriod Requested loan period
     * @return The maximum loan amount that the customer can get
     */
    private int findMaxLoanAmount(int loanPeriod) {
        int amount = creditModifier * loanPeriod;
        if (amount >= DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) {
            return Math.min(amount, DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT);
        }
        return 0;
    }

    /**
     * Finds the minimum loan period that the customer can get the requested loan amount for.
     *
     * @param loanAmount Requested loan amount
     * @return The minimum loan period that the customer can get
     */
    private int findMinLoanPeriod(int loanAmount) {
        int period = loanAmount / creditModifier;
        if (period >= DecisionEngineConstants.MINIMUM_LOAN_PERIOD
                && period <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            return period;
        }
        return 0;
    }

    /**
     * Returns the credit modifier based on the task requirements.
     * @param personalCode The personal ID code of the customer
     * @return The credit modifier
     */
    private int getCreditModifier(String personalCode) {
        return switch (personalCode) {
            case "49002010965" -> 0; // debt
            case "49002010976" -> DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER; // 100
            case "49002010987" -> DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER; // 300
            case "49002010998" -> DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER; // 1000
            default -> 0;
        };
    }

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
     */
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (loanAmount < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT
                || loanAmount > DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (loanPeriod < DecisionEngineConstants.MINIMUM_LOAN_PERIOD
                || loanPeriod > DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }

    }
}
