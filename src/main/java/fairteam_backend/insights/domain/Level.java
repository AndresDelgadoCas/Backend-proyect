package fairteam_backend.insights.domain;

/** Academic level shown to the teacher. Classified on the one-decimal value the teacher sees. */
public enum Level {
    LOW("Necesita refuerzo"), MEDIUM("En seguimiento"), HIGH("Buen progreso"), NO_DATA("Sin notas");

    private final String label;

    Level(String label) { this.label = label; }

    public String label() { return label; }

    public static Level fromAverage(double average) {
        double shown = Grades.round1(average);
        if (shown < 3.0) return LOW;
        if (shown < 4.0) return MEDIUM;
        return HIGH;
    }

    public static Level fromProfile(String value) {
        if (value == null) return MEDIUM;
        return switch (value.trim().toUpperCase()) {
            case "LOW", "BAJO", "BAJA" -> LOW;
            case "HIGH", "ALTO", "ALTA" -> HIGH;
            default -> MEDIUM;
        };
    }
}
