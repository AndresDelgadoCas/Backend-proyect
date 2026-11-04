package fairteam_backend.insights.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class GradesTest {
    @Test
    void formatsWithCommaAndOneDecimal() {
        assertThat(Grades.format(3.45)).isEqualTo("3,5");
        assertThat(Grades.format(3)).isEqualTo("3,0");
        assertThat(Grades.format(4.04)).isEqualTo("4,0");
    }

    @Test
    void averagesAndRounds() {
        assertThat(Grades.average(List.of(2.0, 3.0, 4.0))).isEqualTo(3.0);
        assertThat(Grades.average(List.of())).isNaN();
        assertThat(Grades.round1(2.96)).isEqualTo(3.0);
    }

    @Test
    void writesNaturalPluralsAndFirstNames() {
        assertThat(Grades.plural(1, "nota", "notas")).isEqualTo("1 nota");
        assertThat(Grades.plural(0, "nota", "notas")).isEqualTo("0 notas");
        assertThat(Grades.firstName("  Ana María Torres ")).isEqualTo("Ana");
        assertThat(Grades.firstName(null)).isEmpty();
    }
}
