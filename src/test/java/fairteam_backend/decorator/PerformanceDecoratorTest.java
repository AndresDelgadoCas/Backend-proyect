package fairteam_backend.decorator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PerformanceDecoratorTest {
    @Test
    void addsItsWeightAndExplanationToTheWrappedAnalysis() {
        TeamAnalysis analysis = new PerformanceDecorator(new BaseTeamAnalysis());
        assertThat(analysis.calculateScore()).isEqualTo(35.0);
        assertThat(analysis.getExplanation()).isEqualTo("Base team analysis + academic performance analysis");
    }
}
