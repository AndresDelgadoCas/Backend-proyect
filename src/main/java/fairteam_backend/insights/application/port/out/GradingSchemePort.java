package fairteam_backend.insights.application.port.out;

import java.util.Optional;

import fairteam_backend.insights.domain.grading.GradingScheme;

/** Outbound port: where each course keeps its grading scheme (periods and weights). */
public interface GradingSchemePort {
    Optional<GradingScheme> find(long courseId);

    GradingScheme save(long courseId, GradingScheme scheme);
}
