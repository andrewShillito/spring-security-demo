# About

This repository is an in-progress spring security example app.

# How to run

**Requires Java 21 JDK**

**Dockerhub access is required for the default spring profiles**

## IntelliJ startup

See existing run configurations defined in [.run](.run). These should be imported by intelliJ automatically. **SpringSecurityDemoApplication** is the default run config. It uses a local postgres container which is started automatically by spring-boot-docker-compose.

## Command line startup

Can be run with ./mvnw commands or using mvn if installed locally. Originally tested using mvn 3.6.3. 

### Default spring profile

`./mvnw spring-boot:run`

The above command is equivalent to:

`./mvnw spring-boot:run -Dspring-boot.run.profiles=default,postgres`

The default profiles populate a set of example users into the postgres db from [example-users.json](src/main/resources/seed/example-users.json). 

### Other spring profiles

#### prod

A demo production-like profile which changes some things about the security configuration for the app including requiring https and limited concurrent sessions for users.

Example usage:

`./mvnw spring-boot:run -Dspring-boot.run.profiles=prod`

## To run test suites

`./mvnw clean verify`

## To regenerate dev environment example data

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