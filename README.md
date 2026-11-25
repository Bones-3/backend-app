# Backend Junta Vecinal

Backend de microservicios para la aplicación Android Junta Vecinal. La aplicación móvil consume una única entrada HTTP mediante Retrofit: `http://<host>:8080/api/`.

## Stack y arquitectura

- Java 21 y Spring Boot 4.0.6.
- Spring Cloud 2025.1.1, Eureka y API Gateway reactivo.
- Spring Web MVC, Spring Data JPA, Spring Security y Bean Validation.
- JWT HMAC con access token y refresh token rotatorio.
- MySQL 8 y Flyway para las migraciones de todos los servicios persistentes.
- OpenAPI/Swagger en los servicios de dominio.

Se conserva Spring Boot 4.0.6, la versión del proyecto reutilizado. No se actualizó a 4.1.2 porque esa actualización no estaba disponible en el repositorio Maven local usado para verificar el proyecto y supondría una migración separada del stack base.

| Servicio | Puerto | Responsabilidad |
|---|---:|---|
| api-gateway | 8080 | Entrada única, validación del access token y enrutamiento |
| eureka-service | 8761 | Descubrimiento de servicios |
| auth-service | 8088 | Registro, login, renovación, cierre de sesión y revocación |
| vecino-service | 8083 | Vecinos, RUT, estado y rol |
| finanzas-service | 8086 | Cuotas, pagos, comprobantes, morosidad, movimientos y reportes |
| reserva-service | 8082 | Cancha, salón, horarios y reservas |

## Autenticación y roles

Los únicos roles son `DIRECTIVA`, `TESORERO`, `SECRETARIO` y `VECINO`. No existe `PRESIDENTE` ni `ADMIN`.

La identidad pública de la aplicación Android es el RUT. El registro recibe `rut`, `nombre`, `telefono`, `region`, `comuna` y `password`, y siempre crea un usuario `VECINO`. El login recibe `rut` y `password`.

`POST /api/auth/login` devuelve `usuarioId`, `rut`, `nombre`, `telefono`, `region`, `comuna`, `accessToken`, `refreshToken`, `tokenType`, `expiresIn` y `rol`; también conserva temporalmente los campos `correo` y `token` por compatibilidad histórica. El access token dura 15 minutos y el refresh token 7 días de forma predeterminada.

La renovación mediante `POST /api/auth/refresh` revoca el refresh token presentado y crea un par nuevo. `POST /api/auth/logout` revoca el refresh token. El gateway y los servicios de dominio rechazan un refresh token si se intenta usar como Bearer token.

Retrofit debe enviar en rutas protegidas:

```http
Authorization: Bearer <accessToken>
```

Endpoints públicos:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

Usuarios de demostración:

- `11.111.111-1` / `admin123`
- `22.222.222-2` / `vecino123`
- `33.333.333-3` / `tesorero123`
- `44.444.444-4` / `secretario123`

## Endpoints protegidos

### Vecinos

- `GET /api/vecino/vecinos?texto=&activo=&rol=&page=0&size=20`
- `GET /api/vecino/vecinos/{id}`
- `GET /api/vecino/vecinos/me`
- `POST /api/vecino/vecinos`
- `PUT /api/vecino/vecinos/{id}`
- `DELETE /api/vecino/vecinos/{id}`

`DIRECTIVA`, `TESORERO` y `SECRETARIO` pueden consultar el listado y los perfiles por ID. Un `VECINO` solo puede consultar su perfil mediante `/me`, que no devuelve RUT ni correo. Solo `DIRECTIVA` y `SECRETARIO` pueden crear, editar o desactivar vecinos.

### Reservas

- `GET /api/reserva/espacios`
- `GET /api/reserva/espacios/{id}`
- `POST|PUT|DELETE /api/reserva/espacios[/{id}]`
- `GET /api/reserva/horarios`
- `GET /api/reserva/horarios/{id}`
- `POST|DELETE /api/reserva/horarios[/{id}]`
- `GET /api/reserva/reservas?vecinoId=&espacioId=&estado=&fechaDesde=&fechaHasta=&page=0&size=20`
- `GET|POST|PUT|DELETE /api/reserva/reservas[/{id}]`
- `GET /api/reserva/reservas/app?usuarioId={id}`
- `POST /api/reserva/reservas/app`

`DIRECTIVA`, `TESORERO` y `SECRETARIO` pueden consultar el listado general. La consulta, edición y cancelación por ID están disponibles para el propietario de la reserva y para `DIRECTIVA` o `SECRETARIO`. La creación general está reservada a `DIRECTIVA` y `SECRETARIO`; los vecinos crean sus reservas mediante `/reservas/app`. Solo `DIRECTIVA` y `SECRETARIO` administran espacios y horarios.

