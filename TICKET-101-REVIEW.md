## Positives

- The REST service has clear separation of concerns, separating controller, service and DTOs
- The code is readable and explained well with comments
- README is well-written and provides clear instructions on running the code
- Correctly implemented calculations
- Input validation is done well, using `EstonianPersonalCodeValidator`
- Exception handling properly covers multiple cases

## Changes to be considered

- `DecisionEngine.java` contains input validation, modifier, and loan amount calculation. It would be better to split it to comply with single responsibility principle (SRP)

- Extending Exception rather than Throwable is recommended and is better suited for custom exceptions in this case

- When there is no valid loan amount, status 200 with an error message would be better suited for this case. Status 404 is used when the server cannot find the requested resource

- Creating a single custom exception with different exception statuses rather than creating different exceptions would be a better alternative to comply with "don't repeat yourself" (DRY) principle

## Most important shortcoming

The maximum loan period was set to 60 months. However, according to business logic, it is supposed to be 48 months. This is the most critical shortcoming, since it might lead to financial losses due to approving loans that would not have been approved otherwise.
