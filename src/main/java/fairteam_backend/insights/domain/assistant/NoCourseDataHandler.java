package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/** Asks for the roster when the course has no students. */
final class NoCourseDataHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (AssistantContext.NO_COURSE.equals(c.analysis().courseName())) {
            return Optional.of(AssistantReply.say("no-course", "Todavía no tienes ningún curso. Crea el primero (o carga los de demostración para explorar) y te ayudo con todo lo demás; mientras tanto puedo explicarte cómo funciona FairTeam.")
                    .withActions(ReplyAction.of("navigate", "Ir a Mis cursos", "route", "cursos")));
        }
        if (c.analysis().summary().students() > 0 || q.has("importa", "sube", "cargar", "estudiantes", "lista", "equipo")) return Optional.empty();
        return Optional.of(AssistantReply.say("no-students", c.analysis().courseName() + " todavía no tiene estudiantes. Importa la lista del grupo y te ayudo con todo lo demás.")
                .withActions(ReplyAction.of("navigate", "Ir a estudiantes", "route", "estudiantes")));
    }
}
