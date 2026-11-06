package fairteam_backend.insights.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.Test;

import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

class WeightedPeriodTest {
    private static GradeRecord g(long subject, String period, double value, Double weight) {
        return new GradeRecord(1, subject, "T" + subject, period, value, weight);
    }

    @Test
    void withoutWeightsAPeriodIsAPlainMean() {
        assertThat(GradeRecord.periodAverage(List.of(g(1, "Corte 1", 3, null), g(2, "Corte 1", 5, null)))).isEqualTo(4.0);
    }

    @Test
    void weightsDecideHowMuchEachActivityCounts() {
        // Parcial 60 %, taller 40 %: 0.6 × 2.0 + 0.4 × 5.0 = 3.2 (a plain mean would say 3.5)
        assertThat(GradeRecord.periodAverage(List.of(g(1, "Corte 1", 2, 60.0), g(2, "Corte 1", 5, 40.0)))).isCloseTo(3.2, within(1e-9));
    }

    @Test
    void pendingActivitiesDoNotCountAsZero() {
        // Only the parcial is graded so far: the period average is that grade, not 0.6 × 2.0.
        assertThat(GradeRecord.periodAverage(List.of(g(1, "Corte 1", 2, 60.0)))).isEqualTo(2.0);
    }

    @Test
    void theAnalyzerUsesWeightedPeriodsForTheStudentAverage() {
        CourseAnalysis analysis = new CourseAnalyzer(new TrendWeightedRiskStrategy()).analyze(1, "Curso",
                List.of(new StudentRef(1, "Ana", null, null)),
                List.of(g(1, "Corte 1", 2, 60.0), g(2, "Corte 1", 5, 40.0), g(1, "Corte 2", 4, 100.0)));
        StudentPerformance ana = analysis.students().get(0);
        assertThat(ana.periods()).extracting(PeriodAverage::average).containsExactly(3.2, 4.0);
        assertThat(ana.average()).isCloseTo(3.6, within(1e-9));
    }
}
