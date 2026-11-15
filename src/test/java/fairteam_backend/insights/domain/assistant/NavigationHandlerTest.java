package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NavigationHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new NavigationHandler().handle(AssistantFixture.query("abre el simulador", ctx), ctx).orElseThrow();
        assertThat(reply.run().params()).containsEntry("route", "follow-up").containsEntry("view", "simulator");
        assertThat(new NavigationHandler().handle(AssistantFixture.query("llévame a los ajustes", ctx), ctx).orElseThrow().run().params()).containsEntry("route", "settings");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new NavigationHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
