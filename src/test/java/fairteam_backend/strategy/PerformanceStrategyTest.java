package fairteam_backend.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class PerformanceStrategyTest {
    private final TeamFormationStrategy strategy = new PerformanceStrategy();

    @Test
    void scoresTheAverageAcademicPerformance() {
        assertThat(strategy.calculateScore(List.of(new TeamCandidate(1L, 3.0, 90, 0), new TeamCandidate(2L, 4.0, 10, 0)))).isEqualTo(3.5);
    }

    @Test
    void emptyTeamScoresZero() {
        assertThat(strategy.calculateScore(List.of())).isZero();
        assertThat(strategy.calculateScore(null)).isZero();
    }
}
