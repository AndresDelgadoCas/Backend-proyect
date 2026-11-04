package fairteam_backend.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WorkshopObserver implements AcademicObserver {
    private static final Logger log = LoggerFactory.getLogger(WorkshopObserver.class);

    /** A reinforcement workshop is suggested when the grade the teacher sees is below 3,0. */
    public static boolean requiresWorkshop(double grade) {
        return Math.round(grade * 10.0) / 10.0 < 3.0;
    }

    @Override
    public void update(AcademicEvent event) {
        if (requiresWorkshop(event.getNewGrade())) {
            log.info("Workshop recommendation required for student {}", event.getStudentId());
        }
    }
}
