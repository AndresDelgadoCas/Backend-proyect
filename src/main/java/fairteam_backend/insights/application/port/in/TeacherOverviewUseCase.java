package fairteam_backend.insights.application.port.in;

import java.util.List;

/** Inbound port: one line per course so the teacher sees all groups at a glance. */
public interface TeacherOverviewUseCase {
    record CourseOverview(long courseId, String name, int students, Double average, int low, int fallingBehind, int ungraded, String weakestSubject) { }

    List<CourseOverview> overview();
}
