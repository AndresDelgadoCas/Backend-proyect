package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AssistantQueryTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void normalizesAccentsCaseAndPunctuation() {
        assertThat(AssistantQuery.normalize("¿Quién VIENE bajando?")).isEqualTo("quien viene bajando");
    }

    @Test
    void findsStudentsByFirstNameOrSurname() {
        assertThat(AssistantFixture.query("como va valentina", ctx).student().student().id()).isEqualTo(1);
        assertThat(AssistantFixture.query("y castaño?", ctx).student().student().id()).isEqualTo(3);
        assertThat(AssistantFixture.query("como va el curso", ctx).student()).isNull();
    }

    @Test
    void findsSubjectsByStem() {
        assertThat(AssistantFixture.query("quiz de ciclos", ctx).subject().name()).isEqualTo("Ciclos y condicionales");
        assertThat(AssistantFixture.query("promedio en funcion", ctx).subject().name()).isEqualTo("Funciones");
    }

    @Test
    void detectsTheLevelAndMatchesWordStarts() {
        assertThat(AssistantFixture.query("taller de refuerzo", ctx).level()).isEqualTo(fairteam_backend.insights.domain.Level.LOW);
        assertThat(AssistantFixture.query("un reto", ctx).level()).isEqualTo(fairteam_backend.insights.domain.Level.HIGH);
        assertThat(AssistantFixture.query("recrear", ctx).has("crea")).isFalse();
    }
}
