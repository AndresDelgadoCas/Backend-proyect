package fairteam_backend.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class BalancedLevelStrategyTest {
    private final TeamFormationStrategy strategy = new BalancedLevelStrategy();

    @Test
    void identicalScoresAreFullyBalanced() {
        assertThat(strategy.calculateScore(List.of(new TeamCandidate(1L, 3.5, 0, 0), new TeamCandidate(2L, 3.5, 0, 0)))).isEqualTo(100.0);
    }

    @Test
    void spreadLowersTheScore() {
        // mean 3.0, mean absolute deviation 1.0 -> 100 - 20
        assertThat(strategy.calculateScore(List.of(new TeamCandidate(1L, 2.0, 0, 0), new TeamCandidate(2L, 4.0, 0, 0)))).isEqualTo(80.0);
    }

    @Test
    void neverGoesBelowZeroAndHandlesEmptyTeams() {
        assertThat(strategy.calculateScore(List.of(new TeamCandidate(1L, 0, 0, 0), new TeamCandidate(2L, 20, 0, 0)))).isZero();
        assertThat(strategy.calculateScore(List.of())).isZero();
        assertThat(strategy.calculateScore(null)).isZero();
    }
}
