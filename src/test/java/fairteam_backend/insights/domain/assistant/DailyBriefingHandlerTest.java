package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DailyBriefingHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new Handlers.DailyBriefing().handle(AssistantFixture.query("¿cómo va el curso?", ctx), ctx).orElseThrow();
        assertThat(reply.text()).isEqualTo(ctx.briefing());
        assertThat(reply.actions()).extracting(ReplyAction::label).contains("Preparar refuerzo");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new Handlers.DailyBriefing().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
