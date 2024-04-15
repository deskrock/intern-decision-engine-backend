## Review of TICKET-101

### Good Parts

- Intern created the core part of the implementation of the decision engine
- Intern uses Data Transfer Objects (DTO) to represent the expected and exposed (response) data and doing this, we make sure we limit what we provide to outside.
- Implementation uses smart approach by understanding the core logic of this decision engine is to provide the maximum loan amount possible to the user.
It assumes that the maximum amount of loan comes from the case when `credit_modifier = 1` which makes the `(credit modifier / loan amount) * loan period) = 1`
Then if we want to get the maximum amount that supports this condition, we can just multiply both side with loan amount and this equation turns into
`loan amount = credit modifier * loan period` and with this simplification we find the maximum amount of loan that we can provide without looping and this reduces the complexity to O(1). 
The intern also uses this amount to compare with the lowest possible value to find out if we need to search for a different loan period.
- Intern documented the code by following the `Javadoc` standards
- Provided some unit and integration tests cases

### Problems & Improvement Suggestions

- Intern did not use the git commits effectively. Instead of committing every change in a single commit, I would suggest dividing them into reasonable sizes and with relative short descriptions in commit message.
- I believe intern did not set the local git config right as the second commit has a different username and email, and it does not link to any GitHub user.
- Intern wrote integration test cases by mocking the services and making the mocked bean return exactly what the intern wants it to return. This gives the illusion of testing and does not allow us to detect any bugs.
- Intern decided to use the `DecisionResponse` Dto as a class variable and this can cause multi-thread problems such as race conditions. By default, Spring boot beans are singleton and if there are concurrent requests, this approach might override the response data of another request.
Imagine the case when a positive loan request and a negative loan request comes at the same time but because of concurrency before controller returns the answer of the positive loan request, the negative result overrides the answer, and we return negative answer to both of the loan request.
This should not be a class variable, and it should be a POJO (Plain Old Java Object) and created for each request separately
- Similar problem lies in `DecisionEngine` as the intern decided to make `creditModifier` as class variable, multiple concurrent loan calculations will override each others credit modifiers.
This value needs to be in local scope instead of class variable.
- The project structure seems to be off as multiple packages/folders contains different DTOs and constants are inside of config package.
- `DecisionEngine` breaks the `Single Responsibility` principle as it handles input validation, business logic and exception handling at the same time. This is a service layer, and it should handle only the business logic.
- The `verifyInputs` method of `DecisionEngine` is coupled and not reusable. Instead of a single method, we could split the checks in separate methods to make it reusable in case future decisions might have only the subgroup of the defined input group.
- We are using `Spring Boot 3` and this means we do not need to `@AutoWired` the constructors when there is only one.
- `DecisionResponse` should not be annotated by `@Component` as it would create a singleton bean of it.
- Integration test exception messages do not match with defined messages in code and in README file and we cannot detect it because of the mocking.
- `givenUnexpectedError_whenRequestDecision_thenReturnsInternalServerError` test does not bring any value as we handle the exception cases and if there are exceptions that we did not cover, will be thrown as internal server error by default.
- There are double assertion in integration test, first it gets asserted in JSON format then it gets asserted in POJO.
- The unit tests uses InjectMock annotation but there are not mocks to inject and `DecisionEngine` does not take any property in the constructor. I also believe that mocking shouldn't exist in unit testing
- `DecisionEngineController` handles both communication between layers and handling the exception cases. These exceptions can happen in other possible future controllers as well 
and I believe it would lead to cleaner solution if we handle the exceptions in a specific area that exists only for exception handling purpose. This also makes our controllers feel like they have single responsibility to do.


### Nitpick Suggestions

- The constants are only used inside of `DecisionEngine` and I believe because of its concept, I do not believe that there can be case of reusability of those constants. 
I believe coupling the constants by putting it inside the class would make it easy to understand as the reader does not need to switch between classes
- I believe naming the `DecisionEngine` to something like `DecisionService` to demonstrate that it is service layer could improve the readability as it would be easier for the newcomers to understand that is a service layer class.
- Custom Exceptions should extend `Exception` instead of `Throwable` as they are exception and not an `Error`
- As `Decision` uses final fields, we can use `Record` type for this class as it is immutable
- I prefer integration tests as less mocking as possible because mocking kills the concept of testing unless we are not mocking the third party services such as external API calls etc.
- Tests uses `assert` keyword of Java which we have built-in support but Spring Boot includes `Junit 5` which comes with `Assert` methods that gives more detailed answer when assertion fails, and it also improves readability as it removes the symbols and adds texts like `assertt x == y` vs `Assert.Equals(x, y)`
If tester is not aware of context, they may not be able to detect which one is expected which one is provided but if we use the `Assert` methods, they can distinguish the expected one and actual one.
- I would suggest that intern should use `Sonarqube` to detect code smells and potential bugs. As this tool is free and available [open source](https://github.com/SonarSource/sonarqube)

### Suggestions for next code review

I believe this type of review can feel like it changes the context fast. Maybe if I separated the review per classes it might be easier for the intern. I would ask this approach to the intern if that would be the preferred way and adjust my approach accordingly.