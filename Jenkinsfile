pipeline {
  agent any
  environment {
    GIT_CHECKOUT_DIR="./spring-security-demo"
  }
  stages {
    stage('Build and test') {
      steps {
        cleanWs()
        sh 'echo "Using java version \$(java --version)"'
        sh 'pwd'
        sh 'ls'
        dir($GIT_CHECKOUT_DIR) {
          sh 'echo "Using maven version \$(./mvnw --version)"'
          sh 'pwd'
          sh 'ls'
          sh './mvnw clean install'
        }
      }
    }
    stage('Publish Jacoco results') {
      steps {
        dir($GIT_CHECKOUT_DIR) {
          sh 'pwd'
          sh 'ls'
          jacoco( execPattern: '**/**.exec', classPattern: '**/**/classes/com', sourcePattern: '**/src/main/java' )
        }
      }
    }
  }
  post {
    always {
      dir($GIT_CHECKOUT_DIR) {
        junit(testResults: '**/target/surefire-reports/*.xml', skipPublishingChecks: true)
        archiveArtifacts(artifacts: 'target/**', allowEmptyArchive: true)
      }
      cleanWs()
    }
  }
}