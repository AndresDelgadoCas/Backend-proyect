package fairteam_backend.insights.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseGradingSchemeRepository extends JpaRepository<CourseGradingSchemeEntity, Long> {
    Optional<CourseGradingSchemeEntity> findByCourseId(Long courseId);
}
