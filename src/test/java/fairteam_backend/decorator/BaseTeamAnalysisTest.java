package fairteam_backend.decorator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BaseTeamAnalysisTest {
    @Test
    void startsFromZeroWithABaseExplanation() {
        TeamAnalysis base = new BaseTeamAnalysis();
        assertThat(base.calculateScore()).isZero();
        assertThat(base.getExplanation()).isEqualTo("Base team analysis");
    }
}
