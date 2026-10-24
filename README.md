# FairTeam Backend

API REST para acompañamiento académico docente. Gestiona estudiantes y calificaciones, y conecta de forma privada con un servicio separado que genera talleres a partir del material de clase.

## Tecnologías

- Java 21, Spring Boot, Spring Data JPA y PostgreSQL.
- Spring Security con cuentas propias, contraseñas BCrypt y JWT firmado.
- Flyway para cambios aditivos y aislamiento de datos por docente.
- Servicio de IA Python/Gemini separado; la clave Gemini solo vive en ese servicio.

## Configuración local

Necesitas Java 21 y una base PostgreSQL con el esquema inicial de FairTeam. Flyway toma como baseline el esquema existente (versión 0), luego crea las tablas de evaluación y aplica aislamiento por cuenta con Row Level Security. El primer docente registrado recibe los datos académicos preexistentes; cada registro posterior inicia con un espacio vacío y separado. Hibernate valida el esquema (`ddl-auto=validate`).

Configura estas variables antes de iniciar la API:

| Variable | Uso |
|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Credenciales de PostgreSQL |
| `JWT_SECRET` | Secreto aleatorio de al menos 32 caracteres para firmar sesiones |
| `AI_SERVICE_URL` | URL del servicio de IA, por defecto `http://localhost:8000` |
| `AI_SERVICE_TOKEN` | Secreto compartido entre backend y servicio de IA |

En PowerShell puedes crear valores locales con:

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
```

No uses esos ejemplos en producción ni guardes secretos en Git. En Render configura `JWT_SECRET` y las variables de conexión/IA desde el panel **Environment**. El registro público no necesita invitación.

Inicia y compila localmente:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd -DskipTests package
```

## Autenticación

- `POST /api/auth/register`: crea una cuenta docente; requiere `name`, `email` y `password` (mínimo 10 caracteres).
- `POST /api/auth/login`: recibe `email` y `password`.
- `GET /api/auth/me`: confirma la sesión del token.
- Las demás rutas requieren `Authorization: Bearer <accessToken>`; el token vence a las 8 horas.

Ejemplo de inicio:

```json
{"email":"docente@institucion.edu","password":"tu-contraseña"}
```

El registro está abierto. Las migraciones V2 y V5 aplican el aislamiento por docente y organizan los datos existentes en un curso inicial; cada docente mantiene sus propios cursos y listas mediante Row Level Security de PostgreSQL.

## Rutas principales

| Recurso | Rutas |
|---|---|
| Cursos | `/api/courses` (CRUD) |
| Estudiantes | `/api/students`, `/api/students/import?courseId={id}` |
| Notas e importación CSV | `/api/grades?courseId={id}`, `/api/grades/import?courseId={id}` |
| Asignaturas | `/api/subjects` |
| Actividades y entregas | `/api/assignments`, `/api/assignment-submissions` |
| Rúbricas | `/api/grading-rubrics`, `/api/rubric-criteria` |
| Revisión asistida | `/api/ai-evaluations` |
| Taller desde material | `POST /api/ai/workshops/generate` |

La lista de estudiantes acepta CSV `name,email,academicLevel` (`name` obligatorio). Para importar notas dentro de un curso, usa `studentName` o `studentEmail`, `subject`, `grade` y `period`; también están disponibles los campos de ID para integraciones anteriores. Las materias se crean si no existen. La migración V5 organiza los registros existentes por docente en un curso inicial.

Para crear un taller se envía `multipart/form-data` con `file` (PDF, DOCX o TXT), `subject`, `targetLevel` y opcionalmente `learningObjective`. La llamada a Gemini tiene un límite de tiempo para devolver un error controlado si el proveedor demora; el tablero académico no espera a esta generación.

## Patrones de diseño

El backend conserva ejemplos concretos de Strategy (`strategy/`), Adapter (`adapter/`), Factory Method (`entity/factory/`), Observer (`observer/`) y Decorator (`decorator/`). El análisis de equipos compone Strategy y Decorator; los cambios académicos notifican observadores.

La integración de talleres usa una frontera hexagonal: `AiWorkshopService` valida el caso de uso y depende del puerto `application/port/out/AiWorkshopPort`; `adapter/out/ai/AiWorkshopHttpAdapter` traduce ese puerto al protocolo HTTP del servicio Gemini. El controlador REST es el adaptador de entrada y se conserva la ruta pública existente.

El proyecto todavía es mayoritariamente por capas; no es correcto describir todo el backend como hexagonal ya. La adopción se hace por módulos para evitar mover todas las entidades y APIs de una vez. Spring administra los servicios con alcance singleton, pero no se presenta como un Singleton manual. `RestClient.builder()` es uso de un builder de biblioteca, no un patrón Builder del dominio. No se encontró Abstract Factory ni Bridge en el código actual; no se agregan sin una segunda familia de proveedores o abstracciones que justifiquen su complejidad.

## Despliegue

Render debe usar Java 21 y configurar secretos y datos de conexión desde variables de entorno. Las migraciones presuponen que las tablas base `students` y `subjects` ya existen. La V5 conserva los estudiantes y las notas existentes dentro de un curso inicial por docente. Flyway aplica estas migraciones al iniciar; conserva una copia de seguridad de producción antes del despliegue.
