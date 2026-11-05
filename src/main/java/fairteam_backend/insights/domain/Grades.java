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

    /** Colombian format with comma: 3.45 -> "3,5". */
    public static String format(double value) {
        DecimalFormat format = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));
        return format.format(round1(value));
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
