package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

import fairteam_backend.insights.domain.CourseSummary;
import fairteam_backend.insights.domain.Grades;

/** Counts students and grades by level. */
final class CourseCountsHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("cuantos estudiantes", "cuantos alumnos", "cuantos son", "tamano del curso", "cuantas notas")) return Optional.empty();
        CourseSummary s = c.analysis().summary();
        return Optional.of(AssistantReply.say("course-counts", c.analysis().courseName() + " tiene " + Grades.plural(s.students(), "estudiante", "estudiantes")
                + " y " + Grades.plural(s.gradeCount(), "nota cargada", "notas cargadas") + ": " + s.low() + " en refuerzo, " + s.medium() + " en seguimiento y "
                + s.high() + " con buen progreso" + (s.ungraded() > 0 ? "; " + s.ungraded() + " sin notas" : "") + "."));
    }
}
