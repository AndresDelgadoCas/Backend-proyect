package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.CourseSummary;
import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.PeriodAverage;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;
import fairteam_backend.insights.domain.grading.GradeProjection;
import fairteam_backend.insights.domain.grading.ProjectionStatus;

/** Lists students without grades. */
final class MissingGradesHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("sin notas", "no tiene notas", "no tienen notas", "falta nota", "faltan notas", "sin calificacion")) return Optional.empty();
        List<StudentPerformance> missing = c.analysis().students().stream().filter(p -> !p.hasGrades()).toList();
        if (missing.isEmpty()) return Optional.of(AssistantReply.say("missing-grades", "Todos tus estudiantes tienen al menos una nota."));
        return Optional.of(AssistantReply.say("missing-grades", Grades.plural(missing.size(), "estudiante no tiene", "estudiantes no tienen") + " notas todavía. Revisa si faltan en el archivo.")
                .withItems(missing.stream().limit(8).map(p -> Handlers.item(p, "—", "sin notas")).toList())
                .withActions(ReplyAction.of("navigate", "Ver quiénes", "route", "estudiantes", "filtro", "sin-notas")));
    }
}
