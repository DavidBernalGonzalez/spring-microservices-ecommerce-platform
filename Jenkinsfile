// =============================================================================
// Un solo Jenkinsfile para TODAS las ramas (main, dev, feature/*).
// Deploy en dev si (cualquiera):
//   A) Variable Jenkins ENABLE_K8S_DEPLOY = true / 1 / yes (sin importar mayúsculas)
//   B) Archivo jenkins.properties en la raíz del repo con ENABLE_K8S_DEPLOY=true
//      (copia jenkins.properties.example → jenkins.properties y commit en dev).
// Multibranch: si la variable global no llega al job, usa jenkins.properties en el repo.
// Deploy SIN Kubernetes cloud: solo "agent any". Debes tener en ESE nodo:
//   1) Docker daemon accesible (el cliente puede faltar: el pipeline descarga el binario estático).
//      - Jenkins en Docker: monta -v /var/run/docker.sock:/var/run/docker.sock
//      - Jenkins en VM Linux: apt install docker.io (o docker-ce) y usuario jenkins en grupo docker
//   2) kubectl apuntando a tu cluster: KUBECONFIG o /var/jenkins_home/.kube/config (o el home del usuario del agente)
// Tras el CI se define env.K8S_DEPLOY_ENABLED (variable Jenkins + jenkins.properties en repo).
// Local: bash scripts/ci-local.sh  |  scripts\dev-local-test-and-deploy.bat
// =============================================================================

pipeline {
    agent none
    stages {
        stage('CI - Maven (todas las ramas)') {
            agent any
            steps {
                sh 'chmod +x scripts/ci-local.sh && bash scripts/ci-local.sh'
                script {
                    def deployFromFile = false
                    if (fileExists('jenkins.properties')) {
                        def content = readFile('jenkins.properties')
                        def m = (content =~ /(?m)^\s*ENABLE_K8S_DEPLOY\s*=\s*(\S+)/)
                        if (m.find()) {
                            def val = m.group(1).trim()
                            if (val.equalsIgnoreCase('true') || val == '1' || val.equalsIgnoreCase('yes')) {
                                deployFromFile = true
                            }
                        }
                    }
                    def envVal = env.ENABLE_K8S_DEPLOY?.trim()
                    // Multibranch a veces no rellena env.* aunque la variable exista en el proceso del agente
                    if (!envVal) {
                        try {
                            envVal = sh(script: 'echo -n "${ENABLE_K8S_DEPLOY:-}"', returnStdout: true).trim()
                        } catch (Throwable ignored) {
                            envVal = ''
                        }
                    }
                    def envEnabled = envVal && (envVal.equalsIgnoreCase('true') || envVal == '1' || envVal.equalsIgnoreCase('yes'))
                    env.K8S_DEPLOY_ENABLED = (envEnabled || deployFromFile) ? 'true' : 'false'

                    echo "[K8S deploy flag] jenkins.properties presente=${fileExists('jenkins.properties')} ENABLE_K8S_DEPLOY (env/shell)='${envVal}' desde archivo=${deployFromFile} => K8S_DEPLOY_ENABLED=${env.K8S_DEPLOY_ENABLED} rama=${env.BRANCH_NAME}"

                    def b = env.BRANCH_NAME ?: ''
                    def g = env.GIT_BRANCH ?: ''
                    def isDev = b == 'dev' || g == 'origin/dev' || g?.endsWith('/dev')
                    if (isDev) {
                        stash name: 'maven-targets', includes: 'microservices-platform/**/target/*.jar', allowEmpty: false
                    }
                }
            }
        }
        stage('Deploy a Kubernetes omitido (dev sin ENABLE_K8S_DEPLOY)') {
            when {
                beforeAgent true
                expression {
                    def b = env.BRANCH_NAME ?: ''
                    def g = env.GIT_BRANCH ?: ''
                    def isDev = b == 'dev' || g == 'origin/dev' || g?.endsWith('/dev')
                    return isDev && env.K8S_DEPLOY_ENABLED != 'true'
                }
            }
            agent any
            steps {
                echo 'Rama dev: deploy no ejecutado. Actívalo con ENABLE_K8S_DEPLOY=true/1/yes en Jenkins o jenkins.properties en el repo (ver jenkins.properties.example).'
            }
        }
        stage('Deploy to Kubernetes (solo rama dev)') {
            when {
                beforeAgent true
                expression {
                    def b = env.BRANCH_NAME ?: ''
                    def g = env.GIT_BRANCH ?: ''
                    def isDev = b == 'dev' || g == 'origin/dev' || g?.endsWith('/dev')
                    return isDev && env.K8S_DEPLOY_ENABLED == 'true'
                }
            }
            agent any
            steps {
                // Nodo con Docker + kubeconfig (o in-cluster). checkout + unstash cubren workspace nuevo o reutilizado.
                checkout scm
                unstash 'maven-targets'
                timeout(time: 20, unit: 'MINUTES') {
                    sh '''
                        set -e
                        ARCH=$(uname -m)
                        case "$ARCH" in
                            x86_64) K8S_ARCH=amd64; DOCKER_ARCH=x86_64 ;;
                            aarch64|arm64) K8S_ARCH=arm64; DOCKER_ARCH=aarch64 ;;
                            *) echo "Arquitectura no soportada: $ARCH"; exit 1 ;;
                        esac

                        if ! command -v docker >/dev/null 2>&1; then
                            echo "docker no está en PATH; descargando cliente estático (hace falta daemon en /var/run/docker.sock)..."
                            DOCKER_VER=24.0.9
                            wget -qO /tmp/docker-static.tgz "https://download.docker.com/linux/static/stable/${DOCKER_ARCH}/docker-${DOCKER_VER}.tgz"
                            rm -rf /tmp/docker-cli-extract
                            mkdir -p /tmp/docker-cli-extract
                            tar -xzf /tmp/docker-static.tgz -C /tmp/docker-cli-extract
                            export PATH="/tmp/docker-cli-extract/docker:$PATH"
                        fi
                        docker info >/dev/null 2>&1 || {
                            echo "ERROR: Docker daemon no accesible."
                            echo "  - Jenkins en contenedor: añade -v /var/run/docker.sock:/var/run/docker.sock al run"
                            echo "  - Linux: instala Docker y pon al usuario jenkins en el grupo docker"
                            exit 1
                        }

                        if ! command -v kubectl >/dev/null 2>&1; then
                            wget -qO /tmp/kubectl "https://dl.k8s.io/release/v1.31.2/bin/linux/${K8S_ARCH}/kubectl"
                            chmod +x /tmp/kubectl
                            export PATH="/tmp:$PATH"
                        fi

                        echo "Imagenes desde JARs del CI..."
                        docker build -t product-service:latest -f microservices-platform/product-service/Dockerfile.runtime microservices-platform/product-service
                        docker build -t inventory-service:latest -f microservices-platform/inventory-service/Dockerfile.runtime microservices-platform/inventory-service
                        docker build -t order-service:latest -f microservices-platform/order-service/Dockerfile.runtime microservices-platform/order-service
                        docker build -t gateway-service:latest -f microservices-platform/gateway-service/Dockerfile.runtime microservices-platform/gateway-service
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
    post {
        success {
            echo 'Build completado correctamente'
        }
        failure {
            echo 'Build fallido'
        }
    }
}
