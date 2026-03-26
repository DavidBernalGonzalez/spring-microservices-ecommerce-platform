@echo off
setlocal
title STOP DEV ENVIRONMENT

set "BASE_DIR=%~dp0"
cd /d "%BASE_DIR%"

set "DOCKER_HOST="
set "DOCKER_TLS_VERIFY="
set "DOCKER_CERT_PATH="
set "DOCKER_CONTEXT="

set "DOCKER_EXE=docker"
if exist "%ProgramFiles%\Docker\Docker\resources\bin\docker.exe" (
  set "DOCKER_EXE=%ProgramFiles%\Docker\Docker\resources\bin\docker.exe"
  set "PATH=%ProgramFiles%\Docker\Docker\resources\bin;%PATH%"
) else if exist "%ProgramFiles(x86)%\Docker\Docker\resources\bin\docker.exe" (
  set "DOCKER_EXE=%ProgramFiles(x86)%\Docker\Docker\resources\bin\docker.exe"
  set "PATH=%ProgramFiles(x86)%\Docker\Docker\resources\bin;%PATH%"
) else if exist "%LocalAppData%\Docker\Docker\resources\bin\docker.exe" (
  set "DOCKER_EXE=%LocalAppData%\Docker\Docker\resources\bin\docker.exe"
  set "PATH=%LocalAppData%\Docker\Docker\resources\bin;%PATH%"
)

powershell -Command "Write-Host '==========================================' -ForegroundColor Yellow; Write-Host 'Stopping Microservices Development Environment' -ForegroundColor Yellow; Write-Host '==========================================' -ForegroundColor Yellow; Write-Host ''"

echo [1/2] Docker Compose ^(MySQL: db-products, db-orders, db-inventory^)...
"%DOCKER_EXE%" compose down
if errorlevel 1 (
  powershell -Command "Write-Host '[Aviso] docker compose down devolvio error ^(Docker parado?^).' -ForegroundColor DarkYellow"
)

echo.
echo [2/2] Liberando puertos locales de Spring Boot ^(8081, 8082, 8083, 8088^)...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ports = 8081,8082,8083,8088; foreach ($p in $ports) { Get-NetTCPConnection -LocalPort $p -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue } }"

powershell -Command "Write-Host ''; Write-Host 'Listo. Para arrancar de nuevo: start-dev.bat' -ForegroundColor Green; Write-Host ''"
