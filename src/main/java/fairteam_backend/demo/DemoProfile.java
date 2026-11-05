package fairteam_backend.demo;

/** How a fictional student performs over the semester; drives the generated grades. */
public enum DemoProfile {
    /** Consistently above 4.0. */
    STRONG(4.3, 0.0),
    /** Around 3.5 with small ups and downs. */
    STEADY(3.6, 0.0),
    /** Starts low and climbs each period. */
    IMPROVING(2.8, 0.45),
    /** Starts well and drops each period: the case the assistant should flag early. */
    FALLING(3.9, -0.55),
    /** Below 3.0 all semester. */
    STRUGGLING(2.4, 0.05);

    private final double base;
    private final double trendPerPeriod;

    DemoProfile(double base, double trendPerPeriod) {
        this.base = base;
        this.trendPerPeriod = trendPerPeriod;
    }

    public double base() { return base; }

    public double trendPerPeriod() { return trendPerPeriod; }

    /** Mix per 20 students: 5 strong, 7 steady, 3 improving, 3 falling, 2 struggling. */
    public static DemoProfile forIndex(int index) {
        int slot = Math.floorMod(index * 7 + 3, 20);
        if (slot < 5) return STRONG;
        if (slot < 12) return STEADY;
        if (slot < 15) return IMPROVING;
        if (slot < 18) return FALLING;
        return STRUGGLING;
    }
}
