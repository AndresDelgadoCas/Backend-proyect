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

    @Test
    void anAccountWithoutCoursesStillGetsHowToAnswers() {
        var none = new AssistantContext("Ana", new fairteam_backend.insights.domain.CourseAnalyzer(new fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy()).analyze(0, AssistantContext.NO_COURSE, List.of(), List.of()),
                java.util.Map.of(), 0, List.of(), "");
        assertThat(engine.answer("como creo un curso", none).intent()).isNotEqualTo("no-course");
        assertThat(engine.answer("quien necesita ayuda", none).intent()).isEqualTo("no-course");
    }

    @Test
    void whenTheAiIsDownAUnderstoodTopicStillGetsTheClosestGuide() {
        AssistantReply fallback = engine.answer("cuantos planetas tiene el sistema solar", ctx);
        assertThat(engine.degrade("cuantos planetas tiene el sistema solar", fallback, ctx).intent()).isEqualTo("fallback");
        AssistantReply rules = engine.answer("necesito pasar mis notas desde un archivo excel", ctx);
        assertThat(engine.degrade("necesito pasar mis notas desde un archivo excel", rules, ctx).text()).isNotBlank();
    }
}
