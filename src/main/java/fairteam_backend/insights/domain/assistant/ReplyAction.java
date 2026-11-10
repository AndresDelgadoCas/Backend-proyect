package fairteam_backend.insights.domain.assistant;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Something the interface can do for the teacher. Kinds: navigate, workshop, rubric, review, message, report, briefing,
 * course.
 * Params are plain strings so any client can render them.
 */
public record ReplyAction(String kind, String label, Map<String, String> params) {
    public static ReplyAction of(String kind, String label, String... keyValues) {
        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) if (keyValues[i + 1] != null) params.put(keyValues[i], keyValues[i + 1]);
        return new ReplyAction(kind, label, Map.copyOf(params));
    }

    /** The same action with one more parameter. */
    public ReplyAction with(String key, String value) {
        Map<String, String> next = new LinkedHashMap<>(params);
        if (value != null) next.put(key, value);
        return new ReplyAction(kind, label, Map.copyOf(next));
    }
}
