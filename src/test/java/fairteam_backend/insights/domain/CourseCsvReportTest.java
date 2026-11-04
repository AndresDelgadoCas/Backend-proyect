package fairteam_backend.insights.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

class CourseCsvReportTest {
    @Test
    void writesOneQuotedRowPerStudentWithPeriods() {
        CourseAnalysis a = new CourseAnalyzer(new TrendWeightedRiskStrategy()).analyze(1, "C",
                List.of(new StudentRef(1, "Ana \"La Profe\" Ruiz", "ana@u.co", null), new StudentRef(2, "Luis", null, null)),
                List.of(new GradeRecord(1, 1, "T", "Corte 1", 2.0), new GradeRecord(1, 1, "T", "Corte 2", 3.0)));
        String csv = CourseCsvReport.of(a, Map.of());
        String[] lines = csv.split("\r\n");
        assertThat(lines[0]).startsWith("\uFEFF\"Estudiante\";").contains("\"Corte 1\";\"Corte 2\"");
        assertThat(csv).contains("\"Ana \"\"La Profe\"\" Ruiz\";\"ana@u.co\";\"2,5\"").contains("\"Sube\"");
        assertThat(lines).hasSize(3);
    }
}
