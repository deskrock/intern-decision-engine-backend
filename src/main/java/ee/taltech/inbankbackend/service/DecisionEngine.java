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
 * The loan amount is calculated based on the customer's credit modifier,
 * which is determined by the last four digits of their ID code.
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
     * The loan amount must be between 2000 and 10000€ (inclusive).
     *
     * @param personalCode ID code of the customer that made the request.
     * @param requestedLoanAmount Requested loan amount
     * @param requestedLoanPeriod Requested loan period
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws NoValidLoanException If there is no valid loan found for the given ID code, loan amount and loan period
     */
    public Decision calculateApprovedLoan(String personalCode, Long requestedLoanAmount, int requestedLoanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException {

        try {
            verifyInputs(personalCode, requestedLoanAmount, requestedLoanPeriod);
        } catch (Exception e) {
            return new Decision(null, null, e.getMessage());
        }

        creditModifier = getCreditModifier(personalCode);
        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found, because of existing debt!");
        }

        double creditScore = calculateCreditScore(creditModifier, requestedLoanAmount, requestedLoanPeriod);

        if (creditScore < 0.1) {
            return findAlternativeLoanOption(requestedLoanPeriod);
        }

        int outputLoanAmount = Math.min(DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT,
                calculateLoanAmount(requestedLoanPeriod));

        return new Decision(outputLoanAmount, requestedLoanPeriod, null);
    }

    /**
     * Searches for an alternative loan period that meets the minimum required credit score.
     *
     * @param requestedLoanPeriod The initially requested loan period.
     * @return A Decision object with an alternative loan period and amount.
     * @throws NoValidLoanException If no valid loan option is found.
     */
    private Decision findAlternativeLoanOption(int requestedLoanPeriod) throws NoValidLoanException {
        while (requestedLoanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            requestedLoanPeriod++;
            int alternativeLoanAmount = calculateLoanAmount(requestedLoanPeriod);

            if (alternativeLoanAmount >= DecisionEngineConstants.MINIMUM_LOAN_AMOUNT &&
                    calculateCreditScore(creditModifier, (long) alternativeLoanAmount, requestedLoanPeriod) >= 0.1) {
                return new Decision(alternativeLoanAmount, requestedLoanPeriod, null);
            }
        }
        throw new NoValidLoanException("No valid loan found!");
    }

    /**
     * Calculates the maximum possible loan for a given loan period.
     *
     * @param loanPeriod The requested loan period.
     * @return The calculated loan amount.
     */
    private int calculateLoanAmount(int loanPeriod) {
        return creditModifier * loanPeriod;
    }

    /**
     * Calculates the credit score based on the given formula:
     * credit score = ((credit modifier / loan amount) * loan period) / 10
     *
     * @param creditModifier The credit modifier assigned to the applicant
     * @param loanAmount The requested loan amount
     * @param loanPeriod The requested loan period
     * @return The calculated credit score
     */
    private double calculateCreditScore(int creditModifier, Long loanAmount, int loanPeriod) {
        return ((double) creditModifier / loanAmount) * loanPeriod / 10;
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
    private int getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));

        if (segment < 2500) {
            return 0;
        } else if (segment < 5000) {
            return DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER;
        } else if (segment < 7500) {
            return DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER;
        }

        return DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER;
    }

    /**
     * Validates that all inputs meet business requirements.
     *
     * @param personalCode The customer's personal ID code.
     * @param loanAmount The requested loan amount.
     * @param loanPeriod The requested loan period.
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid.
     * @throws InvalidLoanAmountException If the requested loan amount is out of bounds.
     * @throws InvalidLoanPeriodException If the requested loan period is out of bounds.
     */
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (loanAmount < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT ||
                loanAmount > DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (loanPeriod < DecisionEngineConstants.MINIMUM_LOAN_PERIOD ||
                loanPeriod > DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }
    }

}
