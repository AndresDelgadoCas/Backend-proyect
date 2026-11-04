# API del asistente docente

Todas las rutas requieren `Authorization: Bearer <token>`. La base de datos aplica Row Level Security: un curso de otra cuenta responde 404.

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/courses/{id}/insights` | `analysis` (resumen, cortes, temas, estudiantes con nivel, tendencia, riesgo y distribución), `scheme`, `projections`, `briefing` |
| GET | `/api/courses/{id}/insights/report.csv` | Reporte de seguimiento en CSV (separado por punto y coma) |
| PUT | `/api/courses/{id}/grading-scheme` | Cuerpo `{ "weights": [30, 30, 40], "passingGrade": 3.0 }`. 400 si los pesos no suman 100 |
| GET | `/api/courses/{id}/students/{studentId}/feedback?tone=cercano\|formal\|motivador` | `{ subject, body, email, tone }` |
| POST | `/api/courses/{id}/assistant` | Cuerpo `{ "question": "¿Quién viene bajando?" }` → `{ text, intent, items, actions, run }` |
| GET | `/api/insights/overview` | Un resumen por cada curso del docente |

## Acciones del Búho

`actions` y `run` usan `kind` + `params` de texto:

| kind | params | Qué hace el frontend |
|---|---|---|
| `navigate` | `route`, opcional `vista`, `filtro` | Abre una pantalla |
| `workshop` | `mode` (workshop/quiz), `level`, `topic`, `studentId` | Abre Talleres con el formulario listo |
| `review` | `studentId` | Abre la ficha del estudiante |
| `message` | `studentId` | Abre el mensaje redactado |
| `report` | — | Genera el informe del curso |
| `briefing` | — | Lee el resumen del día |
| `course` | `courseId` | Cambia de curso |
