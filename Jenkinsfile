pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        echo "Start build"
        sh 'echo "Using maven version \$(./mvnw --version)"'
        sh 'echo "Using java version \$(java --version)"'
        sh 'git clone https://github.com/andrewShillito/spring-security-demo.git'
        sh 'pwd'
        sh 'ls'
        sh 'git checkout main'
        sh './mvnw clean install'
      }
    }
    stage('JUnit Tests') {
      steps {
        echo "Start test"
        sh 'echo "Using maven version \$(./mvnw --version)"'
        sh 'echo "Using java version \$(java --version)"'
        sh './mvnw clean verify'
      }
    }
    stage('Automation Tests') {
      steps {
        echo "Start test"
        sh 'echo "Using maven version \$(./mvnw --version)"'
        sh 'echo "Using java version \$(java --version)"'
        sh './mvnw clean verify'
        sh './mvnw spring-boot:run &'
        // todo: wait for spring boot actuator health check
        sh 'sleep 1000'
        sh '`./mvnw clean verify -DrunSuite=com.demo.security.spring.suites.BrowserAutomationSuite`'
      }
    }
  }
}