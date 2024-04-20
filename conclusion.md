# TICKET-101 Review and Conclusion

## Overview

The intern has implemented the MVP scope of the decision engine which evaluates loan applications based on the applicant's personal code, requested loan amount, and loan period. The service successfully meets the basic requirements outlined in TICKET-101.  The purpose of the decision engine is to evaluate the maximum loan amount that can be approvd for the applicant, considering their credit segement or debt status.

### What Was Done Well

- Modular Design: The code is organized into service and controller layers, adhering to the clean architecture that separates concerns. This allows for easier maintenance and testing.
- Exception Handling: Comprehensive error handling is implemented, covering various scenarios like invalid input and no valid loan found.
- Code Comments: The code is well-commented, which helps other developers understand the code better.
- Unit Testing: The code is well-tested, with unit tests for the service layer.
- Integration Testing: The code is also well-tested, with integration tests for the controller layer.
- Documentation: The code is well-documented, with clear and concise comments and documentation. The project includes a [README.md](README.md) file that outlines the purpose of the service, technologies used, installation instructions, API endpoints, and error handling.
- Manging of constants: Instead of using magic numbers the project centralizes constants in the [DecisionEngineConstants.java](src/main/java/ee/taltech/inbankbackend/config/DecisionEngineConstants.java) file. This approach makes the code more readable and maintainable. This approach ensures that any changes to these constants only need to be made in one place, reducing the risk of inconsistencies and errors. The constants are also named in a clear and descriptive manner, making it easy to understand what each constant represents without needing additional context

### Areas of improvement

- API naming: API naming conventions can significantly impact the readability and consistency of codebase. When it comes to naming API endpoints, there's a common debate between using plural vs. singular nouns. The best practice, especially in RESTful API design, leans towards using plural nouns for endpoints. This convention suggests that an endpoint refers to a collection or a resource type rather than an individual instance of that resource. Also including API versioning in the endpoint path or headers to future-proof the API and manage changes more effectively. For example, `/api/v1/loans/decision` for version 1 of the loans decision endpoint.
- Centralize Error Handling: Instead of handling exceptions directly in the controller methods, the intern could use Spring's @ControllerAdvice or @RestControllerAdvice to centralize exception handling across the entire application. This approach reduces boilerplate code in the controller and separates the concerns of request handling and error handling.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidPersonalCodeException.class, InvalidLoanAmountException.class, InvalidLoanPeriodException.class})
    public ResponseEntity<DecisionResponse> handleBadRequest(Exception e) {
        DecisionResponse response = new DecisionResponse();
        response.setErrorMessage(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoValidLoanException.class)
    public ResponseEntity<DecisionResponse> handleNotFound(NoValidLoanException e) {
        DecisionResponse response = new DecisionResponse();
        response.setErrorMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DecisionResponse> handleInternalServerError(Exception e) {
        DecisionResponse response = new DecisionResponse();
        response.setErrorMessage("An unexpected error occurred");
        return ResponseEntity.internalServerError().body(response);
    }
}
```

- Usage of Unnecessary Autowired in DecisionEngineController: The [DecisionEngineController.java](src/main/java/ee/taltech/inbankbackend/endpoint/DecisionEngineController.java) file uses explicit @Autowired annotation which is unnecessary as Springboot will automatically detect and inject the service.

- Remove duplication code for Exception classes: In the exception package there are duplication code for Exception classes. The exception classes could be refactored to extend a common parent exception class.

```java
// base class
public class BaseLoanException extends Exception {
    private final String message;
    private final Throwable cause;

    public BaseLoanException(String message) {
        super(message);
        this.message = message;
        this.cause = null;
    }

    public BaseLoanException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}

// child classes
public class InvalidLoanAmountException extends BaseLoanException {
    public InvalidLoanAmountException(String message) {
        super(message);
    }

    public InvalidLoanAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- Move data transfer classes to corresponding package: Request and response related classes (`DecisionRequest`, `DecisionResponse`) from the @endpoint package and `Decision` of the @service package to a separate package named `dto` can improve the organization and maintainability of the codebase. This approach aligns with the principle of separation of concerns, making the codebase more modular and easier to navigate. It also helps in distinguishing between different types of classes (controllers, services, request/response DTOs, etc.) at a glance.

- Logging: Logging could be implemented at the appropriate levels within the controller and the global exception handler. Logging can provide insights into the flow of requests and errors, which is invaluable for debugging and monitoring.
- Package naming: Though it doesn't matter that much but the base packagename could omit `taltech` from the base package name as the service is not specific to TalTech.
- Lack of a Private Constructor in DecisionEngineConstants: The [DecisionEngineConstants.java](src/main/java/ee/taltech/inbankbackend/config/DecisionEngineConstants.java) file should have a private constructor to prevent instantiation of the class. This is a best practice to enforce the principle of encapsulation, which is a fundamental concept of object-oriented programming.

```java
package ee.taltech.inbankbackend.config;

public class DecisionEngineConstants {
    // Existing constants remain unchanged

    // Private constructor to prevent instantiation
    private DecisionEngineConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
```

- Scoring Algorithm Implementation: The current implementation does not strictly follow the scoring algorithm described in the TICKET-101. The algorithm mentioned involves dividing the credit modifier by the loan amount and multiplying the result by the loan period. However, the actual implementation calculates the highest valid loan amount based on the credit modifier and loan period directly, without considering the requested loan amount in its calculation (`calculateApprovedLoan` method in [DecisionEngine.java](src/main/java/ee/taltech/inbankbackend/service/DecisionEngine.java?plain=1#L38)).

## Most important Shortcoming

The most significant shortcoming is the misinterpretation of the scoring algorithm. The current implementation does not calculate the credit score as specified, which could lead to incorrect loan decisions.

### Fix for the Shortcoming

To address this issue, we need to adjust the implementation to adhere to the scoring algorithm described in TICKET-101. Specifically, we should modify the calculateApprovedLoan method to incorporate the scoring algorithm correctly.

```java
  public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod)
      throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
      NoValidLoanException {
    verifyInputs(personalCode, loanAmount, loanPeriod);

    int creditModifier = getCreditModifier(personalCode);
    if (creditModifier == 0) {
      throw new NoValidLoanException("No valid loan found due to debt!");
    }

    Long maxLoanAmount = findMaxLoanAmount(loanPeriod, creditModifier);

    if (maxLoanAmount != null) {
      return new Decision(Math.toIntExact(maxLoanAmount), loanPeriod, null);
    }

    return findAdjustedLoanAmount(creditModifier);
  }

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

  private Long findMaxLoanAmount(int loanPeriod, int creditModifier) {
    Long maxLoanAmount = null;
    for (long amount = DecisionEngineConstants.MINIMUM_LOAN_AMOUNT;
         amount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT; amount += 1) {
      double creditScore = calculateCreditScore(creditModifier, amount, loanPeriod);
      if (creditScore >= 1) {
        maxLoanAmount = amount;
      } else {
        break;
      }
    }
    return maxLoanAmount;
  }

  private double calculateCreditScore(int creditModifier, long amount, int period) {
    return (double) creditModifier / amount * period;
  }
```
