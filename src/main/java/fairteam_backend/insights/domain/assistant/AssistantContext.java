package fairteam_backend.insights.domain.assistant;

import java.util.List;
import java.util.Map;

import fairteam_backend.insights.domain.CourseAnalysis;
import fairteam_backend.insights.domain.attendance.AttendanceStats;
import fairteam_backend.insights.domain.grading.GradeProjection;

/**
 * Everything a handler may read to answer. Built once per question by the application service: the course
 * reading, the saved activities (newest first) and the earlier turns of the conversation (oldest first).
 */
public record AssistantContext(String teacher, CourseAnalysis analysis, Map<Long, GradeProjection> projections,
        int materials, List<CourseRef> courses, String briefing, Map<Long, AttendanceStats> attendance,
        List<ActivityRef> activities, List<Turn> history) {

    public AssistantContext {
        attendance = attendance == null ? Map.of() : Map.copyOf(attendance);
        activities = activities == null ? List.of() : List.copyOf(activities);
        history = history == null ? List.of() : List.copyOf(history);
    }

    public AssistantContext(String teacher, CourseAnalysis analysis, Map<Long, GradeProjection> projections,
            int materials, List<CourseRef> courses, String briefing, Map<Long, AttendanceStats> attendance) {
        this(teacher, analysis, projections, materials, courses, briefing, attendance, List.of(), List.of());
    }

    /** Context without attendance data (courses that do not take roll). */
    public AssistantContext(String teacher, CourseAnalysis analysis, Map<Long, GradeProjection> projections,
            int materials, List<CourseRef> courses, String briefing) {
        this(teacher, analysis, projections, materials, courses, briefing, Map.of());
    }

    public AssistantContext withConversation(List<ActivityRef> saved, List<Turn> turns) {
        return new AssistantContext(teacher, analysis, projections, materials, courses, briefing, attendance, saved, turns);
    }

    /** Búho's last message, if the conversation has one. */
    public java.util.Optional<Turn> lastBuhoTurn() {
        for (int i = history.size() - 1; i >= 0; i--) if (history.get(i).fromBuho()) return java.util.Optional.of(history.get(i));
        return java.util.Optional.empty();
    }
}
