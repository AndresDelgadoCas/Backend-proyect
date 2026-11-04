package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExplainRiskHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void explainsWithAverageTrendAndSubject() {
        String text = new ExplainRiskHandler().handle(AssistantFixture.query("¿por qué valentina está en riesgo?", ctx), ctx).orElseThrow().text();
        assertThat(text).startsWith("Valentina Rojas tiene riesgo alto").contains("bajó 0,7 entre cortes").contains("Ciclos y condicionales");
    }

    @Test
    void needsAStudent() {
        assertThat(new ExplainRiskHandler().handle(AssistantFixture.query("por qué", ctx), ctx)).isEmpty();
    }
}
