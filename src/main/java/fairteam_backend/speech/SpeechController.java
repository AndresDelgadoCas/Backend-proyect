package fairteam_backend.speech;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Búho's natural voice: forwards the text to the AI service ({@code POST /speak}) and returns the WAV audio.
 * The Gemini key never reaches the browser. Any failure is a 503 so the browser falls back to its own voice.
 */
@RestController
public class SpeechController {
    private static final Logger log = LoggerFactory.getLogger(SpeechController.class);
    private static final int MAX_CHARS = 2000;

    private final RestClient aiRestClient;
    private final String token;

    public record SpeechRequest(String text) { }

    public SpeechController(RestClient aiRestClient, @Value("${fairteam.ai.service-token:}") String token) {
        this.aiRestClient = aiRestClient;
        this.token = token;
    }

    @PostMapping("/api/assistant/speech")
    public ResponseEntity<?> speak(@RequestBody SpeechRequest request) {
        String text = request == null || request.text() == null ? "" : request.text().trim();
        if (text.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "No hay texto para leer."));
        if (text.length() > MAX_CHARS) text = text.substring(0, MAX_CHARS);
        if (token == null || token.isBlank()) return unavailable("La voz natural no está configurada.");
        try {
            byte[] audio = aiRestClient.post().uri("/speak").header("X-AI-Service-Token", token)
                    .contentType(MediaType.APPLICATION_JSON).body(Map.of("text", text))
                    .retrieve().body(byte[].class);
            if (audio == null || audio.length < 64) return unavailable("La voz natural no devolvió audio.");
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("audio/wav")).body(audio);
        } catch (RestClientResponseException ex) {
            log.info("Natural voice unavailable: {}", ex.getStatusCode());
            return unavailable(ex.getStatusCode().value() == 429 ? "Búho habló mucho seguido; uso la voz del navegador." : "La voz natural no está disponible ahora.");
        } catch (RuntimeException ex) {
            log.info("Natural voice unavailable: {}", ex.getMessage());
            return unavailable("La voz natural no está disponible ahora.");
        }
    }

    private static ResponseEntity<Map<String, String>> unavailable(String message) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", message));
    }
}
