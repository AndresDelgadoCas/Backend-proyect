package fairteam_backend.insights.domain.attendance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AttendanceStatsTest {
    @Test
    void lateCountsAsAttendedAndExcusedIsNotCounted() {
        AttendanceStats s = new AttendanceStats(1, 10, 6, 2, 1, 1);
        assertThat(s.rate()).isEqualTo(7 / 9.0);
        assertThat(s.percent()).isEqualTo(78);
        assertThat(s.alert()).isTrue();
    }

    @Test
    void fewSessionsNeverRaiseAnAlert() {
        assertThat(new AttendanceStats(1, 3, 1, 2, 0, 0).alert()).isFalse();
    }

    @Test
    void noCountableSessionsMeansFullAttendance() {
        assertThat(new AttendanceStats(1, 2, 0, 0, 0, 2).rate()).isEqualTo(1.0);
    }

    @Test
    void eightyPercentIsNotAnAlert() {
        assertThat(new AttendanceStats(1, 10, 8, 2, 0, 0).alert()).isFalse();
    }
}
