package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import fairteam_backend.insights.domain.attendance.AttendanceStats;

class AttendanceHandlerTest {
    private final AssistantContext base = AssistantFixture.context();

    private AssistantContext with(Map<Long, AttendanceStats> stats) {
        return new AssistantContext(base.teacher(), base.analysis(), base.projections(), base.materials(), base.courses(), base.briefing(), stats);
    }

    @Test
    void invitesToTakeRollWhenThereIsNoData() {
        AssistantReply reply = new AttendanceHandler().handle(AssistantFixture.query("¿quién ha faltado?", base), base).orElseThrow();
        assertThat(reply.text()).contains("Aún no has tomado asistencia");
        assertThat(reply.actions().get(0).params()).containsEntry("vista", "asistencia");
    }

    @Test
    void listsFrequentAbsencesAndCombinesThemWithGrades() {
        AssistantContext c = with(Map.of(
                1L, new AttendanceStats(1, 10, 6, 4, 0, 0),
                2L, new AttendanceStats(2, 10, 7, 3, 0, 0),
                3L, new AttendanceStats(3, 10, 10, 0, 0, 0)));
        AssistantReply reply = new AttendanceHandler().handle(AssistantFixture.query("¿cómo va la asistencia?", c), c).orElseThrow();
        assertThat(reply.text()).startsWith("2 estudiantes asisten a menos del 80 %");
        assertThat(reply.text()).contains("1 de ellos también está por debajo de 3,0");
        assertThat(reply.items()).extracting(ReplyItem::name).containsExactly("Valentina Rojas", "Santiago Gómez");
        assertThat(reply.items().get(0).value()).isEqualTo("60 %");
    }

    @Test
    void saysWhenAttendanceIsFine() {
        AssistantContext c = with(Map.of(1L, new AttendanceStats(1, 10, 9, 1, 0, 0)));
        assertThat(new AttendanceHandler().handle(AssistantFixture.query("asistencia", c), c).orElseThrow().text())
                .contains("La asistencia va bien");
    }

    @Test
    void ignoresOtherQuestions() {
        assertThat(new AttendanceHandler().handle(AssistantFixture.query("promedio", base), base)).isEmpty();
    }

    @Test
    void missingGradesStillWinForFaltanNotas() {
        assertThat(AssistantEngine.standard().answer("¿faltan notas?", base).intent()).isEqualTo("missing-grades");
    }
}
