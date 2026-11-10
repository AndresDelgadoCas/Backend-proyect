package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class AssistantBriefTest {
    private final AssistantContext ctx = AssistantFixture.withActivities(
            Turn.teacher("¿Cómo va Valentina Rojas?"), Turn.buho("Valentina viene bajando.", List.of(ReplyAction.of("message", "Escribir a Valentina", "studentId", "1")), null));
    private final AssistantBrief brief = new AssistantBrief(ctx);

    private String juan() { return brief.encode("Juan Pablo Castaño"); }

    private String valentina() { return brief.encode("Valentina Rojas"); }

    @Test
    void namesNeverLeaveTheBackend() {
        assertThat(juan()).matches("E\\d");
        AssistantBrief.Request request = brief.request("hazle un taller de reto a juan pablo y otro a valentína");
        String everything = request.toString();
        assertThat(everything).doesNotContain("Valentina", "valentína", "Juan", "juan pablo", "Santiago", "Camila", "v@u.co");
        assertThat(request.message()).isEqualTo("hazle un taller de reto a " + juan() + " y otro a " + valentina());
        assertThat(request.history()).extracting(AssistantBrief.Line::text).first().isEqualTo("¿Cómo va " + valentina() + "?");
        assertThat(request.history().get(1).text()).contains("[Búho ofreció: escribir un mensaje]");
        assertThat(request.students()).extracting(AssistantBrief.Student::code).containsExactly("E1", "E2", "E3", "E4");
        assertThat(request.activities()).first().satisfies(a -> assertThat(a.title()).isEqualTo("Quiz de funciones"));
        assertThat(request.courses()).extracting(AssistantBrief.Course::name).containsExactly("Estructuras de datos");
    }

    @Test
    void codesComeBackAsNames() {
        assertThat(brief.decode(valentina() + " y " + juan() + " necesitan repasar A2; abre C1")).isEqualTo("Valentina Rojas y Juan Pablo Castaño necesitan repasar \"Taller de ciclos anidados\"; abre Estructuras de datos");
    }

    @Test
    void createActivityRunsRightAwayWithTheTeachersDetails() {
        Interpretation i = new Interpretation("create_activity", "", "", "quiz", "", "HIGH", juan(), "8 preguntas, en parejas", "", "", "");
        AssistantReply reply = brief.reply(i, q -> AssistantReply.say("fallback", "")).orElseThrow();
        assertThat(reply.run().kind()).isEqualTo("workshop");
        assertThat(reply.run().params()).containsEntry("mode", "quiz").containsEntry("level", "HIGH").containsEntry("studentId", "3")
                .containsEntry("auto", "true").containsEntry("prompt", "8 preguntas, en parejas").containsKey("topic");
        assertThat(reply.text()).contains("para Juan");
    }

    @Test
    void createRubricUsesTheActivityTheAiChose() {
        Interpretation i = new Interpretation("create_rubric", "", "Armo la rúbrica de A3.", "", "", "", "", "que evalúe la explicación", "A3", "", "");
        AssistantReply reply = brief.reply(i, q -> AssistantReply.say("fallback", "")).orElseThrow();
        assertThat(reply.run().params()).containsEntry("activityId", "29").containsEntry("prompt", "que evalúe la explicación");
        assertThat(reply.text()).isEqualTo("Armo la rúbrica de \"Taller de recursividad\".");
    }

    @Test
    void courseQuestionsAreAnsweredByTheRules() {
        Interpretation i = new Interpretation("ask_course", "¿Qué nota necesita " + valentina() + " para pasar?", "", "", "", "", "", "", "", "", "");
        AssistantReply reply = brief.reply(i, q -> AssistantEngine.standard().answer(q, ctx)).orElseThrow();
        assertThat(reply.intent()).isEqualTo("passing-grade");
        assertThat(reply.text()).contains("Valentina");
    }

    @Test
    void unusableInterpretationsAreEmpty() {
        assertThat(brief.reply(Interpretation.of("draft_message"), q -> null)).isEmpty();
        assertThat(brief.reply(Interpretation.of("answer"), q -> null)).isEmpty();
        assertThat(brief.reply(Interpretation.of("bailar"), q -> null)).isEmpty();
    }

    @Test
    void decidesWhenTheAiIsWorthAsking() {
        AssistantContext fresh = AssistantFixture.context();
        assertThat(AssistantBrief.worthInterpreting("zzz", AssistantReply.say("fallback", ""), fresh)).isTrue();
        assertThat(AssistantBrief.worthInterpreting("¿quién viene bajando?", AssistantReply.say("falling-behind", ""), fresh)).isFalse();
        assertThat(AssistantBrief.worthInterpreting("y para ella también", AssistantReply.say("student-status", ""), ctx)).isTrue();
        assertThat(AssistantBrief.worthInterpreting("dale", AssistantReply.say("follow-up", ""), ctx)).isFalse();
        assertThat(AssistantBrief.worthInterpreting("crea un taller de derivadas con tres ejercicios en parejas y una pregunta de cierre",
                AssistantReply.say("create-activity", ""), fresh)).isTrue();
    }
}
