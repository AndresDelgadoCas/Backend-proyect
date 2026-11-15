package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/** Last link of the chain: always answers. */
final class FallbackHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        return Optional.of(AssistantReply.say("fallback", "Ahora no pude pensar esa respuesta con la IA. Inténtalo de nuevo en unos segundos. Mientras tanto te ayudo con cómo usar FairTeam (“¿cómo creo un curso?”), con tu curso (“¿quién viene bajando?”) o creando actividades (“crea un quiz de funciones”)."));
    }
}
