package fairteam_backend.classroom;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Support log per student: what the teacher already did, so follow-up is not lost between cortes. */
@Service
public class InterventionService {
    public record Intervention(long id, long studentId, InterventionKind kind, String note, LocalDateTime createdAt) { }

    private final JdbcTemplate jdbc;

    public InterventionService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(readOnly = true)
    public List<Intervention> list(long courseId, long studentId) {
        requireStudent(courseId, studentId);
        return jdbc.query("SELECT id, student_id, kind, note, created_at FROM student_interventions "
                + "WHERE course_id = ? AND student_id = ? ORDER BY created_at DESC, id DESC",
                (rs, i) -> new Intervention(rs.getLong(1), rs.getLong(2), InterventionKind.valueOf(rs.getString(3)),
                        rs.getString(4), rs.getTimestamp(5).toLocalDateTime()), courseId, studentId);
    }

    @Transactional(readOnly = true)
    public List<Intervention> recent(long courseId, int limit) {
        return jdbc.query("SELECT id, student_id, kind, note, created_at FROM student_interventions "
                + "WHERE course_id = ? ORDER BY created_at DESC, id DESC LIMIT ?",
                (rs, i) -> new Intervention(rs.getLong(1), rs.getLong(2), InterventionKind.valueOf(rs.getString(3)),
                        rs.getString(4), rs.getTimestamp(5).toLocalDateTime()), courseId, Math.max(1, Math.min(limit, 100)));
    }

    @Transactional
    public Intervention add(long courseId, long studentId, InterventionKind kind, String note) {
        requireStudent(courseId, studentId);
        if (kind == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elige qué tipo de apoyo diste.");
        String text = note == null ? "" : note.trim();
        if (text.length() > 500) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nota no puede superar 500 caracteres.");
        Long id = jdbc.queryForObject("INSERT INTO student_interventions (course_id, student_id, kind, note) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, courseId, studentId, kind.name(), text);
        return list(courseId, studentId).stream().filter(i -> i.id() == id).findFirst().orElseThrow();
    }

    @Transactional
    public void delete(long courseId, long studentId, long id) {
        int removed = jdbc.update("DELETE FROM student_interventions WHERE id = ? AND course_id = ? AND student_id = ?", id, courseId, studentId);
        if (removed == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró ese registro.");
    }

    private void requireStudent(long courseId, long studentId) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM students WHERE id = ? AND course_id = ?", Integer.class, studentId, courseId);
        if (n == null || n == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el estudiante en este curso.");
    }
}
