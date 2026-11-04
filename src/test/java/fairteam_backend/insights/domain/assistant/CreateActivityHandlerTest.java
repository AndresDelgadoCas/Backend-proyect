package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CreateActivityHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new CreateActivityHandler().handle(AssistantFixture.query("crea un quiz de ciclos para valentina", ctx), ctx).orElseThrow();
        assertThat(reply.run().params()).containsEntry("mode", "quiz").containsEntry("studentId", "1").containsEntry("topic", "Ciclos y condicionales");
        assertThat(new CreateActivityHandler().handle(AssistantFixture.query("crea un taller", AssistantFixture.context(0)), AssistantFixture.context(0)).orElseThrow().text()).contains("necesito el material");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new CreateActivityHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
