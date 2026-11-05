package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fairteam_backend.insights.domain.CourseAnalysis;
import fairteam_backend.insights.domain.CourseAnalyzer;
import fairteam_backend.insights.domain.GradeRecord;
import fairteam_backend.insights.domain.StudentRef;
import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

import org.junit.jupiter.api.Test;

class MissingGradesHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void listsStudentsWithoutGrades() {
        AssistantReply reply = new MissingGradesHandler().handle(AssistantFixture.query("¿quién no tiene notas?", ctx), ctx).orElseThrow();
        assertThat(reply.items()).extracting(ReplyItem::name).containsExactly("Camila Ospina");
        assertThat(reply.actions().get(0).params()).containsEntry("filtro", "sin-notas");
    }

    @Test
    void ignoresOtherQuestions() {
        assertThat(new MissingGradesHandler().handle(AssistantFixture.query("promedio", ctx), ctx)).isEmpty();
    }

    @Test
    void listsGradesStillPendingInATopic() {
        List<StudentRef> students = List.of(new StudentRef(1, "Ana Ruiz", null, null), new StudentRef(2, "Luis Mora", null, null));
        List<GradeRecord> grades = new ArrayList<>();
        for (long s = 1; s <= 2; s++) {
            grades.add(new GradeRecord(s, 1, "Ciclos", "Corte 1", 3.5));
            grades.add(new GradeRecord(s, 2, "Funciones", "Corte 1", 3.5));
            grades.add(new GradeRecord(s, 1, "Ciclos", "Corte 2", 3.6));
        }
        grades.add(new GradeRecord(1, 2, "Funciones", "Corte 2", 3.9));
        CourseAnalysis analysis = new CourseAnalyzer(new TrendWeightedRiskStrategy()).analyze(10, "Programación I", students, grades);
        AssistantContext c = new AssistantContext("Ana", analysis, Map.of(), 0, List.of(new CourseRef(10, "Programación I")), "");
        AssistantReply reply = new MissingGradesHandler().handle(AssistantFixture.query("¿faltan notas?", c), c).orElseThrow();
        assertThat(reply.text()).startsWith("1 nota está pendiente.");
        assertThat(reply.items()).extracting(ReplyItem::name, ReplyItem::value, ReplyItem::note)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("Luis Mora", "1", "pendiente en Funciones"));
    }
}
