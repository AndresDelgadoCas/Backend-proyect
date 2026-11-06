package fairteam_backend.insights.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fairteam_backend.entity.Student;
import fairteam_backend.insights.application.port.out.CourseDataPort;
import fairteam_backend.insights.domain.GradeRecord;
import fairteam_backend.insights.domain.StudentRef;
import fairteam_backend.insights.domain.assistant.CourseRef;
import fairteam_backend.repository.CourseMaterialRepository;
import fairteam_backend.repository.CourseRepository;
import fairteam_backend.repository.StudentRepository;

/**
 * Adapter from the existing JPA repositories to the insights domain. Row level security already
 * limits every query to the signed-in teacher, so a course from another account is simply not found.
 */
@Component
public class JpaCourseDataAdapter implements CourseDataPort {
    private final CourseRepository courses;
    private final StudentRepository students;
    private final CourseMaterialRepository materials;

    private final JdbcTemplate jdbc;

    public JpaCourseDataAdapter(CourseRepository courses, StudentRepository students, CourseMaterialRepository materials,
            JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.courses = courses;
        this.students = students;
        this.materials = materials;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseData> load(long courseId) {
        return courses.findById(courseId).map(course -> {
            List<StudentRef> roster = students.findAllByCourse_IdOrderByNameAsc(courseId).stream().map(JpaCourseDataAdapter::toRef).toList();
            // One query with the subject name and the activity weight, instead of loading each relation lazily.
            List<GradeRecord> records = jdbc.query(
                    "SELECT g.student_id, g.subject_id, s.name, g.period, g.grade, w.weight FROM grades g "
                            + "JOIN subjects s ON s.id = g.subject_id "
                            + "LEFT JOIN grade_item_weights w ON w.course_id = g.course_id AND w.period = g.period AND w.subject_id = g.subject_id "
                            + "WHERE g.course_id = ? AND g.grade IS NOT NULL",
                    (rs, i) -> new GradeRecord(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getDouble(5),
                            rs.getObject(6) == null ? null : rs.getDouble(6)),
                    courseId);
            Double max = jdbc.queryForObject("SELECT max_grade FROM courses WHERE id = ?", Double.class, courseId);
            return new CourseData(course.getId(), course.getName(), roster, records, (int) materials.countByCourse_Id(courseId),
                    max == null ? 5.0 : max);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseRef> courses() {
        return courses.findAllByOrderByCreatedAtDesc().stream().map(c -> new CourseRef(c.getId(), c.getName())).toList();
    }

    private static StudentRef toRef(Student s) { return new StudentRef(s.getId(), s.getName(), s.getEmail(), s.getAcademicLevel()); }

}
