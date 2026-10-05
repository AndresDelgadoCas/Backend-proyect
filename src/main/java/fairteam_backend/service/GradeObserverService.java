package fairteam_backend.service;

import org.springframework.stereotype.Service;

import fairteam_backend.observer.AcademicEvent;
import fairteam_backend.observer.AcademicLevelObserver;
import fairteam_backend.observer.AcademicObserver;
import fairteam_backend.observer.AcademicSubject;
import fairteam_backend.observer.NotificationObserver;
import fairteam_backend.observer.WorkshopObserver;

@Service
public class GradeObserverService {

    private final AcademicSubject academicSubject;

    public GradeObserverService(
            AcademicLevelObserver academicLevelObserver,
            WorkshopObserver workshopObserver,
            NotificationObserver notificationObserver) {

        this.academicSubject = new AcademicSubject();

        registerObserver(academicLevelObserver);
        registerObserver(workshopObserver);
        registerObserver(notificationObserver);
    }

    private void registerObserver(AcademicObserver observer) {
        academicSubject.addObserver(observer);
    }

    public void notifyGradeChange(Long studentId, double newGrade) {

        AcademicEvent event = new AcademicEvent(
                studentId,
                newGrade
        );

        academicSubject.notifyObservers(event);
    }
}