package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class SwitchCourseHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new SwitchCourseHandler().handle(AssistantFixture.query("cambia a estructuras", ctx), ctx).orElseThrow();
        assertThat(reply.run().params()).containsEntry("courseId", "11");
        assertThat(new SwitchCourseHandler().handle(AssistantFixture.query("cambia a programacion", ctx), ctx)).isEmpty();
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new SwitchCourseHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }

    private AssistantContext withCourses(CourseRef... courses) {
        return new AssistantContext(ctx.teacher(), ctx.analysis(), ctx.projections(), ctx.materials(), List.of(courses), ctx.briefing());
    }

    @Test
    void picksTheCourseWithMostMatchingWords() {
        AssistantContext c = withCourses(new CourseRef(10, "Programación I"), new CourseRef(11, "Estructuras de Datos"),
                new CourseRef(12, "Bases de Datos I"));
        AssistantReply reply = new SwitchCourseHandler().handle(AssistantFixture.query("¿cómo voy con el curso de bases de datos?", c), c).orElseThrow();
        assertThat(reply.run().params()).containsEntry("courseId", "12");
    }

    @Test
    void asksWhenTwoCoursesMatchEqually() {
        AssistantContext c = withCourses(new CourseRef(10, "Programación I"), new CourseRef(11, "Bases de Datos I"),
                new CourseRef(12, "Bases de Datos II"));
        AssistantReply reply = new SwitchCourseHandler().handle(AssistantFixture.query("abre el curso de bases de datos", c), c).orElseThrow();
        assertThat(reply.text()).contains("¿Cuál abro?");
        assertThat(reply.actions()).hasSize(2);
    }

    @Test
    void genericWordsDoNotCount() {
        assertThat(SwitchCourseHandler.score("Patrones de Diseño — Grupo 61", "curso del grupo")).isZero();
    }
}
