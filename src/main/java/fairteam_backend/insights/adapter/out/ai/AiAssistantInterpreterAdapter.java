package fairteam_backend.insights.adapter.out.ai;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import fairteam_backend.insights.application.port.out.AssistantInterpreterPort;
import fairteam_backend.insights.domain.assistant.AssistantBrief;
import fairteam_backend.insights.domain.assistant.Interpretation;

/**
 * Adapter for {@code POST /assistant/interpret} of the AI service. Any failure (not configured, slow, down,
 * unexpected answer) is an empty result: Búho falls back to its own rules instead of showing an error.
 */
@Component
public class AiAssistantInterpreterAdapter implements AssistantInterpreterPort {
    private static final Logger log = LoggerFactory.getLogger(AiAssistantInterpreterAdapter.class);

    private final RestClient aiRestClient;
    private final String token;

    public AiAssistantInterpreterAdapter(RestClient aiRestClient, @Value("${fairteam.ai.service-token:}") String token) {
        this.aiRestClient = aiRestClient;
        this.token = token;
    }

    @Override
    public Optional<Interpretation> interpret(AssistantBrief.Request request) {
        if (token == null || token.isBlank()) return Optional.empty();
        long started = System.nanoTime();
        try {
            Interpretation result = aiRestClient.post()
                    .uri("/assistant/interpret")
                    .header("X-AI-Service-Token", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Interpretation.class);
            log.info("Búho interpreter answered {} in {} ms", result == null ? "nothing" : result.action(), (System.nanoTime() - started) / 1_000_000);
            return Optional.ofNullable(result).filter(r -> !r.action().isBlank());
        } catch (RuntimeException ex) {
            log.warn("Búho interpreter unavailable after {} ms: {}", (System.nanoTime() - started) / 1_000_000, ex.getMessage());
            return Optional.empty();
        }
    }
}
