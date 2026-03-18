pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.9-eclipse-temurin-21
    command: ['cat']
    tty: true
"""
        }
    }
    stages {
        stage('Build & Test - Product Service') {
            steps {
                dir('microservices-platform/product-service') {
                    sh 'mvn clean install'
                }
            }
        }
        stage('Build & Test - Inventory Service') {
            steps {
                dir('microservices-platform/inventory-service') {
                    sh 'mvn clean install'
                }
            }
        }
        stage('Build & Test - Order Service') {
            steps {
                dir('microservices-platform/order-service') {
                    sh 'mvn clean install'
                }
            }
        }
        stage('Build & Test - Gateway Service') {
            steps {
                dir('microservices-platform/gateway-service') {
                    sh 'mvn clean install'
                }
            }
        }
    }
    post {
        success {
            echo 'Build completado correctamente'
        }
        failure {
            echo 'Build fallido'
        }
    }
}
