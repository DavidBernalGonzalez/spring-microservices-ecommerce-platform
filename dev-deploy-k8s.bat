@echo off
setlocal
echo ==========================================
echo Desplegar en Kubernetes (manual, sin tocar el entorno local)
echo El push a la rama dev ya despliega solo desde Jenkins.
echo ==========================================
echo.

call "%~dp0k8s\ecommerce\deploy-all.bat"
