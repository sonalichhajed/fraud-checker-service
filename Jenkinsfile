pipeline {
	agent any
	stages {
		stage('Build & Test') {
			steps {
				sh './gradlew clean build'
			}
		}
    stage('Build docker image') {
      steps {
        sh '''
          COMMIT_ID=$(git rev-parse HEAD)
          docker build -t fraud-checker-service:${COMMIT_ID} .
        '''
      }
    }
	}
}
