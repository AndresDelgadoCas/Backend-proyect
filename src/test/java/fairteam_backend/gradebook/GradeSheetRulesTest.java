package fairteam_backend.gradebook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class GradeSheetRulesTest {
    @Test
    void periodsAreTrimmedAndCapitalized() {
        assertThat(GradeSheetRules.normalizePeriod("  corte   2 ")).isEqualTo("Corte 2");
        assertThatThrownBy(() -> GradeSheetRules.normalizePeriod(" ")).hasMessageContaining("Indica el corte");
        assertThatThrownBy(() -> GradeSheetRules.normalizePeriod("x".repeat(21))).hasMessageContaining("demasiado largo");
    }

    @Test
    void gradesStayBetweenZeroAndFiveWithTwoDecimals() {
        GradeSheetRules.checkGrade(new BigDecimal("0"));
        GradeSheetRules.checkGrade(new BigDecimal("5.00"));
        GradeSheetRules.checkGrade(new BigDecimal("3.75"));
        GradeSheetRules.checkGrade(null);
        assertThatThrownBy(() -> GradeSheetRules.checkGrade(new BigDecimal("5.1"))).hasMessage("Las notas van de 0 a 5.");
        assertThatThrownBy(() -> GradeSheetRules.checkGrade(new BigDecimal("-1"))).hasMessage("Las notas van de 0 a 5.");
        assertThatThrownBy(() -> GradeSheetRules.checkGrade(new BigDecimal("3.333"))).hasMessageContaining("dos decimales");
    }

    @Test
    void periodsAreOrderedByTheirNumber() {
        assertThat(GradeSheetRules.orderPeriods(List.of("Corte 10", "Final", "Corte 2", "Corte 1", "Corte 2")))
                .containsExactly("Corte 1", "Corte 2", "Corte 10", "Final");
    }

    @Test
    void opensTheLatestPeriodOrCorteOne() {
        assertThat(GradeSheetRules.defaultPeriod(List.of("Corte 1", "Corte 2"))).isEqualTo("Corte 2");
        assertThat(GradeSheetRules.defaultPeriod(List.of())).isEqualTo("Corte 1");
    }
}
