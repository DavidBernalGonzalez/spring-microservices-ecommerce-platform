@echo off
setlocal
title Ensure Jenkins (K8s)
:: Ejecutar al iniciar sesion (acceso directo en shell:startup) o al abrir Docker.
:: Espera a que Kubernetes responda y aplica Helm + RBAC igual que deploy-jenkins.bat (sin pause al final).

set "RETRIES=0"
:wait_k8s
kubectl cluster-info >nul 2>&1
if not errorlevel 1 goto :k8s_ready
helm version >nul 2>&1
if errorlevel 1 exit /b 1
set /a RETRIES+=1
if %RETRIES% GTR 72 (
  echo [ensure-jenkins] Timeout esperando Kubernetes ^(6 min^). Abre Docker Desktop y vuelve a ejecutar.
  exit /b 1
)
timeout /t 5 /nobreak >nul
goto wait_k8s

:k8s_ready
call "%~dp0deploy-jenkins.bat" nopause
exit /b %ERRORLEVEL%
