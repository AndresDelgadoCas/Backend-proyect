package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MissingGradesHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void listsStudentsWithoutGrades() {
        AssistantReply reply = new MissingGradesHandler().handle(AssistantFixture.query("¿quién no tiene notas?", ctx), ctx).orElseThrow();
        assertThat(reply.items()).extracting(ReplyItem::name).containsExactly("Camila Ospina");
        assertThat(reply.actions().get(0).params()).containsEntry("filtro", "sin-notas");
    }

    @Test
    void ignoresOtherQuestions() {
        assertThat(new MissingGradesHandler().handle(AssistantFixture.query("promedio", ctx), ctx)).isEmpty();
    }
}
