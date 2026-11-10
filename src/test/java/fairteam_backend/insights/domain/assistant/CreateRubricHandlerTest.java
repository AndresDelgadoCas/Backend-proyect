package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CreateRubricHandlerTest {
    private final AssistantContext ctx = AssistantFixture.withActivities();

    private AssistantReply ask(String text, AssistantContext c) {
        return new CreateRubricHandler().handle(AssistantFixture.query(text, c), c).orElseThrow();
    }

    @Test
    void picksTheLatestWorkshopOfTheTopic() {
        AssistantReply reply = ask("Crea una rúbrica para evaluar el último taller de ciclos", ctx);
        assertThat(reply.run().kind()).isEqualTo("rubric");
        assertThat(reply.run().params()).containsEntry("activityId", "30");
        assertThat(reply.text()).contains("Taller de ciclos anidados");
    }

    @Test
    void findsATopicThatIsOnlyInTheActivity() {
        assertThat(ask("hazme una rúbrica del taller de recursividad", ctx).run().params()).containsEntry("activityId", "29");
    }

    @Test
    void withoutTopicTakesTheNewestOfTheKind() {
        assertThat(ask("crea una rúbrica para el último taller", ctx).run().params()).containsEntry("activityId", "30");
        assertThat(ask("crea una rúbrica para el último quiz", ctx).run().params()).containsEntry("activityId", "31");
    }

    @Test
    void unknownTopicAsksBeforeUsingAnother() {
        AssistantReply reply = ask("crea una rúbrica para el taller de termodinámica", ctx);
        assertThat(reply.run()).isNull();
        assertThat(reply.actions()).singleElement().satisfies(a -> assertThat(a.params()).containsEntry("activityId", "31"));
    }

    @Test
    void withoutSavedActivitiesOffersToCreateOne() {
        AssistantContext empty = AssistantFixture.context();
        AssistantReply reply = ask("crea una rúbrica para el taller de ciclos", empty);
        assertThat(reply.text()).contains("Todavía no tienes talleres");
        assertThat(reply.actions()).singleElement().satisfies(a -> assertThat(a.kind()).isEqualTo("workshop"));
    }
}
