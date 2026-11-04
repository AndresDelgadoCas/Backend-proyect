package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FallbackHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new Handlers.Fallback().handle(AssistantFixture.query("lorem", ctx), ctx).orElseThrow();
        assertThat(reply.intent()).isEqualTo("fallback");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new Handlers.Fallback().handle(AssistantFixture.query("zzz", ctx), ctx)).isPresent();
    }
}
