@echo off
setlocal
for %%s in (api-gateway auth-service eureka-service vecino-service finanzas-service reserva-service) do (
  echo Limpiando %%s...
  pushd %%s
  call mvnw.cmd clean
  if errorlevel 1 exit /b 1
  popd
)
echo Backend limpio.
