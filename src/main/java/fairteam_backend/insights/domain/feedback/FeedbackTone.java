package fairteam_backend.insights.domain.feedback;

public enum FeedbackTone {
    CERCANO, FORMAL;

    public static FeedbackTone parse(String value) {
        return value != null && value.equalsIgnoreCase("formal") ? FORMAL : CERCANO;
    }
}
