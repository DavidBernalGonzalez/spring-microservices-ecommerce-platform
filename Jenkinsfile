// CI: Pod K8s con imagen Maven (executor Jenkins; no despliega la app). No requiere docker en el controller.
// main: solo este stage. dev: + Deploy (deploy-all.sh) con when { beforeAgent true }.
// Si no tienes cloud Kubernetes en Jenkins, instala Docker en el agente y usa agent { docker { image '...' } } en CI.

pipeline {
    agent none
    stages {
        stage('CI - Maven (todas las ramas)') {
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
  - name: jnlp
    env:
    - name: JENKINS_TUNNEL
      value: "jenkins-agent.jenkins.svc.cluster.local:50000"
"""
                }
            }
            steps {
                container('maven') {
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
