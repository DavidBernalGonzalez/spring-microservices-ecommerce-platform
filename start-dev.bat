@echo off
setlocal

set "BASE_DIR=%~dp0"
set "SERVICES_DIR=%BASE_DIR%microservices-platform"

:: Si no estamos dentro de Windows Terminal, relanzar
if "%WT_SESSION%"=="" (
    wt new-tab --title "DEV ENVIRONMENT" --tabColor "#2496ED" cmd /k "\"%~f0\""
    exit /b
)

title DEV ENVIRONMENT

powershell -Command "Write-Host '==========================================' -ForegroundColor Cyan; Write-Host 'Starting Microservices Development Environment' -ForegroundColor Cyan; Write-Host '==========================================' -ForegroundColor Cyan; Write-Host ''"
powershell -Command "Write-Host 'Base directory:' -ForegroundColor Gray; Write-Host '%BASE_DIR%' -ForegroundColor White; Write-Host ''; Write-Host 'Services directory:' -ForegroundColor Gray; Write-Host '%SERVICES_DIR%' -ForegroundColor White; Write-Host ''"

:: Verificar si Docker esta en ejecucion
powershell -Command "Write-Host 'Checking Docker...' -ForegroundColor Yellow"
docker info >nul 2>&1
if errorlevel 1 (
    powershell -Command "Write-Host ''; Write-Host '[ERROR] Docker Desktop no esta en ejecucion.' -ForegroundColor Red; Write-Host ''; Write-Host 'Por favor inicia Docker Desktop y vuelve a ejecutar este script.' -ForegroundColor Red; Write-Host ''"
    pause
    exit /b 1
)
powershell -Command "Write-Host 'Docker OK.' -ForegroundColor Green; Write-Host ''"

powershell -Command "Write-Host 'Checking Docker containers...' -ForegroundColor Yellow"
set DB_OK=1

docker ps --filter "name=db-products" --filter "status=running" --format "{{.Names}}" | findstr /i "db-products" >nul
if errorlevel 1 set DB_OK=0

docker ps --filter "name=db-orders" --filter "status=running" --format "{{.Names}}" | findstr /i "db-orders" >nul
if errorlevel 1 set DB_OK=0

docker ps --filter "name=db-inventory" --filter "status=running" --format "{{.Names}}" | findstr /i "db-inventory" >nul
if errorlevel 1 set DB_OK=0

if "%DB_OK%"=="0" (
    powershell -Command "Write-Host 'Docker databases are not running. Starting docker compose...' -ForegroundColor Yellow"
    pushd "%BASE_DIR%"
    docker compose up -d
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