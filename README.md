# FairTeam Backend

REST API for academic support for teachers. It manages students and grades, and connects privately to a separate service that generates workshops from class material.

## Technologies

- Java 21, Spring Boot, Spring Data JPA and PostgreSQL.
- Spring Security with own accounts, BCrypt passwords and signed JWTs.
- Flyway for additive changes and per-teacher data isolation.
- Separate Python/Gemini AI service; the Gemini key lives only in that service.

## Local setup

You need Java 21 and a PostgreSQL database with the initial FairTeam schema. Flyway takes the existing schema as its baseline (version 0), then creates the evaluation tables and applies per-account isolation with Row Level Security. The first registered teacher receives the pre-existing academic data; every later registration starts with an empty, separate workspace. Hibernate validates the schema (`ddl-auto=validate`).

Set these variables before starting the API:

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | PostgreSQL credentials |
| `JWT_SECRET` | Random secret of at least 32 characters used to sign sessions |
| `AI_SERVICE_URL` | AI service URL, `http://localhost:8000` by default |
| `AI_SERVICE_TOKEN` | Secret shared between the backend and the AI service |

In PowerShell you can create local values with:

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
```

Do not use those examples in production and do not store secrets in Git. On Render, set `JWT_SECRET` and the connection/AI variables from the **Environment** panel. Public registration does not need an invitation.

Run and build locally:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd -DskipTests package
```

## Authentication

- `POST /api/auth/register`: creates a teacher account; requires `name`, `email` and `password` (minimum 10 characters).
- `POST /api/auth/login`: takes `email` and `password`.
- `GET /api/auth/me`: confirms the token's session.
- All other routes require `Authorization: Bearer <accessToken>`; the token expires after 8 hours.

Login example:

```json
{"email":"teacher@school.edu","password":"your-password"}
```

Registration is open. Migrations V2 and V5 apply per-teacher isolation and organize existing data into an initial course; each teacher keeps their own courses and lists through PostgreSQL Row Level Security.

## Super administrator

An account with the `ADMIN` role can see every account on the platform and manage access. Create it by setting these variables on the server (never in Git):

| Variable | Purpose |
|---|---|
| `ADMIN_EMAIL` | Email of the administrator account |
| `ADMIN_PASSWORD` | Password, at least 12 characters |
| `ADMIN_NAME` | Optional display name |
| `ADMIN_PASSWORD_RESET` | `true` to replace the password of an existing administrator on the next start |

At startup the account is created, or promoted if that email already exists (its password is replaced by the configured one). Routes live under `/api/admin` and require the `ADMIN` role, which is re-checked in the database on every request:

- `GET /api/admin/overview`, `/accounts`, `/accounts/{id}`, `/audit`, `/system`.
- `PATCH /api/admin/accounts/{id}` with `{ active, role }`: deactivating an account ends its sessions immediately. An administrator cannot deactivate themselves and the last active administrator cannot be removed.
- `POST /api/admin/accounts/{id}/reset-password`: returns a one-time temporary password (not available for other administrators).

Administrators read data across accounts through a read-only row level security policy (`fairteam_admin_read`, migration V13); they cannot modify a teacher's academic data. Every change is stored in `admin_audit_log`. Sign-in is limited to 5 failed attempts per email in 15 minutes.

## Main routes

| Resource | Routes |
|---|---|
| Courses | `/api/courses` (CRUD) |
| Students | `/api/students`, `/api/students/import?courseId={id}` |
| Grades and CSV import | `/api/grades?courseId={id}`, `/api/grades/import?courseId={id}` |
| Subjects | `/api/subjects` |
| Assignments and submissions | `/api/assignments`, `/api/assignment-submissions` |
| Rubrics | `/api/grading-rubrics`, `/api/rubric-criteria` |
| Assisted review | `/api/ai-evaluations` |
| Workshop from material | `POST /api/ai/workshops/generate` |

The student list accepts a CSV with `name,email,academicLevel` (`name` is required). To import grades into a course, use `studentName` or `studentEmail`, `subject`, `grade` and `period`; the ID fields are still available for earlier integrations. Subjects are created if they do not exist. Migration V5 organizes existing records into an initial course per teacher.

To create a workshop, send `multipart/form-data` with `file` (PDF, DOCX or TXT), `subject`, `targetLevel`, and optionally `learningObjective` and `learningContext` (maximum 2500 characters). The context only includes counts, the average and the prioritized topic of the course; no individual names or grades are sent to Gemini. The call has a time limit and shows a useful message if the provider rejects the key, quota or model.

## Design patterns

