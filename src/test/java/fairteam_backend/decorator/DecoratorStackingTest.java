package fairteam_backend.decorator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DecoratorStackingTest {
    @Test
    void decoratorsComposeInAnyOrder() {
        TeamAnalysis full = new AvailabilityDecorator(new SkillDecorator(new PerformanceDecorator(new BaseTeamAnalysis())));
        assertThat(full.calculateScore()).isEqualTo(100.0);
        assertThat(full.getExplanation())
                .startsWith("Base team analysis")
                .contains("academic performance", "skill balance", "availability");
    }

    @Test
    void theSameDecoratorCanBeAppliedTwice() {
        TeamAnalysis twice = new SkillDecorator(new SkillDecorator(new BaseTeamAnalysis()));
        assertThat(twice.calculateScore()).isEqualTo(70.0);
    }
}
