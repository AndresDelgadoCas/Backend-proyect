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

/** Asks for the roster when the course has no students. */
final class NoCourseDataHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (c.analysis().summary().students() > 0 || q.has("importa", "sube", "cargar", "estudiantes", "lista", "equipo")) return Optional.empty();
        return Optional.of(AssistantReply.say("no-students", c.analysis().courseName() + " todavía no tiene estudiantes. Importa la lista del grupo y te ayudo con todo lo demás.")
                .withActions(ReplyAction.of("navigate", "Ir a estudiantes", "route", "estudiantes")));
    }
}
