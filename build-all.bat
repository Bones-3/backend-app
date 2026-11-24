@echo off
setlocal
for %%s in (api-gateway auth-service eureka-service vecino-service finanzas-service reserva-service) do (
  echo Construyendo %%s...
  pushd %%s
  call mvnw.cmd clean package
  if errorlevel 1 exit /b 1
  popd
)
echo Backend construido correctamente.
