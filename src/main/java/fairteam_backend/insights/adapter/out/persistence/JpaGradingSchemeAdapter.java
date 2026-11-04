package fairteam_backend.insights.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fairteam_backend.insights.application.port.out.GradingSchemePort;
import fairteam_backend.insights.domain.grading.GradingScheme;

/** Adapter: stores grading schemes in PostgreSQL. */
@Component
public class JpaGradingSchemeAdapter implements GradingSchemePort {
    private final CourseGradingSchemeRepository repository;

    public JpaGradingSchemeAdapter(CourseGradingSchemeRepository repository) { this.repository = repository; }

    @Override
    @Transactional(readOnly = true)
    public Optional<GradingScheme> find(long courseId) {
        return repository.findByCourseId(courseId).map(row -> {
            try {
                return new GradingScheme(Arrays.stream(row.getWeights().split(",")).map(String::trim).map(Integer::valueOf).toList(),
                        row.getPassingGrade().doubleValue());
            } catch (RuntimeException invalid) {
                return null;
            }
        });
    }

    @Override
    @Transactional
    public GradingScheme save(long courseId, GradingScheme scheme) {
        CourseGradingSchemeEntity row = repository.findByCourseId(courseId).orElseGet(CourseGradingSchemeEntity::new);
        row.setCourseId(courseId);
        row.setWeights(scheme.weights().stream().map(String::valueOf).collect(Collectors.joining(",")));
        row.setPassingGrade(BigDecimal.valueOf(scheme.passingGrade()));
        repository.save(row);
        return scheme;
    }
}
