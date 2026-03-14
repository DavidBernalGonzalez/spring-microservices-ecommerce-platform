@echo off
setlocal

set "BASE_DIR=%~dp0"
set "SERVICES_DIR=%BASE_DIR%microservices-platform"

:: Si no estamos dentro de Windows Terminal, relanzar con color Docker
if "%WT_SESSION%"=="" (
    wt new-tab --title "DEV ENVIRONMENT" --tabColor "#2496ED" cmd /k "\"%~f0\""
    exit /b
)

title DEV ENVIRONMENT

echo ==========================================
echo Starting Microservices Development Environment
echo ==========================================

echo.
echo Base directory:
echo %BASE_DIR%
echo.
echo Services directory:
echo %SERVICES_DIR%
echo.

echo Checking Docker containers...

set DB_OK=1

docker ps --filter "name=db-products" --filter "status=running" --format "{{.Names}}" | findstr /i "db-products" >nul
if errorlevel 1 set DB_OK=0

docker ps --filter "name=db-orders" --filter "status=running" --format "{{.Names}}" | findstr /i "db-orders" >nul
if errorlevel 1 set DB_OK=0

docker ps --filter "name=db-inventory" --filter "status=running" --format "{{.Names}}" | findstr /i "db-inventory" >nul
if errorlevel 1 set DB_OK=0

if "%DB_OK%"=="0" (
    echo Docker databases are not running. Starting docker compose...
    pushd "%BASE_DIR%"
    docker compose up -d
    popd

    echo.
    echo Waiting for databases to initialize...
    timeout /t 15 >nul
) else (
    echo Docker databases already running.
)

echo.
echo Starting microservices...

wt ^
new-tab --title "PRODUCT SERVICE" --tabColor "#2ECC71" cmd /k "pushd \"%SERVICES_DIR%\product-service\" && mvnw.cmd spring-boot:run" ; ^
new-tab --title "INVENTORY SERVICE" --tabColor "#3498DB" cmd /k "pushd \"%SERVICES_DIR%\inventory-service\" && mvnw.cmd spring-boot:run" ; ^
new-tab --title "ORDER SERVICE" --tabColor "#F39C12" cmd /k "pushd \"%SERVICES_DIR%\order-service\" && mvnw.cmd spring-boot:run"

echo.
echo ==========================================
echo Environment started
echo ==========================================

echo.
echo This window stays open to monitor Docker.
pause