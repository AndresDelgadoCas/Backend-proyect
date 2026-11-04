package fairteam_backend.insights.domain.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.StudentRef;
import fairteam_backend.insights.domain.SubjectAverage;

class FeedbackTemplateTest {
    private static StudentPerformance student(Level level, Double average, Direction direction) {
        return new StudentPerformance(new StudentRef(1, "Laura Gómez", null, null), average, average == null ? 0 : 4, level,
                List.of(), null, direction, 0, average == null ? List.of() : List.of(new SubjectAverage(1, "Funciones", 2.4, 2, 1)));
    }

    @Test
    void factoryChoosesTheToneAndTheTemplateKeepsTheStructure() {
        String warm = FeedbackTemplate.forTone(FeedbackTone.CERCANO).compose(student(Level.LOW, 2.5, Direction.FLAT), null, "Cálculo", "Prof. Ruiz");
        String formal = FeedbackTemplate.forTone(FeedbackTone.FORMAL).compose(student(Level.LOW, 2.5, Direction.FLAT), null, "Cálculo", "Prof. Ruiz");
        assertThat(warm).startsWith("Hola, Laura:").contains("Un saludo,").endsWith("Prof. Ruiz");
        assertThat(formal).startsWith("Estimado(a) Laura Gómez:").contains("Cordialmente,");
        assertThat(warm).contains("Funciones (2,4)").contains("Te propongo:");
    }

    @Test
    void congratulatesStrongStudents() {
        String text = FeedbackTemplate.forTone(FeedbackTone.CERCANO).compose(student(Level.HIGH, 4.6, Direction.UP), null, "Cálculo", "P");
        assertThat(text).contains("Quería felicitarte").contains("has mejorado").doesNotContain("Te propongo");
    }

    @Test
    void asksAboutMissingGrades() {
        String text = FeedbackTemplate.forTone(FeedbackTone.CERCANO).compose(student(Level.NO_DATA, null, Direction.UNKNOWN), null, "Cálculo", "P");
        assertThat(text).contains("Aún no tengo notas tuyas");
    }

    @Test
    void parsesToneLeniently() {
        assertThat(FeedbackTone.parse("FORMAL")).isEqualTo(FeedbackTone.FORMAL);
        assertThat(FeedbackTone.parse("lo que sea")).isEqualTo(FeedbackTone.CERCANO);
        assertThat(FeedbackTone.parse(null)).isEqualTo(FeedbackTone.CERCANO);
    }
}
