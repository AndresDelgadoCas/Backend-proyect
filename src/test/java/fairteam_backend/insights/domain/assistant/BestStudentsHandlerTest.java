package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BestStudentsHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new BestStudentsHandler().handle(AssistantFixture.query("los mejores", ctx), ctx).orElseThrow();
        assertThat(reply.text()).startsWith("Juan lleva el mejor promedio");
        assertThat(reply.items()).hasSize(3);
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new BestStudentsHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
