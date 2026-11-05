package fairteam_backend.insights.domain.assistant;

import java.util.List;
import java.util.Map;

import fairteam_backend.insights.domain.CourseAnalysis;
import fairteam_backend.insights.domain.attendance.AttendanceStats;
import fairteam_backend.insights.domain.grading.GradeProjection;

/** Everything a handler may read to answer. Built once per question by the application service. */
public record AssistantContext(String teacher, CourseAnalysis analysis, Map<Long, GradeProjection> projections,
        int materials, List<CourseRef> courses, String briefing, Map<Long, AttendanceStats> attendance) {

    public AssistantContext {
        attendance = attendance == null ? Map.of() : Map.copyOf(attendance);
    }

    /** Context without attendance data (courses that do not take roll). */
    public AssistantContext(String teacher, CourseAnalysis analysis, Map<Long, GradeProjection> projections,
            int materials, List<CourseRef> courses, String briefing) {
        this(teacher, analysis, projections, materials, courses, briefing, Map.of());
    }
}
