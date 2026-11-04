package fairteam_backend.insights.domain;

import java.util.List;
import java.util.Optional;

/** The full reading of one course. Immutable; built by {@link CourseAnalyzer}. */
public record CourseAnalysis(
        long courseId,
        String courseName,
        CourseSummary summary,
        List<String> periods,
        List<SubjectAverage> subjects,
        List<StudentPerformance> students,
        List<GradeBand> distribution) {

    public Optional<StudentPerformance> student(long id) {
        return students.stream().filter(s -> s.student().id() == id).findFirst();
    }

    public Optional<SubjectAverage> weakestSubject() { return subjects.stream().findFirst(); }
}
