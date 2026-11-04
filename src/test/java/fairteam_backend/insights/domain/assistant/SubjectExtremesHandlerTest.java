package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SubjectExtremesHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void findsTheHardestAndEasiestSubject() {
        assertThat(new SubjectExtremesHandler().handle(AssistantFixture.query("¿qué tema les cuesta más?", ctx), ctx).orElseThrow().text())
                .startsWith("El tema que más cuesta es Ciclos y condicionales");
        assertThat(new SubjectExtremesHandler().handle(AssistantFixture.query("el tema más fácil", ctx), ctx).orElseThrow().text())
                .startsWith("El tema con mejor promedio es Funciones");
    }

    @Test
    void needsADirection() {
        assertThat(new SubjectExtremesHandler().handle(AssistantFixture.query("tema", ctx), ctx)).isEmpty();
    }
}
