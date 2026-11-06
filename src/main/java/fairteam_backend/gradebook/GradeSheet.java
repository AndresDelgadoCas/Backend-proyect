package fairteam_backend.gradebook;

import java.math.BigDecimal;
import java.util.List;

/** One corte of a course as a table: students in rows, topics in columns, grades in cells. */
public record GradeSheet(String period, List<String> periods, List<Column> subjects, List<Row> students, List<Cell> cells) {
    public record Column(long id, String name) { }

    public record Row(long id, String name, String email) { }

    public record Cell(long studentId, long subjectId, BigDecimal grade) { }

    /** A change from the editor; {@code grade == null} clears the cell. */
    public record Change(long studentId, long subjectId, BigDecimal grade) { }

    public record SaveResult(int saved, int cleared, GradeSheet sheet) { }
}
