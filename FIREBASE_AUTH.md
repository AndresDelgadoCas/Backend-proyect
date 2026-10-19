# Firebase Authentication en el backend

La API puede exigir ID tokens de Firebase y autorizar el acceso mediante una lista de correos docentes o permitir cualquier cuenta Google con correo verificado.

Configura en Render el Web Service:

```text
AUTH_REQUIRED=true
FIREBASE_PROJECT_ID=<projectId de Firebase>
FAIRTEAM_ALLOWED_TEACHER_EMAILS=<correo-docente-1>,<correo-docente-2>
AUTH_ALLOW_ANY_GOOGLE_USER=false
```

El modo recomendado mantiene `AUTH_ALLOW_ANY_GOOGLE_USER=false` y usa la lista de correos. Para permitir que cualquier cuenta Google verificada acceda a la aplicación y a todos los endpoints de datos, configura `AUTH_ALLOW_ANY_GOOGLE_USER=true`; `FAIRTEAM_ALLOWED_TEACHER_EMAILS` deja de limitar las cuentas. Ten presente que la aplicación no tiene permisos diferenciados por usuario: cualquier usuario admitido tendrá las mismas operaciones sobre estudiantes, notas, equipos y talleres.

El backend valida la firma del JWT con las claves públicas oficiales de Firebase, `iss`, `aud`, expiración y `email_verified`. Los endpoints `/` y `/error` quedan públicos; las rutas `/api/**` requieren el rol de usuario autenticado. `GET /api/auth/me` confirma la sesión.

Mientras `AUTH_REQUIRED` no sea `true`, la API conserva el modo actual sin autenticación, para que la configuración de Render no rompa el despliegue antes de habilitar Firebase. Configura las variables y despliega el backend antes de activar el acceso de producción en el frontend.
