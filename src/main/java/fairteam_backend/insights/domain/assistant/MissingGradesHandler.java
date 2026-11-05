package fairteam_backend.insights.domain.assistant;

import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;

/** Lists students without grades, or with grades still pending in some topic. */
final class MissingGradesHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("sin notas", "no tiene notas", "no tienen notas", "falta nota", "faltan notas", "sin calificacion",
                "notas pendientes", "pendiente")) return Optional.empty();
        List<StudentPerformance> missing = c.analysis().students().stream().filter(p -> !p.hasGrades()).toList();
        if (missing.isEmpty()) {
            List<ReplyItem> pending = pending(c);
            if (pending.isEmpty()) return Optional.of(AssistantReply.say("missing-grades", "No falta ninguna nota: todos tienen sus notas de cada tema en cada corte."));
            return Optional.of(AssistantReply.say("missing-grades", Grades.plural(pending.size(), "nota está pendiente", "notas están pendientes")
                    + ". Cuando las registres, actualizo el seguimiento.").withItems(pending.stream().limit(8).toList()));
        }
        return Optional.of(AssistantReply.say("missing-grades", Grades.plural(missing.size(), "estudiante no tiene", "estudiantes no tienen") + " notas todavía. Revisa si faltan en el archivo.")
                .withItems(missing.stream().limit(8).map(p -> Handlers.item(p, "—", "sin notas")).toList())
                .withActions(ReplyAction.of("navigate", "Ver quiénes", "route", "estudiantes", "filtro", "sin-notas")));
    }

    /** One grade per topic and period is expected; fewer means something is still to be recorded. */
    static List<ReplyItem> pending(AssistantContext c) {
        int periods = c.analysis().periods().size();
        List<ReplyItem> items = new java.util.ArrayList<>();
        for (StudentPerformance p : c.analysis().students()) {
            if (!p.hasGrades()) continue;
            for (SubjectAverage subject : c.analysis().subjects()) {
                int have = p.subjects().stream().filter(s -> s.subjectId() == subject.subjectId()).mapToInt(SubjectAverage::count).sum();
                if (have < periods) {
                    // Short value, topic in the note: long topic names would hide the student's name.
                    items.add(new ReplyItem(p.student().id(), p.student().name(), String.valueOf(periods - have),
                            "pendiente en " + subject.name(), "medium"));
                }
            }
        }
        return items;
    }
}
