@echo off
setlocal
cd /d "%~dp0.."
for %%s in (product-service inventory-service order-service gateway-service) do (
  echo === %%s ===
  pushd "microservices-platform\%%s"
  call mvnw.cmd -B clean install
  if errorlevel 1 exit /b 1
  popd
)
exit /b 0
