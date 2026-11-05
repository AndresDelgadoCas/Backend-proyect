package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Opens another of the teacher's courses by name, choosing the one whose name matches best. */
final class SwitchCourseHandler implements IntentHandler {
    private static final Set<String> GENERIC = Set.of("curso", "grupo", "materia", "clase");

    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("cambia", "abre el curso", "ir al curso", "curso de")) return Optional.empty();
        record Match(CourseRef course, long score) { }
        List<Match> matches = c.courses().stream().filter(course -> course.id() != c.analysis().courseId())
                .map(course -> new Match(course, score(course.name(), q.text())))
                .filter(m -> m.score() > 0)
                .sorted(Comparator.comparingLong(Match::score).reversed()).toList();
        if (matches.isEmpty()) return Optional.empty();
        if (matches.size() > 1 && matches.get(0).score() == matches.get(1).score()) {
            return Optional.of(AssistantReply.say("switch-course", "Tienes varios cursos con ese nombre. ¿Cuál abro?")
                    .withActions(matches.stream().limit(4).map(m -> ReplyAction.of("course", m.course().name(),
                            "courseId", String.valueOf(m.course().id()))).toArray(ReplyAction[]::new)));
        }
        CourseRef course = matches.get(0).course();
        return Optional.of(AssistantReply.say("switch-course", "Listo, abrí " + course.name() + ".")
                .andRun(ReplyAction.of("course", course.name(), "courseId", String.valueOf(course.id()))));
    }

    /** How many meaningful words of the course name appear in the question. */
    static long score(String courseName, String question) {
        return List.of(AssistantQuery.normalize(courseName).split(" ")).stream()
                .filter(w -> w.length() > 3 && !GENERIC.contains(w))
                .distinct().filter(question::contains).count();
    }
}
