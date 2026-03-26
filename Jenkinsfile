// Ramas no dev (p. ej. main): solo CI Maven con Docker — sin Kubernetes en el pipeline.
// Rama dev: mismo CI + Deploy ejecutando k8s/ecommerce/deploy-all.sh en un Pod con Docker + kubectl.
// El stage Deploy usa when { beforeAgent true } para no exigir cloud K8s en builds de main.

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
            }
        }
        stage('Deploy Kubernetes (solo dev)') {
            when {
                beforeAgent true
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
                container('docker') {
                    sh 'chmod +x k8s/ecommerce/deploy-all.sh && bash k8s/ecommerce/deploy-all.sh'
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
