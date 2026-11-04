package fairteam_backend.observer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class AcademicSubjectTest {
    @Test
    void notifiesEveryRegisteredObserverInOrder() {
        List<String> calls = new ArrayList<>();
        AcademicSubject subject = new AcademicSubject();
        subject.addObserver(event -> calls.add("first:" + event.getStudentId()));
        subject.addObserver(event -> calls.add("second:" + event.getNewGrade()));
        subject.notifyObservers(new AcademicEvent(5L, 2.5));
        assertThat(calls).containsExactly("first:5", "second:2.5");
    }

    @Test
    void removedObserversAreNotNotified() {
        List<Long> calls = new ArrayList<>();
        AcademicObserver observer = event -> calls.add(event.getStudentId());
        AcademicSubject subject = new AcademicSubject();
        subject.addObserver(observer);
        subject.removeObserver(observer);
        subject.notifyObservers(new AcademicEvent(1L, 4.0));
        assertThat(calls).isEmpty();
    }
}
