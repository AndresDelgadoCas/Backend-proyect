package fairteam_backend.rubrics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.activities.ActivityService;
import fairteam_backend.activities.ActivityText;
import fairteam_backend.gradebook.GradeSheet;
import fairteam_backend.gradebook.GradeSheetRules;
import fairteam_backend.gradebook.GradeSheetService;

/**
 * Rubrics of a course: create (by hand or from an AI draft), assess each student with a click per
 * criterion and publish the resulting grades to a grade book column.
 *
 * <p>Rows are isolated per teacher by PostgreSQL row-level security, like the rest of the course data.
 */
@Service
public class RubricService {
    private static final int MAX_ACTIVITY = 6000;
    private final JdbcTemplate jdbc;
    private final GradeSheetService gradeSheets;
    private final RubricGenerator generator;
    private final ActivityService activities;

    public RubricService(JdbcTemplate jdbc, GradeSheetService gradeSheets, RubricGenerator generator, ActivityService activities) {
        this.jdbc = jdbc;
        this.gradeSheets = gradeSheets;
        this.generator = generator;
        this.activities = activities;
    }

    @Transactional(readOnly = true)
    public List<Rubric.Summary> list(long courseId) {
        courseName(courseId);
        int students = count("SELECT count(*) FROM students WHERE course_id = ?", courseId);
        return jdbc.query("""
                SELECT r.id, r.title, r.period, s.name, r.updated_at, r.activity_id, a.title AS activity_title,
                       (SELECT count(*) FROM course_rubric_criteria c WHERE c.rubric_id = r.id) AS criteria,
                       (SELECT count(*) FROM (SELECT sc.student_id FROM course_rubric_scores sc WHERE sc.rubric_id = r.id
                            GROUP BY sc.student_id
                            HAVING count(*) = (SELECT count(*) FROM course_rubric_criteria c2 WHERE c2.rubric_id = r.id)) done) AS scored
                FROM course_rubrics r LEFT JOIN subjects s ON s.id = r.subject_id
                LEFT JOIN course_activities a ON a.id = r.activity_id
                WHERE r.course_id = ? ORDER BY r.updated_at DESC, r.id DESC""",
                (rs, i) -> new Rubric.Summary(rs.getLong("id"), rs.getString("title"), rs.getInt("criteria"), rs.getInt("scored"),
                        students, rs.getString("period"), rs.getString("name"), rs.getTimestamp("updated_at").toLocalDateTime(),
                        (Long) rs.getObject("activity_id"), rs.getString("activity_title")),
                courseId);
    }

    /** Asks the AI for a draft. Nothing is saved: the teacher reviews and edits it first. */
    public Rubric.Draft generate(long courseId, Rubric.GenerateRequest request) {
        String course = courseName(courseId);
        if (request == null) throw bad("Describe la actividad en al menos una frase.");
        String activity = request.activity() == null ? "" : request.activity().trim();
        if (request.activityId() != null) {
            // The rubric is built from the activity as it was saved, not from a summary typed again.
            String saved = ActivityText.of(activities.get(courseId, request.activityId()));
            activity = activity.isEmpty() ? saved : saved + "\nIndicaciones del docente: " + activity;
        }
        if (activity.length() < 15) throw bad("Describe la actividad en al menos una frase.");
        int count = request.criteriaCount() == null ? 4 : request.criteriaCount();
        if (count < 3 || count > 6) throw bad("Pide entre 3 y 6 criterios.");
        String prompt = request.teacherPrompt() == null ? "" : request.teacherPrompt().trim();
        if (prompt.length() > 2000) throw bad("Las indicaciones pueden tener máximo 2000 caracteres.");
        String trimmed = activity.length() > MAX_ACTIVITY ? activity.substring(0, MAX_ACTIVITY) : activity;
        Rubric.Draft draft = generator.generate(course, trimmed, count, prompt);
        return new Rubric.Draft(draft.title(), draft.description(), draft.criteria(), request.activityId());
    }

