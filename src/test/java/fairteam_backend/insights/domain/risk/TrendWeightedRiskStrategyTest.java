package fairteam_backend.insights.domain.risk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import fairteam_backend.insights.domain.Direction;

class TrendWeightedRiskStrategyTest {
    private final RiskScoringStrategy strategy = new TrendWeightedRiskStrategy();

    @Test
    void lowAveragesAreRiskier() {
        assertThat(strategy.score(2.0, Direction.FLAT, 0)).isGreaterThan(strategy.score(3.5, Direction.FLAT, 0));
        assertThat(strategy.score(4.8, Direction.FLAT, 0)).isZero();
    }

    @Test
    void fallingAddsRiskAndImprovingRemovesIt() {
        int flat = strategy.score(3.4, Direction.FLAT, 0);
        assertThat(strategy.score(3.4, Direction.DOWN, -0.6)).isEqualTo(flat + 18);
        assertThat(strategy.score(3.4, Direction.UP, 0.5)).isEqualTo(flat - 10);
    }

    @Test
    void staysBetweenZeroAndOneHundred() {
        assertThat(strategy.score(0.0, Direction.DOWN, -3)).isEqualTo(100);
        assertThat(strategy.score(5.0, Direction.UP, 1)).isZero();
    }

    @Test
    void studentsWithoutGradesAreNotForgotten() {
        assertThat(strategy.score(null, Direction.UNKNOWN, 0)).isEqualTo(35);
    }
}
