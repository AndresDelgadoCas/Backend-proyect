package fairteam_backend.gradebook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class GradeSheetRulesTest {
    private static final BigDecimal FIVE = BigDecimal.valueOf(5);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @Test
    void periodsAreTrimmedAndCapitalized() {
        assertThat(GradeSheetRules.normalizePeriod("  corte   2 ")).isEqualTo("Corte 2");
        assertThatThrownBy(() -> GradeSheetRules.normalizePeriod(" ")).hasMessageContaining("Indica el corte");
        assertThatThrownBy(() -> GradeSheetRules.normalizePeriod("x".repeat(21))).hasMessageContaining("demasiado largo");
    }

    @Test
    void gradesStayBetweenZeroAndFiveWithTwoDecimals() {
        GradeSheetRules.checkGrade(new BigDecimal("0"), FIVE);
        GradeSheetRules.checkGrade(new BigDecimal("5.00"), FIVE);
        GradeSheetRules.checkGrade(new BigDecimal("3.75"), FIVE);
        GradeSheetRules.checkGrade(null, FIVE);
        assertThatThrownBy(() -> GradeSheetRules.checkGrade(new BigDecimal("5.1"), FIVE)).hasMessage("Las notas van de 0 a 5.");
        assertThatThrownBy(() -> GradeSheetRules.checkGrade(new BigDecimal("-1"), FIVE)).hasMessage("Las notas van de 0 a 5.");
        assertThatThrownBy(() -> GradeSheetRules.checkGrade(new BigDecimal("3.333"), FIVE)).hasMessageContaining("dos decimales");
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

    @Test
    void gradesFollowTheCourseScale() {
        GradeSheetRules.checkGrade(new BigDecimal("87.5"), HUNDRED);
        assertThatThrownBy(() -> GradeSheetRules.checkGrade(new BigDecimal("101"), HUNDRED)).hasMessage("Las notas van de 0 a 100.");
        assertThatThrownBy(() -> GradeSheetRules.checkGrade(new BigDecimal("10.5"), BigDecimal.TEN)).hasMessage("Las notas van de 0 a 10.");
    }

    @Test
    void convertsBetweenTheCourseScaleAndTheStoredOne() {
        assertThat(GradeSheetRules.toStored(new BigDecimal("87.5"), HUNDRED)).isEqualByComparingTo("4.375");
        assertThat(GradeSheetRules.toStored(new BigDecimal("7"), BigDecimal.TEN)).isEqualByComparingTo("3.5");
        assertThat(GradeSheetRules.toScale(new BigDecimal("4.375"), HUNDRED)).isEqualByComparingTo("87.5");
        assertThat(GradeSheetRules.toScale(new BigDecimal("3.5"), FIVE)).isEqualByComparingTo("3.5");
        assertThat(GradeSheetRules.toScale(GradeSheetRules.toStored(new BigDecimal("6.67"), BigDecimal.TEN), BigDecimal.TEN))
                .isEqualByComparingTo("6.67");
    }

    @Test
    void scalesAreSensibleNumbers() {
        assertThat(GradeSheetRules.checkScale(HUNDRED)).isEqualTo(HUNDRED);
        assertThatThrownBy(() -> GradeSheetRules.checkScale(BigDecimal.ZERO)).hasMessageContaining("entre 1 y 1000");
        assertThatThrownBy(() -> GradeSheetRules.checkScale(null)).hasMessageContaining("entre 1 y 1000");
    }

    @Test
    void weightsAreAllOrNothingAndSumOneHundred() {
        GradeSheetRules.checkWeights(List.of(column(1, null), column(2, null)));
        GradeSheetRules.checkWeights(List.of(column(1, "30"), column(2, "70")));
        GradeSheetRules.checkWeights(List.of(column(1, "33.33"), column(2, "33.33"), column(3, "33.34")));
        assertThatThrownBy(() -> GradeSheetRules.checkWeights(List.of(column(1, "30"), column(2, null))))
                .hasMessageContaining("todas las columnas o a ninguna");
        assertThatThrownBy(() -> GradeSheetRules.checkWeights(List.of(column(1, "30"), column(2, "60"))))
                .hasMessage("Los porcentajes del corte suman 90 %; deben sumar 100 %.");
        assertThatThrownBy(() -> GradeSheetRules.checkWeights(List.of(column(1, "50"), column(1, "50"))))
                .hasMessageContaining("repetida");
    }

    private static GradeSheet.ColumnSpec column(long id, String weight) {
        return new GradeSheet.ColumnSpec(id, weight == null ? null : new BigDecimal(weight));
    }
}
