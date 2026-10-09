#  FairTeam Backend

API REST de FairTeam, desarrollada con Java 21 y Spring Boot. Administra estudiantes, calificaciones, habilidades, proyectos, equipos y talleres; también valida sesiones docentes de Firebase y conecta de forma privada con el servicio de generación de talleres con IA.
 
## Tecnologías

- Java 21 y Spring Boot
- Spring Data JPA
- PostgreSQL
- Spring Security OAuth2 Resource Server para validar Firebase ID tokens
- Maven y Docker

## Requisitos

- JDK 21
- PostgreSQL con el esquema de FairTeam disponible
- Para generar talleres con IA: el servicio de IA desplegado y accesible desde el backend

## Ejecutar localmente

1. Clona este repositorio y abre una terminal en la carpeta del proyecto.
2. Configura las variables de conexión a PostgreSQL (ver la tabla de variables).
3. Confirma que la base de datos contiene el esquema que espera JPA. El backend usa `spring.jpa.hibernate.ddl-auto=validate`, así que valida las tablas existentes y no las crea automáticamente.
4. Inicia la API:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

Por defecto, la API escucha en `http://localhost:8080`. Para compilar el proyecto:

```powershell
.\mvnw.cmd -DskipTests package
```

## Variables de entorno

| Variable | Uso | Valor predeterminado |
|---|---|---|
| `PORT` | Puerto HTTP asignado por Render | `8080` |
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/fairteam_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de PostgreSQL | Vacío |
| `AUTH_REQUIRED` | Exigir autenticación para `/api/**` | `false` |
| `FIREBASE_PROJECT_ID` | ID del proyecto Firebase usado para Google Sign-In | Vacío |
| `FAIRTEAM_ALLOWED_TEACHER_EMAILS` | Correos docentes autorizados, separados por comas | Vacío |
| `AUTH_ALLOW_ANY_GOOGLE_USER` | Permitir a cualquier cuenta Google con email verificado usar la API | `false` |
| `AI_SERVICE_URL` | URL base del servicio de IA | `http://localhost:8000` |
| `AI_SERVICE_TOKEN` | Token compartido entre backend y servicio de IA | Vacío |

También se aceptan `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` como alternativas para la conexión a PostgreSQL.

### Activar autenticación Firebase

En el Web Service del backend en Render configura:

```text
AUTH_REQUIRED=true
FIREBASE_PROJECT_ID=<projectId de Firebase>
FAIRTEAM_ALLOWED_TEACHER_EMAILS=<correo-docente@institucion.edu>
AUTH_ALLOW_ANY_GOOGLE_USER=false
```

`FIREBASE_PROJECT_ID` debe coincidir con el proyecto usado por el frontend. Por defecto, solo se autoriza a usuarios verificados que aparezcan en la lista. Si quieres que cualquier persona con una cuenta Google verificada pueda acceder a todas las funciones y datos, configura `AUTH_ALLOW_ANY_GOOGLE_USER=true`; el backend no separa los permisos de lectura y modificación por usuario. Con `AUTH_REQUIRED=false`, las rutas de la API quedan públicas. Consulta [FIREBASE_AUTH.md](FIREBASE_AUTH.md) para los pasos completos.

## Endpoints principales

La URL base de producción es `https://backend-proyect-junz.onrender.com`.

| Recurso | Rutas principales | Operaciones |
|---|---|---|
| Estado | `/` | Estado de la API |
| Sesión | `/api/auth/me` | Verificar el docente autenticado |
| Estudiantes | `/api/students` | CRUD |
| Calificaciones | `/api/grades` | CRUD e importación CSV con `POST /api/grades/import` |
| Asignaturas | `/api/subjects` | CRUD |
| Habilidades | `/api/skills` | CRUD |
| Proyectos | `/api/projects` | CRUD |
| Equipos | `/api/teams` | CRUD, análisis con `POST /api/teams/analyze` y recomendación |
| Talleres | `/api/workshops` | CRUD |
| Recomendaciones de talleres | `/api/workshop-recommendations` | CRUD y creación por Factory Method |
| Talleres generados con IA | `/api/ai/workshops/generate` | Generar desde material de estudio |

También hay recursos para habilidades y preferencias de estudiantes/proyectos, disponibilidad y miembros de equipos; sus rutas usan `/api/student-skills`, `/api/student-project-preferences`, `/api/project-skill-requirements`, `/api/student-availability` y `/api/team-students`.

Los endpoints CRUD usan JSON salvo las rutas de importación/generación de archivos, que usan `multipart/form-data`. Revisa los controladores en `src/main/java/fairteam_backend/controller` para los campos y formatos de cada recurso.

### Importar calificaciones

Envía un archivo CSV en el campo `file` a `POST /api/grades/import`. El encabezado requerido es:

```csv
id,studentId,subjectId,grade,period
```

Los identificadores de estudiante y asignatura deben existir previamente en la base de datos.

### Generar un taller con IA

`POST /api/ai/workshops/generate` recibe `multipart/form-data` con estos campos:

- `file`: material PDF, DOCX o TXT (máximo 10 MB).
- `subject`: asignatura.
- `targetLevel`: `LOW`, `MEDIUM` o `HIGH` (se aceptan equivalentes en español).
- `learningObjective`: objetivo opcional.

El backend envía el archivo al servicio de IA con `AI_SERVICE_TOKEN`. La clave de Gemini debe estar únicamente en ese servicio; no la pongas en este repositorio ni en el frontend. Más detalles en [AI_INTEGRATION.md](AI_INTEGRATION.md).

## Patrones de diseño

El proyecto contiene ejemplos de los patrones requeridos para la materia:

- **Strategy:** estrategias para analizar equilibrio, rendimiento o habilidades de los equipos.
- **Adapter:** lectura de calificaciones desde CSV.
- **Factory Method:** creación de recomendaciones.
- **Observer:** notificación de eventos del dominio.
- **Decorator:** composición de criterios de análisis.

## Despliegue

El backend está preparado para Render como Web Service y también incluye un `Dockerfile`. Render debe desplegar la rama `main`, usar Java 21 y proporcionar las variables de base de datos y de los servicios externos desde **Environment**. Nunca guardes contraseñas, tokens de servicio ni claves Gemini en el repositorio.

## Documentación relacionada

- [Autenticación Firebase](FIREBASE_AUTH.md)
- [Contrato de integración con el servicio de IA](AI_INTEGRATION.md)
