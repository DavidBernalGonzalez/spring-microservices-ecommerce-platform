@echo off
title Jenkins port-forward
echo Abriendo tunel: localhost:8080 -^> servicio jenkins en el cluster
echo Deja esta ventana ABIERTA. En el navegador: http://localhost:8080
echo Ctrl+C para cerrar el tunel.
echo.
kubectl port-forward -n jenkins svc/jenkins 8080:8080
