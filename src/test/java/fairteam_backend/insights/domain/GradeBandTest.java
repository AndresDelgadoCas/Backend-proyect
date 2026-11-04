package fairteam_backend.insights.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class GradeBandTest {
    private static StudentPerformance avg(Double value) {
        return new StudentPerformance(new StudentRef(1, "A", null, null), value, value == null ? 0 : 1, Level.MEDIUM, List.of(), null, Direction.UNKNOWN, 0, List.of());
    }

    @Test
    void countsStudentsPerOnePointBand() {
        List<GradeBand> bands = GradeBand.of(List.of(avg(0.4), avg(2.5), avg(2.96), avg(5.0), avg(null)));
        assertThat(bands).extracting(GradeBand::students).containsExactly(1, 0, 1, 1, 1);
        assertThat(bands.get(4).label()).isEqualTo("4–5");
    }
}
