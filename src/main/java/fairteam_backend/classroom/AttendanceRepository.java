package fairteam_backend.classroom;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import fairteam_backend.insights.domain.attendance.AttendanceStats;

/** SQL access to attendance; row level security already scopes every statement to the signed-in teacher. */
@Repository
public class AttendanceRepository {
    private final JdbcTemplate jdbc;

    public AttendanceRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Map<Long, AttendanceStatus> forSession(long courseId, LocalDate date) {
        Map<Long, AttendanceStatus> result = new LinkedHashMap<>();
        jdbc.query("SELECT student_id, status FROM attendance_records WHERE course_id = ? AND session_date = ?",
                rs -> { result.put(rs.getLong(1), AttendanceStatus.valueOf(rs.getString(2))); }, courseId, Date.valueOf(date));
        return result;
    }

    public void save(long courseId, LocalDate date, Map<Long, AttendanceStatus> marks) {
        List<Object[]> rows = marks.entrySet().stream()
                .map(e -> new Object[] {courseId, e.getKey(), Date.valueOf(date), e.getValue().name()}).toList();
        jdbc.batchUpdate("INSERT INTO attendance_records (course_id, student_id, session_date, status) VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (course_id, student_id, session_date) DO UPDATE SET status = EXCLUDED.status, updated_at = now()", rows);
    }

    public void deleteSession(long courseId, LocalDate date) {
        jdbc.update("DELETE FROM attendance_records WHERE course_id = ? AND session_date = ?", courseId, Date.valueOf(date));
    }

    public List<LocalDate> sessions(long courseId) {
        return jdbc.queryForList("SELECT DISTINCT session_date FROM attendance_records WHERE course_id = ? ORDER BY session_date DESC",
                Date.class, courseId).stream().map(Date::toLocalDate).toList();
    }

    public Map<Long, AttendanceStats> stats(long courseId) {
        Map<Long, AttendanceStats> result = new LinkedHashMap<>();
        jdbc.query("SELECT student_id, count(*), "
                + "count(*) FILTER (WHERE status = 'PRESENT'), count(*) FILTER (WHERE status = 'ABSENT'), "
                + "count(*) FILTER (WHERE status = 'LATE'), count(*) FILTER (WHERE status = 'EXCUSED') "
                + "FROM attendance_records WHERE course_id = ? GROUP BY student_id",
                rs -> {
                    long id = rs.getLong(1);
                    result.put(id, new AttendanceStats(id, rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6)));
                }, courseId);
        return result;
    }

    /** Students of the course, to reject marks for students of another course. */
    public List<Long> studentIds(long courseId) {
        return jdbc.queryForList("SELECT id FROM students WHERE course_id = ?", Long.class, courseId);
    }

    public boolean courseExists(long courseId) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM courses WHERE id = ?", Integer.class, courseId);
        return n != null && n > 0;
    }
}
