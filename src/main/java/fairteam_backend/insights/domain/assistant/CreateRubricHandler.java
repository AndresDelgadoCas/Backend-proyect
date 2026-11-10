package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * "Crea una rúbrica para el último taller de matemáticas": Búho picks that activity among the saved ones (by
 * topic or title, newest first), the app asks the AI for a rubric based on its content, saves it linked to the
 * activity and opens it ready to grade. Without saved activities it offers to create one first.
 */
final class CreateRubricHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("rubrica")) return Optional.empty();
        if (!q.has("crea", "genera", "prepara", "haz", "hazme", "arma", "disena", "quiero", "necesito", "nueva", "evaluar", "calificar")) {
            return Optional.of(AssistantReply.say("create-rubric", "Las rúbricas están en su propia sección: ahí las creas, evalúas a cada estudiante con un clic por criterio y llevas las notas al libro.")
                    .andRun(ReplyAction.of("navigate", "Abrir rúbricas", "route", "rubricas")));
        }
        String target = q.has("quiz", "examen", "prueba", "evaluacion") ? "quiz" : "workshop";
        String noun = target.equals("quiz") ? "quiz" : "taller";
        String topic = q.subject() != null ? q.subject().name() : TopicPhrase.in(q.raw()).orElse(null);
        if (c.activities().isEmpty()) {
            ReplyAction create = ReplyAction.of("workshop", "Crear el " + noun + " primero", "mode", target, "topic", topic, "level", "MEDIUM");
            return Optional.of(AssistantReply.say("create-rubric", "Todavía no tienes talleres ni quizzes guardados en este curso. ¿Creo primero el "
                    + noun + (topic != null ? " de " + topic : "") + " y luego te armo la rúbrica a partir de él?").withActions(create));
        }
        Optional<ActivityRef> found = pick(c.activities(), target, topic);
        if (found.isEmpty()) {
            return Optional.of(AssistantReply.say("create-rubric", "No encontré un " + noun + " sobre " + topic + " entre los guardados. ¿Uso el más reciente, \""
                    + c.activities().get(0).title() + "\"?").withActions(rubricFor(c.activities().get(0), q)));
        }
        ActivityRef activity = found.get();
        return Optional.of(AssistantReply.say("create-rubric", "Tomo \"" + activity.title() + "\", leo sus actividades y armo la rúbrica con esos criterios. "
                + "Queda guardada y enlazada a esa actividad; te la abro para que la revises.").andRun(rubricFor(activity, q)));
    }

    private static ReplyAction rubricFor(ActivityRef activity, AssistantQuery q) {
        return ReplyAction.of("rubric", "Ver la rúbrica", "activityId", String.valueOf(activity.id()), "target", activity.kind(), "prompt", q.raw());
    }

    /** Newest activity of the kind about the topic; any kind about the topic; or the newest of the kind. */
    static Optional<ActivityRef> pick(List<ActivityRef> saved, String target, String topic) {
        List<ActivityRef> newest = saved.stream().sorted(Comparator.comparing(ActivityRef::created, Comparator.nullsLast(Comparator.reverseOrder()))).toList();
        if (topic != null && !topic.isBlank()) {
            String wanted = AssistantQuery.normalize(topic);
            List<ActivityRef> about = newest.stream().filter(a -> matches(a, wanted)).toList();
            return about.stream().filter(a -> sameKind(a, target)).findFirst().or(() -> about.stream().findFirst());
        }
        return newest.stream().filter(a -> sameKind(a, target)).findFirst().or(() -> newest.stream().findFirst());
    }

    private static boolean sameKind(ActivityRef a, String target) {
        return target.equals(a.kind()) || target.equals("workshop") && "custom".equals(a.kind());
    }

    private static boolean matches(ActivityRef a, String wanted) {
        String text = AssistantQuery.normalize((a.topic() == null ? "" : a.topic()) + " " + a.title());
        if (text.contains(wanted)) return true;
        for (String word : wanted.split(" ")) {
            if (word.length() < 5) continue;
            String stem = word.substring(0, word.length() - 2);
            if (text.contains(stem)) return true;
        }
        return false;
    }
}
