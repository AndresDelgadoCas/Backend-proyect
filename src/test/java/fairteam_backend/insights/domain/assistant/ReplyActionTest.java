package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReplyActionTest {
    @Test
    void dropsMissingParameters() {
        ReplyAction action = ReplyAction.of("workshop", "Abrir", "mode", "quiz", "topic", null);
        assertThat(action.params()).containsOnlyKeys("mode");
    }

    @Test
    void replyBuildersAreImmutableCopies() {
        AssistantReply reply = AssistantReply.say("x", "hola").withActions(ReplyAction.of("report", "Informe"));
        assertThat(reply.actions()).hasSize(1);
        assertThat(reply.withItems(java.util.List.of()).actions()).hasSize(1);
        assertThat(reply.run()).isNull();
    }
}
