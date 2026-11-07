package fairteam_backend.gradebook;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.insights.domain.grading.GradingScheme;
import fairteam_backend.service.GradeObserverService;

/**
 * Reads and saves a whole corte at once, so the teacher can type grades like in a spreadsheet.
 *
 * <p>The editor speaks the course's scale (0–5, 0–10, 0–100…): values are converted to the stored 0–5 on the
 * way in and back on the way out. Columns of a corte keep their order and percentage in
 * {@code grade_item_weights}, so a column can exist before it has grades. Saving is an upsert on the
 * (course, student, subject, period) key; an empty cell deletes the grade. Every saved grade is announced to the
 * existing observers (Observer pattern) and the academic level of the touched students is recomputed.
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
        BigDecimal max = scale(courseId);
        List<String> periods = GradeSheetRules.orderPeriods(jdbc.queryForList(
                "SELECT period FROM grades WHERE course_id = ? AND period IS NOT NULL "
                        + "UNION SELECT period FROM grade_item_weights WHERE course_id = ?", String.class, courseId, courseId));
        String period = requestedPeriod == null || requestedPeriod.isBlank()
                ? GradeSheetRules.defaultPeriod(periods) : normalize(requestedPeriod);

        List<GradeSheet.Column> columns = columnsOf(courseId, period);
        boolean suggested = false;
        if (columns.isEmpty()) {
            String previous = previousPeriod(periods, period);
            if (previous != null) {
                columns = columnsOf(courseId, previous);
                suggested = !columns.isEmpty();
            }
        }
        List<GradeSheet.Row> students = jdbc.query(
                "SELECT id, name, email FROM students WHERE course_id = ? ORDER BY lower(name)",
                (rs, i) -> new GradeSheet.Row(rs.getLong(1), rs.getString(2), rs.getString(3)), courseId);
        List<GradeSheet.Cell> cells = jdbc.query(
                "SELECT student_id, subject_id, grade FROM grades WHERE course_id = ? AND period = ?",
                (rs, i) -> new GradeSheet.Cell(rs.getLong(1), rs.getLong(2), GradeSheetRules.toScale(rs.getBigDecimal(3), max)),
                courseId, period);

        Scheme scheme = scheme(courseId, periods.size());
        double passing = GradeSheetRules.toScale(BigDecimal.valueOf(scheme.passing()), max).doubleValue();
        List<String> all = new ArrayList<>(periods);
        if (!all.contains(period)) all.add(period);
        List<GradeSheet.PeriodWeight> periodWeights = new ArrayList<>();
        for (int i = 0; i < Math.max(all.size(), scheme.weights().size()); i++) {
            String name = i < all.size() ? all.get(i) : "Corte " + (i + 1);
            periodWeights.add(new GradeSheet.PeriodWeight(name, i < scheme.weights().size() ? scheme.weights().get(i) : null));
        }
        return new GradeSheet(period, periods, columns, suggested, students, cells, max.doubleValue(), passing, periodWeights);
    }

    @Transactional
    public GradeSheet.SaveResult save(long courseId, String rawPeriod, List<GradeSheet.Change> changes, List<GradeSheet.ColumnSpec> columns) {
        BigDecimal max = scale(courseId);
        String period = normalize(rawPeriod);
        List<GradeSheet.Change> edits = changes == null ? List.of() : changes;
        if (edits.isEmpty() && columns == null) throw bad("No hay cambios para guardar.");
        if (edits.size() > GradeSheetRules.MAX_CHANGES) throw bad("Guarda máximo " + GradeSheetRules.MAX_CHANGES + " notas a la vez.");

        Set<Long> roster = new HashSet<>(jdbc.queryForList("SELECT id FROM students WHERE course_id = ?", Long.class, courseId));
        Set<Long> subjectIds = new HashSet<>(jdbc.queryForList("SELECT id FROM subjects", Long.class));
        for (GradeSheet.Change change : edits) {
            if (!roster.contains(change.studentId())) throw bad("Hay estudiantes que no pertenecen a este curso.");
            if (!subjectIds.contains(change.subjectId())) throw bad("Hay una columna que no existe.");
            rule(() -> GradeSheetRules.checkGrade(change.grade(), max));
        }
        if (columns != null) {
            rule(() -> GradeSheetRules.checkWeights(columns));
            for (GradeSheet.ColumnSpec column : columns) {
                if (!subjectIds.contains(column.subjectId())) throw bad("Hay una columna que no existe.");
            }
            saveColumns(courseId, period, columns, edits);
        }

        List<Object[]> upserts = new ArrayList<>();
        List<Object[]> deletes = new ArrayList<>();
        Set<Long> touched = new HashSet<>();
        for (GradeSheet.Change change : edits) {
            touched.add(change.studentId());
            if (change.grade() == null) deletes.add(new Object[] {courseId, change.studentId(), change.subjectId(), period});
            else upserts.add(new Object[] {change.studentId(), change.subjectId(), GradeSheetRules.toStored(change.grade(), max), period, courseId});
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
        for (Object[] row : upserts) observers.notifyGradeChange((Long) row[0], ((BigDecimal) row[2]).doubleValue());
        return new GradeSheet.SaveResult(upserts.size(), deletes.size(), sheet(courseId, period));
    }

    /** Renames a column of one corte: its grades and percentage move to the activity with the new name. */
    @Transactional
    public GradeSheet renameColumn(long courseId, String rawPeriod, long subjectId, String rawName) {
        scale(courseId);
        String period = normalize(rawPeriod);
        String name = rawName == null ? "" : rawName.trim().replaceAll("\\s+", " ");
        if (name.isEmpty() || name.length() > 100) throw bad("El nombre de la columna debe tener entre 1 y 100 caracteres.");
        long target = subjectNamed(name);
        if (target == subjectId) return sheet(courseId, period);
        Integer clash = jdbc.queryForObject("SELECT count(*) FROM grades WHERE course_id = ? AND period = ? AND subject_id = ?",
                Integer.class, courseId, period, target);
        Integer clashColumn = jdbc.queryForObject("SELECT count(*) FROM grade_item_weights WHERE course_id = ? AND period = ? AND subject_id = ?",
                Integer.class, courseId, period, target);
        if ((clash != null && clash > 0) || (clashColumn != null && clashColumn > 0)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya hay una columna \"" + name + "\" en este corte.");
        }
        jdbc.update("UPDATE grades SET subject_id = ? WHERE course_id = ? AND period = ? AND subject_id = ?", target, courseId, period, subjectId);
        int moved = jdbc.update("UPDATE grade_item_weights SET subject_id = ? WHERE course_id = ? AND period = ? AND subject_id = ?",
                target, courseId, period, subjectId);
        if (moved == 0) {
            jdbc.update("INSERT INTO grade_item_weights (course_id, period, subject_id, weight, position) "
                    + "VALUES (?, ?, ?, NULL, (SELECT coalesce(max(position) + 1, 0) FROM grade_item_weights WHERE course_id = ? AND period = ?))",
                    courseId, period, target, courseId, period);
        }
        return sheet(courseId, period);
    }

    /** Deletes a column of one corte with all its grades; the other columns keep their percentages. */
    @Transactional
    public GradeSheet deleteColumn(long courseId, String rawPeriod, long subjectId) {
        scale(courseId);
        String period = normalize(rawPeriod);
        List<Long> students = jdbc.queryForList("SELECT student_id FROM grades WHERE course_id = ? AND period = ? AND subject_id = ?",
                Long.class, courseId, period, subjectId);
        jdbc.update("DELETE FROM grades WHERE course_id = ? AND period = ? AND subject_id = ?", courseId, period, subjectId);
        jdbc.update("DELETE FROM grade_item_weights WHERE course_id = ? AND period = ? AND subject_id = ?", courseId, period, subjectId);
        recomputeLevels(courseId, new HashSet<>(students));
        return sheet(courseId, period);
    }

    /** Changes the maximum grade of the course. Stored grades keep their proportion: a 4,0/5 becomes 80/100. */
    @Transactional
    public BigDecimal updateScale(long courseId, BigDecimal maxGrade) {
        scale(courseId);
        BigDecimal max = rule(() -> GradeSheetRules.checkScale(maxGrade));
        jdbc.update("UPDATE courses SET max_grade = ? WHERE id = ?", max, courseId);
        return max.stripTrailingZeros();
    }

    private List<GradeSheet.Column> columnsOf(long courseId, String period) {
        Map<Long, GradeSheet.Column> columns = new LinkedHashMap<>();
        jdbc.query("SELECT s.id, s.name, w.weight FROM grade_item_weights w JOIN subjects s ON s.id = w.subject_id "
                + "WHERE w.course_id = ? AND w.period = ? ORDER BY w.position, w.id",
                rs -> { columns.put(rs.getLong(1), new GradeSheet.Column(rs.getLong(1), rs.getString(2), rs.getBigDecimal(3))); },
                courseId, period);
        jdbc.query("SELECT s.id, s.name FROM subjects s WHERE s.id IN "
                + "(SELECT DISTINCT subject_id FROM grades WHERE course_id = ? AND period = ?) ORDER BY s.name",
                rs -> { columns.putIfAbsent(rs.getLong(1), new GradeSheet.Column(rs.getLong(1), rs.getString(2), null)); },
                courseId, period);
        return List.copyOf(columns.values());
    }

    /** Replaces the columns of the corte. Columns with grades that the teacher left out keep existing, unweighted. */
    private void saveColumns(long courseId, String period, List<GradeSheet.ColumnSpec> columns, List<GradeSheet.Change> edits) {
        Set<Long> kept = new HashSet<>();
        columns.forEach(c -> kept.add(c.subjectId()));
        List<Long> graded = jdbc.queryForList("SELECT DISTINCT subject_id FROM grades WHERE course_id = ? AND period = ?", Long.class, courseId, period);
        Set<Long> incoming = new HashSet<>();
        edits.stream().filter(e -> e.grade() != null).forEach(e -> incoming.add(e.subjectId()));
        boolean weighted = columns.stream().anyMatch(c -> c.weight() != null);
        for (Long subject : graded) {
            if (!kept.contains(subject) && weighted) throw bad("Hay una columna con notas sin porcentaje; inclúyela o elimínala.");
        }
        for (Long subject : incoming) {
            if (!kept.contains(subject) && weighted) throw bad("Hay notas en una columna sin porcentaje.");
        }
        jdbc.update("DELETE FROM grade_item_weights WHERE course_id = ? AND period = ?", courseId, period);
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            GradeSheet.ColumnSpec c = columns.get(i);
            rows.add(new Object[] {courseId, period, c.subjectId(), c.weight(), i});
        }
        if (!rows.isEmpty()) {
            jdbc.batchUpdate("INSERT INTO grade_item_weights (course_id, period, subject_id, weight, position) VALUES (?, ?, ?, ?, ?)", rows);
        }
    }

    /** The activity used as a column with this name, created if the teacher never used it before. */
    @Transactional
    public long columnNamed(String rawName) {
        String name = rawName == null ? "" : rawName.trim().replaceAll("\\s+", " ");
        if (name.isEmpty() || name.length() > 100) throw bad("El nombre de la columna debe tener entre 1 y 100 caracteres.");
        return subjectNamed(name);
    }

    private long subjectNamed(String name) {
        List<Long> found = jdbc.queryForList("SELECT id FROM subjects WHERE lower(name) = lower(?) ORDER BY id LIMIT 1", Long.class, name);
        if (!found.isEmpty()) return found.get(0);
        Long id = jdbc.queryForObject("INSERT INTO subjects (name) VALUES (?) RETURNING id", Long.class, name);
        return id == null ? 0 : id;
    }

    private static String previousPeriod(List<String> ordered, String period) {
        if (ordered.isEmpty()) return null;
        List<String> withCurrent = new ArrayList<>(ordered);
        if (!withCurrent.contains(period)) {
            withCurrent.add(period);
            withCurrent = GradeSheetRules.orderPeriods(withCurrent);
        }
        int at = withCurrent.indexOf(period);
        return at > 0 ? withCurrent.get(at - 1) : null;
    }

    private record Scheme(List<Integer> weights, double passing) { }

    private Scheme scheme(long courseId, int observedPeriods) {
        List<Scheme> stored = jdbc.query("SELECT weights, passing_grade FROM course_grading_schemes WHERE course_id = ?",
                (rs, i) -> new Scheme(parseWeights(rs.getString(1)), rs.getDouble(2)), courseId);
        if (!stored.isEmpty() && stored.get(0).weights().size() >= observedPeriods) return stored.get(0);
        GradingScheme fallback = GradingScheme.defaultFor(observedPeriods);
        return new Scheme(fallback.weights(), stored.isEmpty() ? fallback.passingGrade() : stored.get(0).passing());
    }

    private static List<Integer> parseWeights(String raw) {
        List<Integer> weights = new ArrayList<>();
        for (String part : raw.split(",")) {
            if (!part.isBlank()) weights.add(Integer.valueOf(part.trim()));
        }
        return weights;
    }

    /** Stored level follows the rounded average (0–5): below 3.0 LOW, below 4.0 MEDIUM, otherwise HIGH. */
    private void recomputeLevels(long courseId, Set<Long> studentIds) {
        if (studentIds.isEmpty()) return;
        jdbc.batchUpdate("UPDATE students s SET academic_level = CASE "
                + "WHEN a.avg_grade IS NULL THEN s.academic_level "
                + "WHEN a.avg_grade < 3.0 THEN 'LOW' WHEN a.avg_grade < 4.0 THEN 'MEDIUM' ELSE 'HIGH' END "
                + "FROM (SELECT round(avg(grade), 1) AS avg_grade FROM grades WHERE student_id = ? AND course_id = ?) a "
                + "WHERE s.id = ?",
                studentIds.stream().map(id -> new Object[] {id, courseId, id}).toList());
    }

    private BigDecimal scale(long courseId) {
        List<BigDecimal> max = jdbc.queryForList("SELECT max_grade FROM courses WHERE id = ?", BigDecimal.class, courseId);
        if (max.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso.");
        return max.get(0);
    }

    private String normalize(String period) {
        return rule(() -> GradeSheetRules.normalizePeriod(period));
    }

    private static void rule(Runnable check) {
        rule(() -> { check.run(); return null; });
    }

    private static <T> T rule(java.util.function.Supplier<T> check) {
        try { return check.get(); } catch (IllegalArgumentException ex) { throw bad(ex.getMessage()); }
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
