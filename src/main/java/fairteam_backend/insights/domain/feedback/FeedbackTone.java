package fairteam_backend.insights.domain.feedback;

public enum FeedbackTone {
    CERCANO, FORMAL, MOTIVADOR;

    public static FeedbackTone parse(String value) {
        if (value == null) return CERCANO;
        return switch (value.trim().toLowerCase()) {
            case "formal" -> FORMAL;
            case "motivador", "animo", "ánimo" -> MOTIVADOR;
            default -> CERCANO;
        };
    }
}
