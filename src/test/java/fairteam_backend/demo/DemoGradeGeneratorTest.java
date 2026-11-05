package fairteam_backend.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DemoGradeGeneratorTest {

    private final DemoCourse finished = DemoCatalog.COURSES.get(0);
    private final DemoCourse running = DemoCatalog.COURSES.get(2);

    @Test
    void gradesStayOnTheColombianScale() {
        for (DemoProfile profile : DemoProfile.values()) {
            for (int s = 0; s < 30; s++) {
                for (int period = 1; period <= 3; period++) {
                    double grade = DemoGradeGenerator.grade(finished, s, profile, s % 3, period);
                    assertThat(grade).isBetween(1.0, 5.0);
                    assertThat(grade * 10).isEqualTo(Math.rint(grade * 10));
                }
            }
        }
    }

    @Test
    void gradesAreRepeatable() {
        assertThat(DemoGradeGenerator.grade(finished, 4, DemoProfile.STEADY, 1, 2))
                .isEqualTo(DemoGradeGenerator.grade(finished, 4, DemoProfile.STEADY, 1, 2));
    }

    @Test
    void fallingStudentsDropAndImprovingStudentsClimb() {
        assertThat(average(DemoProfile.FALLING, 3)).isLessThan(average(DemoProfile.FALLING, 1) - 0.6);
        assertThat(average(DemoProfile.IMPROVING, 3)).isGreaterThan(average(DemoProfile.IMPROVING, 1) + 0.5);
    }

    @Test
    void onlyRunningCoursesHavePendingGrades() {
        assertThat(DemoGradeGenerator.pending(running, 3, 1, running.recordedPeriods())).isTrue();
        assertThat(DemoGradeGenerator.pending(running, 3, 1, 1)).isFalse();
        assertThat(DemoGradeGenerator.pending(finished, 3, 1, 3)).isFalse();
    }

    private double average(DemoProfile profile, int period) {
        double sum = 0;
        for (int s = 0; s < 20; s++) {
            for (int k = 0; k < 3; k++) sum += DemoGradeGenerator.grade(finished, s, profile, k, period);
        }
        return sum / 60;
    }

    @Test
    void strugglingStudentsMissMoreClassesThanStrongOnes() {
        assertThat(absences(DemoProfile.STRUGGLING)).isGreaterThan(absences(DemoProfile.STRONG) * 3);
    }

    @Test
    void runningCoursesHaveFewerSessions() {
        assertThat(DemoGradeGenerator.sessions(running)).isLessThan(DemoGradeGenerator.sessions(finished));
    }

    private long absences(DemoProfile profile) {
        long count = 0;
        for (int s = 0; s < 40; s++) {
            for (int k = 1; k <= 16; k++) if ("ABSENT".equals(DemoGradeGenerator.attendance(finished, s, profile, k))) count++;
        }
        return count;
    }
}
