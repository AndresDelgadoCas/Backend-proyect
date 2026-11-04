package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FallingBehindHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new FallingBehindHandler().handle(AssistantFixture.query("¿quién viene bajando?", ctx), ctx).orElseThrow();
        assertThat(reply.items()).extracting(ReplyItem::name).containsExactly("Valentina Rojas");
        assertThat(reply.items().get(0).value()).isEqualTo("−0,7");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new FallingBehindHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
