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

/** Compares the course average period by period. */
final class PeriodComparisonHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("compara", "comparacion", "entre cortes", "por corte", "cada corte")) return Optional.empty();
        List<String> periods = c.analysis().periods();
        if (periods.size() < 2) return Optional.of(AssistantReply.say("period-comparison", "Por ahora hay notas de un solo periodo; necesito dos para comparar."));
        List<String> parts = new java.util.ArrayList<>();
        for (String period : periods) {
            List<Double> values = c.analysis().students().stream().flatMap(p -> p.periods().stream()).filter(x -> x.period().equals(period)).map(PeriodAverage::average).toList();
            if (!values.isEmpty()) parts.add(period + ": " + Grades.format(Grades.average(values)));
        }
        return Optional.of(AssistantReply.say("period-comparison", "Promedio del curso por corte. " + String.join(", ", parts) + ".")
                .withActions(ReplyAction.of("navigate", "Ver tendencias", "route", "seguimiento", "vista", "tendencia")));
    }
}
