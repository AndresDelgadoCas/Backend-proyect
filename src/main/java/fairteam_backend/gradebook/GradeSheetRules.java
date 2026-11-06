package fairteam_backend.gradebook;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Validation and conversions of the grade book, free of Spring and SQL so they are easy to test. */
public final class GradeSheetRules {
    /** Same limit as grades.period (VARCHAR(20)). */
    public static final int MAX_PERIOD_LENGTH = 20;
    public static final int MAX_CHANGES = 2_000;
    public static final int MAX_COLUMNS = 30;
    /** Grades are stored on 0–5, whatever the course scale is. */
    public static final BigDecimal STORED_MAX = BigDecimal.valueOf(5);
    private static final Pattern TRAILING_NUMBER = Pattern.compile("(\\d+)\\D*$");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private GradeSheetRules() { }

    /** "corte 2 " → "Corte 2". Rejects empty or very long names with a message for the teacher. */
    public static String normalizePeriod(String raw) {
        String period = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        if (period.isEmpty()) throw new IllegalArgumentException("Indica el corte o periodo de las notas.");
        if (period.length() > MAX_PERIOD_LENGTH) throw new IllegalArgumentException("El nombre del corte es demasiado largo.");
        return period.substring(0, 1).toUpperCase(Locale.ROOT) + period.substring(1);
    }

    /** Scales a teacher may use: anything from 1 to 1000 (5, 10, 20 and 100 are the usual ones). */
    public static BigDecimal checkScale(BigDecimal max) {
        if (max == null || max.compareTo(BigDecimal.ONE) < 0 || max.compareTo(BigDecimal.valueOf(1000)) > 0) {
            throw new IllegalArgumentException("La nota máxima debe estar entre 1 y 1000 (por ejemplo 5, 10 o 100).");
        }
        if (max.stripTrailingZeros().scale() > 2) throw new IllegalArgumentException("La nota máxima puede tener máximo dos decimales.");
        return max;
    }

    /** A grade in the course scale: between 0 and the maximum, two decimals at most. */
    public static void checkGrade(BigDecimal grade, BigDecimal max) {
        if (grade == null) return;
        if (grade.signum() < 0 || grade.compareTo(max) > 0) {
            throw new IllegalArgumentException("Las notas van de 0 a " + plain(max) + ".");
        }
        if (grade.stripTrailingZeros().scale() > 2) throw new IllegalArgumentException("Usa máximo dos decimales en las notas.");
    }

    /** Course scale → stored 0–5 (four decimals keep 87,5/100 = 4,375 exact). */
    public static BigDecimal toStored(BigDecimal grade, BigDecimal max) {
        return grade.multiply(STORED_MAX).divide(max, 4, RoundingMode.HALF_UP);
    }

    /** Stored 0–5 → course scale, rounded to two decimals. */
    public static BigDecimal toScale(BigDecimal stored, BigDecimal max) {
        return stored.multiply(max).divide(STORED_MAX, 2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    /** Either no weights (equal) or every column weighted and summing 100 %. */
    public static void checkWeights(List<GradeSheet.ColumnSpec> columns) {
        if (columns.size() > MAX_COLUMNS) throw new IllegalArgumentException("Un corte puede tener máximo " + MAX_COLUMNS + " columnas.");
        if (columns.stream().map(GradeSheet.ColumnSpec::subjectId).distinct().count() != columns.size()) {
            throw new IllegalArgumentException("Hay una columna repetida.");
        }
        long weighted = columns.stream().map(GradeSheet.ColumnSpec::weight).filter(Objects::nonNull).count();
        if (weighted == 0) return;
        if (weighted != columns.size()) throw new IllegalArgumentException("Pon un porcentaje a todas las columnas o a ninguna.");
        BigDecimal sum = BigDecimal.ZERO;
        for (GradeSheet.ColumnSpec column : columns) {
            if (column.weight().signum() < 0 || column.weight().compareTo(HUNDRED) > 0) throw new IllegalArgumentException("Cada porcentaje va de 0 a 100.");
            sum = sum.add(column.weight());
        }
        if (sum.subtract(HUNDRED).abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new IllegalArgumentException("Los porcentajes del corte suman " + plain(sum) + " %; deben sumar 100 %.");
        }
    }

    /** Orders periods by their trailing number ("Corte 1" before "Corte 10"), then alphabetically. */
    public static List<String> orderPeriods(List<String> periods) {
        return periods.stream().distinct().sorted((a, b) -> {
            Integer na = trailing(a), nb = trailing(b);
            if (na != null && nb != null && !na.equals(nb)) return Integer.compare(na, nb);
            if (na != null && nb == null) return -1;
            if (na == null && nb != null) return 1;
            return a.compareToIgnoreCase(b);
        }).toList();
    }

    /** The corte to open by default: the latest one, or "Corte 1" in a course without grades. */
    public static String defaultPeriod(List<String> ordered) {
        return ordered.isEmpty() ? "Corte 1" : ordered.get(ordered.size() - 1);
    }

    public static String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    private static Integer trailing(String text) {
        Matcher m = TRAILING_NUMBER.matcher(text);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }
}
