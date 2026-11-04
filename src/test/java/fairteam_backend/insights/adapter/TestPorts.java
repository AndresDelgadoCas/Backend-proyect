package fairteam_backend.insights.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fairteam_backend.insights.application.port.out.CourseDataPort;
import fairteam_backend.insights.application.port.out.GradingSchemePort;
import fairteam_backend.insights.domain.GradeRecord;
import fairteam_backend.insights.domain.StudentRef;
import fairteam_backend.insights.domain.assistant.CourseRef;
import fairteam_backend.insights.domain.grading.GradingScheme;

/** In-memory ports for web tests: one course, two students, two periods. */
final class TestPorts {
    private TestPorts() { }

    static CourseDataPort courseData() {
        return new CourseDataPort() {
            public Optional<CourseData> load(long id) {
                if (id != 1) return Optional.empty();
                return Optional.of(new CourseData(1, "Física", List.of(new StudentRef(1, "Ana Ruiz", "ana@u.co", null), new StudentRef(2, "Luis Paz", null, null)),
                        List.of(new GradeRecord(1, 1, "Cinemática", "Corte 1", 3.4), new GradeRecord(1, 1, "Cinemática", "Corte 2", 2.6),
                                new GradeRecord(2, 1, "Cinemática", "Corte 1", 4.1), new GradeRecord(2, 1, "Cinemática", "Corte 2", 4.3)), 1));
            }
            public List<CourseRef> courses() { return List.of(new CourseRef(1, "Física")); }
        };
    }

    static GradingSchemePort schemes() {
        Map<Long, GradingScheme> store = new HashMap<>();
        return new GradingSchemePort() {
            public Optional<GradingScheme> find(long id) { return Optional.ofNullable(store.get(id)); }
            public GradingScheme save(long id, GradingScheme s) { store.put(id, s); return s; }
        };
    }
}
