// CI: agent Docker (Maven). No exige Kubernetes cloud (sirve para main y Jenkins sin K8s).
// El nodo que ejecute el stage debe tener Docker instalado y accesible (plugin Docker Pipeline).
// Si el controller no tiene Docker, asigna un agente con label (p. ej. docker) o instala Docker ahí.
// dev — Deploy: sigue usando Kubernetes (requiere cloud + plugin Kubernetes en ese mismo Jenkins).

pipeline {
    agent none
    stages {
        stage('CI - Maven (todas las ramas)') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-21'
                }
            }
            steps {
                dir('microservices-platform/product-service') {
                    sh 'mvn clean install'
                }
                dir('microservices-platform/inventory-service') {
                    sh 'mvn clean install'
                }
                dir('microservices-platform/order-service') {
                    sh 'mvn clean install'
                }
                dir('microservices-platform/gateway-service') {
                    sh 'mvn clean install'
                }
                script {
                    def b = env.BRANCH_NAME ?: ''
                    def g = env.GIT_BRANCH ?: ''
                    def isDev = b == 'dev' || g == 'origin/dev' || g?.endsWith('/dev')
                    if (isDev) {
                        stash name: 'maven-targets', includes: 'microservices-platform/**/target/*.jar', allowEmpty: false
                    }
                }
            }
        }
        stage('Deploy to Kubernetes (solo rama dev)') {
            when {
                expression {
                    def b = env.BRANCH_NAME ?: ''
                    def g = env.GIT_BRANCH ?: ''
                    return b == 'dev' || g == 'origin/dev' || g?.endsWith('/dev')
                }
            }
            agent {
                kubernetes {
                    yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
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
            steps {
                unstash 'maven-targets'
                timeout(time: 20, unit: 'MINUTES') {
                    container('docker') {
                        sh '''
                            echo "Imagenes desde JARs del CI..."
                            docker build -t product-service:latest -f microservices-platform/product-service/Dockerfile.runtime microservices-platform/product-service
                            docker build -t inventory-service:latest -f microservices-platform/inventory-service/Dockerfile.runtime microservices-platform/inventory-service
                            docker build -t order-service:latest -f microservices-platform/order-service/Dockerfile.runtime microservices-platform/order-service
                            docker build -t gateway-service:latest -f microservices-platform/gateway-service/Dockerfile.runtime microservices-platform/gateway-service
                            wget -qO /usr/local/bin/kubectl "https://dl.k8s.io/release/v1.31.2/bin/linux/amd64/kubectl"
                            chmod +x /usr/local/bin/kubectl
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
                            echo "Despliegue OK. Gateway: http://localhost:30088 (NodePort)"
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
