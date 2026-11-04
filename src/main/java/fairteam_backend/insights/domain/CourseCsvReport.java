package fairteam_backend.insights.domain;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fairteam_backend.insights.domain.grading.GradeProjection;

/** Builds the follow-up report as CSV (semicolon separated, so Excel in Spanish opens it in columns). */
public final class CourseCsvReport {
    private CourseCsvReport() { }

    public static String of(CourseAnalysis analysis, Map<Long, GradeProjection> projections) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        List<String> header = new java.util.ArrayList<>(List.of("Estudiante", "Correo", "Promedio", "Notas", "Estado", "Tendencia", "Riesgo"));
        header.addAll(analysis.periods());
        header.add("Proyección");
        csv.append(line(header));
        for (StudentPerformance p : analysis.students()) {
            List<String> row = new java.util.ArrayList<>();
            row.add(p.student().name());
            row.add(p.student().email() == null ? "" : p.student().email());
            row.add(p.hasGrades() ? Grades.format(p.average()) : "");
            row.add(String.valueOf(p.gradeCount()));
            row.add(p.hasGrades() ? p.level().label() : Level.NO_DATA.label());
            row.add(switch (p.direction()) { case DOWN -> "Baja"; case UP -> "Sube"; case FLAT -> "Estable"; default -> ""; });
            row.add(String.valueOf(p.risk()));
            for (String period : analysis.periods()) {
                row.add(p.periods().stream().filter(x -> x.period().equals(period)).findFirst().map(x -> Grades.format(x.average())).orElse(""));
            }
            GradeProjection projection = projections.get(p.student().id());
            row.add(projection == null ? "" : projection.explanation());
            csv.append(line(row));
        }
        return csv.toString();
    }

    private static String line(List<String> cells) {
        return cells.stream().map(c -> "\"" + c.replace("\"", "\"\"") + "\"").collect(Collectors.joining(";")) + "\r\n";
    }
}
