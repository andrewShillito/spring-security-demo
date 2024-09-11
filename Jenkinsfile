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
// TODO: playwright automation test stage(s) - requires resolving docker certs issue
//     stage('Automation Tests') {
//       steps {
//         echo "Starting browser automation tests"
//         dir("./spring-security-demo") {
//           sh 'echo "Using maven version \$(./mvnw --version)"'
//           sh 'echo "Using java version \$(java --version)"'
//           sh ('./mvnw spring-boot:run -Dspring-boot.run.profiles=default,postgres')
//           sh 'sleep 30'
//           sh './mvnw clean verify -DrunSuite=com.demo.security.spring.suites.BrowserAutomationSuite -Dplaywright.headless=true'
//           catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
//             sh './mvnw spring-boot:stop'
//           }
//         }
//       }
//     }
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
//         TODO: may be needed once browser automation stage is running
//         catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
//           sh './mvnw spring-boot:stop'
//         }
      }
      cleanWs()
    }
  }
}