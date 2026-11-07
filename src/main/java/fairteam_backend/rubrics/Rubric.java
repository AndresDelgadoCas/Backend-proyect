package fairteam_backend.rubrics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Read and write models of the rubrics module, as the API exchanges them. */
public final class Rubric {
    private Rubric() { }

    /** What each level says is observed in the work, from Excelente to Insuficiente. */
    public record Levels(String excellent, String good, String basic, String insufficient) { }

    public record Criterion(long id, String name, String description, BigDecimal weight, Levels levels) { }

    /** A criterion as the teacher (or the AI) writes it; {@code id} is null for a new one. */
    public record CriterionDraft(Long id, String name, String description, BigDecimal weight, Levels levels) { }

    /** A rubric to save. Generated drafts come back in this shape so the teacher reviews them first. */
    public record Draft(String title, String description, List<CriterionDraft> criteria) { }

    public record Summary(long id, String title, int criteria, int scored, int students, String period,
            String columnName, LocalDateTime updatedAt) { }

    public record Student(long id, String name) { }

    /**
     * One student's assessment: the level reached per criterion (0 Excelente … 3 Insuficiente), the
     * comment and the resulting grade in the course scale (null until every criterion has a level).
     */
    public record StudentResult(long studentId, Map<Long, Integer> levels, String comment, BigDecimal grade) { }

    public record Detail(long id, String title, String description, List<Criterion> criteria, List<Student> students,
            List<StudentResult> results, double maxGrade, List<BigDecimal> levelGrades, String period, String columnName) { }

    /** Levels for one student; a criterion mapped to null clears it. */
    public record ScoreRequest(Map<Long, Integer> levels, String comment) { }

    public record PublishRequest(String period, String columnName) { }

    public record PublishResult(int published, int pending, String period, String columnName) { }

    public record GenerateRequest(String activity, Integer criteriaCount, String teacherPrompt) { }
}
