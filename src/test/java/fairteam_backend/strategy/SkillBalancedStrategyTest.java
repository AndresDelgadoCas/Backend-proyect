package fairteam_backend.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class SkillBalancedStrategyTest {
    private final TeamFormationStrategy strategy = new SkillBalancedStrategy();

    @Test
    void scoresTheAverageSkillIgnoringGrades() {
        assertThat(strategy.calculateScore(List.of(new TeamCandidate(1L, 1.0, 60, 0), new TeamCandidate(2L, 5.0, 80, 0)))).isEqualTo(70.0);
    }

    @Test
    void emptyTeamScoresZero() {
        assertThat(strategy.calculateScore(List.of())).isZero();
    }
}
