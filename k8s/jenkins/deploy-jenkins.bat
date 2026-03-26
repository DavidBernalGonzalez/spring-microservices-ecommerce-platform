@echo off
setlocal
set "SKIP_PAUSE=0"
if /i "%~1"=="nopause" set "SKIP_PAUSE=1"

echo ==========================================
echo Deploying Jenkins on Kubernetes
echo ==========================================
echo.

:: Verificar que Helm esta instalado
helm version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Helm no esta instalado.
    echo.
    echo Instalacion: choco install kubernetes-helm
    echo O: https://helm.sh/docs/intro/install/
    echo.
    if "%SKIP_PAUSE%"=="0" pause
    exit /b 1
)

:: Verificar que kubectl funciona
kubectl cluster-info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] No hay conexion al cluster Kubernetes.
    echo Asegurate de que Docker Desktop tiene Kubernetes habilitado.
    echo.
    if "%SKIP_PAUSE%"=="0" pause
    exit /b 1
)

echo Añadiendo repo de Jenkins...
helm repo add jenkins https://charts.jenkins.io
helm repo update

echo.
echo Instalando Jenkins en namespace jenkins...
set "SCRIPT_DIR=%~dp0"
set "VALUES_FILE=%SCRIPT_DIR%values.yaml"

helm upgrade --install jenkins jenkins/jenkins ^
  -n jenkins --create-namespace ^
  -f "%VALUES_FILE%"

echo.
echo Aplicando parche jenkinsTunnel (puerto 50000 en jenkins-agent)...
kubectl apply -f "%SCRIPT_DIR%jcasc-patch.yaml"

echo.
echo Creando namespace ecommerce y RBAC para deploy desde pipeline (rama dev)...
kubectl apply -f "%~dp0..\ecommerce\namespace.yaml"
kubectl apply -f "%SCRIPT_DIR%jenkins-agent-rbac.yaml"

echo.
echo Esperando a que Jenkins este listo (puede tardar 2-3 minutos)...
kubectl wait --for=condition=ready pod -l app.kubernetes.io/component=jenkins-master -n jenkins --timeout=300s

echo.
echo ==========================================
echo Jenkins desplegado
echo ==========================================
echo.
echo Acceso (LoadBalancer en Docker Desktop, suele ser puerto 8080):
echo   http://localhost:8080
echo Si no abre, ejecuta: k8s\jenkins\jenkins-port-forward.bat y usa la URL que indique.
echo.
kubectl get svc -n jenkins jenkins
echo.
echo Obtener password inicial:
echo   kubectl exec -n jenkins deployment/jenkins -c jenkins -- cat /run/secrets/additional/chart-admin-password
echo.
if "%SKIP_PAUSE%"=="0" pause
