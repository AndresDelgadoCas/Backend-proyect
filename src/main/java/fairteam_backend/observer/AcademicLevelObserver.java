package fairteam_backend.observer;

import org.springframework.stereotype.Component;

@Component
public class AcademicLevelObserver implements AcademicObserver {

    @Override
    public void update(AcademicEvent event) {

        String level;

        if (event.getNewGrade() >= 4.0) {
            level = "HIGH";
        } else if (event.getNewGrade() >= 3.0) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }

        System.out.println(
                "Student " + event.getStudentId()
                        + " academic level updated to " + level
        );
    }
}