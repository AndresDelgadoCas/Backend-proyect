package fairteam_backend.insights.domain.assistant;

/** A student shown as a card inside an answer. Tone: low, medium, high or none. */
public record ReplyItem(long studentId, String name, String value, String note, String tone) { }
