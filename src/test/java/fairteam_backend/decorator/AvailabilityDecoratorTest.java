package fairteam_backend.decorator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AvailabilityDecoratorTest {
    @Test
    void addsItsWeightAndExplanationToTheWrappedAnalysis() {
        TeamAnalysis analysis = new AvailabilityDecorator(new BaseTeamAnalysis());
        assertThat(analysis.calculateScore()).isEqualTo(30.0);
        assertThat(analysis.getExplanation()).isEqualTo("Base team analysis + availability analysis");
    }
}
