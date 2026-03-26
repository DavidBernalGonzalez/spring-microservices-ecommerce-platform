@echo off
setlocal
echo Reinicio completo: stop-dev.bat y luego start-dev.bat
echo.
call "%~dp0stop-dev.bat"
call "%~dp0start-dev.bat"
