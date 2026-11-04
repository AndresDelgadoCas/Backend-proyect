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

/** Opens another of the teacher's courses by name. */
final class SwitchCourseHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("cambia", "abre el curso", "ir al curso", "curso de")) return Optional.empty();
        return c.courses().stream().filter(course -> course.id() != c.analysis().courseId())
                .filter(course -> List.of(AssistantQuery.normalize(course.name()).split(" ")).stream().anyMatch(w -> w.length() > 3 && q.text().contains(w)))
                .findFirst().map(course -> AssistantReply.say("switch-course", "Listo, abrí " + course.name() + ".")
                        .andRun(ReplyAction.of("course", course.name(), "courseId", String.valueOf(course.id()))));
    }
}
