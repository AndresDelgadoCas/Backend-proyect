package fairteam_backend.insights.application.port.out;

import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.GradeRecord;
import fairteam_backend.insights.domain.StudentRef;
import fairteam_backend.insights.domain.assistant.CourseRef;

/** Outbound port: reads a course's academic data. Implemented by the JPA adapter. */
public interface CourseDataPort {
    /** @param maxGrade the course's scale (5, 10, 100…); grades in {@code grades} are always on 0–5. */
    record CourseData(long id, String name, List<StudentRef> students, List<GradeRecord> grades, int materials, double maxGrade) {
        public CourseData(long id, String name, List<StudentRef> students, List<GradeRecord> grades, int materials) {
            this(id, name, students, grades, materials, 5.0);
        }
    }

    Optional<CourseData> load(long courseId);

    List<CourseRef> courses();
}
