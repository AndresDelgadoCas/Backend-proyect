package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AssistantEngineTest {
    @Test
    void anEmptyQuestionGetsAPrompt() {
        assertThat(AssistantEngine.standard().answer("  ¿? ", AssistantFixture.context()).intent()).isEqualTo("empty");
    }

    @Test
    void theFirstHandlerThatMatchesWins() {
        AssistantEngine engine = new AssistantEngine(java.util.List.of(
                (q, c) -> java.util.Optional.empty(),
                (q, c) -> java.util.Optional.of(AssistantReply.say("second", "dos")),
                (q, c) -> java.util.Optional.of(AssistantReply.say("third", "tres"))));
        assertThat(engine.answer("hola", AssistantFixture.context()).intent()).isEqualTo("second");
    }

    @Test
    void unknownQuestionsFallBackPolitely() {
        assertThat(AssistantEngine.standard().answer("xyzzy plugh", AssistantFixture.context()).intent()).isEqualTo("fallback");
    }
}
