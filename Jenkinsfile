// CI: agent any + Maven Wrapper (./mvnw). No Docker ni cloud Kubernetes en el controller.
// Requiere JDK en el PATH del agente (java -version). main: solo este stage.
// dev: + Deploy con Kubernetes (deploy-all.sh); hace falta cloud K8s solo para ese stage.

pipeline {
    agent none
    stages {
        stage('CI - Maven (todas las ramas)') {
            agent any
            steps {
                sh '''
                    set -e
                    for svc in product-service inventory-service order-service gateway-service; do
                      echo "=== $svc ==="
                      cd "${WORKSPACE}/microservices-platform/${svc}"
                      chmod +x mvnw
                      ./mvnw -B clean install
                    done
                '''
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
