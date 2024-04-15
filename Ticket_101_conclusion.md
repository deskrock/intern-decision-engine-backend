# Ticket 101 Conclusion

## Summary:
`TICKET-101` aimed to implement a decision engine for calculating the maximum approved loan amount and period based on the provided personal code, loan amount, and loan period. The code provided in the ticket  appears to address this requirement effectively.

## Strengths:
- The ticket implementation follows good coding practices with clear variable names and structured methods, enhancing readability and maintainability.
- Input validation is implemented robustly, handling various exceptions and ensuring the integrity of the calculations.
- The code mostly follows Single Responsibility Principle (SRP).
- Classes and Methods are well documented.
- The code is covered by unit tests, ensuring the reliability of the implementation and facilitating future changes.

## Areas for Improvement:
- Having separate exception classes for different types of exceptions leads to duplication of code and violates the principle of DRY (Don't Repeat Yourself).
- The best practice is to centralize error messages, ensuring a clean separation of concerns and adhering to the Single Responsibility Principle.

## Important Shortcoming:

The most significant shortcoming of `TICKET-101` is the incorrect `Lombok` dependency implementation in the Gradle file. **This issue must be resolved ASAP to allow the project to compile.**

Refer to the official Lombok documentation for proper Gradle setup: https://projectlombok.org/setup/gradl 

