package fairteam_backend.observer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AcademicEventTest {
    @Test
    void carriesStudentAndGrade() {
        AcademicEvent event = new AcademicEvent(9L, 3.7);
        assertThat(event.getStudentId()).isEqualTo(9L);
        assertThat(event.getNewGrade()).isEqualTo(3.7);
    }
}
