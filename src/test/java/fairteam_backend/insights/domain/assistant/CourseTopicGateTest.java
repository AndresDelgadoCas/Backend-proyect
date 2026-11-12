package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class CourseTopicGateTest {
    private final AssistantContext ctx = AssistantFixture.context();
    private final AssistantEngine engine = AssistantEngine.standard();

    @Test
    void generalQuestionsAreNotHijackedByCourseRules() {
        for (String q : List.of("cuantos planetas tiene el sistema solar", "cual es la capital de francia", "quien descubrio america"))
            assertThat(engine.answer(q, ctx).intent()).as(q).isEqualTo("fallback");
    }

    @Test
    void courseQuestionsStillUseTheRules() {
        assertThat(engine.answer("cuantos estudiantes hay", ctx).intent()).isEqualTo("course-counts");
        assertThat(engine.answer("quien necesita ayuda", ctx).intent()).isNotEqualTo("fallback");
    }

    @Test
    void greetingsWithExtraWordsGetAGreeting() {
        assertThat(engine.answer("hola mi vida", ctx).intent()).isEqualTo("small-talk");
        assertThat(engine.answer("hoal brother", ctx).intent()).isEqualTo("small-talk");
    }
}
