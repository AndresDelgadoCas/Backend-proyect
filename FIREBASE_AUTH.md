# Firebase Authentication en el backend

La API puede exigir ID tokens de Firebase y limitar el acceso a docentes enumerados por correo.

Configura en Render el Web Service:

```text
AUTH_REQUIRED=true
FIREBASE_PROJECT_ID=<projectId de Firebase>
FAIRTEAM_ALLOWED_TEACHER_EMAILS=<correo-docente-1>,<correo-docente-2>
```

No incluyas comillas ni espacios alrededor de los correos. El backend valida la firma del JWT con las claves públicas oficiales de Firebase, `iss`, `aud`, expiración, `email_verified` y el correo de la lista. Los endpoints `/` y `/error` quedan públicos; las rutas `/api/**` requieren el rol docente. `GET /api/auth/me` confirma la sesión.

Mientras `AUTH_REQUIRED` no sea `true`, la API conserva el modo actual sin autenticación, para que la configuración de Render no rompa el despliegue antes de habilitar Firebase. Configura las tres variables y despliega el backend antes de activar el acceso de producción en el frontend.
