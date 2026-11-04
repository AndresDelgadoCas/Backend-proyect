package fairteam_backend.insights.domain.grading;

/** What a student has earned so far and what they need in the remaining periods to pass. */
public record GradeProjection(long studentId, String studentName, double earned, double current, double remainingWeight,
        Double needed, ProjectionStatus status, String explanation) {

    /** Final grade if the student gets {@code grade} in everything that is left. */
    public double finalWith(double grade) { return earned + grade * remainingWeight; }
}
