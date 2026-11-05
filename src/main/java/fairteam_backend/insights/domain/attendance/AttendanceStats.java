package fairteam_backend.insights.domain.attendance;

/**
 * Attendance of one student in one course. Excused absences do not count against the student.
 *
 * @param sessions every session with a record for the student
 */
public record AttendanceStats(long studentId, int sessions, int present, int absent, int late, int excused) {
    /** Below this rate (with enough sessions) absence becomes a risk signal, as in early-warning systems. */
    public static final double ALERT_RATE = 0.8;
    public static final int MIN_SESSIONS = 4;

    /** Share of countable sessions the student attended (late counts as attended). 1.0 with no data. */
    public double rate() {
        int countable = sessions - excused;
        return countable <= 0 ? 1.0 : (present + late) / (double) countable;
    }

    public boolean alert() { return sessions >= MIN_SESSIONS && rate() < ALERT_RATE; }

    public int percent() { return (int) Math.round(rate() * 100); }
}
