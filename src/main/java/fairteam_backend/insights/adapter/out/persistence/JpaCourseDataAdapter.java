package fairteam_backend.insights.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fairteam_backend.entity.Grade;
import fairteam_backend.entity.Student;
import fairteam_backend.insights.application.port.out.CourseDataPort;
import fairteam_backend.insights.domain.GradeRecord;
import fairteam_backend.insights.domain.StudentRef;
import fairteam_backend.insights.domain.assistant.CourseRef;
import fairteam_backend.repository.CourseMaterialRepository;
import fairteam_backend.repository.CourseRepository;
import fairteam_backend.repository.GradeRepository;
import fairteam_backend.repository.StudentRepository;

/**
 * Adapter from the existing JPA repositories to the insights domain. Row level security already
 * limits every query to the signed-in teacher, so a course from another account is simply not found.
 */
@Component
public class JpaCourseDataAdapter implements CourseDataPort {
    private final CourseRepository courses;
    private final StudentRepository students;
    private final GradeRepository grades;
    private final CourseMaterialRepository materials;

    public JpaCourseDataAdapter(CourseRepository courses, StudentRepository students, GradeRepository grades, CourseMaterialRepository materials) {
        this.courses = courses;
        this.students = students;
        this.grades = grades;
        this.materials = materials;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseData> load(long courseId) {
        return courses.findById(courseId).map(course -> {
            List<StudentRef> roster = students.findAllByCourse_IdOrderByNameAsc(courseId).stream().map(JpaCourseDataAdapter::toRef).toList();
            List<GradeRecord> records = grades.findAllByCourse_Id(courseId).stream()
                    .filter(g -> g.getGrade() != null && g.getStudent() != null && g.getSubject() != null)
                    .map(JpaCourseDataAdapter::toRecord).toList();
            return new CourseData(course.getId(), course.getName(), roster, records, (int) materials.countByCourse_Id(courseId));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseRef> courses() {
        return courses.findAllByOrderByCreatedAtDesc().stream().map(c -> new CourseRef(c.getId(), c.getName())).toList();
    }

    private static StudentRef toRef(Student s) { return new StudentRef(s.getId(), s.getName(), s.getEmail(), s.getAcademicLevel()); }

    private static GradeRecord toRecord(Grade g) {
        return new GradeRecord(g.getStudent().getId(), g.getSubject().getId(), g.getSubject().getName(), g.getPeriod(), g.getGrade().doubleValue());
    }
}
