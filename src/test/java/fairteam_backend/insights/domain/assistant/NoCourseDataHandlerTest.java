package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NoCourseDataHandlerTest {
    @Test
    void asksForTheRosterInAnEmptyCourse() {
        AssistantContext empty = AssistantFixture.emptyCourse();
        AssistantReply reply = new NoCourseDataHandler().handle(AssistantFixture.query("quien va mal", empty), empty).orElseThrow();
        assertThat(reply.text()).contains("todavía no tiene estudiantes");
    }

    @Test
    void letsNavigationThroughAndStaysQuietWithStudents() {
        AssistantContext empty = AssistantFixture.emptyCourse();
        assertThat(new NoCourseDataHandler().handle(AssistantFixture.query("importar estudiantes", empty), empty)).isEmpty();
        AssistantContext ctx = AssistantFixture.context();
        assertThat(new NoCourseDataHandler().handle(AssistantFixture.query("quien va mal", ctx), ctx)).isEmpty();
    }
}
