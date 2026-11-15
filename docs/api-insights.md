# Teacher assistant API

All routes require `Authorization: Bearer <token>`. The database applies Row Level Security: a course from another account answers 404.

| Method | Route | Response |
|---|---|---|
| GET | `/api/courses/{id}/insights` | `analysis` (summary, periods, topics, students with level, trend, risk and distribution), `scheme`, `projections`, `briefing` |
| GET | `/api/courses/{id}/insights/report.csv` | Follow-up report as CSV (semicolon-separated) |
| PUT | `/api/courses/{id}/grading-scheme` | Body `{ "weights": [30, 30, 40], "passingGrade": 3.0 }`. 400 if the weights do not add up to 100 |
| GET | `/api/courses/{id}/students/{studentId}/feedback?tone=friendly\|formal\|motivational` | `{ subject, body, email, tone }` |
| POST | `/api/courses/{id}/assistant` | Body `{ "question": "¿Quién viene bajando?" }` → `{ text, intent, items, actions, run }` |
| GET | `/api/insights/overview` | One summary per course of the teacher |

## Búho actions

`actions` and `run` use a text `kind` + `params`:

| kind | params | What the frontend does |
|---|---|---|
| `navigate` | `route`, optional `view`, `filter` | Opens a screen |
| `workshop` | `mode` (workshop/quiz), `level`, `topic`, `studentId` | Opens Workshops with the form ready |
| `review` | `studentId` | Opens the student's profile |
| `message` | `studentId` | Opens the drafted message |
| `report` | — | Generates the course report |
| `briefing` | — | Reads the day's summary |
| `course` | `courseId` | Switches course |
