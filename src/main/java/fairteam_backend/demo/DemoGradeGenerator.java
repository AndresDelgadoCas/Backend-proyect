package fairteam_backend.demo;

import java.util.Random;

/** Deterministic grades: the same student always gets the same notes, so demos are repeatable. */
public final class DemoGradeGenerator {

    /** The second topic of every course is the hardest one, so "el tema que más cuesta" is meaningful. */
    private static final double[] TOPIC_OFFSET = {0.15, -0.35, 0.05};

    private DemoGradeGenerator() { }

    /**
     * @param period 1-based "corte"
     */
    public static double grade(DemoCourse course, int studentIndex, DemoProfile profile, int component, int period) {
        long seed = (course.name().hashCode() * 31L + studentIndex) * 31L + component * 7L + period;
        double noise = (new Random(seed).nextDouble() - 0.5) * 0.7;
        double value = profile.base()
                + profile.trendPerPeriod() * (period - 1)
                + TOPIC_OFFSET[component % TOPIC_OFFSET.length]
                + course.difficulty()
                + noise;
        return Math.round(Math.max(1.0, Math.min(5.0, value)) * 10.0) / 10.0;
    }

    /** A couple of grades are still pending in courses that are running, as in real life. */
    public static boolean pending(DemoCourse course, int studentIndex, int component, int period) {
        return course.inProgress() && period == course.recordedPeriods() && component == 1
                && (studentIndex == 3 || studentIndex == 11);
    }
}
