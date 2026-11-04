package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StudentStatusHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new Handlers.StudentStatus().handle(AssistantFixture.query("¿cómo va valentina?", ctx), ctx).orElseThrow();
        assertThat(reply.text()).contains("Viene bajando").contains("Ciclos y condicionales");
        assertThat(reply.actions()).extracting(ReplyAction::kind).containsExactly("review", "workshop", "message");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new Handlers.StudentStatus().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
