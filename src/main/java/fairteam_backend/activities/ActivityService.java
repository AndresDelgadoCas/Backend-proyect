package fairteam_backend.activities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Workshops, quizzes and custom activities of a course, stored on the server so every module (grade book,
 * rubrics, Búho) can find them. The topic is matched to the activity of the grade book with the same name,
 * which is what ties a workshop to its grade column and to the rubric that grades it.
 */
@Service
public class ActivityService {
    private static final Set<String> KINDS = Set.of("workshop", "quiz", "custom");
    private static final int MAX_CONTENT = 300_000;
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final JdbcTemplate jdbc;

    public ActivityService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<Activity.View> list(long courseId) {
        course(courseId);
        return jdbc.query(SELECT + " WHERE a.course_id = ? ORDER BY a.created_at DESC, a.id DESC LIMIT 200", this::view, courseId);
    }

    @Transactional(readOnly = true)
    public Activity.View get(long courseId, long id) {
        return jdbc.query(SELECT + " WHERE a.course_id = ? AND a.id = ?", this::view, courseId, id).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la actividad."));
    }

    @Transactional
    public Activity.View create(long courseId, Activity.Save body) {
        course(courseId);
        Clean clean = clean(body);
        Long id = jdbc.queryForObject("""
                INSERT INTO course_activities (course_id, kind, title, topic, subject_id, target_level, content)
                VALUES (?, ?, ?, ?, ?, ?, ?::jsonb) RETURNING id""", Long.class,
                courseId, clean.kind, clean.title, clean.topic, subjectFor(clean.topic), clean.level, clean.content);
        return get(courseId, id);
    }

    @Transactional
    public Activity.View update(long courseId, long id, Activity.Save body) {
        get(courseId, id);
        Clean clean = clean(body);
        jdbc.update("""
                UPDATE course_activities SET kind = ?, title = ?, topic = ?, subject_id = ?, target_level = ?, content = ?::jsonb,
                    updated_at = now() WHERE id = ? AND course_id = ?""",
                clean.kind, clean.title, clean.topic, subjectFor(clean.topic), clean.level, clean.content, id, courseId);
        return get(courseId, id);
    }

    @Transactional
    public void delete(long courseId, long id) {
        get(courseId, id);
        jdbc.update("DELETE FROM course_activities WHERE id = ? AND course_id = ?", id, courseId);
    }

