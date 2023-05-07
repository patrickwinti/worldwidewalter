pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven "M3"
    }

    stages {
        stage('Build') {
            steps {
                sh 'java -version'
                sh "mvn clean test package"
            }

            post {
                success {
                    junit '**/target/surefire-reports/TEST-*.xml'
                    jacoco(
                        execPattern: '**/build/jacoco/*.exec',
                        classPattern: '**/build/classes/java/main',
                        sourcePattern: '**/src/main'
                    )
                }
            }
        }
    }
}
