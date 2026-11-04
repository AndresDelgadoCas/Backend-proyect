package fairteam_backend.insights;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fairteam_backend.insights.application.port.out.CourseDataPort;
import fairteam_backend.insights.application.port.out.GradingSchemePort;
import fairteam_backend.insights.domain.GradeRecord;
import fairteam_backend.insights.domain.StudentRef;
import fairteam_backend.insights.domain.assistant.CourseRef;
import fairteam_backend.insights.domain.grading.GradingScheme;

/** A small, realistic course: two periods, four subjects. Valentina drops, Juan Pablo improves. */
final class InsightsFixtures {
    static final List<StudentRef> STUDENTS = List.of(
            new StudentRef(1, "Valentina Rojas Díaz", "valentina@u.edu.co", "MEDIUM"),
            new StudentRef(2, "Santiago Gómez", "santiago@u.edu.co", "MEDIUM"),
            new StudentRef(3, "Mariana López Herrera", null, "MEDIUM"),
            new StudentRef(4, "Juan Pablo Castaño", "juan@u.edu.co", "MEDIUM"),
            new StudentRef(5, "Camila Ospina", "camila@u.edu.co", "LOW"));
    static final String[] SUBJECTS = { "Ciclos y condicionales", "Funciones", "Arreglos", "Variables y tipos" };

    static List<GradeRecord> grades() {
        double[][] corte1 = { { 2.0, 2.6, 2.4, 2.8 }, { 3.4, 3.9, 3.7, 4.0 }, { 2.4, 3.0, 2.9, 3.2 }, { 4.0, 4.6, 4.5, 4.8 } };
        double[] change = { -0.6, 0.0, -0.4, 0.4 };
        List<GradeRecord> list = new ArrayList<>();
        for (int s = 0; s < corte1.length; s++) {
            for (int j = 0; j < SUBJECTS.length; j++) {
                list.add(new GradeRecord(s + 1, j + 1, SUBJECTS[j], "Corte 1", corte1[s][j]));
                list.add(new GradeRecord(s + 1, j + 1, SUBJECTS[j], "Corte 2", Math.max(0, Math.min(5, corte1[s][j] + change[s]))));
            }
        }
        return list;
    }

    static CourseDataPort courseData() {
        return new CourseDataPort() {
            public Optional<CourseData> load(long courseId) {
                return courseId == 1 ? Optional.of(new CourseData(1, "Programación I", STUDENTS, grades(), 2)) : Optional.empty();
            }
            public List<CourseRef> courses() { return List.of(new CourseRef(1, "Programación I"), new CourseRef(2, "Estructuras de datos")); }
        };
    }

    static GradingSchemePort schemes() {
        return new GradingSchemePort() {
            private final Map<Long, GradingScheme> store = new java.util.HashMap<>();
            public Optional<GradingScheme> find(long courseId) { return Optional.ofNullable(store.get(courseId)); }
            public GradingScheme save(long courseId, GradingScheme scheme) { store.put(courseId, scheme); return scheme; }
        };
    }
}
