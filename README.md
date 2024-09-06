# About

This repository is an in-progress spring security example app.

# How to run

**Requires Java 21 JDK**

**Dockerhub access is required for the default spring profiles**

## IntelliJ startup

See existing run configurations defined in [.run](.run). These should be imported by intelliJ automatically. **SpringSecurityDemoApplication** is the default run config. It uses a local postgres container which is started automatically by spring-boot-docker-compose.

## Command line startup

Can be run with `./mvnw` command or using `mvn` if installed locally. Originally tested using mvn version 3.6.3. 

### Default spring profile

`./mvnw spring-boot:run`

The above command is equivalent to:

`./mvnw spring-boot:run -Dspring-boot.run.profiles=default,postgres`

The default profiles populate lists of example users, accounts, loans, cards, notices, and contact messages into the postgres db from files in directory [./src/main/resources/seed/](./src/main/resources/seed/). This data can be regenerated during startup as well using property `-Dexample-data.regenerate=true`. See [Regenerating dev environment example data](#regenerating-dev-environment-example-data) for details on regenerating example data. 

### Other spring profiles

#### prod

A demo production-like profile which changes some things about the security configuration for the app including requiring https and limited concurrent sessions for users.

Example usage:

`./mvnw spring-boot:run -Dspring-boot.run.profiles=prod`

## Test suites

`./mvnw clean verify`

The default test suite is [FullRegressionSuite.java](./src/test/java/com/demo/security/spring/suites/FullRegressionSuite.java)
which runs all non-browser automation junit tests. To run any other existing test suite the command format is:

`./mvnw clean verify -DrunSuite=${classPathToTestSuite}`

For example to run the browser automation test suite [BrowserAutomationSuite.java](./src/test/java/com/demo/security/spring/suites/BrowserAutomationSuite.java):

1. Start the application using an IDE or `mvn spring-boot:run`
2. Execute this command to trigger the browser automation test suite:

`./mvnw clean verify -DrunSuite=com.demo.security.spring.suites.BrowserAutomationSuite`

Playwright will sow `ERR_CONNECTION_REFUSED` if the application is not running.

The playwright testing setup supports some environment parameters:

- `playwright.application.base-url` - The base url for the running spring boot application without http or https prefix. The default value is 'localhost:8080'. 
- `playwright.application.is-https` - Whether the spring boot app is using https or http. The default value is 'false' which means it expects the application urls to use http.
- `playwright.headless` - Controls if playwright shows the browser as the test suite runs. The default value is 'false' which means the browser will show.

## Regenerating dev environment example data

Setting property `example-data.regenerate=true` either in properties files or from the command line ie: `-Dexample-data.regenerate=true` causes example data which is seeded into the DB to be regenerated during startup prior to it being populated into the db.
When regenerated, the data is also written to `.json` files in [./src/main/resources/seed](./src/main/resources/seed) for reference. Those files are also used to populate the db during startup when `example-data.regenerate=false`.
The intelliJ run configuration `SpringSecurityDemoApplication Regenerate Example Data` can be used to startup and regenerate example data.

Example data generation leverages the DataFaker library.

#### Some other related properties:

- `example-data.cards.count` - sets the number of cards per user to create during regeneration
- `example-data.loan.count` - sets the number of loans per user to create during regeneration
- `example-data.notice.count` - sets the number of notices to create during regeneration
- `example-data.message.count` - sets the number of contact messages to create during regeneration
- `example-data.account.count` - sets the number of accounts per user to create during regeneration
- `example-data.user.count` - sets the number of randomized users to create during regeneration. There is also a set of standard non-randomized users which is always created.