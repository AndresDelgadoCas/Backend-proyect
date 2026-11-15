package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/** Asks for the roster when the course has no students. */
final class NoCourseDataHandler implements IntentHandler {
    /** Only questions that need the teacher's own numbers get the "create a course" answer; advice and general questions go to the AI. */
    private static final java.util.regex.Pattern DATA = java.util.regex.Pattern.compile(
            "(^| )(quien|quienes|cuantos|cuantas|promedio|resumen|informe|refuerzo|seguimiento|asistencia|mis estudiantes|mis alumnos|mi curso|mi grupo|mis notas|bajando|en riesgo|el peor|el mejor|los peores|los mejores)");

    static boolean asksForCourseData(AssistantQuery q) { return DATA.matcher(q.text()).find(); }

    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (AssistantContext.NO_COURSE.equals(c.analysis().courseName())) {
            // Anything that is not about course data (general questions, "how does FairTeam work") goes on to the AI.
            if (!asksForCourseData(q)) return new FallbackHandler().handle(q, c);
            return Optional.of(AssistantReply.say("no-course", "Todavía no tienes ningún curso. Crea el primero (o carga los de demostración para explorar) y te ayudo con todo lo demás; mientras tanto puedo explicarte cómo funciona FairTeam.")
                    .withActions(ReplyAction.of("navigate", "Ir a Mis cursos", "route", "cursos")));
        }
        if (c.analysis().summary().students() > 0 || q.has("importa", "sube", "cargar", "estudiantes", "lista", "equipo")) return Optional.empty();
        return Optional.of(AssistantReply.say("no-students", c.analysis().courseName() + " todavía no tiene estudiantes. Importa la lista del grupo y te ayudo con todo lo demás.")
                .withActions(ReplyAction.of("navigate", "Ir a estudiantes", "route", "estudiantes")));
    }
}
