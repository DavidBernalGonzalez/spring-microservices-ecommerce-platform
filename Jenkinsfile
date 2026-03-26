// Multibranch: job apuntando al repo; el stage de deploy solo corre en rama `dev`.
// Push a `dev`: CI + despliegue en Kubernetes del mismo Docker Desktop (misma maquina).
// No se detiene nada en local; Maven/DB en tu PC siguen como esten.

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
  - name: docker
    image: docker:24
    command: ['cat']
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
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
        stage('Deploy to Kubernetes (rama dev)') {
            when {
                expression {
                    def b = env.BRANCH_NAME ?: ''
                    def g = env.GIT_BRANCH ?: ''
                    return b == 'dev' || g == 'origin/dev' || g?.endsWith('/dev')
                }
            }
            steps {
                timeout(time: 20, unit: 'MINUTES') {
                    container('docker') {
                        sh '''
                            echo "Imagenes desde JARs ya construidos en CI (sin repetir Maven ni tests)..."
                            docker build -t product-service:latest -f microservices-platform/product-service/Dockerfile.runtime microservices-platform/product-service
                            docker build -t inventory-service:latest -f microservices-platform/inventory-service/Dockerfile.runtime microservices-platform/inventory-service
                            docker build -t order-service:latest -f microservices-platform/order-service/Dockerfile.runtime microservices-platform/order-service
                            docker build -t gateway-service:latest -f microservices-platform/gateway-service/Dockerfile.runtime microservices-platform/gateway-service
                            echo "Descargando kubectl..."
                            wget -qO /usr/local/bin/kubectl "https://dl.k8s.io/release/v1.31.2/bin/linux/amd64/kubectl"
                            chmod +x /usr/local/bin/kubectl
                            echo "Aplicando manifiestos..."
                            kubectl apply -f k8s/ecommerce/namespace.yaml
                            kubectl apply -f k8s/ecommerce/mysql-product.yaml
                            kubectl apply -f k8s/ecommerce/mysql-order.yaml
                            kubectl apply -f k8s/ecommerce/mysql-inventory.yaml
                            echo "Esperando MySQL (60s)..."
                            sleep 60
                            kubectl apply -f k8s/ecommerce/product-service.yaml
                            kubectl apply -f k8s/ecommerce/inventory-service.yaml
                            kubectl apply -f k8s/ecommerce/order-service.yaml
                            kubectl apply -f k8s/ecommerce/gateway-service.yaml
                            echo "Despliegue en ecommerce OK. Gateway NodePort: http://localhost:30088"
                        '''
                    }
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
