@echo off
setlocal EnableDelayedExpansion
REM Ejecuta el mismo CI que Jenkins (mvnw en los 4 servicios) y, si todo pasa,
REM despliega en el Kubernetes de TU PC (kubectl apunta a Docker Desktop, minikube, etc.).
REM Uso (raiz del repo): scripts\dev-local-test-and-deploy.bat
REM Requisitos: JDK, Docker, kubectl con contexto correcto (kubectl config current-context).

cd /d "%~dp0.."

echo ========== 1/2 Tests (ci-local) ==========
call scripts\ci-local.bat
if errorlevel 1 (
  echo Tests fallidos. No se despliega.
  exit /b 1
)

echo.
echo ========== 2/2 Deploy local a Kubernetes ==========
echo Contexto kubectl:
kubectl config current-context 2>nul || echo [AVISO] kubectl no disponible o sin contexto.
echo.

set SKIP_DEPLOY_PAUSE=1
call k8s\ecommerce\deploy-all.bat
set EXITCODE=%ERRORLEVEL%
if not "%EXITCODE%"=="0" exit /b %EXITCODE%
echo.
echo Listo: tests OK y deploy lanzado contra tu cluster local.
exit /b 0
