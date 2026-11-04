package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SmallTalkHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void greetsAndThanks() {
        assertThat(new SmallTalkHandler().handle(AssistantFixture.query("Hola", ctx), ctx).orElseThrow().text()).startsWith("¡Hola, Sebastián!");
        assertThat(new SmallTalkHandler().handle(AssistantFixture.query("muchas gracias", ctx), ctx).orElseThrow().text()).startsWith("Con gusto");
    }

    @Test
    void doesNotSwallowRealQuestions() {
        assertThat(new SmallTalkHandler().handle(AssistantFixture.query("hola, quien va mal", ctx), ctx)).isEmpty();
    }
}
