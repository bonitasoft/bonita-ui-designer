#!/usr/bin/env groovy

timestamps {
    ansiColor('xterm') {
        node {
            stage('Checkout 🌍') {
                checkout scm
            }

            stage('Build ⚙️') {
                sh "./mvnw package -DskipTests"
            }

            stage('Deploy docker 🐳') {
                sh """
                    export DOCKER_HOST=${dockerUrl}
                    (docker kill nightly-uid-${BASE_BRANCH} ) || true
                    (docker rm -vf nightly-uid-${BASE_BRANCH}) || true
                    docker build -t bonita/uid-nightly-${BASE_BRANCH} .
                    docker run -p ${PORT}:8080 --name=nightly-uid-${BASE_BRANCH} -d bonita/uid-nightly-${BASE_BRANCH}
                    echo "Container is running on http://${dockerUrl}:${PORT}/bonita"
                """
            }
        }
    }
}
