package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/** Last link of the chain: always answers. */
final class FallbackHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        return Optional.of(AssistantReply.say("fallback", "No estoy seguro de haberte entendido. Prueba con algo como: “¿quién viene bajando?” o “crea un quiz de funciones”."));
    }
}
