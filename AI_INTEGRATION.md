# FairTeam AI integration contract

The Spring Boot backend is the only service the frontend should call. It proxies
study material to a separate AI service and returns the typed workshop response.

## Backend endpoint

`POST /api/ai/workshops/generate` (`multipart/form-data`)

Fields:

- `file`: required PDF, DOCX, or TXT file; maximum size 10 MB.
- `subject`: required subject name, up to 120 characters.
- `targetLevel`: `LOW`, `MEDIUM`, or `HIGH` (Spanish aliases are accepted).
- `learningObjective`: optional instruction describing the learning goal.

## AI service endpoint

The backend calls `POST {AI_SERVICE_URL}/api/v1/workshops/generate` using the
same multipart fields and an `X-AI-Service-Token` header. Configure `AI_SERVICE_URL`
and `AI_SERVICE_TOKEN` in the backend environment; the URL defaults to
`http://localhost:8000` for local development. Set the same random token in the
AI service environment. Gemini's key remains only in the AI service.

Expected JSON response:

```json
{
  "title": "Taller de ...",
  "subject": "Matemáticas",
  "targetLevel": "LOW",
  "summary": "...",
  "learningObjectives": ["..."],
  "activities": [
    { "title": "...", "instructions": "...", "estimatedMinutes": 15 }
  ],
  "assessment": [
    {
      "question": "...",
      "options": ["...", "..."],
      "correctAnswer": "...",
      "explanation": "..."
    }
  ],
  "sourceReferences": ["Página 2: ..."]
}
```

The AI service owns the Gemini integration and its `GEMINI_API_KEY`. Never put
that key in this backend, the browser, or a committed configuration file.
