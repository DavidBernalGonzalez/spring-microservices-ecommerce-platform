pipeline {
    parameters {
        choice(name: 'DEPLOY', choices: ['yes', 'no'], description: 'Desplegar en Kubernetes tras build exitoso')
    }
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
  - name: docker
    image: docker:24
    command: ['cat']
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  - name: kubectl
    image: bitnami/kubectl:latest
    command: ['cat']
    tty: true
  - name: jnlp
    env:
    - name: JENKINS_TUNNEL
      value: "jenkins-agent.jenkins.svc.cluster.local:50000"
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
"""
        }
    }
    stages {
        stage('Build & Test - Product Service') {
            steps {
                container('maven') {
                    dir('microservices-platform/product-service') {
                        sh 'mvn clean install'
                    }
                }
            }
        }
        stage('Build & Test - Inventory Service') {
            steps {
                container('maven') {
                    dir('microservices-platform/inventory-service') {
                        sh 'mvn clean install'
                    }
                }
            }
        }
        stage('Build & Test - Order Service') {
            steps {
                container('maven') {
                    dir('microservices-platform/order-service') {
                        sh 'mvn clean install'
                    }
                }
            }
        }
        stage('Build & Test - Gateway Service') {
            steps {
                container('maven') {
                    dir('microservices-platform/gateway-service') {
                        sh 'mvn clean install'
                    }
                }
            }
        }
        stage('Deploy to Kubernetes') {
            when { expression { params.DEPLOY == 'yes' } }
            steps {
                container('docker') {
                    sh '''
                        echo "Construyendo imagenes Docker..."
                        docker build -t product-service:latest -f microservices-platform/product-service/Dockerfile microservices-platform/product-service
                        docker build -t inventory-service:latest -f microservices-platform/inventory-service/Dockerfile microservices-platform/inventory-service
                        docker build -t order-service:latest -f microservices-platform/order-service/Dockerfile microservices-platform/order-service
                        docker build -t gateway-service:latest -f microservices-platform/gateway-service/Dockerfile microservices-platform/gateway-service
                        echo "Imagenes construidas correctamente"
                    '''
                }
                container('kubectl') {
                    sh '''
                        echo "Creando namespace y bases de datos..."
                        kubectl apply -f k8s/ecommerce/namespace.yaml
                        kubectl apply -f k8s/ecommerce/mysql-product.yaml
                        kubectl apply -f k8s/ecommerce/mysql-order.yaml
                        kubectl apply -f k8s/ecommerce/mysql-inventory.yaml
                        echo "Esperando 60s a que MySQL este listo..."
                        sleep 60
                        echo "Desplegando microservicios..."
                        kubectl apply -f k8s/ecommerce/product-service.yaml
                        kubectl apply -f k8s/ecommerce/inventory-service.yaml
                        kubectl apply -f k8s/ecommerce/order-service.yaml
                        kubectl apply -f k8s/ecommerce/gateway-service.yaml
                        echo "Despliegue completado. Gateway: http://localhost:30088"
                    '''
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
