package fairteam_backend.insights.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

class CourseAnalyzerTest {
    private final CourseAnalyzer analyzer = new CourseAnalyzer(new TrendWeightedRiskStrategy());

    @Test
    void anEmptyCourseHasAnEmptyReading() {
        CourseAnalysis a = analyzer.analyze(1, "Vacío", List.of(), List.of());
        assertThat(a.summary().students()).isZero();
        assertThat(a.summary().average()).isNull();
        assertThat(a.weakestSubject()).isEmpty();
    }

    @Test
    void studentsWithoutGradesKeepTheirProfileLevel() {
        CourseAnalysis a = analyzer.analyze(1, "C", List.of(new StudentRef(1, "Ana", null, "LOW")), List.of());
        StudentPerformance ana = a.student(1).orElseThrow();
        assertThat(ana.hasGrades()).isFalse();
        assertThat(ana.level()).isEqualTo(Level.LOW);
        assertThat(a.summary().low()).isZero(); // only graded students count in the summary
    }

    @Test
    void countsStudentsBelowPassingPerSubject() {
        List<GradeRecord> grades = List.of(
                new GradeRecord(1, 10, "Funciones", "Corte 1", 2.0),
                new GradeRecord(2, 10, "Funciones", "Corte 1", 4.0),
                new GradeRecord(1, 20, "Arreglos", "Corte 1", 4.5));
        CourseAnalysis a = analyzer.analyze(1, "C", List.of(new StudentRef(1, "Ana", null, null), new StudentRef(2, "Luis", null, null)), grades);
        assertThat(a.subjects()).extracting(SubjectAverage::name).containsExactly("Funciones", "Arreglos");
        assertThat(a.subjects().get(0).studentsBelowPassing()).isEqualTo(1);
        assertThat(a.subjects().get(0).average()).isEqualTo(3.0);
    }

    @Test
    void ordersStudentsByRiskThenName() {
        List<GradeRecord> grades = List.of(new GradeRecord(1, 1, "T", "C1", 4.8), new GradeRecord(2, 1, "T", "C1", 4.8), new GradeRecord(3, 1, "T", "C1", 1.5));
        CourseAnalysis a = analyzer.analyze(1, "C", List.of(new StudentRef(1, "Zoe", null, null), new StudentRef(2, "Ana", null, null), new StudentRef(3, "Luis", null, null)), grades);
        assertThat(a.students()).extracting(p -> p.student().name()).containsExactly("Luis", "Ana", "Zoe");
    }
}
