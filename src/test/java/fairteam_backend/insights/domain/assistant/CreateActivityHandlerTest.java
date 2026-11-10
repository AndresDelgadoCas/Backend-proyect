package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CreateActivityHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new CreateActivityHandler().handle(AssistantFixture.query("crea un quiz de ciclos para valentina", ctx), ctx).orElseThrow();
        assertThat(reply.run().params()).containsEntry("mode", "quiz").containsEntry("studentId", "1").containsEntry("topic", "Ciclos y condicionales");
        assertThat(reply.run().params()).containsEntry("auto", "true");
        AssistantReply noMaterial = new CreateActivityHandler().handle(AssistantFixture.query("crea un taller", AssistantFixture.context(0)), AssistantFixture.context(0)).orElseThrow();
        assertThat(noMaterial.text()).contains("a partir del tema");
        assertThat(noMaterial.run().params()).containsEntry("auto", "true");
    }

    @Test
    void withoutTopicUsesTheOneThatCostsTheMost() {
        AssistantReply reply = new CreateActivityHandler().handle(AssistantFixture.query("hazme el taller", ctx), ctx).orElseThrow();
        assertThat(reply.run().params()).containsKey("topic");
        assertThat(reply.text()).startsWith("Listo, estoy creando");
    }

    @Test
    void leavesRubricsToTheirHandler() {
        assertThat(new CreateActivityHandler().handle(AssistantFixture.query("crea una rubrica para el taller", ctx), ctx)).isEmpty();
        AssistantReply rubric = new CreateRubricHandler().handle(AssistantFixture.query("crea una rúbrica para el último taller de ciclos", ctx), ctx).orElseThrow();
        assertThat(rubric.run().kind()).isEqualTo("rubric");
        assertThat(rubric.run().params()).containsEntry("latest", "true").containsEntry("topic", "Ciclos y condicionales");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new CreateActivityHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
