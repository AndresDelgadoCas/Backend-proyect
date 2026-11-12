package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/** Last link of the chain: always answers. */
final class FallbackHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        return Optional.of(AssistantReply.say("fallback", "No pude responderte esta vez. Inténtalo de nuevo en un momento; mientras tanto puedo decirte “¿quién viene bajando?” o crear “un quiz de funciones”."));
    }
}
