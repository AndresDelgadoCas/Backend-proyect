package fairteam_backend.insights.domain.assistant;

import java.util.List;
import java.util.Optional;

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
