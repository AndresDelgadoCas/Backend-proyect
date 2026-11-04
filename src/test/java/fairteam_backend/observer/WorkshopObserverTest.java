package fairteam_backend.observer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WorkshopObserverTest {
    @Test
    void suggestsWorkshopsOnlyBelowThree() {
        assertThat(WorkshopObserver.requiresWorkshop(2.9)).isTrue();
        assertThat(WorkshopObserver.requiresWorkshop(2.96)).isFalse();
        assertThat(WorkshopObserver.requiresWorkshop(4.0)).isFalse();
    }
}