    /**
     * Does the whole job Búho promises: finds the activity, asks the AI for a rubric built from it, saves it
     * linked to the activity and returns it ready to grade.
     */
    public Rubric.Detail createFromActivity(long courseId, Rubric.FromActivityRequest request) {
        Rubric.FromActivityRequest r = request == null ? new Rubric.FromActivityRequest(null, null, null, null) : request;
        long activityId = r.activityId() != null ? activities.get(courseId, r.activityId()).id()
                : activities.latest(courseId, r.kind() == null || r.kind().isBlank() ? null : r.kind().trim(), r.topic()).id();
        Rubric.Draft draft = generate(courseId, new Rubric.GenerateRequest("", r.criteriaCount(), "", activityId));
        return create(courseId, draft);
    }

    @Transactional
    public Rubric.Detail create(long courseId, Rubric.Draft draft) {
        courseName(courseId);
        Rubric.Draft clean = rule(() -> RubricScoring.clean(draft));
        Long activityId = activityOf(courseId, clean.activityId());
        Long id = jdbc.queryForObject("INSERT INTO course_rubrics (course_id, title, description, activity_id) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, courseId, clean.title(), clean.description(), activityId);
        writeCriteria(id, clean.criteria());
        return detail(courseId, id);
    }

    @Transactional
    public Rubric.Detail update(long courseId, long rubricId, Rubric.Draft draft) {
        exists(courseId, rubricId);
        Rubric.Draft clean = rule(() -> RubricScoring.clean(draft));
        jdbc.update("UPDATE course_rubrics SET title = ?, description = ?, activity_id = ?, updated_at = now() WHERE id = ?",
                clean.title(), clean.description(), activityOf(courseId, clean.activityId()), rubricId);
        writeCriteria(rubricId, clean.criteria());
        return detail(courseId, rubricId);
    }

    @Transactional
    public void delete(long courseId, long rubricId) {
        exists(courseId, rubricId);
        jdbc.update("DELETE FROM course_rubrics WHERE id = ?", rubricId);
    }

    @Transactional(readOnly = true)
    public Rubric.Detail detail(long courseId, long rubricId) {
        BigDecimal max = maxGrade(courseId);
        Map<String, Object> head = jdbc.queryForList("""
                SELECT r.title, r.description, r.period, s.name AS column_name, r.activity_id, a.title AS activity_title,
                    a.topic AS activity_topic
                FROM course_rubrics r LEFT JOIN subjects s ON s.id = r.subject_id
                LEFT JOIN course_activities a ON a.id = r.activity_id
                WHERE r.id = ? AND r.course_id = ?""", rubricId, courseId).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la rúbrica."));
        List<Rubric.Criterion> criteria = criteria(rubricId);
        List<Rubric.Student> students = jdbc.query("SELECT id, name FROM students WHERE course_id = ? ORDER BY lower(name)",
                (rs, i) -> new Rubric.Student(rs.getLong(1), rs.getString(2)), courseId);
        Map<Long, Map<Long, Integer>> levels = new HashMap<>();
        jdbc.query("SELECT student_id, criterion_id, level FROM course_rubric_scores WHERE rubric_id = ?", rs -> {
            levels.computeIfAbsent(rs.getLong(1), k -> new HashMap<>()).put(rs.getLong(2), rs.getInt(3));
        }, rubricId);
        Map<Long, String> comments = new HashMap<>();
        jdbc.query("SELECT student_id, comment FROM course_rubric_comments WHERE rubric_id = ?",
                rs -> { comments.put(rs.getLong(1), rs.getString(2)); }, rubricId);
        List<Rubric.StudentResult> results = new ArrayList<>();
        for (Rubric.Student student : students) {
            Map<Long, Integer> mine = levels.getOrDefault(student.id(), Map.of());
            if (mine.isEmpty() && !comments.containsKey(student.id())) continue;
            results.add(new Rubric.StudentResult(student.id(), mine, comments.get(student.id()), RubricScoring.grade(criteria, mine, max)));
        }
        return new Rubric.Detail(rubricId, (String) head.get("title"), (String) head.get("description"), criteria, students,
                results, max.doubleValue(), RubricScoring.levelGrades(max), (String) head.get("period"), (String) head.get("column_name"),
                head.get("activity_id") == null ? null : ((Number) head.get("activity_id")).longValue(),
                (String) head.get("activity_title"), (String) head.get("activity_topic"));
    }

    /** Saves the levels of one student (null clears a criterion) and the optional comment. */
    @Transactional
    public Rubric.StudentResult score(long courseId, long rubricId, long studentId, Rubric.ScoreRequest request) {
        exists(courseId, rubricId);
        if (count("SELECT count(*) FROM students WHERE id = ? AND course_id = ?", studentId, courseId) == 0) {
            throw bad("El estudiante no pertenece a este curso.");
        }
        List<Rubric.Criterion> criteria = criteria(rubricId);
        Set<Long> ids = new HashSet<>();
        criteria.forEach(c -> ids.add(c.id()));
        Map<Long, Integer> levels = request == null || request.levels() == null ? Map.of() : request.levels();
        for (Map.Entry<Long, Integer> entry : levels.entrySet()) {
            if (!ids.contains(entry.getKey())) throw bad("Hay un criterio que no es de esta rúbrica.");
            rule(() -> { RubricScoring.checkLevel(entry.getValue()); return null; });
            if (entry.getValue() == null) {
                jdbc.update("DELETE FROM course_rubric_scores WHERE criterion_id = ? AND student_id = ?", entry.getKey(), studentId);
            } else {
                jdbc.update("""
                        INSERT INTO course_rubric_scores (rubric_id, criterion_id, student_id, level) VALUES (?, ?, ?, ?)
                        ON CONFLICT (criterion_id, student_id) DO UPDATE SET level = EXCLUDED.level""",
                        rubricId, entry.getKey(), studentId, entry.getValue());
            }
        }
        if (request != null && request.comment() != null) {
            String comment = request.comment().trim();
            if (comment.length() > 1000) throw bad("El comentario puede tener máximo 1000 caracteres.");
            if (comment.isEmpty()) jdbc.update("DELETE FROM course_rubric_comments WHERE rubric_id = ? AND student_id = ?", rubricId, studentId);
            else jdbc.update("""
                    INSERT INTO course_rubric_comments (rubric_id, student_id, comment) VALUES (?, ?, ?)
                    ON CONFLICT (rubric_id, student_id) DO UPDATE SET comment = EXCLUDED.comment""", rubricId, studentId, comment);
        }
        jdbc.update("UPDATE course_rubrics SET updated_at = now() WHERE id = ?", rubricId);
        Map<Long, Integer> saved = new LinkedHashMap<>();
        jdbc.query("SELECT criterion_id, level FROM course_rubric_scores WHERE rubric_id = ? AND student_id = ?",
                rs -> { saved.put(rs.getLong(1), rs.getInt(2)); }, rubricId, studentId);
        List<String> comment = jdbc.queryForList("SELECT comment FROM course_rubric_comments WHERE rubric_id = ? AND student_id = ?",
                String.class, rubricId, studentId);
        return new Rubric.StudentResult(studentId, saved, comment.isEmpty() ? null : comment.get(0),
                RubricScoring.grade(criteria, saved, maxGrade(courseId)));
    }

    /**
     * Writes the grade of every fully assessed student into a grade book column (created if needed) of the
     * chosen corte. Students still pending keep their cell as it was.
     */
    @Transactional
    public Rubric.PublishResult publish(long courseId, long rubricId, Rubric.PublishRequest request) {
        Rubric.Detail detail = detail(courseId, rubricId);
        String period = rule(() -> GradeSheetRules.normalizePeriod(request == null ? null : request.period()));
        String column = request.columnName() != null && !request.columnName().isBlank() ? request.columnName().trim()
                : detail.activityTitle() != null ? detail.activityTitle() : detail.title();
        if (column.length() > 100) column = column.substring(0, 100);
        long subjectId = gradeSheets.columnNamed(column);
        List<GradeSheet.Change> changes = detail.results().stream().filter(r -> r.grade() != null)
                .map(r -> new GradeSheet.Change(r.studentId(), subjectId, r.grade())).toList();
        if (changes.isEmpty()) throw bad("Aún no hay estudiantes evaluados en todos los criterios.");
        gradeSheets.save(courseId, period, changes, null);
        jdbc.update("UPDATE course_rubrics SET period = ?, subject_id = ?, updated_at = now() WHERE id = ?", period, subjectId, rubricId);
        return new Rubric.PublishResult(changes.size(), detail.students().size() - changes.size(), period, column);
    }

    /** The activity must belong to the course; anything else is ignored rather than linked by mistake. */
    private Long activityOf(long courseId, Long activityId) {
        if (activityId == null) return null;
        Integer n = jdbc.queryForObject("SELECT count(*) FROM course_activities WHERE id = ? AND course_id = ?", Integer.class, activityId, courseId);
        if (n == null || n == 0) throw bad("La actividad que quieres evaluar no es de este curso.");
        return activityId;
    }

    private void writeCriteria(long rubricId, List<Rubric.CriterionDraft> criteria) {
        Set<Long> existing = new HashSet<>(jdbc.queryForList("SELECT id FROM course_rubric_criteria WHERE rubric_id = ?", Long.class, rubricId));
        Set<Long> kept = new HashSet<>();
        for (int i = 0; i < criteria.size(); i++) {
            Rubric.CriterionDraft c = criteria.get(i);
            Rubric.Levels l = c.levels();
            if (c.id() != null && existing.contains(c.id())) {
                kept.add(c.id());
                jdbc.update("""
                        UPDATE course_rubric_criteria SET position = ?, name = ?, description = ?, weight = ?,
                            level_excellent = ?, level_good = ?, level_basic = ?, level_insufficient = ? WHERE id = ?""",
                        i, c.name(), c.description(), c.weight(), l.excellent(), l.good(), l.basic(), l.insufficient(), c.id());
            } else {
                jdbc.update("""
                        INSERT INTO course_rubric_criteria (rubric_id, position, name, description, weight,
                            level_excellent, level_good, level_basic, level_insufficient) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                        rubricId, i, c.name(), c.description(), c.weight(), l.excellent(), l.good(), l.basic(), l.insufficient());
            }
        }
        for (Long id : existing) {
            if (!kept.contains(id)) jdbc.update("DELETE FROM course_rubric_criteria WHERE id = ?", id);
        }
    }

    private List<Rubric.Criterion> criteria(long rubricId) {
        return jdbc.query("""
                SELECT id, name, description, weight, level_excellent, level_good, level_basic, level_insufficient
                FROM course_rubric_criteria WHERE rubric_id = ? ORDER BY position, id""",
                (rs, i) -> new Rubric.Criterion(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4).stripTrailingZeros(),
                        new Rubric.Levels(rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8))),
                rubricId);
    }

    private String courseName(long courseId) {
        List<String> names = jdbc.queryForList("SELECT name FROM courses WHERE id = ?", String.class, courseId);
        if (names.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso.");
        return names.get(0);
    }

    private BigDecimal maxGrade(long courseId) {
        List<BigDecimal> max = jdbc.queryForList("SELECT max_grade FROM courses WHERE id = ?", BigDecimal.class, courseId);
        if (max.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso.");
        return max.get(0);
    }

    private void exists(long courseId, long rubricId) {
        if (count("SELECT count(*) FROM course_rubrics WHERE id = ? AND course_id = ?", rubricId, courseId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la rúbrica.");
        }
    }

    private int count(String sql, Object... args) {
        Integer value = jdbc.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    private static <T> T rule(Supplier<T> check) {
        try { return check.get(); } catch (IllegalArgumentException ex) { throw bad(ex.getMessage()); }
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
