package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WhoNeedsHelpHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new Handlers.WhoNeedsHelp().handle(AssistantFixture.query("¿quién necesita refuerzo?", ctx), ctx).orElseThrow();
        assertThat(reply.text()).isEqualTo("Solo Valentina Rojas necesita refuerzo.");
        assertThat(new Handlers.WhoNeedsHelp().handle(AssistantFixture.query("quienes estan en seguimiento", ctx), ctx).orElseThrow().items()).extracting(ReplyItem::name).contains("Santiago Gómez");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new Handlers.WhoNeedsHelp().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
