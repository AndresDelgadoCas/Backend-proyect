package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/** Asks for grades before answering grade questions. */
final class NeedsGradesHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (c.analysis().summary().gradeCount() > 0 || q.has("equipo", "importa", "sube", "material", "abre", "ir a", "llevame")) return Optional.empty();
        return Optional.of(AssistantReply.say("needs-grades", "Aún no hay notas en " + c.analysis().courseName() + ". Cárgalas y podré responderte eso.")
                .withActions(ReplyAction.of("navigate", "Ir a estudiantes y notas", "route", "estudiantes")));
    }
}
