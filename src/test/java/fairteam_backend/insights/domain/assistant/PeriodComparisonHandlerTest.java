package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PeriodComparisonHandlerTest {
    @Test
    void comparesTheCourseAverageByPeriod() {
        AssistantContext ctx = AssistantFixture.context();
        AssistantReply reply = new Handlers.PeriodComparison().handle(AssistantFixture.query("compara los cortes", ctx), ctx).orElseThrow();
        assertThat(reply.text()).startsWith("Promedio del curso por corte. Corte 1:").contains("Corte 2:");
    }

    @Test
    void explainsWhenThereIsOnlyOnePeriod() {
        AssistantContext empty = AssistantFixture.emptyCourse();
        assertThat(new Handlers.PeriodComparison().handle(AssistantFixture.query("compara", empty), empty).orElseThrow().text()).contains("un solo periodo");
    }
}
