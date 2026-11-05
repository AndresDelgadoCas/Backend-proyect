package fairteam_backend.insights.application.port.out;

import java.util.Map;

import fairteam_backend.insights.domain.attendance.AttendanceStats;

/** Outbound port: attendance totals per student of a course. */
public interface AttendancePort {
    Map<Long, AttendanceStats> statsFor(long courseId);

    /** For tests and setups without attendance data. */
    static AttendancePort none() { return courseId -> Map.of(); }
}
