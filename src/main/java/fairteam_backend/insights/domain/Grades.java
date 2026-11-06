package fairteam_backend.insights.domain;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Locale;

/** Small numeric helpers shared by the domain. */
public final class Grades {
    public static final double PASSING = 3.0;

    private Grades() { }

    public static double round1(double value) { return Math.round(value * 10.0) / 10.0; }

    public static double average(Collection<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    /** Grades are analysed on 0–5; this is the course's own maximum used only to write numbers in texts. */
    private static final ThreadLocal<Double> SCALE = ThreadLocal.withInitial(() -> 5.0);

    /**
     * Writes grades in the course's scale for the duration of a request: {@code try (var s = Grades.useScale(100))}.
     * Analysis stays on 0–5, so thresholds (3,0 = 60 %) mean the same on every scale.
     */
    public static AutoCloseable useScale(double maxGrade) {
        double previous = SCALE.get();
        SCALE.set(maxGrade > 0 ? maxGrade : 5.0);
        return () -> SCALE.set(previous);
    }

    public static double scale() { return SCALE.get(); }

    /** Colombian format with comma in the course scale: 3.45 on 0–5 → "3,5"; on 0–100 → "69"; on 0–10 → "6,9". */
    public static String format(double value) {
        double max = SCALE.get();
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO"));
        if (max == 5.0) return new DecimalFormat("0.0", symbols).format(round1(value));
        double scaled = Math.round(value * max / 5.0 * 10) / 10.0;
        // Up to 20 points teachers write one decimal (6,5 / 13,5); on 100 they write whole numbers (65, 87,5 only if needed).
        return new DecimalFormat(max > 20 ? "0.#" : "0.0", symbols).format(scaled);
    }

    public static String plural(long count, String one, String many) {
        return count + " " + (count == 1 ? one : many);
    }

    public static String firstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        return fullName.trim().split("\\s+")[0];
    }

    private static final java.util.Set<String> TITLES = java.util.Set.of(
            "prof", "profe", "profesor", "profesora", "dr", "dra", "doctor", "doctora",
            "ing", "ingeniero", "ingeniera", "mg", "msc", "lic", "phd", "sr", "sra");

    /** First name of a teacher, skipping titles such as "Prof." or "Dra.": "Prof. Carolina Méndez" → "Carolina". */
    public static String teacherFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        for (String word : fullName.trim().split("\\s+")) {
            String bare = word.replaceAll("[.,]+$", "");
            if (!bare.isEmpty() && !TITLES.contains(bare.toLowerCase(java.util.Locale.ROOT))) return bare;
        }
        return "";
    }
}
