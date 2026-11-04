package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AveragesHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new AveragesHandler().handle(AssistantFixture.query("promedio de funciones", ctx), ctx).orElseThrow();
        assertThat(reply.text()).startsWith("En Funciones el promedio es");
        assertThat(new AveragesHandler().handle(AssistantFixture.query("promedio del curso", ctx), ctx).orElseThrow().text()).startsWith("El promedio de Programación I");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new AveragesHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
