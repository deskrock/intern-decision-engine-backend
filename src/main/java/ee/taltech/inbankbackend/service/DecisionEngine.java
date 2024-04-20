package ee.taltech.inbankbackend.service;

import static ee.taltech.inbankbackend.config.DecisionEngineConstants.MAXIMUM_EXPECTED_LIFETIME;
import static ee.taltech.inbankbackend.config.DecisionEngineConstants.MAXIMUM_LOAN_PERIOD_IN_YEARS;
import static ee.taltech.inbankbackend.config.DecisionEngineConstants.MINIMUM_ELIGIBLE_AGE;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.AgeRestrictionException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exceptions.NoValidLoanException;
import java.time.LocalDate;
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

  /**
   * Calculates the maximum loan amount and period for the customer based on their ID code,
   * the requested loan amount and the loan period.
   * The loan period must be between 12 and 60 months (inclusive).
   * The loan amount must be between 2000 and 10000€ months (inclusive).
   *
   * @param personalCode ID code of the customer that made the request.
   * @param loanAmount   Requested loan amount
   * @param loanPeriod   Requested loan period
   * @return A Decision object containing the approved loan amount and period, and an error message (if any)
   * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
   * @throws InvalidLoanAmountException   If the requested loan amount is invalid
   * @throws InvalidLoanPeriodException   If the requested loan period is invalid
   * @throws NoValidLoanException         If there is no valid loan found for the given ID code, loan amount and loan period
   */
  public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod)
      throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
      NoValidLoanException, AgeRestrictionException {
    verifyInputs(personalCode, loanAmount, loanPeriod);

    int creditModifier = getCreditModifier(personalCode);
    if (creditModifier == 0) {
      throw new NoValidLoanException("No valid loan found due to debt!");
    }

    Integer maxLoanAmount = findMaxLoanAmount(loanPeriod, creditModifier);

    if (maxLoanAmount != null) {
      return new Decision(maxLoanAmount, loanPeriod, null);
    }

    return findAdjustedLoanAmount(creditModifier);
  }

  /**
   * Attempts to find an adjusted loan amount and period that meets the approval criteria
   * when the initially requested loan amount cannot be approved.
   * It iterates through all possible loan periods and amounts, calculating the credit score
   * for each combination until it finds a valid loan or exhausts all options.
   *
   * @param creditModifier The credit modifier calculated based on the customer's ID code.
   * @return A Decision object containing an approvable loan amount and period.
   * @throws NoValidLoanException If no valid loan configuration can be found.
   */
  private Decision findAdjustedLoanAmount(int creditModifier) throws NoValidLoanException {
    for (int period = DecisionEngineConstants.MINIMUM_LOAN_PERIOD; period <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD; period++) {
      for (int amount = DecisionEngineConstants.MINIMUM_LOAN_AMOUNT; amount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT; amount += 1) {
        double creditScore = calculateCreditScore(creditModifier, amount, period);;
        if (creditScore >= 1) {
          return new Decision(amount, period, null);
        }
      }
    }
    throw new NoValidLoanException("No valid loan found within the constraints.");
  }

  /**
   * Finds the maximum loan amount that can be approved for a given loan period and credit modifier.
   * It iterates through possible loan amounts from the minimum to the maximum,
   * calculating the credit score for each. The iteration stops when a loan amount
   * results in a credit score below 1, indicating that higher amounts will not be approvable.
   *
   * @param loanPeriod The requested loan period.
   * @param creditModifier The credit modifier calculated based on the customer's ID code.
   * @return The maximum approvable loan amount, or null if no amount can be approved.
   */
  private Integer findMaxLoanAmount(int loanPeriod, int creditModifier) {
    Integer maxLoanAmount = null;
    for (int amount = DecisionEngineConstants.MINIMUM_LOAN_AMOUNT;
         amount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT; amount += 1) {
      double creditScore = calculateCreditScore(creditModifier, amount, loanPeriod);
      if (creditScore >= 1) {
        maxLoanAmount = amount;
      } else {
        break; // Break the loop if the credit score falls below 1, as higher amounts won't be valid
      }
    }
    return maxLoanAmount;
  }

  /**
   * Calculates the credit score based on the credit modifier, loan amount, and loan period.
   * The credit score is used to determine the approvability of a loan.
   * A higher credit score indicates a higher likelihood of loan approval.
   *
   * @param creditModifier The credit modifier calculated based on the customer's ID code.
   * @param amount The loan amount for which the credit score is being calculated.
   * @param period The loan period over which the loan amount would be repaid.
   * @return The calculated credit score.
   */
  private double calculateCreditScore(int creditModifier, long amount, int period) {
    return (double) creditModifier / amount * period;
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
        throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
        AgeRestrictionException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
      if (isInvalidAgeForGivenId(personalCode)) {
        throw new AgeRestrictionException("Loan not granted due to age restrictions.");
      }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                || !(loanAmount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_PERIOD <= loanPeriod)
                || !(loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }

    }

  /**
   * Extracts the birthdate from the given identification code.
   *
   * @param idCode The identification code containing information about birthdate.
   * @return The birthdate extracted from the identification code.
   */
  private LocalDate extractBirthdate(String idCode) {
    // Extracting the century, year, month, and day from the identification code.
    int century = Integer.parseInt(idCode.substring(0, 1));
    int year = Integer.parseInt(idCode.substring(1, 3));
    int month = Integer.parseInt(idCode.substring(3, 5));
    int day = Integer.parseInt(idCode.substring(5, 7));

    // Determining the prefix for the birth year based on the century digit.
    // 1-2: 19th century; 3-4: 20th century; 5-6: 21st century
    int prefix;
    if (century <= 2) {
      prefix = 1800;
    } else if (century <= 4) {
      prefix = 1900;
    } else {
      prefix = 2000;
    }

    // Adding the prefix to the extracted birth year to get the full birth year.
    year += prefix;

    return LocalDate.of(year, month, day);
  }

  /**
   * Calculates the age based on the birthdate extracted from the identification code.
   *
   * @param idCode The identification code containing information about birthdate.
   * @return The calculated age based on the birthdate.
   */
  private int calculateAge(String idCode) {
    LocalDate birthdate = extractBirthdate(idCode);
    LocalDate now = LocalDate.now();

    int age = now.getYear() - birthdate.getYear();

    // Adjusting age if the current month and day are before the birth month and day.
    if (now.getMonthValue() < birthdate.getMonthValue() ||
        (now.getMonthValue() == birthdate.getMonthValue() && now.getDayOfMonth() < birthdate.getDayOfMonth())) {
      age--;
    }

    return age;
  }

  /**
   * Determines if the age extracted from the identification code is valid for a loan application.
   *
   * @param idCode The identification code containing information about birthdate.
   * @return True if the age is invalid for a loan application, false otherwise.
   */
  private boolean isInvalidAgeForGivenId(String idCode) {
    int age = calculateAge(idCode);
    return age < MINIMUM_ELIGIBLE_AGE || age > (MAXIMUM_EXPECTED_LIFETIME - MAXIMUM_LOAN_PERIOD_IN_YEARS);
  }
}
