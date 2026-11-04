package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PassingGradeHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new PassingGradeHandler().handle(AssistantFixture.query("¿qué nota necesita valentina para pasar?", ctx), ctx).orElseThrow();
        assertThat(reply.text()).startsWith("Valentina Rojas: Necesita");
        AssistantReply group = new PassingGradeHandler().handle(AssistantFixture.query("quien puede perder", ctx), ctx).orElseThrow();
        assertThat(group.items()).extracting(ReplyItem::name).contains("Valentina Rojas");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new PassingGradeHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
