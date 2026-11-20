package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

import fairteam_backend.insights.domain.Grades;

/** Answers how many courses the teacher has and which ones, with a button to open each. */
final class CoursesListHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("cuantos cursos", "cuantas materias", "cuantos grupos", "que cursos", "cuales cursos", "mis cursos", "lista de cursos", "lista mis cursos", "cursos tengo", "cursos hay")) {
            return Optional.empty();
        }
        var courses = c.courses();
        if (courses.isEmpty()) {
            return Optional.of(AssistantReply.say("courses-list", "Todavía no tienes cursos. Crea el primero en Mis cursos, o carga el curso de demostración."));
        }
        String names = String.join(", ", courses.stream().map(CourseRef::name).limit(8).toList());
        AssistantReply reply = AssistantReply.say("courses-list", "Tienes " + Grades.plural(courses.size(), "curso", "cursos") + ": " + names + ".");
        return Optional.of(reply.withActions(courses.stream().limit(4)
                .map(course -> ReplyAction.of("course", course.name(), "courseId", String.valueOf(course.id()))).toArray(ReplyAction[]::new)));
    }
}
