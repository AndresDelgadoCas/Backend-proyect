package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.grading.GradeProjection;
import fairteam_backend.insights.domain.grading.ProjectionStatus;

/** Summarizes one student. */
final class StudentStatusHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        StudentPerformance s = q.student();
        if (s == null || q.has("quienes", "cuantos")) return Optional.empty();
        String id = String.valueOf(s.student().id());
        StringBuilder text = new StringBuilder();
        if (!s.hasGrades()) text.append(s.student().name()).append(" todavía no tiene notas.");
        else {
            text.append(s.student().name()).append(" tiene ").append(Grades.format(s.average())).append(" de promedio en ")
                    .append(Grades.plural(s.gradeCount(), "nota", "notas")).append(": ").append(s.level().label().toLowerCase()).append('.');
            if (s.direction() == Direction.DOWN) text.append(" Viene bajando (").append(Handlers.path(s)).append(").");
            if (s.direction() == Direction.UP) text.append(" Viene mejorando.");
            if (s.weakest() != null) text.append(" Su tema más difícil es ").append(s.weakest().name()).append(", con ").append(Grades.format(s.weakest().average())).append('.');
            GradeProjection p = c.projections().get(s.student().id());
            if (p != null && p.status() != ProjectionStatus.PASSED && p.status() != ProjectionStatus.NO_DATA) text.append(' ').append(p.explanation());
        }
        Level level = s.level() == Level.NO_DATA ? Level.MEDIUM : s.level();
        return Optional.of(AssistantReply.say("student-status", text.toString()).withActions(
                ReplyAction.of("review", "Ver ficha", "studentId", id),
                Handlers.workshop("Taller para este estudiante", "workshop", level, s.weakest() == null ? null : s.weakest().name(), s),
                ReplyAction.of("message", "Escribirle", "studentId", id)));
    }
}
