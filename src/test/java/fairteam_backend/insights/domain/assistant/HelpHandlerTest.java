package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HelpHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new HelpHandler().handle(AssistantFixture.query("¿qué puedes hacer?", ctx), ctx).orElseThrow();
        assertThat(reply.text()).contains("talleres y quizzes");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new HelpHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
