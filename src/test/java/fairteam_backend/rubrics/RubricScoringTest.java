package fairteam_backend.rubrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class RubricScoringTest {
    private static final Rubric.Levels LEVELS = new Rubric.Levels("Todo", "Casi todo", "Lo mínimo", "Falta");
    private static final List<Rubric.Criterion> CRITERIA = List.of(
            new Rubric.Criterion(1, "Diseño", "", new BigDecimal("40"), LEVELS),
            new Rubric.Criterion(2, "Código", "", new BigDecimal("40"), LEVELS),
            new Rubric.Criterion(3, "Informe", "", new BigDecimal("20"), LEVELS));

    @Test
    void basicEverywhereIsExactlyThePassingGrade() {
        assertThat(RubricScoring.grade(CRITERIA, Map.of(1L, 2, 2L, 2, 3L, 2), BigDecimal.valueOf(5))).isEqualByComparingTo("3");
        assertThat(RubricScoring.grade(CRITERIA, Map.of(1L, 2, 2L, 2, 3L, 2), BigDecimal.valueOf(100))).isEqualByComparingTo("60");
    }

    @Test
    void weightsDecideTheGrade() {
        // 40 % Excelente + 40 % Bueno + 20 % Insuficiente = 0.4 + 0.32 + 0.06 = 0.78 → 3.9 / 5
        assertThat(RubricScoring.grade(CRITERIA, Map.of(1L, 0, 2L, 1, 3L, 3), BigDecimal.valueOf(5))).isEqualByComparingTo("3.9");
    }

    @Test
    void noGradeUntilEveryCriterionHasALevel() {
        assertThat(RubricScoring.grade(CRITERIA, Map.of(1L, 0, 2L, 0), BigDecimal.valueOf(5))).isNull();
        assertThat(RubricScoring.grade(List.of(), Map.of(), BigDecimal.valueOf(5))).isNull();
    }

    @Test
    void levelValuesFollowTheScale() {
        assertThat(RubricScoring.levelGrades(BigDecimal.valueOf(5))).extracting(BigDecimal::toPlainString).containsExactly("5", "4", "3", "1.5");
        assertThat(RubricScoring.levelGrades(BigDecimal.valueOf(100))).extracting(BigDecimal::toPlainString).containsExactly("100", "80", "60", "30");
    }

    @Test
    void draftsAreCleanedAndChecked() {
        Rubric.Draft draft = new Rubric.Draft("  Rúbrica   Observer ", null, List.of(
                new Rubric.CriterionDraft(null, " Diseño ", null, new BigDecimal("60"), LEVELS),
                new Rubric.CriterionDraft(null, "Código", "", new BigDecimal("40"), LEVELS)));
        Rubric.Draft clean = RubricScoring.clean(draft);
        assertThat(clean.title()).isEqualTo("Rúbrica Observer");
        assertThat(clean.criteria().get(0).name()).isEqualTo("Diseño");
        assertThat(clean.description()).isEmpty();
    }

    @Test
    void rejectsWeightsAndLevelsThatCannotBeUsed() {
        Rubric.CriterionDraft half = new Rubric.CriterionDraft(null, "A", "", new BigDecimal("50"), LEVELS);
        assertThatThrownBy(() -> RubricScoring.clean(new Rubric.Draft("R", "", List.of(half))))
                .hasMessage("Los pesos suman 50 %; deben sumar 100 %.");
        Rubric.CriterionDraft empty = new Rubric.CriterionDraft(null, "A", "", new BigDecimal("100"), new Rubric.Levels("x", "", "y", "z"));
        assertThatThrownBy(() -> RubricScoring.clean(new Rubric.Draft("R", "", List.of(empty)))).hasMessageContaining("cuatro niveles");
        assertThatThrownBy(() -> RubricScoring.checkLevel(4)).hasMessageContaining("entre 0 y 3");
    }
}
