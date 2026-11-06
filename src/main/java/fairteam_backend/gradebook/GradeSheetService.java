package fairteam_backend.gradebook;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.service.GradeObserverService;

/**
 * Reads and saves a whole corte at once, so the teacher can type grades like in a spreadsheet.
 *
 * <p>Saving is an upsert on the (course, student, subject, period) unique key; an empty cell deletes the
 * grade. Every saved grade is announced to the existing observers (Observer pattern) and the stored
 * academic level of the touched students is recomputed with the same 3.0 / 4.0 cut points as the app.
 */
@Service
public class GradeSheetService {
    private final JdbcTemplate jdbc;
    private final GradeObserverService observers;

    public GradeSheetService(JdbcTemplate jdbc, GradeObserverService observers) {
        this.jdbc = jdbc;
        this.observers = observers;
    }

    @Transactional(readOnly = true)
    public GradeSheet sheet(long courseId, String requestedPeriod) {
        requireCourse(courseId);
        List<String> periods = GradeSheetRules.orderPeriods(
                jdbc.queryForList("SELECT DISTINCT period FROM grades WHERE course_id = ? AND period IS NOT NULL", String.class, courseId));
        String period = requestedPeriod == null || requestedPeriod.isBlank()
                ? GradeSheetRules.defaultPeriod(periods) : normalize(requestedPeriod);
        List<GradeSheet.Column> subjects = jdbc.query(
                "SELECT s.id, s.name FROM subjects s WHERE s.id IN (SELECT DISTINCT subject_id FROM grades WHERE course_id = ?) ORDER BY s.name",
                (rs, i) -> new GradeSheet.Column(rs.getLong(1), rs.getString(2)), courseId);
        List<GradeSheet.Row> students = jdbc.query(
                "SELECT id, name, email FROM students WHERE course_id = ? ORDER BY lower(name)",
                (rs, i) -> new GradeSheet.Row(rs.getLong(1), rs.getString(2), rs.getString(3)), courseId);
        List<GradeSheet.Cell> cells = jdbc.query(
                "SELECT student_id, subject_id, grade FROM grades WHERE course_id = ? AND period = ?",
                (rs, i) -> new GradeSheet.Cell(rs.getLong(1), rs.getLong(2), rs.getBigDecimal(3)), courseId, period);
        return new GradeSheet(period, periods, subjects, students, cells);
    }

    @Transactional
    public GradeSheet.SaveResult save(long courseId, String rawPeriod, List<GradeSheet.Change> changes) {
        requireCourse(courseId);
        String period = normalize(rawPeriod);
        if (changes == null || changes.isEmpty()) throw bad("No hay cambios para guardar.");
        if (changes.size() > GradeSheetRules.MAX_CHANGES) throw bad("Guarda máximo " + GradeSheetRules.MAX_CHANGES + " notas a la vez.");

        Set<Long> roster = new HashSet<>(jdbc.queryForList("SELECT id FROM students WHERE course_id = ?", Long.class, courseId));
        Set<Long> subjectIds = new HashSet<>(jdbc.queryForList("SELECT id FROM subjects", Long.class));
        for (GradeSheet.Change change : changes) {
            if (!roster.contains(change.studentId())) throw bad("Hay estudiantes que no pertenecen a este curso.");
            if (!subjectIds.contains(change.subjectId())) throw bad("Hay un tema que no existe.");
            try { GradeSheetRules.checkGrade(change.grade()); } catch (IllegalArgumentException ex) { throw bad(ex.getMessage()); }
        }

        List<Object[]> upserts = new ArrayList<>();
        List<Object[]> deletes = new ArrayList<>();
        Set<Long> touched = new HashSet<>();
        for (GradeSheet.Change change : changes) {
            touched.add(change.studentId());
            if (change.grade() == null) deletes.add(new Object[] {courseId, change.studentId(), change.subjectId(), period});
            else upserts.add(new Object[] {change.studentId(), change.subjectId(), change.grade(), period, courseId});
        }
        if (!upserts.isEmpty()) {
            jdbc.batchUpdate("INSERT INTO grades (student_id, subject_id, grade, period, course_id) VALUES (?, ?, ?, ?, ?) "
                    + "ON CONFLICT (course_id, student_id, subject_id, period) WHERE course_id IS NOT NULL "
                    + "DO UPDATE SET grade = EXCLUDED.grade", upserts);
        }
        if (!deletes.isEmpty()) {
            jdbc.batchUpdate("DELETE FROM grades WHERE course_id = ? AND student_id = ? AND subject_id = ? AND period = ?", deletes);
        }
        recomputeLevels(courseId, touched);
        for (GradeSheet.Change change : changes) {
            if (change.grade() != null) observers.notifyGradeChange(change.studentId(), change.grade().doubleValue());
        }
        return new GradeSheet.SaveResult(upserts.size(), deletes.size(), sheet(courseId, period));
    }

    /** Stored level follows the rounded average: below 3.0 LOW, below 4.0 MEDIUM, otherwise HIGH. */
    private void recomputeLevels(long courseId, Set<Long> studentIds) {
        if (studentIds.isEmpty()) return;
        jdbc.batchUpdate("UPDATE students s SET academic_level = CASE "
                + "WHEN a.avg_grade IS NULL THEN s.academic_level "
                + "WHEN a.avg_grade < 3.0 THEN 'LOW' WHEN a.avg_grade < 4.0 THEN 'MEDIUM' ELSE 'HIGH' END "
                + "FROM (SELECT round(avg(grade), 1) AS avg_grade FROM grades WHERE student_id = ? AND course_id = ?) a "
                + "WHERE s.id = ?",
                studentIds.stream().map(id -> new Object[] {id, courseId, id}).toList());
    }

    private String normalize(String period) {
        try { return GradeSheetRules.normalizePeriod(period); } catch (IllegalArgumentException ex) { throw bad(ex.getMessage()); }
    }

    private void requireCourse(long courseId) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM courses WHERE id = ?", Integer.class, courseId);
        if (n == null || n == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso.");
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
