package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TeacherAdviceHandlerTest {
    @Test
    void prioritizesReinforcementTrendAndMissingGrades() {
        AssistantContext ctx = AssistantFixture.context();
        String text = new TeacherAdviceHandler().handle(AssistantFixture.query("¿qué me recomiendas?", ctx), ctx).orElseThrow().text();
        assertThat(text).startsWith("Te recomiendo: prepara un refuerzo de Ciclos y condicionales para 1 estudiante")
                .contains("quien viene bajando").contains("completa las notas de 1 estudiante");
    }

    @Test
    void asksForMaterialWhenThereIsNone() {
        AssistantContext ctx = AssistantFixture.context(0);
        assertThat(new TeacherAdviceHandler().handle(AssistantFixture.query("que hago", ctx), ctx).orElseThrow().text()).doesNotContain("sube el material"); // only top 3 steps
    }
}
