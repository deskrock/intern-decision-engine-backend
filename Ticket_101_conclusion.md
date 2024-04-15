# Ticket 101 Conclusion

## Summary:
`TICKET-101` aimed to implement a decision engine for calculating the maximum approved loan amount and period based on the provided personal code, loan amount, and loan period. The code provided in the ticket  appears to address this requirement effectively.

## Strengths:
- The ticket implementation follows good coding practices with clear variable names and structured methods, enhancing readability and maintainability.
- Input validation is implemented robustly, handling various exceptions and ensuring the integrity of the calculations.
- The code follows Single Responsibility Principle (SRP).
- Classes and Methods are well documented.

## Areas for Improvement:
- Although the errors are handled well the Strings should be stored in one central location which adheres to the Single Responsibility Principle by separating the responsibility of managing error messages from other functionalities in the codebase.

## Important Shortcoming:

The most significant shortcoming of `TICKET-101` is the incorrect `Lombok` dependency implementation in the Gradle file. **This issue must be resolved ASAP to allow the project to compile.**

Refer to the official Lombok documentation for proper Gradle setup: https://projectlombok.org/setup/gradl 

