package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/** Asks for the roster when the course has no students. */
final class NoCourseDataHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (c.analysis().summary().students() > 0 || q.has("importa", "sube", "cargar", "estudiantes", "lista", "equipo")) return Optional.empty();
        return Optional.of(AssistantReply.say("no-students", c.analysis().courseName() + " todavía no tiene estudiantes. Importa la lista del grupo y te ayudo con todo lo demás.")
                .withActions(ReplyAction.of("navigate", "Ir a estudiantes", "route", "estudiantes")));
    }
}
