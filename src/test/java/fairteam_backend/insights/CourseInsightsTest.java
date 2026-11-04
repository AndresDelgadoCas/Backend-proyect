package fairteam_backend.insights;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import fairteam_backend.insights.application.CourseNotFoundException;
import fairteam_backend.insights.application.service.CourseInsightsService;
import fairteam_backend.insights.domain.CourseAnalysis;
import fairteam_backend.insights.domain.CourseAnalyzer;
import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.assistant.AssistantEngine;
import fairteam_backend.insights.domain.assistant.AssistantReply;
import fairteam_backend.insights.domain.feedback.FeedbackTone;
import fairteam_backend.insights.domain.grading.GradeProjector;
import fairteam_backend.insights.domain.grading.GradingScheme;
import fairteam_backend.insights.domain.grading.ProjectionStatus;
import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

class CourseInsightsTest {
    private final CourseAnalyzer analyzer = new CourseAnalyzer(new TrendWeightedRiskStrategy());
    private final Clock afternoon = Clock.fixed(Instant.parse("2026-11-04T20:00:00Z"), ZoneId.of("America/Bogota"));
    private final CourseInsightsService service = new CourseInsightsService(InsightsFixtures.courseData(), InsightsFixtures.schemes(),
            analyzer, new GradeProjector(), AssistantEngine.standard(), afternoon);

    private CourseAnalysis analysis() {
        return analyzer.analyze(1, "Programación I", InsightsFixtures.STUDENTS, InsightsFixtures.grades());
    }

    private StudentPerformance student(long id) { return analysis().student(id).orElseThrow(); }

    @Test
    void classifiesOnTheValueTheTeacherSees() {
        assertThat(Level.fromAverage(2.97)).isEqualTo(Level.MEDIUM);
        assertThat(Level.fromAverage(2.94)).isEqualTo(Level.LOW);
        assertThat(Level.fromAverage(3.96)).isEqualTo(Level.HIGH);
    }

    @Test
    void summarizesTheCourseAndDetectsTrends() {
        CourseAnalysis a = analysis();
        assertThat(a.periods()).containsExactly("Corte 1", "Corte 2");
        assertThat(a.summary().students()).isEqualTo(5);
        assertThat(a.summary().ungraded()).isEqualTo(1);
        assertThat(student(1).direction()).isEqualTo(Direction.DOWN);
        assertThat(student(4).direction()).isEqualTo(Direction.UP);
        assertThat(student(1).level()).isEqualTo(Level.LOW);
        assertThat(a.students().get(0).student().name()).isEqualTo("Valentina Rojas Díaz"); // highest risk first
        assertThat(a.weakestSubject().orElseThrow().name()).isEqualTo("Ciclos y condicionales");
        assertThat(student(5).hasGrades()).isFalse();
    }

    @Test
    void projectsTheGradeNeededToPass() {
        GradingScheme scheme = new GradingScheme(List.of(30, 30, 40), 3.0);
        var projector = new GradeProjector();
        var valentina = projector.project(student(1), analysis().periods(), scheme);
        // earned = 2.45*0.3 + 1.85*0.3 = 1.29 -> needs (3 - 1.29) / 0.4 = 4.275
        assertThat(valentina.needed()).isCloseTo(4.275, org.assertj.core.data.Offset.offset(0.001));
        assertThat(valentina.status()).isEqualTo(ProjectionStatus.HARD);
        var juan = projector.project(student(4), analysis().periods(), scheme);
        // about 2.8 earned (grades are capped at 5) -> barely half a point needed in the last 40%
        assertThat(juan.status()).isEqualTo(ProjectionStatus.POSSIBLE);
        assertThat(juan.needed()).isLessThan(1.0);
        assertThat(projector.project(student(4), analysis().periods(), new GradingScheme(List.of(50, 50), 3.0)).status()).isEqualTo(ProjectionStatus.PASSED);
        assertThat(projector.project(student(5), analysis().periods(), scheme).status()).isEqualTo(ProjectionStatus.NO_DATA);
    }

    @Test
    void rejectsSchemesThatDoNotAddUp() {
        assertThatThrownBy(() -> new GradingScheme(List.of(30, 30, 30), 3.0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void writesTheBriefingAndFeedbackFromTheData() {
        var insights = service.insights(1, "Sebastián Granda");
        assertThat(insights.briefing()).startsWith("Buenas tardes, Sebastián.").contains("Ojo: 2 estudiantes vienen bajando");
        assertThat(insights.scheme().weights()).containsExactly(30, 30, 40);
        assertThat(insights.scheme().periods()).containsExactly("Corte 1", "Corte 2", "Corte 3");
        var message = service.draft(1, 1, FeedbackTone.CERCANO, "Sebastián Granda");
        assertThat(message.body()).startsWith("Hola, Valentina:").contains("ha bajado").contains("Ciclos y condicionales").endsWith("Sebastián Granda");
        assertThat(service.draft(1, 1, FeedbackTone.FORMAL, "S").body()).startsWith("Estimado(a) Valentina Rojas Díaz:");
        assertThatThrownBy(() -> service.insights(99, "x")).isInstanceOf(CourseNotFoundException.class);
    }

    @Test
    void storesTheGradingScheme() {
        var view = service.updateScheme(1, new GradingScheme(List.of(20, 30, 50), 3.0));
        assertThat(view.custom()).isTrue();
        assertThat(view.weights()).containsExactly(20, 30, 50);
    }

    @Test
    void assistantUnderstandsTeachersQuestions() {
        AssistantReply bajando = service.ask(1, "¿Quién viene bajando?", "Sebastián");
        assertThat(bajando.intent()).isEqualTo("falling-behind");
        assertThat(bajando.items()).extracting(i -> i.name()).containsExactly("Valentina Rojas Díaz", "Mariana López Herrera");

        assertThat(service.ask(1, "¿Qué nota necesita Valentina para pasar?", "S").text()).contains("Valentina Rojas Díaz: Necesita 4,3");
        assertThat(service.ask(1, "¿Quién necesita refuerzo en Funciones?", "S").intent()).isEqualTo("who-needs-help");
        AssistantReply quiz = service.ask(1, "Crea un quiz de ciclos para refuerzo", "S");
        assertThat(quiz.run().kind()).isEqualTo("workshop");
        assertThat(quiz.run().params()).containsEntry("mode", "quiz").containsEntry("level", "LOW").containsEntry("topic", "Ciclos y condicionales");
        assertThat(service.ask(1, "Escríbele un mensaje a Santiago", "S").run().params()).containsEntry("studentId", "2");
        assertThat(service.ask(1, "cambia a estructuras", "S").run().params()).containsEntry("courseId", "2");
        assertThat(service.ask(1, "¿cómo va Mariana?", "S").intent()).isEqualTo("student-status");
        assertThat(service.ask(1, "genera el informe", "S").run().kind()).isEqualTo("report");
        assertThat(service.ask(1, "¿cómo va el curso?", "Sebastián").intent()).isEqualTo("briefing");
        assertThat(service.ask(1, "blablá xyz", "S").intent()).isEqualTo("fallback");
    }
}
