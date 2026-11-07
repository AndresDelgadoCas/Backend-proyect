package fairteam_backend.rubrics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Turns the levels a student reached into a grade, and checks rubrics before saving them.
 * Pure Java: no Spring, no SQL, so the arithmetic is easy to test.
 *
 * <p>Each level is worth a fixed share of the maximum grade (Excelente 100 %, Bueno 80 %, Básico 60 %,
 * Insuficiente 30 %). The grade is the weighted average of those shares, so a student who reaches
 * "Básico" everywhere passes with exactly the passing grade (60 %).
 */
public final class RubricScoring {
    public static final List<BigDecimal> LEVEL_SHARE = List.of(
            BigDecimal.ONE, new BigDecimal("0.8"), new BigDecimal("0.6"), new BigDecimal("0.3"));
    public static final List<String> LEVEL_NAMES = List.of("Excelente", "Bueno", "Básico", "Insuficiente");
    public static final int MIN_CRITERIA = 1;
    public static final int MAX_CRITERIA = 10;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private RubricScoring() { }

    /** Grade in the course scale, or null while some criterion has no level. */
    public static BigDecimal grade(List<Rubric.Criterion> criteria, Map<Long, Integer> levels, BigDecimal maxGrade) {
        if (criteria.isEmpty()) return null;
        BigDecimal earned = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (Rubric.Criterion criterion : criteria) {
            Integer level = levels.get(criterion.id());
            if (level == null) return null;
            earned = earned.add(criterion.weight().multiply(LEVEL_SHARE.get(level)));
            total = total.add(criterion.weight());
        }
        if (total.signum() == 0) return null;
        return earned.multiply(maxGrade).divide(total, 2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    /** Value of each level in the course scale, for the editor: 5 / 4 / 3 / 1,5 on 0–5. */
    public static List<BigDecimal> levelGrades(BigDecimal maxGrade) {
        return LEVEL_SHARE.stream().map(share -> share.multiply(maxGrade).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()).toList();
    }

    public static void checkLevel(Integer level) {
        if (level != null && (level < 0 || level > 3)) throw new IllegalArgumentException("El nivel debe estar entre 0 y 3.");
    }

    /** Cleans a draft and rejects what cannot be saved, with messages for the teacher. */
    public static Rubric.Draft clean(Rubric.Draft draft) {
        if (draft == null) throw new IllegalArgumentException("Falta la rúbrica.");
        String title = text(draft.title());
        if (title.isEmpty() || title.length() > 150) throw new IllegalArgumentException("El título debe tener entre 1 y 150 caracteres.");
        List<Rubric.CriterionDraft> criteria = draft.criteria() == null ? List.of() : draft.criteria();
        if (criteria.size() < MIN_CRITERIA || criteria.size() > MAX_CRITERIA) {
            throw new IllegalArgumentException("Una rúbrica tiene entre " + MIN_CRITERIA + " y " + MAX_CRITERIA + " criterios.");
        }
        BigDecimal sum = BigDecimal.ZERO;
        List<Rubric.CriterionDraft> cleaned = new java.util.ArrayList<>();
        for (Rubric.CriterionDraft c : criteria) {
            String name = text(c.name());
            if (name.isEmpty() || name.length() > 150) throw new IllegalArgumentException("Cada criterio necesita un nombre (máximo 150 caracteres).");
            if (c.weight() == null || c.weight().signum() < 0 || c.weight().compareTo(HUNDRED) > 0) {
                throw new IllegalArgumentException("El peso de \"" + name + "\" debe estar entre 0 y 100.");
            }
            Rubric.Levels l = Objects.requireNonNullElse(c.levels(), new Rubric.Levels("", "", "", ""));
            Rubric.Levels levels = new Rubric.Levels(text(l.excellent()), text(l.good()), text(l.basic()), text(l.insufficient()));
            if (levels.excellent().isEmpty() || levels.good().isEmpty() || levels.basic().isEmpty() || levels.insufficient().isEmpty()) {
                throw new IllegalArgumentException("Describe los cuatro niveles de \"" + name + "\".");
            }
            sum = sum.add(c.weight());
            cleaned.add(new Rubric.CriterionDraft(c.id(), name, text(c.description()), c.weight().setScale(2, RoundingMode.HALF_UP), levels));
        }
        if (sum.subtract(HUNDRED).abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new IllegalArgumentException("Los pesos suman " + sum.stripTrailingZeros().toPlainString().replace('.', ',') + " %; deben sumar 100 %.");
        }
        return new Rubric.Draft(title, text(draft.description()), cleaned);
    }

    private static String text(String value) {
        return value == null ? "" : value.trim().replaceAll("[ \\t]+", " ");
    }
}
