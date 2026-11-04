package fairteam_backend.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AcademicLevelObserver implements AcademicObserver {
    private static final Logger log = LoggerFactory.getLogger(AcademicLevelObserver.class);

    /** Same rule the teacher sees in FairTeam: classify on the grade rounded to one decimal. */
    public static String levelFor(double grade) {
        double shown = Math.round(grade * 10.0) / 10.0;
        if (shown >= 4.0) return "HIGH";
        if (shown >= 3.0) return "MEDIUM";
        return "LOW";
    }

    @Override
    public void update(AcademicEvent event) {
        log.info("Student {} academic level updated to {}", event.getStudentId(), levelFor(event.getNewGrade()));
    }
}