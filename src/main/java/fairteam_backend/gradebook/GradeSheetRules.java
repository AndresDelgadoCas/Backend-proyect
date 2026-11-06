package fairteam_backend.gradebook;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Validation and ordering rules of the grade sheet, kept free of Spring and SQL so they are easy to test. */
public final class GradeSheetRules {
    /** Same limit as grades.period (VARCHAR(20)). */
    public static final int MAX_PERIOD_LENGTH = 20;
    public static final int MAX_CHANGES = 2_000;
    private static final BigDecimal MAX = BigDecimal.valueOf(5);
    private static final Pattern TRAILING_NUMBER = Pattern.compile("(\\d+)\\D*$");

    private GradeSheetRules() { }

    /** "corte 2 " → "Corte 2". Rejects empty or very long names with a message for the teacher. */
    public static String normalizePeriod(String raw) {
        String period = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        if (period.isEmpty()) throw new IllegalArgumentException("Indica el corte o periodo de las notas.");
        if (period.length() > MAX_PERIOD_LENGTH) throw new IllegalArgumentException("El nombre del corte es demasiado largo.");
        return period.substring(0, 1).toUpperCase(Locale.ROOT) + period.substring(1);
    }

    /** Grades go from 0 to 5 with at most two decimals, the scale used by Colombian universities. */
    public static void checkGrade(BigDecimal grade) {
        if (grade == null) return;
        if (grade.signum() < 0 || grade.compareTo(MAX) > 0) throw new IllegalArgumentException("Las notas van de 0 a 5.");
        if (grade.stripTrailingZeros().scale() > 2) throw new IllegalArgumentException("Usa máximo dos decimales en las notas.");
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

    private static Integer trailing(String text) {
        Matcher m = TRAILING_NUMBER.matcher(text);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }
}
