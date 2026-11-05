package fairteam_backend.config;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Sends the reason of a {@link ResponseStatusException} to the client.
 *
 * <p>Those reasons are written for teachers ("Gemini alcanzó el límite...", "Formatos permitidos...").
 * Spring Boot hides them by default, so the UI could only say "el servidor encontró un problema".
 * Unexpected exceptions are not handled here and keep Boot's safe, message-free response.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        if (status.is5xxServerError()) {
            log.warn("Request failed with {}: {}", status.value(), ex.getReason());
        }
        return body(status, ex.getReason());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleUploadSize(MaxUploadSizeExceededException ex) {
        return body(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo supera el límite de 10 MB.");
    }

    static ResponseEntity<Map<String, Object>> body(HttpStatusCode status, String reason) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        HttpStatus known = HttpStatus.resolve(status.value());
        body.put("error", known == null ? "Error" : known.getReasonPhrase());
        body.put("message", reason == null ? "" : reason);
        return ResponseEntity.status(status).body(body);
    }
}
