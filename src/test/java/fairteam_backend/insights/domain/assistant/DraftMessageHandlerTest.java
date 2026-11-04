package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DraftMessageHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new DraftMessageHandler().handle(AssistantFixture.query("escríbele a santiago", ctx), ctx).orElseThrow();
        assertThat(reply.run().kind()).isEqualTo("message");
        assertThat(new DraftMessageHandler().handle(AssistantFixture.query("redacta un mensaje", ctx), ctx).orElseThrow().text()).startsWith("¿A quién");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new DraftMessageHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
