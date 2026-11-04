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

/** Lists the best averages. */
final class BestStudentsHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("mejor", "mejores", "destacad", "top", "quien va bien", "quienes van bien")) return Optional.empty();
        List<StudentPerformance> best = c.analysis().students().stream().filter(StudentPerformance::hasGrades)
                .sorted(Comparator.comparingDouble(StudentPerformance::average).reversed()).limit(5).toList();
        if (best.isEmpty()) return Optional.empty();
        return Optional.of(AssistantReply.say("best", Grades.firstName(best.get(0).student().name()) + " lleva el mejor promedio con " + Grades.format(best.get(0).average()) + ".")
                .withItems(best.stream().map(p -> Handlers.item(p, Grades.format(p.average()), p.level().label())).toList())
                .withActions(Handlers.workshop("Crear un reto", "workshop", Level.HIGH, null, null)));
    }
}
