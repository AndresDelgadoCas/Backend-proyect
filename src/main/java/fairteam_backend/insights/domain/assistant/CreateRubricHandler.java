package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/**
 * "Crea una rúbrica para el último taller de Gauss": the app finds that workshop among the saved ones, asks the
 * AI for a rubric based on its content, saves it linked to the workshop and opens it ready to grade.
 */
final class CreateRubricHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("rubrica")) return Optional.empty();
        if (!q.has("crea", "genera", "prepara", "haz", "hazme", "arma", "disena", "quiero", "necesito", "nueva", "evaluar", "calificar")) {
            return Optional.of(AssistantReply.say("create-rubric", "Las rúbricas están en su propia sección: ahí las creas, evalúas a cada estudiante con un clic por criterio y llevas las notas al libro.")
                    .andRun(ReplyAction.of("navigate", "Abrir rúbricas", "route", "rubricas")));
        }
        String topic = q.subject() != null ? q.subject().name() : null;
        String target = q.has("quiz", "examen", "prueba") ? "quiz" : "workshop";
        ReplyAction create = ReplyAction.of("rubric", "Ver la rúbrica", "topic", topic, "target", target, "latest", "true");
        String which = topic != null ? "el último " + (target.equals("quiz") ? "quiz" : "taller") + " de " + topic : "el último " + (target.equals("quiz") ? "quiz" : "taller") + " que preparaste";
        return Optional.of(AssistantReply.say("create-rubric", "Busco " + which + ", leo sus actividades y armo la rúbrica con esos criterios. Queda guardada y enlazada al taller; te la abro para que la revises.")
                .andRun(create));
    }
}
