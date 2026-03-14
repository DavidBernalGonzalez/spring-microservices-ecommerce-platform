@echo off

echo =====================================
echo Stopping Docker Databases
echo =====================================

docker compose down

echo.
echo Databases stopped
pause