El endpoint `/reservas/app` usa directamente el contrato de la aplicación Android: `usuarioId`, `nombre`, `fecha` en milisegundos Unix, `horaInicioMin`, `horaFinMin`, `asistentes`, `observaciones`, `requiereProyector` y `aceptaReglamento`. Las reservas creadas desde la app se asignan al espacio `SALON`, ya que la interfaz móvil actual no permite escoger un espacio.

Reglas del dominio:

- Solo existen inicialmente `CANCHA` y `SALON`.
- No hay límite de reservas por vecino, cantidad de asistentes ni política de inasistencias.
- Se impiden solapamientos para el mismo espacio, fecha y rango horario.

### Finanzas

- `GET|POST /api/finanzas/cuotas`
- `POST /api/finanzas/cuotas/{id}/asignaciones`
- `GET /api/finanzas/cuotas/asignaciones?vecinoId=&estado=&soloMorosas=&page=0&size=20`
- `GET /api/finanzas/cuotas/morosidad?vecinoId=&page=0&size=20`
- `GET|POST /api/finanzas/pagos`
- `GET /api/finanzas/pagos/{id}`
- `POST /api/finanzas/comprobantes`
- `GET /api/finanzas/comprobantes/pago/{pagoId}`
- `GET|POST /api/finanzas/movimientos?tipo=&desde=&hasta=&page=0&size=20`
- `GET /api/finanzas/reportes?desde=AAAA-MM-DD&hasta=AAAA-MM-DD`

`DIRECTIVA`, `TESORERO` y `SECRETARIO` pueden consultar. Las operaciones financieras de escritura corresponden a `DIRECTIVA` y `TESORERO`. Se soportan cuotas `ORDINARIA` y `EXTRAORDINARIA`, pagos parciales o totales, metadatos de comprobantes, morosidad, ingresos/egresos y resumen por período.

Los listados paginados responden con:

```json
{
  "contenido": [],
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 0,
  "totalPaginas": 0
}
```

## Ejecución

Desde `backend/`:

```powershell
docker compose up --build
```

Variables relevantes:

- `JWT_SECRET`: mínimo 32 bytes; debe ser idéntico en gateway y servicios.
- `JWT_ACCESS_EXPIRATION`: milisegundos, valor predeterminado `900000`.
- `JWT_REFRESH_EXPIRATION`: milisegundos, valor predeterminado `604800000`.
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

Cada servicio es un proyecto Maven independiente. Por ejemplo:

```powershell
cd auth-service
.\mvnw.cmd test
```

## Acceso a Swagger

1. Inicia los servicios desde la raíz del backend con `docker compose up --build` y comprueba en otra terminal que estén activos con `docker compose ps`.
2. Abre en el navegador la interfaz del servicio que quieres consultar:

   | Servicio | Desde el mismo equipo | Desde otro equipo de la red local |
   |---|---|---|
   | Autenticación | `http://localhost:8088/swagger-ui/index.html` | `http://<IP_DEL_EQUIPO>:8088/swagger-ui/index.html` |
   | Vecinos | `http://localhost:8083/swagger-ui/index.html` | `http://<IP_DEL_EQUIPO>:8083/swagger-ui/index.html` |
   | Finanzas | `http://localhost:8086/swagger-ui/index.html` | `http://<IP_DEL_EQUIPO>:8086/swagger-ui/index.html` |
   | Reservas | `http://localhost:8082/swagger-ui/index.html` | `http://<IP_DEL_EQUIPO>:8082/swagger-ui/index.html` |

3. Para conectarte desde otro dispositivo, ejecuta `ipconfig` en Windows y busca la dirección **IPv4** del equipo donde corre Docker. Sustituye `<IP_DEL_EQUIPO>` por esa dirección; por ejemplo, `http://192.168.1.10:8082/swagger-ui/index.html`. Ambos dispositivos deben estar en la misma red y el firewall debe permitir el puerto del servicio.
4. Para probar endpoints protegidos, inicia sesión en Swagger de autenticación con `POST /auth/login`, copia el campo `accessToken` y pulsa **Authorize** en Swagger de Vecinos, Finanzas o Reservas. Pega solo el token; Swagger agrega el prefijo `Bearer`.

El documento OpenAPI en JSON de cada servicio está en `http://localhost:<PUERTO>/v3/api-docs`; desde otro dispositivo también puedes sustituir `localhost` por la IPv4 del equipo. El gateway (`http://localhost:8080/api/`) enruta las llamadas de la aplicación, pero no publica Swagger. En Swagger las rutas se muestran sin el prefijo `/api` del gateway.
