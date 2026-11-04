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

/** Asks for grades before answering grade questions. */
final class NeedsGradesHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (c.analysis().summary().gradeCount() > 0 || q.has("equipo", "importa", "sube", "material", "abre", "ir a", "llevame")) return Optional.empty();
        return Optional.of(AssistantReply.say("needs-grades", "Aún no hay notas en " + c.analysis().courseName() + ". Cárgalas y podré responderte eso.")
                .withActions(ReplyAction.of("navigate", "Ir a estudiantes y notas", "route", "estudiantes")));
    }
}
