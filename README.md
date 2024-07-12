# About

This repository is an in-progress spring security example app.

# How to run

**Requires Java 21 JDK**

**Dockerhub access is required for the default spring profiles**

## IntelliJ startup

See existing run configurations defined in [.run](.run). These should be imported by intelliJ automatically. **SpringSecurityDemoApplication** is the default run config. It uses a local postgres container which is started automatically by spring-boot-docker-compose. Alternatively, you can run **SpringSecurityDemoApplication - inMemoryUsers** to startup without a db container but note that the security setup for the in memory profile is more limited.

## Command line startup

Can be run with ./mvnw commands or using mvn if installed locally. Originally tested using mvn 3.6.3. 

### Default spring profile

`./mvnw spring-boot:run`

The above command is equivalent to:

`./mvnw spring-boot:run -Dspring-boot.run.profiles=default,postgres`

The default profiles populate a set of example users into the postgres db from [example-users.json](src/main/resources/seed/example-users.json). 

### Other spring profiles

#### inMemoryUsers

Not really recommended as I maintained support for it more to make sure the application had cross-profile support at runtime and during test suites. Note that this profile also turns off docker-compose startup of postgres and adminer containers. The example users populated into system are stored in [example-users.json](src/main/resources/seed/example-users.json).

Example usage:

`./mvnw spring-boot:run -Dspring-boot.run.profiles=inMemoryUsers`

## To run test suites

`./mvnw clean verify`

## To regenerate dev environment example data

Set property `example-data.regenerate` to true causes example data which is seeded into the DB to be regenerated during startup prior to it being populated into the db.
When regenerated, the data is also written to the source files which are used when not generating data on startup. This allows also for referencing the data which was populated into the db on startup.

Example data generation leverages the DataFaker library.

#### Some other related properties:

- `example-data.cards.count` - sets the number of cards per user to create during regeneration
- `example-data.loan.count` - sets the number of loans per user to create during regeneration
- `example-data.notice.count` - sets the number of notices to create during regeneration
- `example-data.message.count` - sets the number of contact messages to create during regeneration
- `example-data.account.count` - sets the number of accounts per user to create during regeneration
- `example-data.user.count` - sets the number of randomized users to create during regeneration. There is also a set of standard non-randomized users which is always created.