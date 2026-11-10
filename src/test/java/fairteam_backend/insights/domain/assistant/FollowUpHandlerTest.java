package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class FollowUpHandlerTest {
    private final ReplyAction offer = ReplyAction.of("workshop", "Preparar refuerzo", "mode", "workshop", "level", "LOW", "topic", "Funciones");

    @Test
    void yesRunsWhatBuhoOffered() {
        AssistantContext c = AssistantFixture.withActivities(Turn.teacher("¿qué me recomiendas?"), Turn.buho("Te propongo un refuerzo de Funciones.", List.of(offer), null));
        for (String yes : List.of("sí", "Dale", "sí, hazlo por favor", "ok", "de una")) {
            AssistantReply reply = new FollowUpHandler().handle(AssistantFixture.query(yes, c), c).orElseThrow();
            assertThat(reply.run().kind()).isEqualTo("workshop");
            assertThat(reply.run().params()).containsEntry("auto", "true").containsEntry("topic", "Funciones");
        }
    }

    @Test
    void onlyShortRepliesAfterAnOfferCount() {
        AssistantContext c = AssistantFixture.withActivities(Turn.buho("Te propongo un refuerzo.", List.of(offer), null));
        assertThat(new FollowUpHandler().handle(AssistantFixture.query("sí pero que sea de recursividad y en parejas", c), c)).isEmpty();
        AssistantContext fresh = AssistantFixture.context();
        assertThat(new FollowUpHandler().handle(AssistantFixture.query("sí", fresh), fresh)).isEmpty();
    }

    @Test
    void theEngineAnswersAYesFirst() {
        AssistantContext c = AssistantFixture.withActivities(Turn.buho("¿Te preparo un quiz?", List.of(offer), null));
        assertThat(AssistantEngine.standard().answer("dale", c).intent()).isEqualTo("follow-up");
    }
}
