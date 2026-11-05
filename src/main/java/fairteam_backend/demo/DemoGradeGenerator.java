package fairteam_backend.demo;

import java.util.SplittableRandom;

/** Deterministic grades: the same student always gets the same notes, so demos are repeatable. */
public final class DemoGradeGenerator {

    /** The second topic of every course is the hardest one, so "el tema que más cuesta" is meaningful. */
    private static final double[] TOPIC_OFFSET = {0.15, -0.35, 0.05};

    private DemoGradeGenerator() { }

    /**
     * @param period 1-based "corte"
     */
    public static double grade(DemoCourse course, int studentIndex, DemoProfile profile, int component, int period) {
        // SplittableRandom mixes nearby seeds well; java.util.Random gives correlated first values for them.
        long seed = (course.name().hashCode() * 31L + studentIndex) * 31L + component * 7L + period;
        double noise = (new SplittableRandom(seed).nextDouble() - 0.5) * 0.7;
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

    /** Sessions held so far: two a week; finished courses had a full semester. */
    public static int sessions(DemoCourse course) { return course.inProgress() ? 10 : 16; }

    /** Attendance mark for one session; weaker profiles miss more, and falling students miss more over time. */
    public static String attendance(DemoCourse course, int studentIndex, DemoProfile profile, int session) {
        double absence = switch (profile) {
            case STRONG -> 0.03;
            case STEADY -> 0.07;
            case IMPROVING -> 0.10;
            case FALLING -> 0.08 + 0.025 * session;
            case STRUGGLING -> 0.28;
        };
        double roll = new SplittableRandom((course.name().hashCode() * 17L + studentIndex) * 13L + session).nextDouble();
        if (roll < absence * 0.25) return "EXCUSED";
        if (roll < absence) return "ABSENT";
        if (roll < absence + 0.06) return "LATE";
        return "PRESENT";
    }
}
