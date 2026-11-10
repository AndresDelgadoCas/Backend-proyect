package fairteam_backend.insights.adapter.out.persistence;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import fairteam_backend.insights.application.port.out.ActivityCatalogPort;
import fairteam_backend.insights.domain.assistant.ActivityRef;

/** Reads the last saved activities of a course (metadata only, not their content) for Búho. */
@Component
public class JdbcActivityCatalogAdapter implements ActivityCatalogPort {
    private final JdbcTemplate jdbc;

    public JdbcActivityCatalogAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public List<ActivityRef> recent(long courseId) {
        return jdbc.query("""
                SELECT id, kind, title, topic, created_at FROM course_activities
                 WHERE course_id = ? ORDER BY created_at DESC, id DESC LIMIT 20""",
                (rs, n) -> {
                    Timestamp created = rs.getTimestamp("created_at");
                    return new ActivityRef(rs.getLong("id"), rs.getString("kind"), rs.getString("title"), rs.getString("topic"),
                            created == null ? null : created.toLocalDateTime().toLocalDate());
                }, courseId);
    }
}
