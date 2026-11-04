package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/**
 * Chain of Responsibility: each handler recognizes one kind of request. If it does not apply,
 * it returns empty and {@link AssistantEngine} asks the next one. Adding a new skill to the
 * assistant means adding one class, without touching the others.
 */
public interface IntentHandler {
    Optional<AssistantReply> handle(AssistantQuery query, AssistantContext context);
}
