# api-prestamos

# PrestaControl API (Spring Boot + JWT)

API REST del Ejercicio 1. Group ID `com.bellodas`, artifact `prestamoservice`, paquete `com.bellodas.prestamoservice`.

## Requisitos

- JDK 17
- PostgreSQL en ejecución con una base de datos creada (ej.: `prestacontrol`)

## Variables de entorno

`JWT_SECRET` debe ser una cadena **Base64 de al menos 32 bytes**. Se genera, por ejemplo, con:

```powershell
[Convert]::ToBase64String([byte[]](1..32))
```

Configura antes de arrancar (PowerShell):

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/prestacontrol"
$env:DB_USERNAME = "prestacontrol"
$env:DB_PASSWORD = "tu-password"
$env:JWT_SECRET = "tu-secreto-base64-de-32-bytes-o-mas"
$env:JWT_EXPIRATION_MS = "86400000"
```

También están listas como plantilla en `.env.example`. **No subir valores reales al repositorio.**

## Ejecutar

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

Para pruebas sin PostgreSQL (perfil de test usa H2 en memoria):

```powershell
.\mvnw.cmd test
```

## Endpoints

Públicos:

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/auth/register` | Crea prestamista, guarda contraseña con BCrypt y devuelve token |
| POST | `/auth/login` | Valida credenciales y devuelve token JWT |

Protegidos (enviar `Authorization: Bearer <token>`):

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/prestamos` | Préstamos del prestamista del token, con `total` |
| GET | `/prestamos/resumen` | Capital prestado, total por cobrar y ganancia esperada |
| GET | `/prestamos/{id}` | Un préstamo propio |
| POST | `/prestamos` | Registrar préstamo |
| PUT | `/prestamos/{id}` | Modificar o marcar como pagado |
| DELETE | `/prestamos/{id}` | Anular préstamo |

El `total` se calcula como `monto * (1 + interes/100)` con redondeo a 2 decimales. El prestamista siempre se obtiene del token; un préstamo ajeno responde 404. Sin token, con token alterado o vencido: 401.
