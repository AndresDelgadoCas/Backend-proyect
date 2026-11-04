package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CourseCountsHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void summarizesHowManyStudentsAndGrades() {
        AssistantReply reply = new CourseCountsHandler().handle(AssistantFixture.query("¿cuántos estudiantes hay?", ctx), ctx).orElseThrow();
        assertThat(reply.text()).startsWith("Programación I tiene 4 estudiantes y 12 notas cargadas").contains("1 sin notas");
    }
}
