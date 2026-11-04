package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

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
}