    /** The most recent activity of a kind, optionally about a topic (for "la rúbrica del último taller de X"). */
    @Transactional(readOnly = true)
    public Activity.View latest(long courseId, String kind, String topic) {
        String like = topic == null || topic.isBlank() ? null : "%" + topic.trim().toLowerCase(Locale.ROOT) + "%";
        return jdbc.query(SELECT + " " + """
                 WHERE a.course_id = ? AND (CAST(? AS TEXT) IS NULL OR a.kind = CAST(? AS TEXT) OR (CAST(? AS TEXT) = 'workshop' AND a.kind = 'custom'))
                   AND (CAST(? AS TEXT) IS NULL OR lower(coalesce(a.topic, '')) LIKE CAST(? AS TEXT) OR lower(a.title) LIKE CAST(? AS TEXT))
                 ORDER BY a.created_at DESC, a.id DESC LIMIT 1""", this::view,
                courseId, kind, kind, kind, like, like, like).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        topic == null ? "Todavía no hay talleres guardados en este curso." : "No encontré un taller sobre \"" + topic + "\" en este curso."));
    }

    /**
     * Everything about one topic: its activities, the rubrics that evaluate them or were published to its
     * column, and its columns in each corte with their average.
     */
    @Transactional(readOnly = true)
    public Activity.TopicSummary topic(long courseId, long subjectId) {
        course(courseId);
        String name = jdbc.queryForList("SELECT name FROM subjects WHERE id = ?", String.class, subjectId).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el tema."));
        String like = "%" + name.toLowerCase(Locale.ROOT) + "%";
        List<Activity.View> activities = jdbc.query(SELECT + " " + """
                 WHERE a.course_id = ? AND (a.subject_id = ? OR lower(coalesce(a.topic, '')) LIKE ? OR lower(a.title) LIKE ?)
                 ORDER BY a.created_at DESC""", this::view, courseId, subjectId, like, like);
        List<Activity.RubricRef> rubrics = jdbc.query("""
                SELECT r.id, r.title, r.activity_id, r.period, s.name,
                    (SELECT count(DISTINCT sc.student_id) FROM course_rubric_scores sc WHERE sc.rubric_id = r.id) AS scored
                FROM course_rubrics r LEFT JOIN subjects s ON s.id = r.subject_id
                LEFT JOIN course_activities a ON a.id = r.activity_id
                WHERE r.course_id = ? AND (r.subject_id = ? OR a.subject_id = ? OR lower(coalesce(a.topic, '')) LIKE ?)
                ORDER BY r.updated_at DESC""",
                (rs, i) -> new Activity.RubricRef(rs.getLong(1), rs.getString(2), (Long) rs.getObject(3), rs.getString(4), rs.getString(5), rs.getInt(6)),
                courseId, subjectId, subjectId, like);
        List<Activity.ColumnRef> columns = jdbc.query("""
                SELECT g.period, round(avg(g.grade)::numeric, 2), count(*) FROM grades g
                WHERE g.course_id = ? AND g.subject_id = ? GROUP BY g.period ORDER BY g.period""",
                (rs, i) -> new Activity.ColumnRef(rs.getString(1), subjectId, name, rs.getDouble(2), rs.getInt(3)), courseId, subjectId);
        return new Activity.TopicSummary(subjectId, name, activities, rubrics, columns);
    }

    private static final String SELECT = """
            SELECT a.id, a.kind, a.title, a.topic, a.subject_id, a.target_level, a.content::text, a.created_at,
                (SELECT count(*) FROM course_rubrics r WHERE r.activity_id = a.id) AS rubrics
            FROM course_activities a""";

    private Activity.View view(ResultSet rs, int row) throws SQLException {
        return new Activity.View(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), (Long) rs.getObject(5),
                rs.getString(6), JSON.readTree(rs.getString(7)), rs.getTimestamp(8).toLocalDateTime(), rs.getInt(9));
    }

    private record Clean(String kind, String title, String topic, String level, String content) { }

    private static Clean clean(Activity.Save body) {
        if (body == null || body.content() == null || !body.content().isObject()) throw bad("Falta el contenido de la actividad.");
        String kind = body.kind() == null ? "workshop" : body.kind().trim().toLowerCase(Locale.ROOT);
        if (!KINDS.contains(kind)) throw bad("El tipo debe ser workshop, quiz o custom.");
        String title = body.title() == null ? "" : body.title().trim();
        if (title.isEmpty()) title = body.content().path("title").asString("").trim();
        if (title.isEmpty() || title.length() > 200) throw bad("El título debe tener entre 1 y 200 caracteres.");
        String topic = body.topic() == null || body.topic().isBlank() ? null : body.topic().trim();
        if (topic != null && topic.length() > 150) topic = topic.substring(0, 150);
        String level = body.targetLevel() == null ? null : body.targetLevel().trim().toUpperCase(Locale.ROOT);
        if (level != null && !Set.of("LOW", "MEDIUM", "HIGH").contains(level)) level = null;
        String content = body.content().toString();
        if (content.length() > MAX_CONTENT) throw bad("La actividad es demasiado grande para guardarla.");
        return new Clean(kind, title, topic, level, content);
    }

    /** The grade book activity with the same name as the topic, if the teacher already uses it as a column. */
    private Long subjectFor(String topic) {
        if (topic == null) return null;
        return jdbc.queryForList("SELECT id FROM subjects WHERE lower(name) = lower(?) ORDER BY id LIMIT 1", Long.class, topic)
                .stream().findFirst().orElse(null);
    }

    private void course(long courseId) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM courses WHERE id = ?", Integer.class, courseId);
        if (n == null || n == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso.");
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
