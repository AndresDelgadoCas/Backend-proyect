package fairteam_backend.activities;

import java.time.LocalDateTime;

import tools.jackson.databind.JsonNode;

/** Models of the activities module (workshops, quizzes and custom activities of a course). */
public final class Activity {
    private Activity() { }

    /** A saved activity: its metadata plus the full generated document as JSON. */
    public record View(long id, String kind, String title, String topic, Long subjectId, String targetLevel,
            JsonNode content, LocalDateTime createdAt, int rubrics) { }

    /** What the frontend sends after generating (or editing) an activity. */
    public record Save(String kind, String title, String topic, String targetLevel, JsonNode content) { }

    /** Everything the platform knows about one topic of the course, for the "ver todo del tema" panel. */
    public record TopicSummary(Long subjectId, String topic, java.util.List<View> activities,
            java.util.List<RubricRef> rubrics, java.util.List<ColumnRef> columns) { }

    public record RubricRef(long id, String title, Long activityId, String period, String columnName, int scored) { }

    public record ColumnRef(String period, long subjectId, String name, Double average, int graded) { }
}
