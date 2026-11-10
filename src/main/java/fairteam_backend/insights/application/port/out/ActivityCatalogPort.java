package fairteam_backend.insights.application.port.out;

import java.util.List;

import fairteam_backend.insights.domain.assistant.ActivityRef;

/** Outbound port: the saved workshops, quizzes and custom activities of a course, newest first. */
public interface ActivityCatalogPort {
    List<ActivityRef> recent(long courseId);

    static ActivityCatalogPort none() { return courseId -> List.of(); }
}
