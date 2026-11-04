package fairteam_backend.insights.domain.risk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import fairteam_backend.insights.domain.Direction;

class GradeOnlyRiskStrategyTest {
    @Test
    void ignoresTheTrend() {
        RiskScoringStrategy strategy = new GradeOnlyRiskStrategy();
        assertThat(strategy.score(3.0, Direction.DOWN, -1)).isEqualTo(strategy.score(3.0, Direction.UP, 1)).isEqualTo(54);
        assertThat(strategy.score(null, Direction.UNKNOWN, 0)).isEqualTo(35);
    }
}
