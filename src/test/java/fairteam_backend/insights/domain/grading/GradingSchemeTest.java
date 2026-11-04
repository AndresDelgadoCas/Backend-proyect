package fairteam_backend.insights.domain.grading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class GradingSchemeTest {
    @Test
    void acceptsWeightsThatAddUp() {
        GradingScheme scheme = new GradingScheme(List.of(30, 30, 40), 3.0);
        assertThat(scheme.periods()).isEqualTo(3);
    }

    @Test
    void rejectsInvalidSchemes() {
        assertThatThrownBy(() -> new GradingScheme(List.of(50, 40), 3.0)).hasMessageContaining("100%");
        assertThatThrownBy(() -> new GradingScheme(List.of(), 3.0)).hasMessageContaining("entre 1 y 6");
        assertThatThrownBy(() -> new GradingScheme(List.of(110, -10), 3.0)).hasMessageContaining("entre 0 y 100");
        assertThatThrownBy(() -> new GradingScheme(List.of(100), 6.0)).hasMessageContaining("nota mínima");
    }

    @Test
    void defaultsToThirtyThirtyForty() {
        assertThat(GradingScheme.defaultFor(0).weights()).containsExactly(30, 30, 40);
        assertThat(GradingScheme.defaultFor(2).weights()).containsExactly(30, 30, 40);
        assertThat(GradingScheme.defaultFor(4).weights()).containsExactly(25, 25, 25, 25);
        assertThat(GradingScheme.defaultFor(6).weights().stream().mapToInt(Integer::intValue).sum()).isEqualTo(100);
    }
}
