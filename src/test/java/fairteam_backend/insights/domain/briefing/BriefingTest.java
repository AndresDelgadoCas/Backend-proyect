package fairteam_backend.insights.domain.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import java.time.LocalTime;

import fairteam_backend.insights.domain.CourseAnalysis;
import fairteam_backend.insights.domain.CourseAnalyzer;
import fairteam_backend.insights.domain.GradeRecord;
import fairteam_backend.insights.domain.StudentRef;
import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

class BriefingTest {
    private final CourseAnalyzer analyzer = new CourseAnalyzer(new TrendWeightedRiskStrategy());

    @Test
    void greetsByTimeOfDay() {
        assertThat(Briefing.greeting(LocalTime.of(8, 0))).isEqualTo("Buenos días");
        assertThat(Briefing.greeting(LocalTime.of(15, 0))).isEqualTo("Buenas tardes");
        assertThat(Briefing.greeting(LocalTime.of(20, 0))).isEqualTo("Buenas noches");
    }

    @Test
    void asksForTheRosterWhenTheCourseIsEmpty() {
        CourseAnalysis empty = analyzer.analyze(1, "Cálculo", List.of(), List.of());
        String text = Briefing.builder().greet("Ana Torres", LocalTime.NOON).state(empty).support(empty).nextStep(empty, 0).build();
        assertThat(text).isEqualTo("Buenas tardes, Ana. Cálculo está listo, pero falta la lista de estudiantes. Importa el Excel del grupo y te digo cómo va cada uno.");
    }

    @Test
    void celebratesWhenNobodyIsBelowThree() {
        CourseAnalysis fine = analyzer.analyze(1, "Cálculo", List.of(new StudentRef(1, "Luis", null, null)), List.of(new GradeRecord(1, 1, "Límites", "C1", 4.5)));
        String text = Briefing.builder().state(fine).support(fine).subject(fine).trend(fine).nextStep(fine, 1).build();
        assertThat(text).contains("Nadie está por debajo de tres.").contains("proponer un reto").doesNotContain("Ojo");
    }
}
