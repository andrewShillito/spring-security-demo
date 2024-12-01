# About

This repository is an in-progress spring security example app.

# How to run

**Requires Java 21 JDK**

**Dockerhub access is required for the default spring profiles**

## Building the project

Run `./mvnw clean install` from the root directory of the project or if you wish to skip tests then run `./mvnw clean install -DskipTests`.

## IntelliJ startup

See existing run configurations defined in [.run](.run). These should be imported by intelliJ automatically. **SpringSecurityDemoApplication** is the default run config. It uses a local postgres container which is started automatically by spring-boot-docker-compose.

## Command line startup

Can be run with `./mvnw` command or using `mvn` if installed locally. Originally tested using mvn version 3.6.3. 

### Default spring profile

`./mvnw spring-boot:run`

The above command is equivalent to:

`./mvnw spring-boot:run -Dspring-boot.run.profiles=default,postgres,dockerCompose,liquibase`

The default profiles populate lists of example users, accounts, loans, cards, notices, and contact messages into the postgres db from files in directory [./src/main/resources/seed/](./src/main/resources/seed/). This data can be regenerated during startup as well using property `-Dexample-data.regenerate=true`. See [Regenerating dev environment example data](#regenerating-dev-environment-example-data) for details on regenerating example data. 

### Other spring profiles

#### prod

A demo production-like profile which changes some things about the security configuration for the app including requiring https and limited concurrent sessions for users.

Example usage:

`./mvnw spring-boot:run -Dspring-boot.run.profiles=default,prod,postgres,dockerCompose,liquibase`

## Test suites

`./mvnw clean verify`

The default test suite is [FullRegressionSuite.java](./src/test/java/com/demo/security/spring/suites/FullRegressionSuite.java)
which runs all non-browser automation junit tests. To run any other existing test suite the command format is:

`./mvnw clean verify -DrunSuite=${classPathToTestSuite}`

For example to run the browser automation test suite [BrowserAutomationSuite.java](./src/test/java/com/demo/security/spring/suites/BrowserAutomationSuite.java):

1. Start the application using an IDE or `mvn spring-boot:run`
2. Execute this command to trigger the browser automation test suite:

`./mvnw clean verify -DrunSuite=com.demo.security.spring.suites.BrowserAutomationSuite`

Playwright will show `ERR_CONNECTION_REFUSED` if the application is not running.

The playwright testing setup supports some environment parameters:

- `playwright.application.base-url` - The base url for the running spring boot application without http or https prefix. The default value is 'localhost:8080'. 
- `playwright.application.is-https` - Whether the spring boot app is using https or http. The default value is 'false' which means it expects the application urls to use http.
- `playwright.headless` - Controls if playwright shows the browser as the test suite runs. The default value is 'false' which means the browser will show.

## Dev environment example data

The default value for property `example-data.enabled` is `true` which enables insertion of example users, accounts, account transactions, cards, contact messages, loans, and notices into the database during application startup *if the database is empty of those items*. If you set `example-data.enabled=false` then initial example data will not be inserted into the database.

## Regenerating dev environment example data

Setting property `example-data.regenerate=true` either in properties files or from the command line ie: `-Dexample-data.regenerate=true` causes example data which is seeded into the DB to be regenerated during startup prior to it being populated into the db.
When regenerated, the data is also written to the `.json` files in [./src/main/resources/seed](./src/main/resources/seed) for reference. Those files are also used to populate the db during startup when `example-data.enabled=true` and `example-data.regenerate=false`.
The intelliJ run configuration `SpringSecurityDemoApplication Regenerate Example Data` can be used to startup and regenerate example data.

Example data generation leverages the DataFaker library.

#### Some other related properties:

- `example-data.accounts.count` - sets the number of accounts per user to create during regeneration
- `example-data.accounts.populate` - sets if startup population of example accounts data should run 
- `example-data.cards.count` - sets the number of cards per user to create during regeneration
- `example-data.cards.populate` - sets if startup population of example cards data should run 
- `example-data.loans.count` - sets the number of loans per user to create during regeneration
- `example-data.loans.populate` - sets if startup population of example loans data should run 
- `example-data.messages.count` - sets the number of contact messages to create during regeneration
- `example-data.messages.populate` - sets if startup population of example contact messages data should run 
- `example-data.notices.count` - sets the number of notices to create during regeneration
- `example-data.notices.populate` - sets if startup population of example notices data should run 
- `example-data.users.count` - sets the number of randomized users to create during regeneration. There is also a set of standard non-randomized users which is always created.
- `example-data.users.populate` - sets if startup population of example users data should run 

*Populating example user data is required for accounts, cards, and loans example data population to run.*

## Jenkins

Based on [Jenkins Installing Docker](https://www.jenkins.io/doc/book/installing/docker/) and [Post-installation setup wizard](https://www.jenkins.io/doc/book/installing/docker/#setup-wizard).

The docker image which is generated installs all required plugins for running the pipeline defined in [Jenkinsfile](./Jenkinsfile)

1. Create the jenkins-test container using the following commands

```shell
# starting from root directory of project
cd ./jenkins
# build the jenkins image defined in ./jenkins/Dockerfile
bash ./build.sh
# start a jenkins container from that image
bash ./start.sh
# Locate initial admin password which can be located as below in the container logs
docker container logs -f jenkins-test

###
#Jenkins initial setup is required. An admin user has been created and a password generated.
#Please use the following password to proceed to installation:

#theAdminPassword

#This may also be found at: /var/jenkins_home/secrets/initialAdminPassword
###
```

2. Go to localhost:8082 and unlock jenkins using the initial admin password from the container logs
3. The jenkins image produced by [build.sh](./jenkins/build.sh) contains all required plugins to run the existing [Jenkinsfile](./Jenkinsfile) so you can just press install suggested plugins or skip
4. Setup your admin user as desired or skip to keep using the initial generated credentials
5. Go to new item -> select pipeline
6. In pipeline definition select 'Pipeline script from SCM'
7. Select Git in SCM
8. Add this repository's URL in the Repository URL input
9. In Branch Specifier put '*/main' or whichever branch is preferred
10. Script Path should be set to Jenkinsfile
11. Save the build and test the pipeline by triggering it manually using the Build Now button

## Troubleshooting

### Docker

At times the automatic shutdown of associated docker containers does not process correctly and that can cause subsequent startups to fail. Removing the docker postgres & adminer containers as well as associated volumes and then starting up again usually resolves that issue. The development environment docker-compose used for this project is [docker-compose-postgres.yml](./docker-compose-postgres.yml).  