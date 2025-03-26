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

        double creditScore = calculateCreditScore(loanAmount.intValue(), loanPeriod);
        int maxLoanAmount = findMaxLoanAmount(loanPeriod);
        if (creditScore >= DecisionEngineConstants.CREDIT_SCORE_THRESHOLD) {
            return new Decision(maxLoanAmount, loanPeriod);
        } else {
            if (maxLoanAmount >= DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) {
                return new Decision(maxLoanAmount, loanPeriod);
            }
            for (int period = DecisionEngineConstants.MINIMUM_LOAN_PERIOD;
                 period <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD; period++) {
                maxLoanAmount = findMaxLoanAmount(period);
                if (maxLoanAmount >= DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) {
                    return new Decision(maxLoanAmount, period);
                }
            }
            throw new NoValidLoanException("No valid loan found within constraints!");
        }
    }
    /**
     * Calculates the credit score of the customer based on the requested loan amount and period.
     * The credit score is calculated as follows:
     * (creditModifier / loanAmount) * loanPeriod / 10
     *
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @return The credit score of the customer
     */
    private double calculateCreditScore(int loanAmount, int loanPeriod) {
        return ((double) creditModifier / loanAmount) * loanPeriod / 10.0;
    }

    /**
     * Finds the maximum loan amount that the customer can get based on their credit score.
     * The loan amount is decremented by 100 until a valid loan amount is found.
     *
     * @param loanPeriod Requested loan period
     * @return The maximum loan amount that the customer can get
     */
    private int findMaxLoanAmount(int loanPeriod) {
        for (int amount = DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT;
             amount >= DecisionEngineConstants.MINIMUM_LOAN_AMOUNT; amount -= 100) {
            double score = calculateCreditScore(amount, loanPeriod);
            if (score >= DecisionEngineConstants.CREDIT_SCORE_THRESHOLD) {
                return amount;
            }
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
