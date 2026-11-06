package fairteam_backend.gradebook;

import java.math.BigDecimal;
import java.util.List;

/**
 * One corte of a course as a table, in the course's own scale (0–5, 0–10, 0–100…).
 *
 * @param columns     activities graded in this corte, in the teacher's order, with their weight (null = equal)
 * @param suggested   true when the corte has no columns yet and these were copied from the previous corte
 * @param periodWeights weight of each corte in the final grade, aligned with {@code periods}
 */
public record GradeSheet(String period, List<String> periods, List<Column> columns, boolean suggested, List<Row> students,
        List<Cell> cells, double maxGrade, double passingGrade, List<PeriodWeight> periodWeights) {
    public record Column(long id, String name, BigDecimal weight) { }

    public record Row(long id, String name, String email) { }

    public record Cell(long studentId, long subjectId, BigDecimal grade) { }

    public record PeriodWeight(String period, Integer weight) { }

    /** A change from the editor, in the course scale; {@code grade == null} clears the cell. */
    public record Change(long studentId, long subjectId, BigDecimal grade) { }

    /** The columns of the corte in order; {@code weight == null} for every column means equal weights. */
    public record ColumnSpec(long subjectId, BigDecimal weight) { }

    public record SaveResult(int saved, int cleared, GradeSheet sheet) { }
}
