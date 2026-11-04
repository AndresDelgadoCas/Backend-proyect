package fairteam_backend.observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class AcademicLevelObserverTest {
    @Test
    void classifiesLikeTheTeacherSeesTheGrade() {
        assertThat(AcademicLevelObserver.levelFor(2.94)).isEqualTo("LOW");
        assertThat(AcademicLevelObserver.levelFor(2.96)).isEqualTo("MEDIUM");
        assertThat(AcademicLevelObserver.levelFor(3.96)).isEqualTo("HIGH");
    }

    @Test
    void handlesEventsWithoutFailing() {
        assertThatCode(() -> new AcademicLevelObserver().update(new AcademicEvent(1L, 2.0))).doesNotThrowAnyException();
    }
}
