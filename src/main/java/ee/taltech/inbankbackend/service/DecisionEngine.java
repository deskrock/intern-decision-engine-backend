package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.utils.DecisionEngineConstant;
import ee.taltech.inbankbackend.exception.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exception.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exception.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exception.NoValidLoanException;
import ee.taltech.inbankbackend.model.Decision;
import ee.taltech.inbankbackend.utils.Validator;
import org.springframework.stereotype.Service;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier,
 * which is determined by the last four digits of their ID code.
 */
@Service
public class DecisionEngine {
    private Validator validator;

    DecisionEngine(Validator validator){
        this.validator = validator;
    }

    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 60 months (inclusive).
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
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod) throws InvalidLoanPeriodException, InvalidPersonalCodeException, InvalidLoanAmountException, NoValidLoanException {
        validator.verifyPersonalCode(personalCode);
        validator.verifyLoanAmount(loanAmount);
        validator.verifyLoanPeriod(loanPeriod);
        int outputLoanAmount;
        int creditModifier = getCreditModifier(personalCode);

        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found!");
        }

        while (highestValidLoanAmount(creditModifier, loanPeriod) < DecisionEngineConstant.MINIMUM_LOAN_AMOUNT) {
            loanPeriod++;
        }

        if (loanPeriod <= DecisionEngineConstant.MAXIMUM_LOAN_PERIOD) {
            outputLoanAmount = Math.min(DecisionEngineConstant.MAXIMUM_LOAN_AMOUNT, highestValidLoanAmount(creditModifier, loanPeriod));
        } else {
            throw new NoValidLoanException("No valid loan found!");
        }

        return new Decision(outputLoanAmount, loanPeriod, null);
    }

    /**
     * Calculates the largest valid loan for the current credit modifier and loan period.
     *
     * @return Largest valid loan amount
     */
    private int highestValidLoanAmount(int creditModifier, int loanPeriod) {
        return creditModifier * loanPeriod;
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
            return DecisionEngineConstant.SEGMENT_1_CREDIT_MODIFIER;
        } else if (segment < 7500) {
            return DecisionEngineConstant.SEGMENT_2_CREDIT_MODIFIER;
        }

        return DecisionEngineConstant.SEGMENT_3_CREDIT_MODIFIER;
    }

}
