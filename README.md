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

Para crear un taller se envía `multipart/form-data` con `file` (PDF, DOCX o TXT), `subject`, `targetLevel`, y opcionalmente `learningObjective` y `learningContext` (máximo 2500 caracteres). El contexto solo incluye recuentos, promedio y tema priorizado del curso; no se envían nombres ni notas individuales a Gemini. La llamada tiene límite de tiempo y presenta un mensaje útil si el proveedor rechaza la clave, cuota o modelo.

## Patrones de diseño

El backend conserva ejemplos concretos de Strategy (`strategy/`), Adapter (`adapter/`), Factory Method (`entity/factory/`), Observer (`observer/`), Decorator (`decorator/`) y Proxy (`material/`, ver [docs/proxy-pattern.md](docs/proxy-pattern.md)). El análisis de equipos compone Strategy y Decorator; los cambios académicos notifican observadores.

La integración de talleres usa una frontera hexagonal: `AiWorkshopService` valida el caso de uso y depende del puerto `application/port/out/AiWorkshopPort`; `adapter/out/ai/AiWorkshopHttpAdapter` traduce ese puerto al protocolo HTTP del servicio Gemini. El controlador REST es el adaptador de entrada y se conserva la ruta pública existente.

El proyecto todavía es mayoritariamente por capas; no es correcto describir todo el backend como hexagonal ya. La adopción se hace por módulos para evitar mover todas las entidades y APIs de una vez. Spring administra los servicios con alcance singleton, pero no se presenta como un Singleton manual. `RestClient.builder()` es uso de un builder de biblioteca, no un patrón Builder del dominio. No se encontró Abstract Factory ni Bridge en el código actual; no se agregan sin una segunda familia de proveedores o abstracciones que justifiquen su complejidad.

## Despliegue

Render debe usar Java 21 y configurar secretos y datos de conexión desde variables de entorno. Las migraciones presuponen que las tablas base `students` y `subjects` ya existen. La V5 conserva los estudiantes y las notas existentes dentro de un curso inicial por docente. Flyway aplica estas migraciones al iniciar; conserva una copia de seguridad de producción antes del despliegue.

## Módulo `insights`: el cerebro del asistente docente

Documentación detallada: [arquitectura y patrones](docs/insights-architecture.md) · [API](docs/api-insights.md).

Arquitectura hexagonal dentro de `fairteam_backend.insights`:

| Capa | Paquete | Contenido |
|---|---|---|
| Dominio (Java puro) | `insights.domain` | `CourseAnalyzer` (niveles, tendencias por corte, promedio por tema), `GradeProjector` y `GradingScheme` (nota necesaria para aprobar), `Briefing` (resumen hablado), `FeedbackTemplate` (mensajes), `assistant` (intérprete del Búho) |
| Aplicación | `insights.application` | Puertos de entrada (`CourseInsightsUseCase`, `DraftFeedbackUseCase`, `AskAssistantUseCase`), puertos de salida (`CourseDataPort`, `GradingSchemePort`) y `CourseInsightsService` |
| Adaptadores | `insights.adapter` | `CourseInsightsController` (REST) y adaptadores JPA sobre los repositorios existentes |
| Composición | `insights.config` | `InsightsConfiguration` elige las implementaciones |

Patrones de diseño aplicados:

- **Strategy**: `RiskScoringStrategy` / `TrendWeightedRiskStrategy` calcula el riesgo 0–100 (promedio + tendencia).
- **Chain of Responsibility**: `AssistantEngine` pasa la pregunta por `IntentHandler`s (resumen, quién baja, qué nota necesita, crear quiz, escribir mensaje…); agregar una habilidad es agregar una clase.
- **Template Method + Factory Method**: `FeedbackTemplate.forTone()` crea `WarmFeedback` o `FormalFeedback`; la estructura del mensaje es fija y cada tono define saludo y despedida.
- **Builder**: `Briefing` arma el resumen frase por frase, solo con lo que tiene algo que decir.
- **Ports & Adapters**: el dominio y el servicio no importan Spring ni JPA; se prueban con puertos falsos (`CourseInsightsTest`).

Endpoints (todos protegidos por JWT y Row Level Security):

- `GET /api/courses/{id}/insights`: análisis, esquema de notas, proyecciones y resumen del día.
- `PUT /api/courses/{id}/grading-scheme`: `{ "weights": [30, 30, 40], "passingGrade": 3.0 }` (tabla `course_grading_schemes`, migración V7).
- `GET /api/courses/{id}/students/{studentId}/feedback?tone=cercano|formal`: mensaje de retroalimentación.
- `POST /api/courses/{id}/assistant`: `{ "question": "¿Quién viene bajando?" }` → texto, estudiantes y acciones.

## Asistencia y acompañamiento

- `GET|PUT /api/courses/{id}/attendance?date=AAAA-MM-DD`: marcas de la sesión (`PRESENT`, `ABSENT`, `LATE`, `EXCUSED`).
  Un PUT con lista vacía borra la sesión; no se aceptan fechas futuras.
- `GET /api/courses/{id}/attendance/sessions` y `/summary`: fechas registradas y porcentaje de asistencia por estudiante.
  Quien asiste a menos del 80 % (con al menos 4 sesiones) queda marcado, y el Búho lo combina con las notas.
- `GET|POST /api/courses/{id}/students/{sid}/interventions` y `DELETE .../{interventionId}`: registro de apoyos
  (conversación, tutoría, taller, mensaje, familia, otro) con una nota breve.

## Tipos de taller

Las rutas de generación aceptan `activityStyles` (hasta tres de `GUIDED`, `CODE`, `APP`, `DEPLOY`, `CASE`, `TEAM`).
El servicio de IA adapta las actividades: código con casos de prueba, aplicaciones con historias de usuario,
despliegues con URL pública como evidencia, casos de estudio o proyectos por roles.

## Cursos de demostración

`POST /api/demo/workspace` (con sesión) crea en la cuenta del docente cuatro cursos ficticios de tres semestres:
Programación Orientada a Objetos (2025-2), Estructuras de Datos (2026-1), Patrones de Diseño y Bases de Datos I
(2026-2, todavía en curso). Cada uno trae estudiantes distintos (92 en total), notas por tema y corte con perfiles
variados (buen progreso, estable, mejorando, bajando y en riesgo), su esquema de cortes y dos PDF de material de clase
redactados a partir de fuentes abiertas citadas en cada documento. Volver a llamarlo no duplica nada. En la interfaz
está como "Cargar cursos de demostración" en Inicio, Mis cursos y Ajustes.

## Base de datos nueva

El callback de Flyway `db/migration/beforeMigrate.sql` crea las tablas académicas originales cuando la base está vacía
y no hace nada en una base existente. Así el backend arranca tanto en la base anterior como en una base nueva de Render.

## Pruebas

```bash
./mvnw test -Dtest='!FairteamBackendApplicationTests' -Dsurefire.failIfNoSpecifiedTests=false
```

Las pruebas unitarias no necesitan base de datos: el dominio y el servicio de `insights` se prueban con puertos en memoria, los controladores con MockMvc y los adaptadores JPA con Mockito. `FairteamBackendApplicationTests` levanta el contexto completo y requiere PostgreSQL. GitHub Actions corre las pruebas en cada push (`.github/workflows/ci.yml`).
