package fairteam_backend.observer;

import org.springframework.stereotype.Component;

@Component
public class NotificationObserver implements AcademicObserver {

    @Override
    public void update(AcademicEvent event) {

        System.out.println(
                "Academic notification generated for student "
                        + event.getStudentId()
        );
    }
}