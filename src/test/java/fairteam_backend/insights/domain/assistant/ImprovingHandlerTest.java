package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ImprovingHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new ImprovingHandler().handle(AssistantFixture.query("quién va mejorando", ctx), ctx).orElseThrow();
        assertThat(reply.items()).extracting(ReplyItem::name).containsExactly("Juan Pablo Castaño");
        assertThat(reply.items().get(0).value()).startsWith("+");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new ImprovingHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