The backend keeps concrete examples of Strategy (`strategy/`), Adapter (`adapter/`), Factory Method (`entity/factory/`), Observer (`observer/`), Decorator (`decorator/`) and Proxy (`material/`, see [docs/proxy-pattern.md](docs/proxy-pattern.md)). Team analysis composes Strategy and Decorator; academic changes notify observers.

The workshop integration uses a hexagonal boundary: `AiWorkshopService` validates the use case and depends on the port `application/port/out/AiWorkshopPort`; `adapter/out/ai/AiWorkshopHttpAdapter` translates that port to the HTTP protocol of the Gemini service. The REST controller is the input adapter and the existing public route is kept.

The project is still mostly layered; it would be wrong to describe the whole backend as hexagonal already. Adoption happens module by module to avoid moving every entity and API at once. Spring manages services with singleton scope, but that is not presented as a hand-written Singleton. `RestClient.builder()` is use of a library builder, not a domain Builder pattern. No Abstract Factory or Bridge was found in the current code; they are not added without a second family of providers or abstractions that justify their complexity.

## Deployment

Render must use Java 21 and configure secrets and connection data from environment variables. The migrations assume the base tables `students` and `subjects` already exist. V5 keeps existing students and grades inside an initial course per teacher. Flyway applies these migrations at startup; keep a production backup before deploying.

## `insights` module: the teacher assistant's brain

Detailed documentation: [architecture and patterns](docs/insights-architecture.md) · [API](docs/api-insights.md).

Hexagonal architecture inside `fairteam_backend.insights`:

| Layer | Package | Contents |
|---|---|---|
| Domain (pure Java) | `insights.domain` | `CourseAnalyzer` (levels, per-period trends, average per topic), `GradeProjector` and `GradingScheme` (grade needed to pass), `Briefing` (spoken summary), `FeedbackTemplate` (messages), `assistant` (the Búho interpreter) |
| Application | `insights.application` | Input ports (`CourseInsightsUseCase`, `DraftFeedbackUseCase`, `AskAssistantUseCase`), output ports (`CourseDataPort`, `GradingSchemePort`) and `CourseInsightsService` |
| Adapters | `insights.adapter` | `CourseInsightsController` (REST) and JPA adapters over the existing repositories |
| Composition | `insights.config` | `InsightsConfiguration` chooses the implementations |

Design patterns applied:

- **Strategy**: `RiskScoringStrategy` / `TrendWeightedRiskStrategy` computes the 0–100 risk (average + trend).
- **Chain of Responsibility**: `AssistantEngine` passes the question through `IntentHandler`s (summary, who is dropping, what grade is needed, create quiz, write message…); adding a skill means adding a class.
- **Template Method + Factory Method**: `FeedbackTemplate.forTone()` creates `WarmFeedback` or `FormalFeedback`; the message structure is fixed and each tone defines the greeting and the closing.
- **Builder**: `Briefing` assembles the summary sentence by sentence, only with what has something to say.
- **Ports & Adapters**: the domain and the service do not import Spring or JPA; they are tested with fake ports (`CourseInsightsTest`).

Endpoints (all protected by JWT and Row Level Security):

- `GET /api/courses/{id}/insights`: analysis, grading scheme, projections and the day's summary.
- `PUT /api/courses/{id}/grading-scheme`: `{ "weights": [30, 30, 40], "passingGrade": 3.0 }` (table `course_grading_schemes`, migration V7).
- `GET /api/courses/{id}/students/{studentId}/feedback?tone=friendly|formal|motivational`: feedback message.
- `POST /api/courses/{id}/assistant`: `{ "question": "¿Quién viene bajando?" }` → text, students and actions.

## Attendance and support

- `GET|PUT /api/courses/{id}/attendance?date=YYYY-MM-DD`: marks for the session (`PRESENT`, `ABSENT`, `LATE`, `EXCUSED`).
  A PUT with an empty list clears the session; future dates are not accepted.
- `GET /api/courses/{id}/attendance/sessions` and `/summary`: recorded dates and attendance percentage per student.
  Anyone attending less than 80 % (with at least 4 sessions) is flagged, and Búho combines that with grades.
- `GET|POST /api/courses/{id}/students/{sid}/interventions` and `DELETE .../{interventionId}`: log of support actions
  (conversation, tutoring, workshop, message, family, other) with a short note.

## Grade editor and quick add

Each course has its own scale (`courses.max_grade`: 5, 10, 20, 100…). Grades are always stored from 0 to 5 with
four decimals (87.5/100 is stored as 4.375) and the editor, the assistant and the imports speak in the
course's scale. The minimum passing grade is the same proportion on any scale (3.0/5 = 60/100).

