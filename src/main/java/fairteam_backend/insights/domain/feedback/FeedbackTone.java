package fairteam_backend.insights.domain.feedback;

/** Voice of a message to a student. The parser also accepts the Spanish names the UI used to send. */
public enum FeedbackTone {
    FRIENDLY, FORMAL, MOTIVATIONAL;

    public static FeedbackTone parse(String value) {
        if (value == null) return FRIENDLY;
        return switch (value.trim().toLowerCase()) {
            case "formal" -> FORMAL;
            case "motivational", "motivador", "animo", "ánimo" -> MOTIVATIONAL;
            default -> FRIENDLY;
        };
    }
}
