package fairteam_backend.insights.domain;

import java.util.List;

/** Everything the teacher needs to know about one student, computed from the grades. */
public record StudentPerformance(
        StudentRef student,
        Double average,
        int gradeCount,
        Level level,
        List<PeriodAverage> periods,
        Double delta,
        Direction direction,
        int risk,
        List<SubjectAverage> subjects) {

    public boolean hasGrades() { return gradeCount > 0; }

    public SubjectAverage weakest() { return subjects.isEmpty() ? null : subjects.get(0); }
}
