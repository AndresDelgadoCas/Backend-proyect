package fairteam_backend.insights.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class LevelTest {
    @Test
    void classifiesOnTheRoundedValue() {
        assertThat(Level.fromAverage(2.94)).isEqualTo(Level.LOW);
        assertThat(Level.fromAverage(2.95)).isEqualTo(Level.MEDIUM);
        assertThat(Level.fromAverage(3.94)).isEqualTo(Level.MEDIUM);
        assertThat(Level.fromAverage(3.95)).isEqualTo(Level.HIGH);
        assertThat(Level.fromAverage(5.0)).isEqualTo(Level.HIGH);
    }

    @Test
    void readsProfileLevelsInSpanishOrEnglish() {
        assertThat(Level.fromProfile("bajo")).isEqualTo(Level.LOW);
        assertThat(Level.fromProfile("ALTA")).isEqualTo(Level.HIGH);
        assertThat(Level.fromProfile("HIGH")).isEqualTo(Level.HIGH);
        assertThat(Level.fromProfile(null)).isEqualTo(Level.MEDIUM);
        assertThat(Level.fromProfile("otro")).isEqualTo(Level.MEDIUM);
    }

    @Test
    void hasTeacherFriendlyLabels() {
        assertThat(Level.LOW.label()).isEqualTo("Necesita refuerzo");
        assertThat(Level.NO_DATA.label()).isEqualTo("Sin notas");
    }
}
