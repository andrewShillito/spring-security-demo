# How to run

*Requires Java 21*

## In IntelliJ

See existing run configurations defined in [.run](.run). These should be imported by intelliJ automatically. **SpringSecurityDemoApplication** is the default run config. It uses a local postgres container which is started automatically by spring-boot-docker-compose. Alternatively, you can run **SpringSecurityDemoApplication - inMemoryUsers** to startup without a db container. 

*Note that you will need dockerhub credentials in order to pull the postgres and adminer images if they are not already present.*

### Default spring profile

`./mvnw spring-boot:run`


The above command is equivalent to:

`./mvnw -P default spring-boot:run`

and

`./mvnw spring-boot:run -Dspring-boot.run.profiles=default,postgres`

### Other spring profiles

#### inMemoryUsers

Uses an in memory user management service which is seeded from a configured [csv file](./src/main/resources/seed/in-memory-users.csv). Note that this turns off docker-compose startup of postgres and adminer containers.

Example usage:

`./mvnw -P inMemoryUsers spring-boot:run`

or

`./mvnw spring-boot:run -Dspring-boot.run.profiles=inMemoryUsers`