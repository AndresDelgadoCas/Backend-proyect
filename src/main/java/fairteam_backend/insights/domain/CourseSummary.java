package fairteam_backend.insights.domain;

public record CourseSummary(int students, int graded, int ungraded, int low, int medium, int high,
        Double average, int gradeCount, int fallingBehind) { }
