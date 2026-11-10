package fairteam_backend.insights.application.port.out;

import java.util.Optional;

import fairteam_backend.insights.domain.assistant.AssistantBrief;
import fairteam_backend.insights.domain.assistant.Interpretation;

/**
 * Outbound port: the AI that reads a free request. Empty when it is not configured, slow or failing: Búho then
 * answers with its own rules.
 */
public interface AssistantInterpreterPort {
    Optional<Interpretation> interpret(AssistantBrief.Request request);

    static AssistantInterpreterPort none() { return request -> Optional.empty(); }
}
