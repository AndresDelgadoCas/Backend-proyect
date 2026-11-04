package fairteam_backend.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationObserver implements AcademicObserver {
    private static final Logger log = LoggerFactory.getLogger(NotificationObserver.class);

    @Override
    public void update(AcademicEvent event) {
        log.info("Academic notification generated for student {}", event.getStudentId());
    }
}
