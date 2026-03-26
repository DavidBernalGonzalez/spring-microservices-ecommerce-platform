@echo off
setlocal

set "BASE_DIR=%~dp0"
set "SERVICES_DIR=%BASE_DIR%microservices-platform"

:: Si existen (por ejemplo de un curso o Docker remoto), rompen la conexion al motor local
set "DOCKER_HOST="
set "DOCKER_TLS_VERIFY="
set "DOCKER_CERT_PATH="
set "DOCKER_CONTEXT="

:: Usar SIEMPRE el docker.exe de Docker Desktop si existe (prepend: evita otro "docker" en PATH)
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

:: Si no estamos dentro de Windows Terminal, relanzar
if "%WT_SESSION%"=="" (
    wt new-tab --title "DEV ENVIRONMENT" --tabColor "#2496ED" cmd /k "\"%~f0\""
    exit /b
)

title DEV ENVIRONMENT

powershell -Command "Write-Host '==========================================' -ForegroundColor Cyan; Write-Host 'Starting Microservices Development Environment' -ForegroundColor Cyan; Write-Host '==========================================' -ForegroundColor Cyan; Write-Host ''"
powershell -Command "Write-Host 'Base directory:' -ForegroundColor Gray; Write-Host '%BASE_DIR%' -ForegroundColor White; Write-Host ''; Write-Host 'Services directory:' -ForegroundColor Gray; Write-Host '%SERVICES_DIR%' -ForegroundColor White; Write-Host ''"

:: Verificar motor Docker (reintentos: el pipe dockerDesktopLinuxEngine tarda a veces tras abrir Docker Desktop)
powershell -Command "Write-Host 'Checking Docker...' -ForegroundColor Yellow"
set TRY=0
:docker_retry
set /a TRY+=1
"%DOCKER_EXE%" info >nul 2>&1
if not errorlevel 1 goto :docker_ok
if %TRY% LSS 8 (
    echo Esperando motor Linux de Docker Desktop ^(intento %TRY%/8, 5s^)...
    timeout /t 5 /nobreak >nul
    goto :docker_retry
)
echo.
echo [ERROR] No se conecta al motor ^(pipe dockerDesktopLinuxEngine^). Diagnostico:
echo ----------------------------------------
"%DOCKER_EXE%" info 2>&1
echo ----------------------------------------
echo.
echo Que significa: el cliente Docker esta bien, pero el motor Linux de Docker Desktop no expone el socket todavia o no arranco.
echo.
echo Prueba en este orden:
echo   1. Cierra Docker Desktop por completo ^(clic derecho en la ballena -^> Quit^), espera 10s y abrelo de nuevo.
echo   2. Docker Desktop -^> Settings -^> General: activa "Use the WSL 2 based engine".
echo   3. Settings -^> Resources -^> WSL Integration: activa tu distro.
echo   4. En PowerShell admin: wsl --update   y   wsl --shutdown   luego reinicia Docker.
echo   5. Troubleshoot -^> Restart Docker Desktop.
echo.
pause
exit /b 1
:docker_ok
powershell -Command "Write-Host 'Docker OK.' -ForegroundColor Green; Write-Host ''"

powershell -Command "Write-Host 'Checking Docker containers...' -ForegroundColor Yellow"
set DB_OK=1

"%DOCKER_EXE%" ps --filter "name=db-products" --filter "status=running" --format "{{.Names}}" | findstr /i "db-products" >nul
if errorlevel 1 set DB_OK=0

"%DOCKER_EXE%" ps --filter "name=db-orders" --filter "status=running" --format "{{.Names}}" | findstr /i "db-orders" >nul
if errorlevel 1 set DB_OK=0

"%DOCKER_EXE%" ps --filter "name=db-inventory" --filter "status=running" --format "{{.Names}}" | findstr /i "db-inventory" >nul
if errorlevel 1 set DB_OK=0

if "%DB_OK%"=="0" (
    powershell -Command "Write-Host 'Docker databases are not running. Starting docker compose...' -ForegroundColor Yellow"
    pushd "%BASE_DIR%"
    "%DOCKER_EXE%" compose up -d
    popd

    powershell -Command "Write-Host ''; Write-Host 'Waiting for databases to initialize...' -ForegroundColor Yellow"
    timeout /t 15 >nul
) else (
    powershell -Command "Write-Host 'Docker databases already running.' -ForegroundColor Green"
)

powershell -Command "Write-Host ''; Write-Host 'Starting microservices...' -ForegroundColor Cyan"

wt ^
new-tab --title "PRODUCT SERVICE" --tabColor "#2ECC71" cmd /k "pushd \"%SERVICES_DIR%\product-service\" && mvnw.cmd spring-boot:run" ; ^
new-tab --title "INVENTORY SERVICE" --tabColor "#3498DB" cmd /k "pushd \"%SERVICES_DIR%\inventory-service\" && mvnw.cmd spring-boot:run" ; ^
new-tab --title "ORDER SERVICE" --tabColor "#F39C12" cmd /k "pushd \"%SERVICES_DIR%\order-service\" && mvnw.cmd spring-boot:run" ; ^
new-tab --title "GATEWAY" --tabColor "#9B59B6" cmd /k "pushd \"%SERVICES_DIR%\gateway-service\" && mvnw.cmd spring-boot:run"

powershell -Command "Write-Host ''; Write-Host '==========================================' -ForegroundColor Green; Write-Host 'Environment started' -ForegroundColor Green; Write-Host '==========================================' -ForegroundColor Green; Write-Host ''; Write-Host 'This window stays open to monitor Docker.' -ForegroundColor Gray; Write-Host ''"
pause