pipeline {
  agent any
  tools {
      maven 'Maven 3.9.9'
  }
  stages {
    stage('Build') {
      steps {
        echo "Start build"
        cleanWs()
        sh 'echo "Using java version \$(java --version)"'
        sh 'rm -rf ./spring-security-demo'
        sh 'git clone https://github.com/andrewShillito/spring-security-demo.git'
        sh 'pwd'
        sh 'ls'
        dir("./spring-security-demo") {
          sh 'echo "Using maven version \$(./mvnw --version)"'
          sh 'pwd'
          sh 'ls'
          sh 'git checkout main'
          sh 'git pull'
          sh './mvnw clean install'
        }
      }
    }
    stage('Automation Tests') {
      steps {
        echo "Starting browser automation tests"
        dir("./spring-security-demo") {
          sh 'echo "Using maven version \$(./mvnw --version)"'
          sh 'echo "Using java version \$(java --version)"'
          sh './mvnw clean verify'
          sh './mvnw spring-boot:run'
          // todo: wait for spring boot actuator health check
          sh 'sleep 1000'
          sh '`./mvnw clean verify -DrunSuite=com.demo.security.spring.suites.BrowserAutomationSuite`'
        }
      }
    }
  }
}