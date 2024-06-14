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

### Other spring profiles

#### inMemoryUsers

Note really recommended as I maintained support for it more to make sure the application had cross-profile support at runtime and during test suites. It uses an in memory user management service which is seeded from a configured [csv file](./src/main/resources/seed/in-memory-users.csv). Note that this turns off docker-compose startup of postgres and adminer containers.

Example usage:

`./mvnw spring-boot:run -Dspring-boot.run.profiles=inMemoryUsers`

## To run test suites

`./mvnw clean verify`