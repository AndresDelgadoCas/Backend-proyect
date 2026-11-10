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
    }

    @Test
    void aTopicThatIsNotACourseTopicYetIsKept() {
        AssistantReply reply = new CreateActivityHandler().handle(AssistantFixture.query("Crea un taller de refuerzo sobre la Recursividad en parejas", ctx), ctx).orElseThrow();
        assertThat(reply.run().params()).containsEntry("topic", "Recursividad").containsEntry("level", "LOW")
                .containsEntry("prompt", "Crea un taller de refuerzo sobre la Recursividad en parejas");
        assertThat(reply.text()).contains("sobre Recursividad");
    }

    @Test
    void aSharedWordDoesNotTurnTheTopicIntoAnother() {
        AssistantReply reply = new CreateActivityHandler().handle(AssistantFixture.query("crea un quiz sobre funciones lambda en Java", ctx), ctx).orElseThrow();
        assertThat(reply.run().params()).containsEntry("topic", "Funciones");
        AssistantReply decorator = new CreateActivityHandler().handle(AssistantFixture.query("crea un taller sobre el patrón Decorator", ctx), ctx).orElseThrow();
        assertThat(decorator.run().params()).containsEntry("topic", "patrón Decorator");
        assertThat(decorator.text()).startsWith("Listo, estoy creando un taller de refuerzo sobre patrón Decorator.");
    }

    @Test
    void theLevelTheTeacherAsksForWinsOverTheStudentsOne() {
        AssistantReply reply = new CreateActivityHandler().handle(AssistantFixture.query("haz un taller de reto para Juan Pablo", ctx), ctx).orElseThrow();
        assertThat(reply.run().params()).containsEntry("level", "HIGH").containsEntry("studentId", "3");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new CreateActivityHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
