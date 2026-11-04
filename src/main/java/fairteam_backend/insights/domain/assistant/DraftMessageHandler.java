package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

import fairteam_backend.insights.domain.Grades;

/** Opens a feedback message for a named student. */
final class DraftMessageHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("mensaje", "retroalimentacion", "correo", "escribele", "escribe", "redacta", "felicita", "feedback")) return Optional.empty();
        if (q.student() == null) return Optional.of(AssistantReply.say("draft-message", "¿A quién le escribo? Dime el nombre del estudiante."));
        String id = String.valueOf(q.student().student().id());
        return Optional.of(AssistantReply.say("draft-message", "Te dejé listo un mensaje para " + Grades.firstName(q.student().student().name()) + ". Revísalo antes de enviarlo.")
                .andRun(ReplyAction.of("message", "Ver mensaje", "studentId", id)));
    }
}
