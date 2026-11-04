package fairteam_backend.insights.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class PeriodOrderTest {
    private static GradeRecord g(String period) { return new GradeRecord(1, 1, "Tema", period, 3.0); }

    @Test
    void sortsNumberedPeriodsNumerically() {
        assertThat(PeriodOrder.of(List.of(g("Corte 10"), g("Corte 2"), g("Corte 1")))).containsExactly("Corte 1", "Corte 2", "Corte 10");
    }

    @Test
    void keepsFirstSeenOrderForNames() {
        assertThat(PeriodOrder.of(List.of(g("Parcial"), g("Final"), g("Parcial")))).containsExactly("Parcial", "Final");
    }

    @Test
    void groupsBlankPeriods() {
        assertThat(PeriodOrder.of(List.of(g(null), g("  ")))).containsExactly(PeriodOrder.NO_PERIOD);
        assertThat(PeriodOrder.normalize(" Corte 1 ")).isEqualTo("Corte 1");
    }
}
