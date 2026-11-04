package fairteam_backend.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import fairteam_backend.observer.AcademicLevelObserver;
import fairteam_backend.observer.NotificationObserver;
import fairteam_backend.observer.WorkshopObserver;

class GradeObserverServiceTest {
    @Test
    void notifiesEveryObserverAboutAGradeChange() {
        AcademicLevelObserver level = mock(AcademicLevelObserver.class);
        WorkshopObserver workshop = mock(WorkshopObserver.class);
        NotificationObserver notification = mock(NotificationObserver.class);
        new GradeObserverService(level, workshop, notification).notifyGradeChange(4L, 2.5);
        verify(level).update(argThat(e -> e.getStudentId() == 4L && e.getNewGrade() == 2.5));
        verify(workshop).update(argThat(e -> e.getStudentId() == 4L));
        verify(notification).update(argThat(e -> e.getStudentId() == 4L));
    }
}
