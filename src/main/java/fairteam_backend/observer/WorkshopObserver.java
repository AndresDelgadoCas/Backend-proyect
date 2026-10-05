package fairteam_backend.observer;

import org.springframework.stereotype.Component;

@Component
public class WorkshopObserver implements AcademicObserver {

    @Override
    public void update(AcademicEvent event) {

        if (event.getNewGrade() < 3.0) {
            System.out.println(
                    "Workshop recommendation required for student "
                            + event.getStudentId()
            );
        }
    }
}