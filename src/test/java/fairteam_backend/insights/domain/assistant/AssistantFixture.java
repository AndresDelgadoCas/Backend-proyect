package fairteam_backend.insights.domain.assistant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fairteam_backend.insights.domain.CourseAnalysis;
import fairteam_backend.insights.domain.CourseAnalyzer;
import fairteam_backend.insights.domain.GradeRecord;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.StudentRef;
import fairteam_backend.insights.domain.grading.GradeProjection;
import fairteam_backend.insights.domain.grading.GradeProjector;
import fairteam_backend.insights.domain.grading.GradingScheme;
import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

/** Shared course for the assistant tests: Valentina falls, Juan improves, Camila has no grades. */
final class AssistantFixture {
    private AssistantFixture() { }

    static AssistantContext context() { return context(2); }

    static AssistantContext context(int materials) {
        List<StudentRef> students = List.of(new StudentRef(1, "Valentina Rojas", "v@u.co", null), new StudentRef(2, "Santiago Gómez", "s@u.co", null),
                new StudentRef(3, "Juan Pablo Castaño", null, null), new StudentRef(4, "Camila Ospina", null, null));
        String[] subjects = { "Ciclos y condicionales", "Funciones" };
        double[][] c1 = { { 2.2, 2.8 }, { 3.6, 3.8 }, { 4.2, 4.4 } };
        double[] change = { -0.7, 0.0, 0.5 };
        List<GradeRecord> grades = new ArrayList<>();
        for (int s = 0; s < c1.length; s++) for (int j = 0; j < subjects.length; j++) {
            grades.add(new GradeRecord(s + 1, j + 1, subjects[j], "Corte 1", c1[s][j]));
            grades.add(new GradeRecord(s + 1, j + 1, subjects[j], "Corte 2", Math.min(5, c1[s][j] + change[s])));
        }
        CourseAnalysis analysis = new CourseAnalyzer(new TrendWeightedRiskStrategy()).analyze(10, "Programación I", students, grades);
        Map<Long, GradeProjection> projections = new LinkedHashMap<>();
        GradingScheme scheme = new GradingScheme(List.of(30, 30, 40), 3.0);
        for (StudentPerformance p : analysis.students()) projections.put(p.student().id(), new GradeProjector().project(p, analysis.periods(), scheme));
        return new AssistantContext("Sebastián Granda", analysis, projections, materials,
                List.of(new CourseRef(10, "Programación I"), new CourseRef(11, "Estructuras de datos")), "Buenas tardes, Sebastián. Resumen de prueba.");
    }

    static AssistantContext emptyCourse() {
        CourseAnalysis analysis = new CourseAnalyzer(new TrendWeightedRiskStrategy()).analyze(12, "Cálculo", List.of(), List.of());
        return new AssistantContext("Ana", analysis, Map.of(), 0, List.of(new CourseRef(12, "Cálculo")), "Hola");
    }

    /** The same course with saved activities (newest first) and earlier turns. */
    static AssistantContext withActivities(Turn... history) {
        return context().withConversation(List.of(
                new ActivityRef(31, "quiz", "Quiz de funciones", "Funciones", java.time.LocalDate.of(2026, 11, 8)),
                new ActivityRef(30, "workshop", "Taller de ciclos anidados", "Ciclos y condicionales", java.time.LocalDate.of(2026, 11, 5)),
                new ActivityRef(29, "workshop", "Taller de recursividad", "Recursividad", java.time.LocalDate.of(2026, 10, 30))), List.of(history));
    }

    static AssistantQuery query(String text, AssistantContext context) { return AssistantQuery.parse(text, context); }
}
