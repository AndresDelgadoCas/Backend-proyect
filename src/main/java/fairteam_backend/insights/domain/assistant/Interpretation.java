package fairteam_backend.insights.domain.assistant;

/**
 * What the AI interpreter understood from a free request: one action and its details, with students,
 * activities and courses as the codes of the {@link AssistantBrief} (E3, A2, C1). Unknown values are empty.
 */
public record Interpretation(String action, String question, String reply, String kind, String topic, String level,
        String student, String instructions, String activity, String section, String course) {

    public Interpretation {
        action = blank(action);
        question = blank(question);
        reply = blank(reply);
        kind = blank(kind);
        topic = blank(topic);
        level = blank(level);
        student = blank(student);
        instructions = blank(instructions);
        activity = blank(activity);
        section = blank(section);
        course = blank(course);
    }

    public static Interpretation of(String action) { return new Interpretation(action, "", "", "", "", "", "", "", "", "", ""); }

    private static String blank(String value) { return value == null ? "" : value.trim(); }
}
