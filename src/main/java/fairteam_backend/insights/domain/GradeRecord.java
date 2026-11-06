package fairteam_backend.insights.domain;

/**
 * One grade, on the internal 0–5 scale.
 *
 * @param weight percentage of this activity inside its period, or {@code null} when the teacher did not
 *               set weights (then every activity of the period counts the same)
 */
public record GradeRecord(long studentId, long subjectId, String subjectName, String period, double value, Double weight) {
    public GradeRecord(long studentId, long subjectId, String subjectName, String period, double value) {
        this(studentId, subjectId, subjectName, period, value, null);
    }

    /** Weighted mean of a period: activities with a weight count by it; without weights it is a plain mean. */
    public static double periodAverage(java.util.List<GradeRecord> marks) {
        boolean weighted = marks.stream().anyMatch(g -> g.weight() != null);
        if (!weighted) return Grades.average(marks.stream().map(GradeRecord::value).toList());
        double total = 0, sum = 0;
        for (GradeRecord g : marks) {
            double w = g.weight() == null ? 0 : g.weight();
            total += w;
            sum += w * g.value();
        }
        // Only weights of activities that already have a grade: a pending activity does not drag the period to 0.
        return total > 0 ? sum / total : Grades.average(marks.stream().map(GradeRecord::value).toList());
    }
}
