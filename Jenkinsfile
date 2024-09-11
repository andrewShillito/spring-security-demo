pipeline {
  agent any
  stages {
    stage('Build and test') {
      steps {
        cleanWs()
        sh 'echo "Using java version \$(java --version)"'
        sh 'pwd'
        sh 'ls'
        dir("./spring-security-demo") {
          sh 'echo "Using maven version \$(./mvnw --version)"'
          sh 'pwd'
          sh 'ls'
          sh './mvnw clean install'
        }
      }
    }
    stage('Publish Jacoco results') {
      steps {
        dir("./spring-security-demo") {
          sh 'pwd'
          sh 'ls'
          jacoco( execPattern: '**/**.exec', classPattern: '**/**/classes/com', sourcePattern: '**/src/main/java' )
        }
      }
    }
  }
  post {
    always {
      dir("./spring-security-demo") {
        junit(testResults: '**/target/surefire-reports/*.xml', skipPublishingChecks: true)
        archiveArtifacts(artifacts: 'target/**', allowEmptyArchive: true)
      }
      cleanWs()
    }
  }
}