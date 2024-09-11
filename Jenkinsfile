pipeline {
  agent any
  stages {
    stage('Build and test') {
      steps {
        echo "Start build"
        cleanWs()
        sh 'echo "Using java version \$(java --version)"'
        sh 'pwd'
        sh 'git clone https://github.com/andrewShillito/spring-security-demo.git'
        sh 'ls'
        dir("./spring-security-demo") {
          sh 'echo "Using maven version \$(./mvnw --version)"'
          sh 'pwd'
          sh 'ls'
          sh 'git checkout initial-jenkins-pipeline-implementation && git pull'
          sh './mvnw clean install package'
        }
      }
    }
    stage('Publish Jacoco results') {
      steps {
        dir("./spring-security-demo") {
          echo "WARNING: Jacoco reports are currently showing as empty on jenkins coverage report but can be found in archived artifacts at target/site/jacoco/index.html"
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