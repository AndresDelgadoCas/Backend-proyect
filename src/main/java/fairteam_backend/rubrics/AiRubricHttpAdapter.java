package fairteam_backend.rubrics;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

/** Adapter for {@code POST /generate-rubric} of the AI service; translates its JSON to a {@link Rubric.Draft}. */
@Component
public class AiRubricHttpAdapter implements RubricGenerator {
    private static final Pattern DETAIL = Pattern.compile("\"detail\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private final RestClient aiRestClient;
    private final String token;

    record AiLevels(String excellent, String good, String basic, String insufficient) { }

    record AiCriterion(String name, String description, BigDecimal weight, AiLevels levels) { }

    record AiRubric(String title, String description, List<AiCriterion> criteria) { }

    public AiRubricHttpAdapter(RestClient aiRestClient, @Value("${fairteam.ai.service-token}") String token) {
        this.aiRestClient = aiRestClient;
        this.token = token;
    }

    @Override
    public Rubric.Draft generate(String subject, String activity, int criteriaCount, String teacherPrompt) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "La integración interna con IA aún no está configurada.");
        }
        try {
            AiRubric rubric = aiRestClient.post()
                    .uri("/generate-rubric")
                    .header("X-AI-Service-Token", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("subject", subject, "activity", activity, "criteriaCount", criteriaCount,
                            "teacherPrompt", teacherPrompt == null ? "" : teacherPrompt))
                    .retrieve()
                    .body(AiRubric.class);
            if (rubric == null || rubric.criteria() == null || rubric.criteria().isEmpty()) {
                throw new ResponseStatusException(BAD_GATEWAY, "El servicio de IA devolvió una rúbrica vacía.");
            }
            return new Rubric.Draft(rubric.title(), rubric.description(), rubric.criteria().stream()
                    .map(c -> new Rubric.CriterionDraft(null, c.name(), c.description(), c.weight(),
                            c.levels() == null ? null : new Rubric.Levels(c.levels().excellent(), c.levels().good(),
                                    c.levels().basic(), c.levels().insufficient())))
                    .toList());
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "El servicio de IA no está disponible en este momento.");
        } catch (RestClientResponseException ex) {
            String message = switch (ex.getStatusCode().value()) {
                case 401, 403 -> "No se pudo autenticar la conexión interna con IA.";
                case 422 -> "Describe la actividad con un poco más de detalle.";
                case 429 -> "Se generaron muchas cosas seguidas. Espera un momento e inténtalo de nuevo.";
                case 502 -> detail(ex);
                default -> "La IA no pudo crear la rúbrica. Inténtalo de nuevo.";
            };
            throw new ResponseStatusException(BAD_GATEWAY, message);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(BAD_GATEWAY, "No se pudo completar la solicitud al servicio de IA.");
        }
    }

    private static String detail(RestClientResponseException ex) {
        Matcher matcher = DETAIL.matcher(ex.getResponseBodyAsString());
        if (matcher.find()) {
            String message = matcher.group(1).replace("\\\"", "\"").trim();
            if (!message.isEmpty() && message.length() <= 300) return message;
        }
        return "La IA no pudo crear la rúbrica. Inténtalo de nuevo.";
    }
}
