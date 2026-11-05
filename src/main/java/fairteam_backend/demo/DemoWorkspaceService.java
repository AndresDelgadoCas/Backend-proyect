package fairteam_backend.demo;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fairteam_backend.observer.AcademicLevelObserver;

/**
 * Fills the signed-in teacher's account with the demo courses.
 *
 * <p>Runs inside the request transaction where {@code app.teacher_id} is already set, so the
 * tenant trigger stamps every row with the teacher and row level security keeps it private.
 * Courses that already exist (same name) are skipped, so pressing the button twice is harmless.
 */
@Service
public class DemoWorkspaceService {

    private final JdbcTemplate jdbc;

    public DemoWorkspaceService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public DemoSummary load() {
        List<String> created = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        int students = 0;
        int grades = 0;
        int materials = 0;

        for (int c = 0; c < DemoCatalog.COURSES.size(); c++) {
            DemoCourse course = DemoCatalog.COURSES.get(c);
            Integer existing = jdbc.queryForObject(
                    "SELECT count(*) FROM courses WHERE lower(name) = lower(?)", Integer.class, course.name());
            if (existing != null && existing > 0) {
                skipped.add(course.name());
                continue;
            }
            Long courseId = jdbc.queryForObject(
                    "INSERT INTO courses (name, period, description) VALUES (?, ?, ?) RETURNING id",
                    Long.class, course.name(), course.period(), course.description());
            jdbc.update("INSERT INTO course_grading_schemes (course_id, weights, passing_grade) VALUES (?, ?, 3.0)",
                    courseId, course.weights().stream().map(String::valueOf).collect(Collectors.joining(",")));

            Map<String, Long> subjects = ensureSubjects(course.topics());
            List<DemoCatalog.DemoStudent> roster = DemoCatalog.studentsOf(c);
            for (int s = 0; s < roster.size(); s++) {
                DemoCatalog.DemoStudent student = roster.get(s);
                List<Object[]> rows = new ArrayList<>();
                double sum = 0;
                for (int period = 1; period <= course.recordedPeriods(); period++) {
                    for (int k = 0; k < course.topics().size(); k++) {
                        if (DemoGradeGenerator.pending(course, s, k, period)) continue;
                        double value = DemoGradeGenerator.grade(course, s, student.profile(), k, period);
                        sum += value;
                        rows.add(new Object[] {subjects.get(course.topics().get(k)),
                                BigDecimal.valueOf(value), "Corte " + period, courseId});
                    }
                }
                String level = AcademicLevelObserver.levelFor(sum / rows.size());
                Long studentId = jdbc.queryForObject(
                        "INSERT INTO students (name, email, academic_level, course_id) VALUES (?, ?, ?, ?) RETURNING id",
                        Long.class, student.name(), student.email(), level, courseId);
                List<Object[]> withStudent = rows.stream()
                        .map(r -> new Object[] {studentId, r[0], r[1], r[2], r[3]}).toList();
                jdbc.batchUpdate(
                        "INSERT INTO grades (student_id, subject_id, grade, period, course_id) VALUES (?, ?, ?, ?, ?)",
                        withStudent);
                students++;
                grades += withStudent.size();
            }

            for (String file : course.materials()) {
                byte[] content = read("demo/materials/" + file);
                jdbc.update("INSERT INTO course_materials (course_id, file_name, content_type, file_size, content) "
                        + "VALUES (?, ?, 'application/pdf', ?, ?)", courseId, file, content.length, content);
                materials++;
            }
            created.add(course.name());
        }
        return new DemoSummary(created, skipped, students, grades, materials);
    }

    private Map<String, Long> ensureSubjects(List<String> names) {
        Map<String, Long> ids = new LinkedHashMap<>();
        for (String name : names) {
            List<Long> found = jdbc.queryForList("SELECT id FROM subjects WHERE lower(name) = lower(?)", Long.class, name);
            ids.put(name, found.isEmpty()
                    ? jdbc.queryForObject("INSERT INTO subjects (name) VALUES (?) RETURNING id", Long.class, name)
                    : found.get(0));
        }
        return ids;
    }

    private static byte[] read(String path) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return in.readAllBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Falta el material de demostración " + path, ex);
        }
    }

    public record DemoSummary(List<String> createdCourses, List<String> skippedCourses,
                              int students, int grades, int materials) { }
}
