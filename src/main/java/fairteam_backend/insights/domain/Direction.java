package fairteam_backend.insights.domain;

/** How a student's average moved between the first and the last period with grades. */
public enum Direction {
    DOWN, UP, FLAT, UNKNOWN;

    public static final double THRESHOLD = 0.3;

    public static Direction of(Double delta) {
        if (delta == null) return UNKNOWN;
        if (delta <= -THRESHOLD) return DOWN;
        if (delta >= THRESHOLD) return UP;
        return FLAT;
    }
}