- `GET /api/courses/{id}/grade-sheet?period=Corte 2`: the period as a table (students × columns), with
  `maxGrade`, `passingGrade`, the percentage of each column and the weight of each period (`periodWeights`). Without
  `period` it opens the latest period. A new period without columns proposes those of the previous period (`suggested: true`).
- `PUT /api/courses/{id}/grade-sheet` with `{ period, changes: [{ studentId, subjectId, grade }], columns:
  [{ subjectId, weight }] }`: saves everything in one transaction; `grade: null` deletes the grade. `columns` is optional
  and sets the order and percentages of the period: all `null` (same weight) or all with a value adding up to 100 %.
- `PUT /api/courses/{id}/grade-sheet/columns/{subjectId}?period=` with `{ name }`: renames a column of the period
  (its grades and percentage move to the activity with the new name). `DELETE` removes it along with its grades.
- `PUT /api/courses/{id}/grading-scale` with `{ maxGrade }`: changes the scale; grades keep their
  proportion (a 4.0/5 is shown as 80/100).
- `POST /api/courses/{id}/students/quick` with `{ text }`: one line per student, with an optional email
  separated by `;`, `,` or a tab (as when copying from Excel). Duplicates are reported and not created.

A period's average weights the graded columns by their percentages (pending ones do not count) and the
final grade weights the periods with the course scheme (`PUT /grading-scheme`), which also computes how much
each student needs in what remains.

## Rubrics

Analytic rubrics per course (`rubrics/`): criteria with weights and four described levels (Excellent, Good,
Basic, Insufficient). Each level is worth a fixed share of the maximum grade (100 %, 80 %, 60 % and 30 %), so
"Basic" on everything is exactly the passing grade on any scale.

- `POST /api/courses/{id}/rubrics/generate` with `{ activity, criteriaCount, teacherPrompt }`: draft made by the
  AI (not saved until the teacher reviews it).
- `POST /api/courses/{id}/rubrics` and `PUT /{rubricId}`: saves the rubric; the weights must add up to 100 %.
- `PUT /{rubricId}/students/{studentId}` with `{ levels: { criterionId: 0..3 }, comment }`: evaluates a student.
- `POST /{rubricId}/publish` with `{ period, columnName }`: moves the grades of those with every criterion
  evaluated into a gradebook column (uses `GradeSheetService`, so it also notifies the observers).

Tables `course_rubrics`, `course_rubric_criteria`, `course_rubric_scores` and `course_rubric_comments` (migration V10),
isolated per teacher with RLS like the rest.

## Workshop types

The generation routes accept `activityStyles` (up to three of `GUIDED`, `CODE`, `APP`, `DEPLOY`, `CASE`, `TEAM`).
The AI service adapts the activities: code with test cases, applications with user stories,
deployments with a public URL as evidence, case studies or role-based projects.

`teacherPrompt` (up to 2000 characters) carries the teacher's instructions. With `generationType=CUSTOM` the workshop
is built from them and the material is optional: `POST /api/courses/{id}/materials/generate-workshop` without
`materialIds`. The fields travel as a `WorkshopRequestDTO` (Parameter Object) from the controller to the
AI adapter. The response also carries `studentIntro`, `teacherNotes` and, per activity, `goal`, `hints` and
`deliverable`.

## Demo courses

`POST /api/demo/workspace` (with a session) creates in the teacher's account four fictional courses across three semesters:
Object-Oriented Programming (2025-2), Data Structures (2026-1), Design Patterns and Databases I
(2026-2, still in progress). Each has different students (92 in total), grades per topic and period with varied profiles
(good progress, steady, improving, dropping and at risk), its period scheme and two PDFs of class material
written from open sources cited in each document. Calling it again does not duplicate anything. In the interface
it appears as "Cargar cursos de demostración" on Home, My courses and Settings.

## New database

The Flyway callback `db/migration/beforeMigrate.sql` creates the original academic tables when the database is empty
and does nothing on an existing database. That way the backend starts on both the previous database and a new Render database.

## Tests

```bash
./mvnw test -Dtest='!FairteamBackendApplicationTests' -Dsurefire.failIfNoSpecifiedTests=false
```

The unit tests need no database: the `insights` domain and service are tested with in-memory ports, the controllers with MockMvc and the JPA adapters with Mockito. `FairteamBackendApplicationTests` starts the full context and requires PostgreSQL. GitHub Actions runs the tests on every push (`.github/workflows/ci.yml`).
