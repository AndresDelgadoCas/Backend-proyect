package fairteam_backend.insights.domain.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.StudentRef;

class EncouragingFeedbackTest {
    @Test
    void keepsTheSameBodyWithAWarmerFrame() {
        var student = new StudentPerformance(new StudentRef(1, "Laura Gómez", null, null), 2.6, 3, Level.LOW, List.of(), null, Direction.FLAT, 0, List.of());
        String text = FeedbackTemplate.forTone(FeedbackTone.parse("motivador")).compose(student, null, "Cálculo", "Prof. Ruiz");
        assertThat(text).startsWith("¡Hola, Laura! Quiero contarte cómo vas:").contains("Te propongo:").contains("¡Cuentas conmigo!");
    }
}
