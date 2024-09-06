pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        echo "Start build"
        echo "Using maven version \$(mvn --version)"
        echo "Using java version \$(java --version)"
        sh 'git clone https://github.com/andrewShillito/spring-security-demo.git'
        sh 'pwd'
        sh 'ls'
        sh 'git checkout main'
        sh 'mvn clean install'
      }
    }
    stage('JUnit Tests') {
      steps {
        echo "Start test"
        echo "Using maven version \$(mvn --version)"
        echo "Using java version \$(java --version)"
        sh 'mvn clean verify'
      }
    }
    stage('Automation Tests') {
      steps {
        echo "Start test"
        echo "Using maven version \$(mvn --version)"
        echo "Using java version \$(java --version)"
        sh 'mvn clean verify'
        sh 'mvn spring-boot:run &'
        // todo: wait for spring boot actuator health check
        sh 'sleep 1000'
        sh '`mvn clean verify -DrunSuite=com.demo.security.spring.suites.BrowserAutomationSuite`'
      }
    }
  }
}