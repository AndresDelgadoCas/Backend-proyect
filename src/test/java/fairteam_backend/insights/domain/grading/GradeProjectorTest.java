package fairteam_backend.insights.domain.grading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.PeriodAverage;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.StudentRef;

class GradeProjectorTest {
    private final GradeProjector projector = new GradeProjector();
    private final GradingScheme scheme = new GradingScheme(List.of(30, 30, 40), 3.0);

    private static StudentPerformance with(double... averages) {
        List<PeriodAverage> points = new java.util.ArrayList<>();
        for (int i = 0; i < averages.length; i++) points.add(new PeriodAverage("Corte " + (i + 1), averages[i]));
        return new StudentPerformance(new StudentRef(1, "Ana", null, null), averages.length == 0 ? null : 3.0, averages.length,
                Level.MEDIUM, points, null, Direction.UNKNOWN, 0, List.of());
    }

    private static List<String> periods(int n) { return java.util.stream.IntStream.rangeClosed(1, n).mapToObj(i -> "Corte " + i).toList(); }

    @Test
    void computesTheGradeNeededInTheRemainingPeriods() {
        GradeProjection p = projector.project(with(3.0, 2.0), periods(2), scheme);
        // earned 0.9 + 0.6 = 1.5 -> (3 - 1.5) / 0.4 = 3.75
        assertThat(p.needed()).isEqualTo(3.75, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(p.status()).isEqualTo(ProjectionStatus.HARD);
        assertThat(p.explanation()).isEqualTo("Necesita 3,8 en lo que falta para llegar a 3,0.");
    }

    @Test
    void recognizesAlreadyPassedAndImpossibleCases() {
        assertThat(projector.project(with(5.0, 5.0), periods(2), scheme).status()).isEqualTo(ProjectionStatus.PASSED);
        assertThat(projector.project(with(0.5, 0.5), periods(2), scheme).status()).isEqualTo(ProjectionStatus.IMPOSSIBLE);
    }

    @Test
    void closedSemestersReportTheFinalGrade() {
        GradeProjection p = projector.project(with(3.0, 3.0, 3.0), periods(3), scheme);
        assertThat(p.status()).isEqualTo(ProjectionStatus.PASSED);
        assertThat(p.remainingWeight()).isZero();
        assertThat(p.explanation()).isEqualTo("Aprobó con 3,0.");
    }

    @Test
    void finalWithSimulatesAWhatIf() {
        GradeProjection p = projector.project(with(3.0, 2.0), periods(2), scheme);
        assertThat(p.finalWith(5.0)).isEqualTo(3.5, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void studentsWithoutGradesHaveNoProjection() {
        assertThat(projector.project(with(), periods(0), scheme).status()).isEqualTo(ProjectionStatus.NO_DATA);
    }
}
