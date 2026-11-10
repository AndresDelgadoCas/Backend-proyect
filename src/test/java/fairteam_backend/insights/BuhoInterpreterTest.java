package fairteam_backend.insights;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import fairteam_backend.insights.application.port.out.ActivityCatalogPort;
import fairteam_backend.insights.application.port.out.AssistantInterpreterPort;
import fairteam_backend.insights.application.port.out.AttendancePort;
import fairteam_backend.insights.application.service.CourseInsightsService;
import fairteam_backend.insights.domain.CourseAnalyzer;
import fairteam_backend.insights.domain.assistant.ActivityRef;
import fairteam_backend.insights.domain.assistant.AssistantBrief;
import fairteam_backend.insights.domain.assistant.AssistantEngine;
import fairteam_backend.insights.domain.assistant.AssistantReply;
import fairteam_backend.insights.domain.assistant.Interpretation;
import fairteam_backend.insights.domain.assistant.ReplyAction;
import fairteam_backend.insights.domain.assistant.Turn;
import fairteam_backend.insights.domain.grading.GradeProjector;
import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

/** Búho with the AI interpreter: when it is consulted, what it receives and what happens when it fails. */
class BuhoInterpreterTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-04T20:00:00Z"), ZoneId.of("America/Bogota"));
    private final List<AssistantBrief.Request> seen = new ArrayList<>();
    private final ActivityCatalogPort catalog = id -> List.of(new ActivityRef(7, "workshop", "Taller de ciclos", "Ciclos y condicionales", LocalDate.of(2026, 11, 3)));

    private CourseInsightsService service(Optional<Interpretation> answer) {
        AssistantInterpreterPort interpreter = request -> { seen.add(request); return answer; };
        return new CourseInsightsService(InsightsFixtures.courseData(), InsightsFixtures.schemes(), new CourseAnalyzer(new TrendWeightedRiskStrategy()),
                new GradeProjector(), AssistantEngine.standard(), clock, AttendancePort.none(), catalog, interpreter);
    }

    @Test
    void clearQuestionsStayWithTheRulesAndNeverReachTheAi() {
        AssistantReply reply = service(Optional.empty()).ask(1, "¿Quién viene bajando?", "S", List.of());
        assertThat(reply.intent()).isEqualTo("falling-behind");
        assertThat(seen).isEmpty();
    }

    @Test
    void whatTheRulesDoNotGetIsDoneByTheAi() {
        Interpretation answer = new Interpretation("answer", "", "Empieza con un ejemplo de la vida diaria y luego formaliza.", "", "", "", "", "", "", "", "");
        AssistantReply reply = service(Optional.of(answer)).ask(1, "¿cómo le explico mejor la recursividad a un grupo que se pierde?", "S", List.of());
        assertThat(reply.intent()).isEqualTo("ai-answer");
        assertThat(reply.text()).startsWith("Empieza con un ejemplo");
        assertThat(seen).singleElement().satisfies(r -> assertThat(r.activities()).extracting(AssistantBrief.Activity::title).containsExactly("Taller de ciclos"));
    }

    @Test
    void followUpsUseTheConversation() {
        Interpretation answer = new Interpretation("create_activity", "", "", "quiz", "Funciones", "HIGH", "", "", "", "", "");
        List<Turn> history = List.of(Turn.teacher("crea un quiz de ciclos para refuerzo"),
                Turn.buho("Listo, estoy creando un quiz sobre Ciclos.", List.of(), ReplyAction.of("workshop", "Ver en Talleres", "mode", "quiz", "topic", "Ciclos y condicionales")));
        AssistantReply reply = service(Optional.of(answer)).ask(1, "ahora uno de reto pero de funciones", "S", history);
        assertThat(reply.run().params()).containsEntry("mode", "quiz").containsEntry("level", "HIGH").containsEntry("topic", "Funciones").containsEntry("auto", "true");
        assertThat(seen.get(0).history()).hasSize(2);
        assertThat(seen.get(0).history().get(1).text()).contains("Búho ya hizo: crear un quiz sobre Ciclos y condicionales");
    }

    @Test
    void ifTheAiFailsTheRulesAnswer() {
        AssistantReply reply = service(Optional.empty()).ask(1, "blablá xyz", "S", List.of());
        assertThat(reply.intent()).isEqualTo("fallback");
        assertThat(seen).hasSize(1);
    }
}
