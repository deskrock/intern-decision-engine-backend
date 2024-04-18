# Conclusion for TICKET-101

## What intern did well:
- **Code organization and project structure:** Classes are separated into different packages with meaningful names.
- **Exception Handling:** The intern implemented custom exception classes for different cases.
- **Controller layer:** The endpoint name, request body, response body, response code, and exception handling look good.
- **Verifying ID code and inputs:** The `getCreditModifier` method looks correct.
- **Intern wrote unit and integration tests** for each method and for different cases.

## The most important shortcoming:
The `calculateApprovedLoan` and `highestValidLoanAmount` methods are not perfect. For example, if a customer wants to get a loan but a suitable loan amount is not found within the selected period, the decision engine should also try to find a new suitable period. To approve the loan, the credit score should be >= 1. It follows from the formula that it's not important if the credit score is 2 or 3; we are only interested if it's 1 or more or less than 1. The biggest loan we can get when the credit score is 1. So our formula for `maxLoan` would look like this: `maxLoan = (creditModifier * loanPeriod) / 1` (but dividing by 1 doesn't change anything, so our formula could look like this: `maxLoan = creditModifier * loanPeriod`). We get the maximum loan amount, and if it's bigger than the maximum loan limit, then `maxLoan = maxLoanLimit`; if it's less than the minimum loan limit, then we need to increase `loanPeriod`. 

## Places for improvement:
- Improved project structure and class names to make it more readable and easier to scale the application.
- Changed `int` and `long` to `Integer` and `Long` to make the code more readable and more solid.
- Fixed the `calculateApprovedLoan` and `highestValidLoanAmount` methods.
- Intern did many exception classes but did not specify them for each case, so they all just get message and cause. And if we don't specify exceptions then we can have just one and in our endpoint we will have only one catch what will increase code readability.