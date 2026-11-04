package fairteam_backend.insights.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class DirectionTest {
    @Test
    void usesAThreeTenthsThreshold() {
        assertThat(Direction.of(-0.3)).isEqualTo(Direction.DOWN);
        assertThat(Direction.of(-0.29)).isEqualTo(Direction.FLAT);
        assertThat(Direction.of(0.3)).isEqualTo(Direction.UP);
        assertThat(Direction.of(null)).isEqualTo(Direction.UNKNOWN);
    }
}
